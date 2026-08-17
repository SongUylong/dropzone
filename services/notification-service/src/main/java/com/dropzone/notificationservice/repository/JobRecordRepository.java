package com.dropzone.notificationservice.repository;

import com.dropzone.notificationservice.model.JobRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRecordRepository extends JpaRepository<JobRecord, Long> {
    List<JobRecord> findByTargetQueueIgnoreCase(String targetQueue);
}
