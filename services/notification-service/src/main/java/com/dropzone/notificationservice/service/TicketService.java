package com.dropzone.notificationservice.service;

import com.dropzone.notificationservice.model.TicketRecord;
import com.dropzone.notificationservice.repository.TicketRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRecordRepository ticketRecordRepository;

    @Transactional
    public void recordTicket(TicketRecord ticket) {
        // Idempotency: skip if ticket for this order already exists
        if (ticketRecordRepository.existsByOrderNumber(ticket.getOrderNumber())) {
            log.warn("[Ticket Service] Duplicate ticket generation skipped for Order={}", ticket.getOrderNumber());
            return;
        }
        ticketRecordRepository.save(ticket);
        log.info("[Ticket Service] Persisted ticket to database: TicketId={}, Order={}, PDF={}",
                ticket.getTicketId(), ticket.getOrderNumber(), ticket.getPdfUrl());
    }

    public List<TicketRecord> getAllTickets() {
        return ticketRecordRepository.findAll();
    }

    public Optional<TicketRecord> getTicketByOrderNumber(String orderNumber) {
        return ticketRecordRepository.findByOrderNumberIgnoreCase(orderNumber);
    }

    public Optional<TicketRecord> getTicketById(String ticketId) {
        return ticketRecordRepository.findByTicketIdIgnoreCase(ticketId);
    }
}
