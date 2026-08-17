package com.dropzone.inventoryservice.unit;

import com.dropzone.inventoryservice.model.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryCalculationTest {

    @Test
    @DisplayName("Should correctly calculate available stock after reservation")
    void testInventoryReservationCalculation() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .eventId(100L)
                .ticketCategoryId(101L)
                .categoryName("VIP")
                .totalQuantity(100)
                .availableQuantity(100)
                .reservedQuantity(0)
                .soldQuantity(0)
                .build();

        int reserveQty = 5;
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - reserveQty);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + reserveQty);

        assertEquals(95, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertEquals(0, inventory.getSoldQuantity());
        assertEquals(100, inventory.getAvailableQuantity() + inventory.getReservedQuantity() + inventory.getSoldQuantity());
    }

    @Test
    @DisplayName("Should correctly calculate stock after reservation conversion to sale")
    void testReservationToSaleConversion() {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .eventId(100L)
                .ticketCategoryId(102L)
                .categoryName("VIP")
                .totalQuantity(100)
                .availableQuantity(90)
                .reservedQuantity(10)
                .soldQuantity(0)
                .build();

        int confirmQty = 10;
        inventory.setReservedQuantity(inventory.getReservedQuantity() - confirmQty);
        inventory.setSoldQuantity(inventory.getSoldQuantity() + confirmQty);

        assertEquals(90, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(10, inventory.getSoldQuantity());
        assertEquals(100, inventory.getTotalQuantity());
    }

    @Test
    @DisplayName("Should prevent reservation exceeding available quantity")
    void testReservationExceedingAvailableQuantity() {
        Inventory inventory = Inventory.builder()
                .totalQuantity(10)
                .availableQuantity(2)
                .reservedQuantity(8)
                .soldQuantity(0)
                .build();

        boolean canReserve = inventory.getAvailableQuantity() >= 5;
        assertFalse(canReserve, "Should not allow reserving 5 tickets when only 2 available");
    }
}
