package com.dropzone.paymentservice.repository;

import com.dropzone.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByOrderNumber(String orderNumber);
    List<Payment> findAllByOrderNumber(String orderNumber);
}
