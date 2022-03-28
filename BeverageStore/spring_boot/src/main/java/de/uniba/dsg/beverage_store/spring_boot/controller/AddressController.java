package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.AddressDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/address")
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public String getAddresses(Model model, Principal principal) {
        log.info("Retrieving addresses - start");

        model.addAttribute("addressDTO", new AddressDTO());
        model.addAttribute("addresses", retrievingAllAddressesByUsername(principal.getName()));

        log.info("Retrieving addresses - completed");

        return "address/list";
    }

    @PostMapping
    public String createAddress(@Valid AddressDTO addressDTO, Errors errors, Model model, Principal principal) {
        log.info("Creating addresses - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Creating addresses - failed, found model error");
        }

        if (!hasModelError) {
            try {
                addressService.addAddress(addressDTO, principal.getName());

                log.info("Creating addresses - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Creating addresses - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("hasServerError", hasServerError);
            model.addAttribute("addresses", retrievingAllAddressesByUsername(principal.getName()));

            return "address/list";
        }

        return "redirect:/address";
    }

    @GetMapping("/edit/{id}")
    public String getEditAddress(@PathVariable("id") long id, Model model) {
        try {
            log.info("Retrieving address with ID: " + id + " - start");

            Address address = addressService.getAddressById(id);

            model.addAttribute("addressDTO", new AddressDTO(address.getName(), address.getStreet(), address.getHouseNumber(), address.getPostalCode()));

            log.info("Retrieving address with ID: " + id + " - completed");
        } catch (NotFoundException e) {
            model.addAttribute("addressNotFound", true);

            log.info("Retrieving address with ID: " + id + " - failed, found not found exception");
        }

        model.addAttribute("addressId", id);

        return "address/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateAddress(@PathVariable("id") long id, @Valid AddressDTO addressDTO, Errors errors, Model model) {
        log.info("Updating address with ID: " + id + " - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Updating address with ID: " + id + " - failed, found model error");
        }

        if (!hasModelError) {
            try {
                addressService.updateAddress(id, addressDTO);

                log.info("Updating address with ID: " + id + " - completed");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Updating address with ID: " + id + " - failed, found server error");
            }
        }

        if (hasModelError || hasServerError) {
            model.addAttribute("addressId", id);
            model.addAttribute("hasServerError", hasServerError);

            return "address/edit";
        }

        return "redirect:/address";
    }

    private List<Address> retrievingAllAddressesByUsername(String username) {
        return  addressService.getAllByUsername(username);
    }
}
