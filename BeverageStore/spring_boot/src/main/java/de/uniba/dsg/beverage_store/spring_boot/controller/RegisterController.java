package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.CredentialConflictException;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CustomerDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping(value = { "/register" })
public class RegisterController {

    private final UserService userService;

    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/customer")
    public String getRegisterCustomer(Model model) {
        model.addAttribute("customerDTO", new CustomerDTO());

        return "register-customer";
    }

    @PostMapping(value = "/customer")
    public String createCustomer(@Valid CustomerDTO customerDTO, Errors errors, Model model) {
        log.info("Creating customer - start");

        boolean hasModelError = false, hasServerError = false, hasConflictError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Creating customer - failed, found model error");
        }

        if (!hasModelError) {
            try {
                userService.addCustomer(customerDTO);

                log.info("Creating customer - completed");
            } catch (CredentialConflictException e) {
                hasConflictError = true;

                model.addAttribute("conflictErrorMessage", e.getMessage());

                log.info("Creating customer - failed, found credential conflict error");
            } catch (Exception e) {
                hasServerError = true;

                log.info("Creating customer - failed, found server error");
            }
        }

        if (hasModelError || hasServerError || hasConflictError) {
            model.addAttribute("hasServerError", hasServerError);
            model.addAttribute("hasConflictError", hasConflictError);

            return "register-customer";
        }

        return "redirect:/login";
    }
}
