package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.exception.InsufficientStockException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CartItemDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
public class CartItemRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockHttpSession mockHttpSession;

    @MockBean
    private CartService cartService;

    private final String BASE_PATH = "/api/cart-items";

    @BeforeEach
    public void init() {
        mockHttpSession.setAttribute("sessionScopedCartService", cartService);
    }

    @Test
    public void getCount_success() throws Exception {
        int cartItemCount = 5;

        when(cartService.getCartItemCount()).thenReturn(cartItemCount);

        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/count", TestHelper.getCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(cartItemCount)));

        verify(cartService, times(1)).getCartItemCount();
    }

    @Test
    public void getCount_security() throws Exception {
        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/count", null))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/count", TestHelper.getManager()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getTotalPrice_success() throws Exception {
        double cartTotal = 10.0;

        when(cartService.getCartTotal()).thenReturn(cartTotal);

        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/total-price", TestHelper.getCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(cartTotal)));

        verify(cartService, times(1)).getCartTotal();
    }

    @Test
    public void getTotalPrice_security() throws Exception {
        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/total-price", null))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestGetRequest(BASE_PATH + "/total-price", TestHelper.getManager()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addCartItem_success() throws Exception {
        CartItemDTO requestBody = new CartItemDTO(1L, BeverageType.BOTTLE, 5);

        when(cartService.addCartItem(any(BeverageType.class), anyLong(), anyInt())).thenAnswer(invocation -> {
            BeverageType beverageType = invocation.getArgument(0, BeverageType.class);
            long beverageId = invocation.getArgument(1, Long.class);
            int quantity = invocation.getArgument(2, Integer.class);

            return new CartItem(1, beverageType, beverageId, quantity, "Pepsi", "Pepsi Pic Url", 1.0, 10, 1.0, 0.0, "Pepsi Limited", 0);
        });

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, TestHelper.getCustomer(), requestBody))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.beverageType", is(requestBody.getBeverageType().name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.beverageId", is(requestBody.getBeverageId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.quantity", is(requestBody.getQuantity())));

        verify(cartService, times(1)).addCartItem(any(BeverageType.class), anyLong(), anyInt());
    }

    @Test
    public void addCartItem_beverageNotFound() throws Exception {
        CartItemDTO requestBody = new CartItemDTO(100L, BeverageType.BOTTLE, 5);

        when(cartService.addCartItem(any(BeverageType.class), anyLong(), anyInt())).thenThrow(NotFoundException.class);

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, TestHelper.getCustomer(), requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addCartItem_insufficientStock() throws Exception {
        CartItemDTO requestBody = new CartItemDTO(1L, BeverageType.BOTTLE, 100);

        when(cartService.addCartItem(any(BeverageType.class), anyLong(), anyInt())).thenThrow(InsufficientStockException.class);

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, TestHelper.getCustomer(), requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCartItem_invalidData() throws Exception {
        CartItemDTO requestBody = new CartItemDTO(0L, null, 0);

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, TestHelper.getCustomer(), requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addCartItem_security() throws Exception {
        CartItemDTO requestBody = new CartItemDTO(1L, BeverageType.BOTTLE, 5);

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, null, requestBody))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestPostRequest(BASE_PATH, TestHelper.getManager(), requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteCartItem_success() throws Exception {
        doNothing().when(cartService).removeCartItem(anyInt());

        mockMvc.perform(TestHelper.createRestDeleteRequest(BASE_PATH + "/1", TestHelper.getCustomer()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCartItem_cartItemNotfound() throws Exception {
        doThrow(NotFoundException.class).when(cartService).removeCartItem(anyInt());

        mockMvc.perform(TestHelper.createRestDeleteRequest(BASE_PATH + "/0", TestHelper.getCustomer()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCartItem_security() throws Exception {
        mockMvc.perform(TestHelper.createRestDeleteRequest(BASE_PATH + "/1", null))
                .andExpect(status().isFound());

        mockMvc.perform(TestHelper.createRestDeleteRequest(BASE_PATH + "/1", TestHelper.getManager()))
                .andExpect(status().isForbidden());
    }
}
