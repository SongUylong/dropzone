package com.dropzone.inventoryservice.controller;

import com.dropzone.inventoryservice.dto.*;
import com.dropzone.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 1. Inventory Management
    @PostMapping
    public ResponseEntity<InventoryDto> createOrUpdateInventory(@RequestBody CreateInventoryRequest request) {
        InventoryDto dto = inventoryService.createOrUpdateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<InventoryDto> getInventoryByCategoryId(@PathVariable Long categoryId) {
        InventoryDto dto = inventoryService.getInventoryByCategoryId(categoryId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByEventId(@PathVariable Long eventId) {
        List<InventoryDto> list = inventoryService.getInventoryByEventId(eventId);
        return ResponseEntity.ok(list);
    }

    // 2. Ticket Reservations & Expiration
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponseDto> reserveTickets(@RequestBody ReserveTicketRequest request) {
        ReservationResponseDto dto = inventoryService.reserveTickets(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponseDto> getReservation(@PathVariable String reservationId) {
        ReservationResponseDto dto = inventoryService.getReservation(reservationId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/confirm/{reservationId}")
    public ResponseEntity<ReservationResponseDto> confirmReservation(@PathVariable String reservationId) {
        ReservationResponseDto dto = inventoryService.confirmReservation(reservationId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@PathVariable String reservationId) {
        ReservationResponseDto dto = inventoryService.cancelReservation(reservationId);
        return ResponseEntity.ok(dto);
    }

    // 3. Idempotency (Redis Key: idempotency:{key})
    @PostMapping("/idempotency/{key}")
    public ResponseEntity<IdempotencyResponseDto> checkIdempotency(
            @PathVariable String key,
            @RequestBody(required = false) String payload) {
        IdempotencyResponseDto dto = inventoryService.checkAndStoreIdempotencyKey(key, payload);
        return ResponseEntity.ok(dto);
    }

    // 4. Rate Limiting (Redis Key: rate_limit:{key})
    @GetMapping("/rate-limit/{key}")
    public ResponseEntity<RateLimitStatusDto> checkRateLimit(
            @PathVariable String key,
            @RequestParam(defaultValue = "5") long limit,
            @RequestParam(defaultValue = "10") long windowSeconds) {
        RateLimitStatusDto dto = inventoryService.checkRateLimit(key, limit, windowSeconds);
        if (!dto.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(dto);
        }
        return ResponseEntity.ok(dto);
    }

    // 5. Caching (Redis Key: cache:inventory:{categoryId})
    @GetMapping("/cached/category/{categoryId}")
    public ResponseEntity<InventoryDto> getCachedInventoryByCategoryId(@PathVariable Long categoryId) {
        InventoryDto dto = inventoryService.getCachedInventoryByCategoryId(categoryId);
        return ResponseEntity.ok(dto);
    }

    // 6. Flash Sale Waiting Room (Redis Keys: waiting_room:queue:{eventId}, waiting_room:token:{userId})
    @PostMapping("/waiting-room/{eventId}/join")
    public ResponseEntity<WaitingRoomStatusDto> joinWaitingRoom(
            @PathVariable Long eventId,
            @RequestParam String userId) {
        WaitingRoomStatusDto dto = inventoryService.joinWaitingRoom(eventId, userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/waiting-room/{eventId}/status")
    public ResponseEntity<WaitingRoomStatusDto> getWaitingRoomStatus(
            @PathVariable Long eventId,
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "0") int ratePerSec) {
        WaitingRoomStatusDto dto = inventoryService.getWaitingRoomStatus(eventId, userId, ratePerSec);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/waiting-room/{eventId}/formatted", produces = "text/plain")
    public ResponseEntity<String> getWaitingRoomFormattedStatus(
            @PathVariable Long eventId,
            @RequestParam String userId) {
        String formatted = inventoryService.getWaitingRoomFormattedStatus(eventId, userId);
        return ResponseEntity.ok(formatted);
    }

    @PostMapping("/waiting-room/{eventId}/admit")
    public ResponseEntity<List<String>> admitWaitingRoomUsers(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "10") int count) {
        List<String> admitted = inventoryService.admitWaitingRoomUsers(eventId, count);
        return ResponseEntity.ok(admitted);
    }

    // Flash Sale Concurrency Simulation
    @PostMapping("/flash-sale/simulate")
    public ResponseEntity<FlashSaleSummaryDto> simulateFlashSale(
            @RequestParam(defaultValue = "50000") int totalUsers,
            @RequestParam(defaultValue = "10000") int totalTickets) {
        FlashSaleSummaryDto summary = inventoryService.simulateFlashSale(totalUsers, totalTickets);
        return ResponseEntity.ok(summary);
    }

    @GetMapping(value = "/flash-sale/formatted", produces = "text/plain")
    public ResponseEntity<String> getFlashSaleFormattedSummary(
            @RequestParam(defaultValue = "50000") int totalUsers,
            @RequestParam(defaultValue = "10000") int totalTickets) {
        String formatted = inventoryService.getFlashSaleFormattedSummary(totalUsers, totalTickets);
        return ResponseEntity.ok(formatted);
    }

    // 7. Temporary Checkout State (Redis Key: checkout:{sessionId})
    @PostMapping("/checkout/initiate")
    public ResponseEntity<CheckoutSessionDto> initiateCheckout(@RequestBody CheckoutSessionDto request) {
        CheckoutSessionDto dto = inventoryService.initiateCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/checkout/{sessionId}")
    public ResponseEntity<CheckoutSessionDto> getCheckoutSession(@PathVariable String sessionId) {
        CheckoutSessionDto dto = inventoryService.getCheckoutSession(sessionId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/checkout/{sessionId}")
    public ResponseEntity<Void> clearCheckoutSession(@PathVariable String sessionId) {
        inventoryService.clearCheckoutSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
