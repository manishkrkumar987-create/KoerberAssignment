// java
package com.koerber.assignment.service;

import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 Brief:
 - Unit tests the service implementation using Mockito.
 - Assumes an implementation class `InventoryServiceImpl` that depends on
   `InventoryBatchRepository` and exposes `getSortedInventory(Long)` and
   `updateInventory(Long, Integer)`.
 - Adjust class/method names if your implementation differs.
*/

@ExtendWith(MockitoExtension.class)
class InventoryServiceUnitTest {

    @Mock
    private InventoryRepository repository;

    @InjectMocks
    private InventoryService service; // adjust class name if necessary

    @Test
    void getSortedInventory_returnsSortedBatches() {
        Long productId = 1L;

        InventoryBatch b1 = new InventoryBatch();
        b1.setBatchId(1L); b1.setQuantity(10); b1.setExpiryDate(LocalDate.of(2025, 12, 1)); b1.setProductName("P");

        InventoryBatch b2 = new InventoryBatch();
        b2.setBatchId(2L); b2.setQuantity(5);  b2.setExpiryDate(LocalDate.of(2024, 6, 1));  b2.setProductName("P");

        // repository returns unsorted list - service should sort by expiry (implementation dependent)
        when(repository.findByProductIdOrderByExpiryDateAsc(productId)).thenReturn(List.of(b2, b1));

        List<InventoryBatch> result = service.getSortedInventory(productId);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getBatchId());
        assertEquals(1L, result.get(1).getBatchId());
        verify(repository, times(1)).findByProductIdOrderByExpiryDateAsc(productId);
    }

    @Test
    void updateInventory_consumesBatchesAndSavesUpdates_returnsUsedBatchIds() {
        Long productId = 1L;
        int requested = 12;

        InventoryBatch b1 = new InventoryBatch();
        b1.setBatchId(10L); b1.setQuantity(10); b1.setExpiryDate(LocalDate.of(2024,1,1)); b1.setProductName("P");

        InventoryBatch b2 = new InventoryBatch();
        b2.setBatchId(11L); b2.setQuantity(5); b2.setExpiryDate(LocalDate.of(2024,6,1)); b2.setProductName("P");

        when(repository.findByProductIdOrderByExpiryDateAsc(productId)).thenReturn(List.of(b1, b2));
        when(repository.saveAll(anyIterable())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> used = service.updateInventory(productId, requested);

        // expected to use b1 fully (10) and b2 partially (2) => used ids [10,11]
        assertEquals(2, used.size());
        assertTrue(used.contains(10L));
        assertTrue(used.contains(11L));

        ArgumentCaptor<Iterable<InventoryBatch>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(repository).saveAll(captor.capture());

        // verify saved quantities: b1 -> 0, b2 -> 3
        boolean foundB1 = false, foundB2 = false;
        for (InventoryBatch saved : captor.getValue()) {
            if (saved.getBatchId().equals(10L)) { foundB1 = true; assertEquals(0, saved.getQuantity()); }
            if (saved.getBatchId().equals(11L)) { foundB2 = true; assertEquals(3, saved.getQuantity()); }
        }
        assertTrue(foundB1 && foundB2);
    }

    @Test
    void updateInventory_insufficientStock_throws() {
        Long productId = 1L;
        int requested = 20;

        InventoryBatch b1 = new InventoryBatch();
        b1.setBatchId(10L); b1.setQuantity(5); b1.setExpiryDate(LocalDate.of(2024,1,1)); b1.setProductName("P");

        when(repository.findByProductIdOrderByExpiryDateAsc(productId)).thenReturn(List.of(b1));

        assertThrows(IllegalStateException.class, () -> service.updateInventory(productId, requested));
        verify(repository, never()).saveAll(anyIterable());
    }
}