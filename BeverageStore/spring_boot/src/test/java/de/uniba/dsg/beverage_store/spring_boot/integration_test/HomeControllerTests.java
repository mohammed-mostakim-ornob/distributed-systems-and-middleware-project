package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getHome_success() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest("/", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("home"));

        mockMvc.perform(TestHelper.createGetRequest("/", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("home"));

        mockMvc.perform(TestHelper.createGetRequest("/home", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("home"));

        mockMvc.perform(TestHelper.createGetRequest("/home", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    public void getHome_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest("/", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest("/home", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
