package com.dropzone.inventoryservice.repository;

import com.dropzone.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByTicketCategoryId(Long ticketCategoryId);

    List<Inventory> findByEventId(Long eventId);
}
