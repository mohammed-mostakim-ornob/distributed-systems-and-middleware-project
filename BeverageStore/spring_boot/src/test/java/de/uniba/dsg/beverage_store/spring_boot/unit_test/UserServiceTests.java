package de.uniba.dsg.beverage_store.spring_boot.unit_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.demo.DemoData;
import de.uniba.dsg.beverage_store.spring_boot.exception.CredentialConflictException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CustomerDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.UserRepository;
import de.uniba.dsg.beverage_store.spring_boot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void loadUserByUsername_success() {
        ApplicationUser expectedUser = TestHelper.getUser();

        assertNotNull(expectedUser);

        UserDetails actualUser = userService.loadUserByUsername(expectedUser.getUsername());

        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        assertEquals(expectedUser.getAuthorities(), actualUser.getAuthorities());
    }

    @Test
    public void loadUserByUsername_userNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("Test User"));
    }

    @Test
    public void getUserByUserName_success() throws NotFoundException {
        ApplicationUser expectedUser = TestHelper.getUser();

        assertNotNull(expectedUser);

        ApplicationUser actualUser = userService.getUserByUserName(expectedUser.getUsername());

        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(), actualUser.getLastName());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getBirthday(), actualUser.getBirthday());
        assertEquals(expectedUser.getRole(), actualUser.getRole());
        assertEquals(expectedUser.getAuthorities(), actualUser.getAuthorities());
    }

    @Test
    public void getUserByUserName_userNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getUserByUserName("Test User"));
    }

    @Test
    @Transactional
    public void addCustomer_success() throws CredentialConflictException {
        long countBeforeAdd = userRepository.findAllByRole(Role.ROLE_CUSTOMER)
                .size();

        CustomerDTO customerDTO = new CustomerDTO(
                "Test",
                "User",
                "test-user",
                "testuser@beveragestore.com",
                "test-user",
                "test-user",
                LocalDate.of(1993, 1, 1));

        ApplicationUser addedCustomer = userService.addCustomer(customerDTO);

        assertNotNull(addedCustomer);
        assertNotNull(addedCustomer.getId());
        assertEquals(customerDTO.getFirstName(), customerDTO.getFirstName());
        assertEquals(customerDTO.getLastName(), customerDTO.getLastName());
        assertEquals(customerDTO.getUsername(), customerDTO.getUsername());
        assertEquals(customerDTO.getEmail(), customerDTO.getEmail());
        assertEquals(customerDTO.getBirthday(), customerDTO.getBirthday());
        assertEquals(countBeforeAdd + 1, userRepository.findAllByRole(Role.ROLE_CUSTOMER).size());
    }

    @Test
    public void addCustomer_credentialConflict() {
        ApplicationUser customer = DemoData.applicationUsers.stream()
                .filter(x -> x.getRole() == Role.ROLE_CUSTOMER)
                .findFirst()
                .orElse(null);

        assertNotNull(customer);

        assertThrows(CredentialConflictException.class, () -> userService.addCustomer(new CustomerDTO(
                "Test",
                "User 2",
                customer.getUsername(),
                "testuser2@beveragestore.com",
                "test-user",
                "test-user",
                LocalDate.of(1993, 1, 1))));

        assertThrows(CredentialConflictException.class, () -> userService.addCustomer(new CustomerDTO(
                "Test",
                "User",
                "test-user2",
                customer.getEmail(),
                "test-user",
                "test-user",
                LocalDate.of(1993, 1, 1))));

        assertThrows(CredentialConflictException.class, () -> userService.addCustomer(new CustomerDTO(
                "Test",
                "User 2",
                customer.getUsername(),
                customer.getEmail(),
                "test-user",
                "test-user",
                LocalDate.of(1993, 1, 1))));
    }

    @Test
    public void getPagedCustomers_success() {
        int customerCount = userRepository.findAllByRole(Role.ROLE_CUSTOMER).size();

        Page<ApplicationUser> firstPage = userService.getPagedCustomers(1, customerCount + 1);
        Page<ApplicationUser> secondPage = userService.getPagedCustomers(2, customerCount + 1);

        assertEquals(1, firstPage.getTotalPages());
        assertEquals(customerCount, firstPage.stream().count());
        assertEquals(customerCount, firstPage.getTotalElements());

        assertEquals(0, secondPage.stream().count());
    }
}
