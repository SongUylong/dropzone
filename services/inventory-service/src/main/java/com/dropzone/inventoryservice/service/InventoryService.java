package com.dropzone.inventoryservice.service;

import com.dropzone.inventoryservice.dto.*;

import java.util.List;

public interface InventoryService {
    InventoryDto createOrUpdateInventory(CreateInventoryRequest request);

    InventoryDto getInventoryByCategoryId(Long ticketCategoryId);

    List<InventoryDto> getInventoryByEventId(Long eventId);

    ReservationResponseDto reserveTickets(ReserveTicketRequest request);

    ReservationResponseDto getReservation(String reservationId);

    ReservationResponseDto confirmReservation(String reservationId);

    ReservationResponseDto cancelReservation(String reservationId);

    void releaseExpiredReservations();
}
