package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.BeverageStockAddDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.BottleRepository;
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
public class BottleRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BottleRepository bottleRepository;

    private final String BASE_PATH = "/api/bottles";

    @Test
    public void addToStock_success() throws Exception {
        int addedQuantity = 10;
        Bottle bottleBeforeUpdate = bottleRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(bottleBeforeUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(addedQuantity);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + bottleBeforeUpdate.getId() + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bottleBeforeUpdate.getId().intValue())))
                .andExpect(jsonPath("$.inStock", is(bottleBeforeUpdate.getInStock() + addedQuantity)));

        Bottle bottleAfterUpdate = bottleRepository.findById(bottleBeforeUpdate.getId())
                .get();

        assertEquals(bottleAfterUpdate.getInStock(), bottleBeforeUpdate.getInStock() + addedQuantity);
    }

    @Test
    public void addToStock_invalidData() throws Exception {
        Bottle bottleBeforeUpdate = bottleRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(bottleBeforeUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(0);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + bottleBeforeUpdate.getId() + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addToStock_bottleNotFound() throws Exception {
        long nonExistingBottleId = 0;

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(10);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + nonExistingBottleId + "/stock", TestHelper.getManager(), requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addToStock_security() throws Exception {
        int addedQuantity = 10;
        Bottle bottleToUpdate = bottleRepository.findAll()
                .stream()
                .findAny()
                .orElse(null);

        assertNotNull(bottleToUpdate);

        BeverageStockAddDTO requestBody = new BeverageStockAddDTO(addedQuantity);

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + bottleToUpdate.getId() + "/stock", null, requestBody))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestPatchRequest(BASE_PATH + "/" + bottleToUpdate.getId() + "/stock", TestHelper.getCustomer(), requestBody))
                .andExpect(status().isForbidden());
    }
}
