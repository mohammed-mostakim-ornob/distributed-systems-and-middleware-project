package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BeverageStockAddDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.CrateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CrateRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CrateRepository crateRepository;

    private final String BASE_PATH = "/api/crates";

    @Test
    public void addToStock_success() throws Exception {
        int addedQuantity = 10;
        Crate crateBeforeUpdate = crateRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(crateBeforeUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(addedQuantity);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + crateBeforeUpdate.getId() + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(crateBeforeUpdate.getId().intValue())))
                .andExpect(jsonPath("$.inStock", is(crateBeforeUpdate.getInStock() + addedQuantity)));

        Crate crateAfterUpdate = crateRepository.findById(crateBeforeUpdate.getId())
                .get();

        assertEquals(crateAfterUpdate.getInStock(), crateBeforeUpdate.getInStock() + addedQuantity);
    }

    @Test
    public void addToStock_invalidData() throws Exception {
        Crate crateBeforeUpdate = crateRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(crateBeforeUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(0);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + crateBeforeUpdate.getId() + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addToStock_crateNotFound() throws Exception {
        long nonExistingCrateId = 0;

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(10);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + nonExistingCrateId + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addToStock_security() throws Exception {
        int addedQuantity = 10;
        Crate crateToUpdate = crateRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(crateToUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(addedQuantity);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + crateToUpdate.getId() + "/stock", null, requestBody))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + crateToUpdate.getId() + "/stock", TestHelper.getCustomer(), requestBody))
                .andExpect(status().isForbidden());
    }
}
