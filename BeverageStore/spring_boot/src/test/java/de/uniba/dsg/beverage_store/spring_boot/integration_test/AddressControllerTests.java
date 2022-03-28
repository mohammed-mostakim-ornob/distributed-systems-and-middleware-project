package de.uniba.dsg.beverage_store.spring_boot.integration_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.AddressDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.AddressRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressRepository addressRepository;

    @SpyBean
    private AddressService addressService;

    private final String NAME = "Uni Erba";
    private final String HOUSE_NUMBER = "5";
    private final String POSTAL_CODE = "96049";
    private final String STREET = "An Der Weberei";

    private final String BASE_PATH = "/address";

    @Test
    public void getAddresses_success() throws Exception {
        List<Address> mockAddresses = TestHelper.getMockAddresses();

        when(addressService.getAllByUsername(anyString())).thenReturn(mockAddresses);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("addressDTO"))
                .andExpect(MockMvcResultMatchers.model().attribute("addresses", mockAddresses))
                .andExpect(view().name("address/list"));
    }

    @Test
    public void getAddresses_security() throws Exception {
        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", null, TestHelper.getPageParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "", TestHelper.getManager(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void createAddress_success() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();

        long countBeforeAdd = addressRepository.findAllByUserUsername(customer.getUsername())
                .size();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "", customer, getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/address"));

        long countAfterAdd = addressRepository.findAllByUserUsername(customer.getUsername())
                .size();

        assertEquals(countBeforeAdd + 1, countAfterAdd);

        Address addedAddress = addressRepository.findAllByUserUsername(customer.getUsername())
                .stream()
                .max(Comparator.comparing(Address::getId))
                .orElse(null);

        assertNotNull(addedAddress);

        assertEquals(NAME, addedAddress.getName());
        assertEquals(STREET, addedAddress.getStreet());
        assertEquals(POSTAL_CODE, addedAddress.getPostalCode());
        assertEquals(HOUSE_NUMBER, addedAddress.getHouseNumber());
    }

    @Test
    public void createAddress_invalidData() throws Exception {
        List<Address> mockAddresses = TestHelper.getMockAddresses();

        when(addressService.getAllByUsername(anyString())).thenReturn(mockAddresses);

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "", TestHelper.getCustomer(), getAddressDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(MockMvcResultMatchers.model().attribute("addresses", mockAddresses))
                .andExpect(view().name("address/list"));
    }

    @Test
    public void createAddress_security() throws Exception {
        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "", null, getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "", TestHelper.getManager(), getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getEditAddress_success() throws Exception {
        Address mockAddress = TestHelper.getAddress();

        when(addressService.getAddressById(mockAddress.getId())).thenReturn(mockAddress);

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/edit/" + mockAddress.getId(), TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("addressId", mockAddress.getId()))
                .andExpect(MockMvcResultMatchers.model().attribute("addressDTO", new AddressDTO(mockAddress.getName(), mockAddress.getStreet(), mockAddress.getHouseNumber(), mockAddress.getPostalCode())))
                .andExpect(view().name("address/edit"));
    }

    @Test
    public void getEditAddress_addressNotFound() throws Exception {
        long nonExistingId = 0;

        mockMvc.perform(TestHelper.createGetRequest(BASE_PATH + "/edit/" + nonExistingId, TestHelper.getCustomer(), new LinkedMultiValueMap<>()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("addressNotFound", true))
                .andExpect(MockMvcResultMatchers.model().attribute("addressId", nonExistingId))
                .andExpect(view().name("address/edit"));
    }

    @Test
    public void getEditAddress_security() throws Exception {
        Address mockAddress = TestHelper.getAddress();

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + mockAddress.getId(), null, getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + mockAddress.getId(), TestHelper.getManager(), getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    public void updateAddress_success() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + address.getId(), customer, getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("/**/address"));

        Optional<Address> optionalAddress = addressRepository.findById(address.getId());

        assertTrue(optionalAddress.isPresent());

        assertEquals(NAME, optionalAddress.get().getName());
        assertEquals(STREET, optionalAddress.get().getStreet());
        assertEquals(POSTAL_CODE, optionalAddress.get().getPostalCode());
        assertEquals(HOUSE_NUMBER, optionalAddress.get().getHouseNumber());
    }

    @Test
    public void updateAddress_invalidData() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + address.getId(), customer, getAddressDTOInvalidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().hasErrors())
                .andExpect(view().name("address/edit"));
    }

    @Test
    public void updateAddress_addressNotFound() throws Exception {
        long nonExistingId = 0;

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + nonExistingId, TestHelper.getCustomer(), getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("addressId", nonExistingId))
                .andExpect(MockMvcResultMatchers.model().attribute("hasServerError", true))
                .andExpect(view().name("address/edit"));
    }

    @Test
    public void updateAddress_security() throws Exception {
        ApplicationUser customer = TestHelper.getCustomer();
        Address address = TestHelper.getUserAddress(customer.getUsername());

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + address.getId(), null, getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(TestHelper.createPostRequest(BASE_PATH + "/edit/" + address.getId(), TestHelper.getManager(), getAddressDTOValidParams()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private MultiValueMap<String, String> getAddressDTOValidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", NAME);
        params.add("street", STREET);
        params.add("houseNumber", HOUSE_NUMBER);
        params.add("postalCode", POSTAL_CODE);

        return params;
    }

    private MultiValueMap<String, String> getAddressDTOInvalidParams() {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", null);
        params.add("street", null);
        params.add("houseNumber", null);
        params.add("postalCode", null);

        return params;
    }
}
