package com.dropzone.inventoryservice.service;

import com.dropzone.inventoryservice.dto.*;

import java.util.List;

public interface InventoryService {
    // 1. Inventory Management
    InventoryDto createOrUpdateInventory(CreateInventoryRequest request);
    InventoryDto getInventoryByCategoryId(Long ticketCategoryId);
    List<InventoryDto> getInventoryByEventId(Long eventId);

    // 2. Ticket Reservations & Expiration (Redis Key: reservation:{reservationId})
    ReservationResponseDto reserveTickets(ReserveTicketRequest request);
    ReservationResponseDto getReservation(String reservationId);
    ReservationResponseDto confirmReservation(String reservationId);
    ReservationResponseDto cancelReservation(String reservationId);
    void releaseExpiredReservations();

    // 3. Idempotency (Redis Key: idempotency:{key})
    IdempotencyResponseDto checkAndStoreIdempotencyKey(String idempotencyKey, String payload);

    // 4. Rate Limiting (Redis Key: rate_limit:{key})
    RateLimitStatusDto checkRateLimit(String key, long maxRequests, long windowSeconds);

    // 5. Caching (Redis Key: cache:inventory:{categoryId})
    InventoryDto getCachedInventoryByCategoryId(Long ticketCategoryId);

    // 6. Flash Sale Waiting Room (Redis Keys: waiting_room:queue:{eventId}, waiting_room:token:{userId})
    WaitingRoomStatusDto joinWaitingRoom(Long eventId, String userId);
    WaitingRoomStatusDto getWaitingRoomStatus(Long eventId, String userId);
    List<String> admitWaitingRoomUsers(Long eventId, int count);

    // 7. Temporary Checkout State (Redis Key: checkout:{sessionId})
    CheckoutSessionDto initiateCheckout(CheckoutSessionDto request);
    CheckoutSessionDto getCheckoutSession(String sessionId);
    void clearCheckoutSession(String sessionId);
}
