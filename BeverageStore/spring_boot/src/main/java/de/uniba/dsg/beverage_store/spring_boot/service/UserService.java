package de.uniba.dsg.beverage_store.spring_boot.service;

import de.uniba.dsg.beverage_store.spring_boot.exception.CredentialConflictException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CustomerDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ApplicationUser> userOptional = userRepository.findByUsername(username);

        if(userOptional.isPresent()) {
            return userOptional.get();
        }

        throw new UsernameNotFoundException("User '" + username + "' not found!");
    }

    public ApplicationUser getUserByUserName(String username) throws NotFoundException {
        Optional<ApplicationUser> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No User found with Username: " + username);
        }

        return optionalUser.get();
    }

    public ApplicationUser addCustomer(CustomerDTO customerDTO) throws CredentialConflictException {
        if (userRepository.findByUsername(customerDTO.getUsername()).isPresent())
            throw new CredentialConflictException("Username already taken.");

        if (userRepository.findByEmailIgnoreCase(customerDTO.getEmail()).isPresent())
            throw new CredentialConflictException("Customer already exists with this email.");

        ApplicationUser user = new ApplicationUser(
                null,
                customerDTO.getUsername(),
                customerDTO.getFirstName(),
                customerDTO.getLastName(),
                customerDTO.getEmail(),
                Helper.encryptPassword(customerDTO.getPassword()),
                customerDTO.getBirthday(),
                Role.ROLE_CUSTOMER,
                null,
                null
        );

        userRepository.save(user);

        return user;
    }

    public Page<ApplicationUser> getPagedCustomers(int page, int pageSize) {
        return userRepository.findAllByRole(PageRequest.of(page - 1, pageSize), Role.ROLE_CUSTOMER);
    }
}
