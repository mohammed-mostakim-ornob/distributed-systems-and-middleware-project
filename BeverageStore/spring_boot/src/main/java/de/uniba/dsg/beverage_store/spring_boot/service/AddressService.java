package de.uniba.dsg.beverage_store.spring_boot.service;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.AddressDTO;
import de.uniba.dsg.beverage_store.spring_boot.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final UserService userService;
    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(UserService userService, AddressRepository addressRepository) {
        this.userService = userService;
        this.addressRepository = addressRepository;
    }

    public Address getAddressById(Long id) throws NotFoundException {
        Optional<Address> addressOptional = addressRepository.findById(id);

        if (addressOptional.isEmpty()) {
            throw new NotFoundException("No Address found with the ID: "+ id);
        }

        return addressOptional.get();
    }

    public List<Address> getAllByUsername(String username) {
        return addressRepository.findAllByUserUsername(username);
    }

    public Address addAddress(AddressDTO addressDTO, String username) throws NotFoundException {
        ApplicationUser user = userService.getUserByUserName(username);

        Address address = new Address(
                null,
                addressDTO.getName().trim(),
                addressDTO.getStreet().trim(),
                addressDTO.getHouseNumber().trim(),
                addressDTO.getPostalCode().trim(),
                user,
                null,
                null
        );
        addressRepository.save(address);

        return address;
    }

    public void updateAddress(Long addressId, AddressDTO addressDTO) throws NotFoundException {
        Address address = getAddressById(addressId);

        address.setName(addressDTO.getName().trim());
        address.setStreet(addressDTO.getStreet().trim());
        address.setHouseNumber(addressDTO.getHouseNumber().trim());
        address.setPostalCode(addressDTO.getPostalCode().trim());

        addressRepository.save(address);
    }
}
