package de.uniba.dsg.beverage_store.spring_boot.unit_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.demo.DemoData;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.AddressDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.AddressRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AddressServiceTests {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    public void getAddressById_success() throws NotFoundException {
        Address expectedAddress = TestHelper.getAddress();

        assertNotNull(expectedAddress);

        Address actualAddress = addressService.getAddressById(expectedAddress.getId());

        assertEquals(expectedAddress.getId(), actualAddress.getId());
        assertEquals(expectedAddress.getName(), actualAddress.getName());
    }

    @Test
    public void getAddressById_addressNotFound() {
        assertThrows(NotFoundException.class, () -> addressService.getAddressById(0L));
    }

    @Test
    public void getAllByUsername_success() {
        ApplicationUser customer = DemoData.applicationUsers.stream()
                .filter(x -> x.getRole() == Role.ROLE_CUSTOMER)
                .findFirst()
                .orElse(null);

        assertNotNull(customer);

        List<Address> expectedData = addressRepository.findAllByUserUsername(customer.getUsername());

        List<Address> actualData = addressService.getAllByUsername(customer.getUsername());

        assertEquals(expectedData.size(), actualData.size());
    }

    @Test
    @Transactional
    public void addAddress_success() throws NotFoundException {
        ApplicationUser customer = DemoData.applicationUsers.stream()
                .filter(x -> x.getRole() == Role.ROLE_CUSTOMER)
                .findFirst()
                .orElse(null);

        assertNotNull(customer);

        long countBeforeAdd = addressRepository.count();

        long userAddressCountBeforeAdd = addressRepository.findAllByUserUsername(customer.getUsername())
                .size();

        AddressDTO addressDTO = new AddressDTO(
                "Address 1",
                "Pestalozzistraße",
                "9F",
                "96052");

        Address addedAddress = addressService.addAddress(addressDTO, customer.getUsername());

        assertNotNull(addedAddress);
        assertNotNull(addedAddress.getId());
        assertEquals(addressDTO.getName(), addedAddress.getName());
        assertEquals(countBeforeAdd + 1, addressRepository.count());
        assertEquals(userAddressCountBeforeAdd + 1, addressRepository.findAllByUserUsername(customer.getUsername()).size());
    }

    @Test
    public void addAddress_useNotFound() {
        assertThrows(NotFoundException.class, () -> addressService.addAddress(new AddressDTO(
                "Address 1",
                "Pestalozzistraße",
                "9F",
                "96052"), "testuser"));
    }

    @Test
    @Transactional
    public void updateAddress_success() throws NotFoundException {
        long countBeforeUpdate = addressRepository.count();

        Address address = TestHelper.getAddress();

        assertNotNull(address);

        addressService.updateAddress(address.getId(), new AddressDTO(
                "Address 2",
                "Kapellenstraße",
                "23",
                "96050"));

        Address updatedAddress = addressService.getAddressById(address.getId());

        assertEquals(countBeforeUpdate, addressRepository.count());

        assertEquals("Address 2", updatedAddress.getName());
        assertEquals("Kapellenstraße", updatedAddress.getStreet());
        assertEquals("23", updatedAddress.getHouseNumber());
        assertEquals("96050", updatedAddress.getPostalCode());
    }

    @Test
    public void updateAddress_addressNotFound() {
        assertThrows(NotFoundException.class, () -> addressService.updateAddress(0L, new AddressDTO(
                "Address 2",
                "Kapellenstraße",
                "23",
                "96050")));
    }
}
