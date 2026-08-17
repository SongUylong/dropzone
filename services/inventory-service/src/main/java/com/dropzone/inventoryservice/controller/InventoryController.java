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
}
