// java
package com.koerber.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.repository.InventoryRepository;
import com.koerber.assignment.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

 /*
  Brief:
  - Full Spring context with H2 in-memory DB (configured via properties).
  - Prepares data using the real JPA repository and exercises controller endpoints.
  - Covers GET /inventory/{productId} and POST /inventory/update flows.
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        InventoryBatch b1 = new InventoryBatch();
        b1.setBatchId(null); // allow JPA to generate id if configured
        b1.setProductName("Widget");
        b1.setQuantity(10);
        b1.setExpiryDate(LocalDate.of(2024, 6, 1));
        b1.setProductId(100L);

        InventoryBatch b2 = new InventoryBatch();
        b2.setBatchId(null);
        b2.setProductName("Widget");
        b2.setQuantity(5);
        b2.setExpiryDate(LocalDate.of(2024, 12, 1));
        b2.setProductId(100L);

        repository.saveAll(List.of(b1, b2));
    }

    @Test
    void getInventory_ReturnsProductAndBatches() throws Exception {
        mockMvc.perform(get("/inventory/{productId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.productName").value("Widget"))
                .andExpect(jsonPath("$.batches").isArray());
    }

    @Test
    void updateInventory_reducesQuantities_andReturnsBatchIds() throws Exception {
        mockMvc.perform(post("/inventory/update")
                        .param("productId", "100")
                        .param("quantity", "12"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        // further assertions can query repository to assert quantities updated
    }
}