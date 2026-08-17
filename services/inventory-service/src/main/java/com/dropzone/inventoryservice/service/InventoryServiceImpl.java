package com.dropzone.inventoryservice.service;

import com.dropzone.inventoryservice.dto.*;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${inventory.reservation-ttl-seconds:600}")
    private long reservationTtlSeconds;

    private static final String RESERVATION_KEY_PREFIX = "reservation:";
    private static final String ACTIVE_RESERVATIONS_INDEX = "active_reservations";

    @Override
    @Transactional
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

    @Override
    @Transactional
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

        TicketReservation reservation = TicketReservation.builder()
                .reservationId(reservationId)
                .userId(request.getUserId())
                .eventId(request.getEventId())
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

    private ReservationResponseDto mapToReservationDto(TicketReservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .userId(reservation.getUserId())
                .eventId(reservation.getEventId())
                .ticketCategoryId(reservation.getTicketCategoryId())
                .categoryName(reservation.getCategoryName())
                .quantity(reservation.getQuantity())
                .createdAt(reservation.getCreatedAt())
                .expiresAt(reservation.getExpiresAt())
                .status(reservation.getStatus())
                .build();
    }
}
