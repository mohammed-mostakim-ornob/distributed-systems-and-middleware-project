package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrderItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.properties.OrderProperties;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/order")
public class OrderController {

    private final OrderService orderService;

    private final OrderProperties orderProperties;

    @Autowired
    public OrderController(
            OrderService orderService,
            OrderProperties orderProperties) {
        this.orderService = orderService;

        this.orderProperties = orderProperties;
    }

    @GetMapping
    public String getCustomerOrders(@RequestParam(defaultValue = "1") int page, Model model, Principal principal) {
        Page<BeverageOrder> orderPage = Page.empty();

        Optional<? extends GrantedAuthority> grantedAuthority = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst();

        if (grantedAuthority.isPresent()) {
            String userRole = grantedAuthority.get()
                    .getAuthority();

            log.info("Retrieving order page: " + page + " - start");

            orderPage = userRole.equals(Role.ROLE_MANAGER.name())
                    ? orderService.getPagedOrders(page, orderProperties.getPageSize())
                    : userRole.equals(Role.ROLE_CUSTOMER.name())
                        ? orderService.getPagedOrdersByUsername(principal.getName(), page, orderProperties.getPageSize())
                        : Page.empty();

            log.info("Retrieving order page: " + page + " - completed");
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("numberOfPages", orderPage.getTotalPages());

        return "order/list";
    }

    @GetMapping(value = "/{orderNumber}")
    public String getOrder(@PathVariable("orderNumber") String orderNumber, Model model) {
        log.info("Retrieving order with order number: " + orderNumber + " - start");

        try {
            BeverageOrder order = orderService.getOrderByOrderNumber(orderNumber);
            List<BeverageOrderItem> orderItems = orderService.getOrderItemsByOrderNumber(orderNumber);

            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("orderNotFound", false);

            log.info("Retrieving order with order number: " + orderNumber + " - completed");
        } catch (NotFoundException ex) {
            model.addAttribute("orderNotFound", true);

            log.info("Retrieving order with order number: " + orderNumber + " - failed, found not found exception");
        }

        return "order/details";
    }
}
