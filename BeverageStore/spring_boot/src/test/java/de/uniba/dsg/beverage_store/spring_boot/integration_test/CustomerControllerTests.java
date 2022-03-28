package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
import de.uniba.dsg.beverage_store.spring_boot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AddressService addressService;

    private final String BASE_PATH = "/customer";

    @Test
    public void getCustomer_success() throws Exception {
        List<ApplicationUser> mockCustomers = TestHelper.getMockCustomers();

        when(userService.getPagedCustomers(anyInt(), anyInt())).thenReturn(new PageImpl<>(mockCustomers));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("customers", mockCustomers))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("customer/list"));
    }

    @Test
    public void getCustomer_noPageParam() throws Exception {
        List<ApplicationUser> mockCustomers = TestHelper.getMockCustomers();

        when(userService.getPagedCustomers(anyInt(), anyInt())).thenReturn(new PageImpl<>(mockCustomers));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("customers", mockCustomers))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("customer/list"));
    }

    @Test
    public void getCustomer_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getCustomerDetails_success() throws Exception {
        ApplicationUser mockCustomer = TestHelper.getMockCustomer();
        List<Address> mockAddresses = TestHelper.getMockAddresses();
        List<BeverageOrder> mockOrders = TestHelper.getMockOrdersForCustomer();

        when(userService.getUserByUserName(anyString())).thenReturn(mockCustomer);
        when(orderService.getOrdersByUsername(anyString())).thenReturn(mockOrders);
        when(addressService.getAllByUsername(anyString())).thenReturn(mockAddresses);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/" + mockCustomer.getUsername(), TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("customer", mockCustomer))
                .andExpect(MockMvcResultMatchers.model().attribute("orders", mockOrders))
                .andExpect(MockMvcResultMatchers.model().attribute("addresses", mockAddresses))
                .andExpect(MockMvcResultMatchers.model().attribute("customerNotFound", false))
                .andExpect(view().name("customer/details"));
    }

    @Test
    public void getCustomerDetails_customerNotFound() throws Exception {
        when(userService.getUserByUserName(anyString())).thenThrow(NotFoundException.class);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/customer1", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("customerNotFound", true))
                .andExpect(view().name("customer/details"));
    }

    @Test
    public void getCustomerDetails_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/customer1", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/customer1", TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
