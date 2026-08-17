package com.dropzone.notificationservice.repository;

import com.dropzone.notificationservice.model.TicketRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRecordRepository extends JpaRepository<TicketRecord, Long> {
    Optional<TicketRecord> findByOrderNumberIgnoreCase(String orderNumber);
    Optional<TicketRecord> findByTicketIdIgnoreCase(String ticketId);
    boolean existsByOrderNumber(String orderNumber);
}
