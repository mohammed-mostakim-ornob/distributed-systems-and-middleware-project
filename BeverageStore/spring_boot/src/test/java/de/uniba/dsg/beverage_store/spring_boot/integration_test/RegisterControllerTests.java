package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class RegisterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    private final String FIRST_NAME = "Test";
    private final String LAST_NAME = "Customer";
    private final String USERNAME = "testcustomer";
    private final String EMAIL = "testcustomer@email.com";

    private final String BASE_PATH = "/register";

    @Test
    public void getRegisterCustomer_success() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest( BASE_PATH + "/customer", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("customerDTO"))
                .andExpect(view().name("register-customer"));
    }

    @Test
    public void getRegisterCustomer_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/customer", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/customer",TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void createCustomer_success() throws Exception {
        long countBeforeAdd = userRepository.findAllByRole(Role.ROLE_CUSTOMER)
                .size();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/customer", null, getCreateCustomerValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/login"));

        long countAfterAdd =  userRepository.findAllByRole(Role.ROLE_CUSTOMER)
                .size();

        assertEquals(countBeforeAdd + 1, countAfterAdd);

        Optional<ApplicationUser> optionalCustomer = userRepository.findByUsername(USERNAME);

        assertTrue(optionalCustomer.isPresent());

        assertEquals(optionalCustomer.get().getEmail(), EMAIL);
        assertEquals(optionalCustomer.get().getUsername(), USERNAME);
        assertEquals(optionalCustomer.get().getLastName(), LAST_NAME);
        assertEquals(optionalCustomer.get().getFirstName(), FIRST_NAME);
        assertEquals(optionalCustomer.get().getRole(), Role.ROLE_CUSTOMER);
    }

    @Test
    public void createCustomer_invalidData() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/customer", null, getCreateCustomerInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(view().name("register-customer"));
    }

    @Test
    public void createCustomer_duplicateCredential() throws Exception {
        ApplicationUser user = TestHelper.getUser();

        MultiValueMap<String, String> params = getCreateCustomerValidParams();
        params.put("username", Collections.singletonList(user.getUsername()));
        params.put("email", Collections.singletonList(user.getEmail()));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/customer", null, params))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasConflictError", true))
                .andExpect(view().name("register-customer"));
    }

    @Test
    public void createCustomer_security() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/customer", TestHelper.getCustomer(), getCreateCustomerValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/customer", TestHelper.getManager(), getCreateCustomerValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private MultiValueMap<String, String> getCreateCustomerValidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", EMAIL);
        params.add("username", USERNAME);
        params.add("lastName", LAST_NAME);
        params.add("firstName", FIRST_NAME);
        params.add("birthday", "1990-01-01");
        params.add("password", "testcustomer");
        params.add("repeatPassword", "testcustomer");

        return params;
    }

    private MultiValueMap<String, String> getCreateCustomerInvalidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", null);
        params.add("lastName", null);
        params.add("username", null);
        params.add("email", null);
        params.add("password", null);
        params.add("repeatPassword", null);
        params.add("birthday", null);

        return params;
    }
}
