package com.koerber.assignment.factory;


import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FEFOUpdateStrategyTest {
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private FEFOUpdateStrategy fefoStrategy;

    private InventoryBatch earlyExpiryBatch;
    private InventoryBatch lateExpiryBatch;

    @BeforeEach
    void setUp() {
        earlyExpiryBatch = new InventoryBatch(1L, 1005L, "Smartwatch", 10, LocalDate.now().plusDays(5));
        lateExpiryBatch = new InventoryBatch(2L, 1005L, "Smartwatch", 10, LocalDate.now().plusDays(10));
    }

    @Test
    void shouldDeductFromEarliestBatchFirst() {
        // Arrange
        when(inventoryRepository.findByProductIdOrderByExpiryDateAsc(1005L))
                .thenReturn(Arrays.asList(earlyExpiryBatch, lateExpiryBatch));

        // Act: Request 12 items. Should take 10 from early batch and 2 from late batch.
        List<Long> affectedIds = fefoStrategy.updateStock(1005L, 12);

        // Assert
        assertEquals(2, affectedIds.size());
        assertEquals(0, earlyExpiryBatch.getQuantity(), "Early batch should be empty");
        assertEquals(8, lateExpiryBatch.getQuantity(), "Late batch should have 8 left");
        verify(inventoryRepository, times(1)).saveAll(any());
    }
}
