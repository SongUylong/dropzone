package com.dropzone.notificationservice.repository;

import com.dropzone.notificationservice.model.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByOrderNumberIgnoreCase(String orderNumber);
    List<NotificationRecord> findByUserIdIgnoreCase(String userId);
}
