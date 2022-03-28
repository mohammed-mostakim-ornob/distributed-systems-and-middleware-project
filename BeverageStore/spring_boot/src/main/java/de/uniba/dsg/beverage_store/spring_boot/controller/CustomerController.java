package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.properties.CustomerProperties;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
import de.uniba.dsg.beverage_store.spring_boot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = { "/customer"})
public class CustomerController {

    private final UserService userService;
    private final OrderService orderService;
    private final AddressService addressService;

    private final CustomerProperties customerProperties;

    @Autowired
    public CustomerController(UserService userService,
                              OrderService orderService,
                              AddressService addressService,
                              CustomerProperties customerProperties) {
        this.userService = userService;
        this.orderService = orderService;
        this.addressService = addressService;

        this.customerProperties = customerProperties;
    }

    @GetMapping
    public String getCustomer(@RequestParam(defaultValue = "1") int page, Model model) {
        log.info("Retrieving customer page: " + page + " - start");

        Page<ApplicationUser> customerPage = userService.getPagedCustomers(page, customerProperties.getPageSize());

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("numberOfPages", customerPage.getTotalPages());

        log.info("Retrieving customer page: " + page + " - completed");

        return "customer/list";
    }

    @GetMapping(value = "/{username}")
    public String getCustomerDetails(@PathVariable("username") String username, Model model) {
        log.info("Retrieving customer with username: " + username + " - start");

        try {
            ApplicationUser customer = userService.getUserByUserName(username);
            List<BeverageOrder> orders = orderService.getOrdersByUsername(username);
            List<Address> addresses = addressService.getAllByUsername(username);

            model.addAttribute("customer", customer);
            model.addAttribute("orders", orders);
            model.addAttribute("addresses", addresses);
            model.addAttribute("customerNotFound", false);

            log.info("Retrieving customer with username: " + username + " - completed");
        } catch (NotFoundException ex) {
            model.addAttribute("customerNotFound", true);

            log.info("Retrieving customer with username: " + username + " - failed, found not found exception");
        }

        return "customer/details";
    }
}
