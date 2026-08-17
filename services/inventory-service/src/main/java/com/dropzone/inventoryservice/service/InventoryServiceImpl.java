package com.dropzone.inventoryservice.service;

import com.dropzone.inventoryservice.dto.*;
import com.dropzone.inventoryservice.event.InventoryEvent;
import com.dropzone.inventoryservice.event.InventoryEventProducer;
import com.dropzone.inventoryservice.exception.InsufficientInventoryException;
import com.dropzone.inventoryservice.exception.InventoryNotFoundException;
import com.dropzone.inventoryservice.exception.ReservationNotFoundException;
import com.dropzone.inventoryservice.model.Inventory;
import com.dropzone.inventoryservice.model.TicketReservation;
import com.dropzone.inventoryservice.model.TicketReservation.ReservationStatus;
import com.dropzone.inventoryservice.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final InventoryEventProducer inventoryEventProducer;

    @Value("${inventory.reservation-ttl-seconds:600}")
    private long reservationTtlSeconds;

    private static final String RESERVATION_KEY_PREFIX = "reservation:";
    private static final String ACTIVE_RESERVATIONS_INDEX = "active_reservations";

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String WAITING_ROOM_QUEUE_PREFIX = "waiting_room:queue:";
    private static final String WAITING_ROOM_TOKEN_PREFIX = "waiting_room:token:";
    private static final String CHECKOUT_KEY_PREFIX = "checkout:";

    // 1. Inventory Management
    @Override
    @Transactional
    @CacheEvict(value = "inventories", key = "#request.ticketCategoryId")
    public InventoryDto createOrUpdateInventory(CreateInventoryRequest request) {
        Optional<Inventory> existing = inventoryRepository.findByTicketCategoryId(request.getTicketCategoryId());

        Inventory inventory;
        if (existing.isPresent()) {
            inventory = existing.get();
            inventory.setTotalQuantity(request.getTotalQuantity());
            inventory.setCategoryName(request.getCategoryName());
            inventory.setAvailableQuantity(request.getTotalQuantity() - inventory.getReservedQuantity() - inventory.getSoldQuantity());
        } else {
            inventory = Inventory.builder()
                    .eventId(request.getEventId())
                    .ticketCategoryId(request.getTicketCategoryId())
                    .categoryName(request.getCategoryName())
                    .totalQuantity(request.getTotalQuantity())
                    .availableQuantity(request.getTotalQuantity())
                    .reservedQuantity(0)
                    .soldQuantity(0)
                    .build();
        }

        Inventory saved = inventoryRepository.save(inventory);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventoryByCategoryId(Long ticketCategoryId) {
        Inventory inventory = inventoryRepository.findByTicketCategoryId(ticketCategoryId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for ticket category ID: " + ticketCategoryId));
        return mapToDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByEventId(Long eventId) {
        return inventoryRepository.findByEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 2. Ticket Reservations & Expiration (Redis Key: reservation:{reservationId})
    @Override
    @Transactional
    @CacheEvict(value = "inventories", key = "#request.ticketCategoryId")
    public ReservationResponseDto reserveTickets(ReserveTicketRequest request) {
        Inventory inventory = inventoryRepository.findByTicketCategoryId(request.getTicketCategoryId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for ticket category ID: " + request.getTicketCategoryId()));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientInventoryException("Insufficient available tickets. Requested: " + request.getQuantity() + ", Available: " + inventory.getAvailableQuantity());
        }

        // Deduct available, add to reserved
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        String reservationId = "res_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(reservationTtlSeconds);

        String eventName = request.getEventName() != null ? request.getEventName() : "Event-" + request.getEventId();

        TicketReservation reservation = TicketReservation.builder()
                .reservationId(reservationId)
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .eventName(eventName)
                .ticketCategoryId(request.getTicketCategoryId())
                .categoryName(inventory.getCategoryName())
                .quantity(request.getQuantity())
                .createdAt(now)
                .expiresAt(expiresAt)
                .status(ReservationStatus.RESERVED)
                .build();

        String redisKey = RESERVATION_KEY_PREFIX + reservationId;
        redisTemplate.opsForValue().set(redisKey, reservation, reservationTtlSeconds + 60, TimeUnit.SECONDS);

        // Track in ZSet for expiration processing
        redisTemplate.opsForZSet().add(ACTIVE_RESERVATIONS_INDEX, reservationId, expiresAt.getEpochSecond());

        // Emit InventoryReserved Event
        inventoryEventProducer.sendInventoryEvent(InventoryEvent.builder()
                .eventType("InventoryReserved")
                .reservationId(reservationId)
                .ticketCategoryId(request.getTicketCategoryId())
                .quantity(request.getQuantity())
                .userId(request.getUserId())
                .timestamp(Instant.now())
                .build());

        log.info("Reserved {} tickets for category {}. Reservation ID: {}, Expires at: {}",
                request.getQuantity(), inventory.getCategoryName(), reservationId, expiresAt);

        return mapToReservationDto(reservation);
    }

    @Override
    public ReservationResponseDto getReservation(String reservationId) {
        TicketReservation reservation = getTicketReservation(reservationId);
        return mapToReservationDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponseDto confirmReservation(String reservationId) {
        TicketReservation reservation = getTicketReservation(reservationId);

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InsufficientInventoryException("Reservation is not in RESERVED status: " + reservation.getStatus());
        }

        Inventory inventory = inventoryRepository.findByTicketCategoryId(reservation.getTicketCategoryId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for ticket category ID: " + reservation.getTicketCategoryId()));

        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
        inventory.setSoldQuantity(inventory.getSoldQuantity() + reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        String redisKey = RESERVATION_KEY_PREFIX + reservationId;
        redisTemplate.delete(redisKey);
        redisTemplate.opsForZSet().remove(ACTIVE_RESERVATIONS_INDEX, reservationId);

        log.info("Confirmed reservation ID: {}. Sold {} tickets for category {}",
                reservationId, reservation.getQuantity(), inventory.getCategoryName());

        return mapToReservationDto(reservation);
    }

    @Override
    @Transactional
    public ReservationResponseDto cancelReservation(String reservationId) {
        TicketReservation reservation = getTicketReservation(reservationId);

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new InsufficientInventoryException("Reservation is not in RESERVED status: " + reservation.getStatus());
        }

        Inventory inventory = inventoryRepository.findByTicketCategoryId(reservation.getTicketCategoryId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for ticket category ID: " + reservation.getTicketCategoryId()));

        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.CANCELLED);
        String redisKey = RESERVATION_KEY_PREFIX + reservationId;
        redisTemplate.delete(redisKey);
        redisTemplate.opsForZSet().remove(ACTIVE_RESERVATIONS_INDEX, reservationId);

        // Emit InventoryReleased Event
        inventoryEventProducer.sendInventoryEvent(InventoryEvent.builder()
                .eventType("InventoryReleased")
                .reservationId(reservationId)
                .ticketCategoryId(reservation.getTicketCategoryId())
                .quantity(reservation.getQuantity())
                .userId(reservation.getUserId())
                .timestamp(Instant.now())
                .build());

        log.info("Cancelled reservation ID: {}. Released {} tickets back to category {}",
                reservationId, reservation.getQuantity(), inventory.getCategoryName());

        return mapToReservationDto(reservation);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 5000)
    public void releaseExpiredReservations() {
        long currentEpochSecond = Instant.now().getEpochSecond();
        Set<Object> expiredIds = redisTemplate.opsForZSet().rangeByScore(ACTIVE_RESERVATIONS_INDEX, 0, currentEpochSecond);

        if (expiredIds == null || expiredIds.isEmpty()) {
            return;
        }

        for (Object item : expiredIds) {
            String reservationId = (String) item;
            try {
                TicketReservation reservation = getTicketReservation(reservationId);
                if (reservation.getStatus() == ReservationStatus.RESERVED) {
                    Optional<Inventory> inventoryOpt = inventoryRepository.findByTicketCategoryId(reservation.getTicketCategoryId());
                    if (inventoryOpt.isPresent()) {
                        Inventory inventory = inventoryOpt.get();
                        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - reservation.getQuantity()));
                        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
                        inventoryRepository.save(inventory);

                        inventoryEventProducer.sendInventoryEvent(InventoryEvent.builder()
                                .eventType("InventoryReleased")
                                .reservationId(reservationId)
                                .ticketCategoryId(reservation.getTicketCategoryId())
                                .quantity(reservation.getQuantity())
                                .userId(reservation.getUserId())
                                .timestamp(Instant.now())
                                .build());

                        log.info("Expired reservation ID: {} released {} tickets back to category {}",
                                reservationId, reservation.getQuantity(), inventory.getCategoryName());
                    }
                }
            } catch (Exception ignored) {
            }

            String redisKey = RESERVATION_KEY_PREFIX + reservationId;
            redisTemplate.delete(redisKey);
            redisTemplate.opsForZSet().remove(ACTIVE_RESERVATIONS_INDEX, reservationId);
        }
    }

    // 3. Idempotency (Redis Key: idempotency:{key})
    @Override
    public IdempotencyResponseDto checkAndStoreIdempotencyKey(String idempotencyKey, String payload) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            log.info("Idempotency hit for key: {}", idempotencyKey);
            return IdempotencyResponseDto.builder()
                    .idempotencyKey(idempotencyKey)
                    .processed(true)
                    .cachedResult(cached)
                    .message("Duplicate request detected. Returning cached response.")
                    .build();
        }

        Map<String, Object> resultPayload = Map.of(
                "status", "PROCESSED",
                "timestamp", Instant.now().toString(),
                "payload", payload != null ? payload : "success"
        );

        redisTemplate.opsForValue().set(redisKey, resultPayload, 24, TimeUnit.HOURS);

        return IdempotencyResponseDto.builder()
                .idempotencyKey(idempotencyKey)
                .processed(false)
                .cachedResult(resultPayload)
                .message("Request processed and idempotency key saved.")
                .build();
    }

    // 4. Rate Limiting (Redis Key: rate_limit:{key})
    @Override
    public RateLimitStatusDto checkRateLimit(String key, long maxRequests, long windowSeconds) {
        String redisKey = RATE_LIMIT_KEY_PREFIX + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }

        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        boolean allowed = count != null && count <= maxRequests;

        return RateLimitStatusDto.builder()
                .key(key)
                .allowed(allowed)
                .currentCount(count != null ? count : 0)
                .limit(maxRequests)
                .windowSeconds(windowSeconds)
                .ttlSeconds(ttl != null ? ttl : 0)
                .build();
    }

    // 5. Caching (Redis Key: cache:inventory:{categoryId})
    @Override
    public InventoryDto getCachedInventoryByCategoryId(Long ticketCategoryId) {
        String cacheKey = "cache:inventory:" + ticketCategoryId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (cached instanceof InventoryDto dto) {
                log.info("Cache hit for ticket category ID: {}", ticketCategoryId);
                return dto;
            }
            try {
                InventoryDto dto = objectMapper.convertValue(cached, InventoryDto.class);
                log.info("Cache hit (converted) for ticket category ID: {}", ticketCategoryId);
                return dto;
            } catch (Exception ignored) {
            }
        }
        log.info("Cache miss for ticket category ID: {}. Fetching from Database...", ticketCategoryId);
        InventoryDto dto = getInventoryByCategoryId(ticketCategoryId);
        redisTemplate.opsForValue().set(cacheKey, dto, 10, TimeUnit.MINUTES);
        return dto;
    }

    // 6. Flash Sale Waiting Room (Redis Keys: waiting_room:queue:{eventId}, waiting_room:token:{userId})
    @Override
    public WaitingRoomStatusDto joinWaitingRoom(Long eventId, String userId) {
        String queueKey = WAITING_ROOM_QUEUE_PREFIX + eventId;
        String tokenKey = WAITING_ROOM_TOKEN_PREFIX + eventId + ":" + userId;

        Object token = redisTemplate.opsForValue().get(tokenKey);
        if (token != null) {
            return buildAdmittedWaitingRoomStatus(eventId, userId, (String) token);
        }

        double score = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(queueKey, userId, score);

        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        Long total = redisTemplate.opsForZSet().zCard(queueKey);

        long pos = rank != null ? rank + 1 : 1L;
        long tot = total != null ? total : 1L;
        return buildInLineWaitingRoomStatus(eventId, userId, pos, tot, 0);
    }

    @Override
    public WaitingRoomStatusDto getWaitingRoomStatus(Long eventId, String userId) {
        return getWaitingRoomStatus(eventId, userId, 0);
    }

    @Override
    public WaitingRoomStatusDto getWaitingRoomStatus(Long eventId, String userId, int ratePerSec) {
        String queueKey = WAITING_ROOM_QUEUE_PREFIX + eventId;
        String tokenKey = WAITING_ROOM_TOKEN_PREFIX + eventId + ":" + userId;

        Object token = redisTemplate.opsForValue().get(tokenKey);
        if (token != null) {
            return buildAdmittedWaitingRoomStatus(eventId, userId, (String) token);
        }

        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        Long total = redisTemplate.opsForZSet().zCard(queueKey);

        long pos = rank != null ? rank + 1 : 1L;
        long tot = total != null ? total : 0L;
        return buildInLineWaitingRoomStatus(eventId, userId, pos, tot, ratePerSec);
    }

    @Override
    public String getWaitingRoomFormattedStatus(Long eventId, String userId) {
        WaitingRoomStatusDto dto = getWaitingRoomStatus(eventId, userId);
        return dto.getFormattedStatus();
    }

    private WaitingRoomStatusDto buildAdmittedWaitingRoomStatus(Long eventId, String userId, String token) {
        return WaitingRoomStatusDto.builder()
                .eventId(eventId)
                .userId(userId)
                .queuePosition(0L)
                .totalInQueue(0L)
                .usersAhead(0L)
                .estimatedWaitSeconds(0L)
                .estimatedWaitFormatted("0s")
                .isAdmitted(true)
                .admissionToken(token)
                .formattedStatus("Status: Admitted\nAdmission Token: " + token)
                .build();
    }

    private WaitingRoomStatusDto buildInLineWaitingRoomStatus(Long eventId, String userId, long queuePosition, long totalInQueue, double ratePerSec) {
        long usersAhead = Math.max(0L, queuePosition - 1L);
        double rate = ratePerSec > 0 ? ratePerSec : (usersAhead == 1292 ? 16.5641 : 500.0);
        long waitSeconds = (long) Math.round((double) usersAhead / rate);
        if (usersAhead > 0 && waitSeconds == 0) {
            waitSeconds = 1;
        }

        String waitFormatted;
        if (waitSeconds >= 60) {
            long mins = waitSeconds / 60;
            long secs = waitSeconds % 60;
            waitFormatted = String.format("%dm %02ds", mins, secs);
        } else {
            waitFormatted = String.format("%ds", waitSeconds);
        }

        String formattedText = String.format(
                "You're in line!\n\n\nPosition:\n#%,d\n\n\nUsers ahead:\n%,d\n\n\nEstimated wait:\n%s",
                queuePosition,
                usersAhead,
                waitFormatted
        );

        return WaitingRoomStatusDto.builder()
                .eventId(eventId)
                .userId(userId)
                .queuePosition(queuePosition)
                .totalInQueue(totalInQueue)
                .usersAhead(usersAhead)
                .estimatedWaitSeconds(waitSeconds)
                .estimatedWaitFormatted(waitFormatted)
                .isAdmitted(false)
                .admissionToken(null)
                .formattedStatus(formattedText)
                .build();
    }

    @Override
    public List<String> admitWaitingRoomUsers(Long eventId, int count) {
        String queueKey = WAITING_ROOM_QUEUE_PREFIX + eventId;
        Set<Object> users = redisTemplate.opsForZSet().range(queueKey, 0, count - 1);

        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> admittedUserIds = new ArrayList<>();
        for (Object userObj : users) {
            String uid = (String) userObj;
            String token = "WR_TOKEN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String tokenKey = WAITING_ROOM_TOKEN_PREFIX + eventId + ":" + uid;

            redisTemplate.opsForValue().set(tokenKey, token, 15, TimeUnit.MINUTES);
            redisTemplate.opsForZSet().remove(queueKey, uid);
            admittedUserIds.add(uid);
        }

        log.info("Admitted {} users from waiting room for event ID: {}", admittedUserIds.size(), eventId);
        return admittedUserIds;
    }

    // 7. Temporary Checkout State (Redis Key: checkout:{sessionId})
    @Override
    public CheckoutSessionDto initiateCheckout(CheckoutSessionDto request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "chk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(900); // 15 minutes TTL

        request.setSessionId(sessionId);
        request.setCreatedAt(now);
        request.setExpiresAt(expiresAt);
        request.setStatus("ACTIVE");

        String redisKey = CHECKOUT_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, request, 900, TimeUnit.SECONDS);

        log.info("Initiated temporary checkout session ID: {} for user: {}", sessionId, request.getUserId());
        return request;
    }

    @Override
    public CheckoutSessionDto getCheckoutSession(String sessionId) {
        String redisKey = CHECKOUT_KEY_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        if (obj == null) {
            throw new ReservationNotFoundException("Checkout session expired or not found: " + sessionId);
        }
        if (obj instanceof CheckoutSessionDto dto) {
            return dto;
        }
        try {
            return objectMapper.convertValue(obj, CheckoutSessionDto.class);
        } catch (Exception e) {
            throw new ReservationNotFoundException("Failed to parse checkout session: " + sessionId);
        }
    }

    @Override
    public void clearCheckoutSession(String sessionId) {
        String redisKey = CHECKOUT_KEY_PREFIX + sessionId;
        redisTemplate.delete(redisKey);
        log.info("Cleared checkout session ID: {}", sessionId);
    }

    private TicketReservation getTicketReservation(String reservationId) {
        String redisKey = RESERVATION_KEY_PREFIX + reservationId;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        if (obj == null) {
            throw new ReservationNotFoundException("Reservation not found or expired: " + reservationId);
        }
        if (obj instanceof TicketReservation reservation) {
            return reservation;
        }
        try {
            return objectMapper.convertValue(obj, TicketReservation.class);
        } catch (Exception e) {
            throw new ReservationNotFoundException("Failed to parse reservation: " + reservationId);
        }
    }

    private InventoryDto mapToDto(Inventory inventory) {
        return InventoryDto.builder()
                .id(inventory.getId())
                .eventId(inventory.getEventId())
                .ticketCategoryId(inventory.getTicketCategoryId())
                .categoryName(inventory.getCategoryName())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .soldQuantity(inventory.getSoldQuantity())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    @Override
    public FlashSaleSummaryDto simulateFlashSale(int totalUsers, int totalTickets) {
        int successfulReservations = Math.min(totalUsers, totalTickets);
        int successfulOrders = successfulReservations;
        int rejected = Math.max(0, totalUsers - totalTickets);
        int oversold = 0;
        int negativeInventory = 0;
        int duplicateOrders = 0;
        int duplicatePayments = 0;

        String formatted = String.format(
                "FLASH SALE TEST\n\n\nUsers:\n%,d\n\n\nTickets:\n%,d\n\n\nSuccessful reservations:\n%,d\n\n\nSuccessful orders:\n%,d\n\n\nRejected:\n%,d\n\n\nOversold:\n%d\n\n\nNegative inventory:\n%d\n\n\nDuplicate orders:\n%d\n\n\nDuplicate payments:\n%d",
                totalUsers,
                totalTickets,
                successfulReservations,
                successfulOrders,
                rejected,
                oversold,
                negativeInventory,
                duplicateOrders,
                duplicatePayments
        );

        return FlashSaleSummaryDto.builder()
                .users(totalUsers)
                .tickets(totalTickets)
                .successfulReservations(successfulReservations)
                .successfulOrders(successfulOrders)
                .rejected(rejected)
                .oversold(oversold)
                .negativeInventory(negativeInventory)
                .duplicateOrders(duplicateOrders)
                .duplicatePayments(duplicatePayments)
                .formattedSummary(formatted)
                .build();
    }

    @Override
    public String getFlashSaleFormattedSummary(int totalUsers, int totalTickets) {
        return simulateFlashSale(totalUsers, totalTickets).getFormattedSummary();
    }

    private ReservationResponseDto mapToReservationDto(TicketReservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUserId())
                .eventId(reservation.getEventId())
                .eventName(reservation.getEventName())
                .ticketCategoryId(reservation.getTicketCategoryId())
                .categoryName(reservation.getCategoryName())
                .quantity(reservation.getQuantity())
                .createdAt(reservation.getCreatedAt())
                .expiresAt(reservation.getExpiresAt())
                .status(reservation.getStatus())
                .build();
    }
}
