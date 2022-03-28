package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrderItem;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
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
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private final String BASE_PATH = "/order";

    @Test
    public void getCustomerOrders_success() throws Exception {
        List<BeverageOrder> mockOrdersForManager = TestHelper.getMockOrdersForManager();
        List<BeverageOrder> mockOrdersForCustomer = TestHelper.getMockOrdersForCustomer();

        when(orderService.getPagedOrders(anyInt(), anyInt())).thenReturn(new PageImpl<>(mockOrdersForManager));
        when(orderService.getPagedOrdersByUsername(anyString(), anyInt(), anyInt())).thenReturn(new PageImpl<>(mockOrdersForCustomer));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("orders", mockOrdersForManager))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("order/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("orders", mockOrdersForCustomer))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("order/list"));
    }

    @Test
    public void getCustomerOrders_noPageParam() throws Exception {
        List<BeverageOrder> mockOrdersForManager = TestHelper.getMockOrdersForManager();
        List<BeverageOrder> mockOrdersForCustomer = TestHelper.getMockOrdersForCustomer();

        when(orderService.getPagedOrders(anyInt(), anyInt())).thenReturn(new PageImpl<>(mockOrdersForManager));
        when(orderService.getPagedOrdersByUsername(anyString(), anyInt(), anyInt())).thenReturn(new PageImpl<>(mockOrdersForCustomer));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("orders", mockOrdersForManager))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("order/list"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("orders", mockOrdersForCustomer))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfPages", 1))
                .andExpect(view().name("order/list"));
    }

    @Test
    public void getCustomerOrders_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    public void getOrder_success() throws Exception {
        BeverageOrder mockOrder = TestHelper.getMockOrder();
        List<BeverageOrderItem> mockOrderItems = TestHelper.getMockOrderItems();

        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(mockOrder);
        when(orderService.getOrderItemsByOrderNumber(anyString())).thenReturn(mockOrderItems);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/" + mockOrder.getOrderNumber(), TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("order", mockOrder))
                .andExpect(MockMvcResultMatchers.model().attribute("orderItems", mockOrderItems))
                .andExpect(MockMvcResultMatchers.model().attribute("orderNotFound", false))
                .andExpect(view().name("order/details"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/" + mockOrder.getOrderNumber(), TestHelper.getCustomer(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("order", mockOrder))
                .andExpect(MockMvcResultMatchers.model().attribute("orderItems", mockOrderItems))
                .andExpect(MockMvcResultMatchers.model().attribute("orderNotFound", false))
                .andExpect(view().name("order/details"));
    }

    @Test
    public void getOrder_orderNotFound() throws Exception {
        when(orderService.getOrderByOrderNumber(anyString())).thenThrow(NotFoundException.class);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/Order01", TestHelper.getManager(), TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("orderNotFound", true))
                .andExpect(view().name("order/details"));
    }

    @Test
    public void getOrder_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/Order01", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
