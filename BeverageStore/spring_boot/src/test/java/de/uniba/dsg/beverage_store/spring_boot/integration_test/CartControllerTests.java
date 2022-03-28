package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrderItem;
import de.uniba.dsg.beverage_store.spring_boot.repository.OrderItemRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.OrderRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
public class CartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockHttpSession mockHttpSession;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockBean
    private CartService cartService;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private FireStoreService fireStoreService;

    @SpyBean
    AddressService addressService;

    @InjectMocks
    OrderService orderService;

    private final String BASE_PATH = "/cart";

    @BeforeEach
    public void init() {
        mockHttpSession.setAttribute("sessionScopedCartService", cartService);
    }

    @Test
    public void getCart_success() throws Exception {
        when(cartService.getCartItems()).thenReturn(getMockCartItems());
        when(cartService.getCartTotal()).thenReturn(getMockCartTotal());
        when(cartService.getCartItemCount()).thenReturn(getMockCartItemCount());

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getCustomer(), new LinkedMultiValueMap<>())
                .session(mockHttpSession))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("cartItems", getMockCartItems()))
                .andExpect(MockMvcResultMatchers.model().attribute("cartTotal", getMockCartTotal()))
                .andExpect(MockMvcResultMatchers.model().attribute("cartItemCount", getMockCartItemCount()))
                .andExpect(view().name("cart/details"));
    }

    @Test
    public void getCart_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getCheckout_success() throws Exception {
        when(cartService.getCartTotal()).thenReturn(getMockCartTotal());
        when(cartService.getCartItemCount()).thenReturn(getMockCartItemCount());
        when(addressService.getAllByUsername(anyString())).thenReturn(TestHelper.getMockAddresses());

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/checkout", TestHelper.getCustomer(), new LinkedMultiValueMap<>())
                .session(mockHttpSession))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("cartTotal", getMockCartTotal()))
                .andExpect(MockMvcResultMatchers.model().attribute("cartItemCount", getMockCartItemCount()))
                .andExpect(MockMvcResultMatchers.model().attribute("isEmptyCart", getMockCartItemCount() == 0))
                .andExpect(MockMvcResultMatchers.model().attribute("addressesDropdownListItems", TestHelper.getMockAddressDropdownList()))
                .andExpect(view().name("cart/checkout"));
    }

    @Test
    public void getCheckout_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/checkout", null, new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/checkout", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void checkout_success() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        double cartTotal = getMockCartTotal();

        when(cartService.getCartItems()).thenReturn(getMockCartItems());
        when(cartService.getCartTotal()).thenReturn(getMockCartTotal());
        when(cartService.getCartItemCount()).thenReturn(getMockCartItemCount());

        long orderCountBeforeAdd = orderRepository.count();
        long orderItemCountBeforeAdd = orderItemRepository.count();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/checkout", customer, getSubmitOrderDTOValidParams(address.getId(), address.getId()))
                .session(mockHttpSession))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/order/**"));

        verify(cartService, times(1)).clearCart();

        long orderCountAfterAdd = orderRepository.count();
        long orderItemCountAfterAdd = orderItemRepository.count();

        assertEquals(orderCountBeforeAdd + 1, orderCountAfterAdd);
        assertEquals(orderItemCountBeforeAdd + getMockCartItems().size(), orderItemCountAfterAdd);

        BeverageOrder addedOrder = orderRepository.findAll()
                .stream()
                .max(Comparator.comparing(BeverageOrder::getId))
                .orElse(null);

        assertNotNull(addedOrder);

        assertEquals(addedOrder.getPrice(), cartTotal);
        assertEquals(addedOrder.getDate(), LocalDate.now());
        assertEquals(addedOrder.getUser().getId(), customer.getId());
        assertEquals(addedOrder.getDeliveryAddress().getId(), address.getId());
        assertEquals(addedOrder.getBillingAddress().getId(), address.getId());

        List<BeverageOrderItem> addedOrderItems = orderItemRepository.findAllByOrderOrderNumber(addedOrder.getOrderNumber());

        assertEquals(addedOrderItems.size(), getMockCartItems().size());
    }

    @Test
    public void checkout_invalidData() throws Exception {
        when(cartService.getCartTotal()).thenReturn(getMockCartTotal());
        when(cartService.getCartItemCount()).thenReturn(getMockCartItemCount());
        when(addressService.getAllByUsername(anyString())).thenReturn(TestHelper.getMockAddresses());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/checkout", TestHelper.getCustomer(), getSubmitOrderDTOInvalidParams())
                .session(mockHttpSession))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("cartTotal", getMockCartTotal()))
                .andExpect(MockMvcResultMatchers.model().attribute("cartItemCount", getMockCartItemCount()))
                .andExpect(MockMvcResultMatchers.model().attribute("isEmptyCart", getMockCartItemCount() == 0))
                .andExpect(MockMvcResultMatchers.model().attribute("addressesDropdownListItems", TestHelper.getMockAddressDropdownList()))
                .andExpect(view().name("cart/checkout"));
    }

    @Test
    public void checkout_addressNotFound() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        when(cartService.getCartItems()).thenReturn(getMockCartItems());
        when(cartService.getCartTotal()).thenReturn(getMockCartTotal());
        when(cartService.getCartItemCount()).thenReturn(getMockCartItemCount());
        when(addressService.getAllByUsername(anyString())).thenReturn(TestHelper.getMockAddresses());

        MultiValueMap<String, String> params = getSubmitOrderDTOValidParams(address.getId(), address.getId());
        params.put("deliveryAddressId", Collections.singletonList(String.valueOf(100)));
        params.put("billingAddressId", Collections.singletonList(String.valueOf(100)));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/checkout", customer, params)
                .session(mockHttpSession))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(MockMvcResultMatchers.model().attribute("cartTotal", getMockCartTotal()))
                .andExpect(MockMvcResultMatchers.model().attribute("cartItemCount", getMockCartItemCount()))
                .andExpect(MockMvcResultMatchers.model().attribute("isEmptyCart", getMockCartItemCount() == 0))
                .andExpect(MockMvcResultMatchers.model().attribute("addressesDropdownListItems", TestHelper.getMockAddressDropdownList()))
                .andExpect(view().name("cart/checkout"));
    }

    @Test
    public void checkout_security() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/checkout", null, getSubmitOrderDTOValidParams(address.getId(), address.getId())))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/checkout", TestHelper.getManager(), getSubmitOrderDTOValidParams(address.getId(), address.getId())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private List<CartItem> getMockCartItems() {
        return TestHelper.getMockCartItems();
    }

    private double getMockCartTotal() {
        return getMockCartItems().stream()
                .mapToDouble(CartItem::getItemTotal)
                .sum();
    }

    private int getMockCartItemCount() {
        return getMockCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private MultiValueMap<String, String> getSubmitOrderDTOValidParams(Long deliveryAddressId, Long  billingAddressId) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("deliveryAddressId", String.valueOf(deliveryAddressId));
        params.add("billingAddressId", String.valueOf(billingAddressId));

        return params;
    }

    private MultiValueMap<String, String> getSubmitOrderDTOInvalidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("deliveryAddressId", String.valueOf(0));
        params.add("billingAddressId", String.valueOf(0));

        return params;
    }
}
