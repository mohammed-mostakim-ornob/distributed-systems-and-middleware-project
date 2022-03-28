package de.uniba.dsg.beverage_store.spring_boot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniba.dsg.beverage_store.spring_boot.demo.DemoData;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.DropdownListItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

public class TestHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Crate getCrate() {
        return DemoData.crates.stream()
                .findFirst()
                .orElse(null);
    }

    public static Bottle getBottle() {
        return DemoData.bottles.stream()
                .findFirst()
                .orElse(null);
    }

    public static Address getAddress() {
        return DemoData.addresses.stream()
                .findFirst()
                .orElse(null);
    }

    public static ApplicationUser getUser() {
        return DemoData.applicationUsers.stream()
                .findFirst()
                .orElse(null);
    }

    public static ApplicationUser getManager() {
        return DemoData.applicationUsers.stream()
                .filter(x -> x.getRole() == Role.ROLE_MANAGER)
                .findFirst()
                .orElse(null);
    }

    public static ApplicationUser getCustomer() {
        return DemoData.applicationUsers.stream()
                .filter(x -> x.getRole() == Role.ROLE_CUSTOMER)
                .findFirst()
                .orElse(null);
    }

    public static Address getUserAddress(String username) {
        return DemoData.addresses.stream()
                .filter(x -> x.getUser().getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public static BeverageOrder getMockOrder() {
        Address address = new Address(1L, "Address 1", "Pestalozzistraße", "9F", "96052", null, null, null);

        ApplicationUser customer = new ApplicationUser(1L, "testuser1", "Test", "User1", "testuser1@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);

        return new BeverageOrder(1L, "Order01", LocalDate.now(), 20.0, customer, address, address, null);
    }

    public static Crate getMockCrate() {
        Bottle bottle = new Bottle(1L, "Pepsi", "Pepsi pic", 1.0, 0.0, 1.0, "Pepsi Limited", 10, null, null);

        return new Crate(1L, "Pepsi Crate", "Pepsi pic", 10, 10.0, 10, bottle, null);
    }

    public static List<Crate> getMockCrates() {
        Bottle bottle1 = new Bottle(1L, "Pepsi", "Pepsi pic", 1.0, 0.0, 1.0, "Pepsi Limited", 10, null, null);
        Bottle bottle2 = new Bottle(2L, "Coca-cola", "Coca-cola pic", 1.0, 0.0, 1.0, "Coca-cola Limited", 10, null, null);

        Crate crate1 = new Crate(1L, "Pepsi Crate", "Pepsi pic", 10, 10.0, 10, bottle1, null);
        Crate crate2 = new Crate(1L, "Coca-cola", "Coca-cola pic", 10, 10.0, 10, bottle2, null);

        return Arrays.asList(crate1, crate2);
    }

    public static Bottle getMockBottle() {
        return new Bottle(1L, "Pepsi", "Pepsi pic", 1.0, 0.0, 1.0, "Pepsi Limited", 10, null, null);
    }

    public static List<Bottle> getMockBottles() {
        Bottle bottle1 = new Bottle(1L, "Pepsi", "Pepsi pic", 1.0, 0.0, 1.0, "Pepsi Limited", 10, null, null);
        Bottle bottle2 = new Bottle(2L, "Coca-cola", "Coca-cola pic", 1.0, 0.0, 1.0, "Coca-cola Limited", 10, null, null);

        return Arrays.asList(bottle1, bottle2);
    }

    public static List<Address> getMockAddresses() {
        Address address1 = new Address(1L, "Address 1", "Pestalozzistraße", "9F", "96052", null, null, null);
        Address address2 = new Address(2L, "Address 2", "Kapellenstraße", "23", "96050", null, null, null);

        return Arrays.asList(address1, address2);
    }

    public static ApplicationUser getMockCustomer() {
        return new ApplicationUser(1L, "testuser1", "Test", "User1", "testuser1@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);
    }

    public static List<ApplicationUser> getMockCustomers() {
        ApplicationUser customer1 = new ApplicationUser(1L, "testuser1", "Test", "User1", "testuser1@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);
        ApplicationUser customer2 = new ApplicationUser(2L, "testuser2", "Test", "User2", "testuser2@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);

        return Arrays.asList(customer1, customer2);
    }

    public static List<BeverageOrderItem> getMockOrderItems() {
        Bottle bottle1 = new Bottle(1L, "Pepsi", "Pepsi pic", 1.0, 0.0, 1.0, "Pepsi Limited", 10, null, null);
        Bottle bottle2 = new Bottle(2L, "Coca-cola", "Coca-cola pic", 1.0, 0.0, 1.0, "Coca-cola Limited", 10, null, null);

        BeverageOrderItem orderItem1 = new BeverageOrderItem(1L, BeverageType.BOTTLE, 10, 1, bottle1, null, null);
        BeverageOrderItem orderItem2 = new BeverageOrderItem(2L, BeverageType.BOTTLE, 10, 2, bottle2, null, null);

        return Arrays.asList(orderItem1, orderItem2);
    }

    public static List<BeverageOrder> getMockOrdersForManager() {
        ApplicationUser customer1 = new ApplicationUser(1L, "testuser1", "Test", "User1", "testuser1@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);
        ApplicationUser customer2 = new ApplicationUser(2L, "testuser2", "Test", "User2", "testuser2@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);

        BeverageOrder order1 = new BeverageOrder(1L, "Order01", LocalDate.now(), 10.0, customer1, null, null, null);
        BeverageOrder order2 = new BeverageOrder(2L, "Order02", LocalDate.now(), 20.0, customer1, null, null, null);
        BeverageOrder order3 = new BeverageOrder(3L, "Order03", LocalDate.now(), 30.0, customer2, null, null, null);

        return Arrays.asList(order1, order2, order3);
    }

    public static List<BeverageOrder> getMockOrdersForCustomer() {
        ApplicationUser customer = new ApplicationUser(1L, "testuser1", "Test", "User1", "testuser1@email.com", null, LocalDate.of(1990, 1, 1), Role.ROLE_CUSTOMER, null, null);

        BeverageOrder order1 = new BeverageOrder(1L, "Order01", LocalDate.now(), 10.0, customer, null, null, null);
        BeverageOrder order2 = new BeverageOrder(2L, "Order02", LocalDate.now(), 20.0, customer, null, null, null);

        return Arrays.asList(order1, order2);
    }

    public static List<CartItem> getMockCartItems() {
        Crate mockCrate = getMockCrate();
        Bottle mockBottle = getMockBottle();

        CartItem cartItem1 = new CartItem(1, BeverageType.CRATE, mockCrate.getId(), 1, mockCrate.getName(), mockCrate.getPicUrl(), mockCrate.getPrice(), mockCrate.getInStock(), 0.0, 0.0, null, mockCrate.getNoOfBottles());
        CartItem cartItem2 = new CartItem(1, BeverageType.BOTTLE, mockBottle.getId(), 1, mockBottle.getName(), mockBottle.getPicUrl(), mockBottle.getPrice(), mockBottle.getInStock(), mockBottle.getVolume(), mockBottle.getVolumePercent(), mockBottle.getSupplier(), 0);

        return Arrays.asList(cartItem1, cartItem2);
    }

    public static List<DropdownListItem<Long>> getMockBottleDropdownList() {
        return getMockBottles()
                .stream()
                .map(Bottle::getDropdownListItem)
                .collect(Collectors.toList());
    }

    public static List<DropdownListItem<Long>> getMockAddressDropdownList() {
        return getMockAddresses()
                .stream()
                .map(Address::getDropdownListItem)
                .collect(Collectors.toList());
    }

    public static MultiValueMap<String, String> getPageParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "1");

        return params;
    }

    public static MockHttpServletRequestBuilder createGetRequest(String url, UserDetails user, MultiValueMap<String, String> params) {
        return MockMvcRequestBuilders.get(url)
                .with(user == null ? anonymous() : user(user))
                .params(params);
    }

    public static MockHttpServletRequestBuilder createPostRequest(String url, UserDetails user, MultiValueMap<String, String> params) {
        return MockMvcRequestBuilders.post(url)
                .with(csrf())
                .with(user == null ? anonymous() : user(user))
                .params(params);
    }

    public static MockHttpServletRequestBuilder createRestGetRequest(String url, UserDetails user) {
        return MockMvcRequestBuilders.get(url)
                .with(user == null ? anonymous() : user(user));
    }

    public static <T> MockHttpServletRequestBuilder createRestPostRequest(String url, UserDetails user, T requestBody) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
                .with(user == null ? anonymous() : user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));
    }

    public static <T> MockHttpServletRequestBuilder createRestPatchRequest(String url, UserDetails user, T requestBody) throws JsonProcessingException {
        return MockMvcRequestBuilders.patch(url)
                .with(user == null ? anonymous() : user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));
    }

    public static MockHttpServletRequestBuilder createRestDeleteRequest(String url, UserDetails user) {
        return MockMvcRequestBuilders.delete(url)
                .with(user == null ? anonymous() : user(user));
    }
}
