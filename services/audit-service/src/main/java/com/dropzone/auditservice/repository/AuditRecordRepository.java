package com.dropzone.auditservice.repository;

import com.dropzone.auditservice.model.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    List<AuditRecord> findByTopicIgnoreCase(String topic);
    List<AuditRecord> findByOrderNumberIgnoreCase(String orderNumber);
    Page<AuditRecord> findAll(Pageable pageable);
    Page<AuditRecord> findByTopicIgnoreCase(String topic, Pageable pageable);
}
