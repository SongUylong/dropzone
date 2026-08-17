package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.model.TicketRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TicketService {

    private final List<TicketRecord> ticketRecords = new ArrayList<>();

    public synchronized void recordTicket(TicketRecord ticket) {
        ticketRecords.add(ticket);
        log.info("[Ticket Service] Recorded generated ticket: TicketId={}, Order={}, PDF={}",
                ticket.getTicketId(), ticket.getOrderNumber(), ticket.getPdfUrl());
    }

    public synchronized List<TicketRecord> getAllTickets() {
        return new ArrayList<>(ticketRecords);
    }

    public synchronized Optional<TicketRecord> getTicketByOrderNumber(String orderNumber) {
        return ticketRecords.stream()
                .filter(t -> t.getOrderNumber() != null && t.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst();
    }

    public synchronized Optional<TicketRecord> getTicketById(String ticketId) {
        return ticketRecords.stream()
                .filter(t -> t.getTicketId() != null && t.getTicketId().equalsIgnoreCase(ticketId))
                .findFirst();
    }
}
