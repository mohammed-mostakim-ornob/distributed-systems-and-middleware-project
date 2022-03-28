package de.uniba.dsg.beverage_store.spring_boot.controller;

import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.DropdownListItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.SubmitOrderDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.AddressService;
import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import de.uniba.dsg.beverage_store.spring_boot.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "/cart")
public class CartController {

    private final AddressService addressService;
    private final OrderService orderService;

    @Resource(name = "sessionScopedCartService")
    private CartService cartService;

    public CartController(AddressService addressService, OrderService orderService) {
        this.addressService = addressService;
        this.orderService = orderService;
    }

    @GetMapping
    public String getCart(Model model) {
        log.info("Retrieving cart items - start");

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartTotal", cartService.getCartTotal());
        model.addAttribute("cartItemCount", cartService.getCartItemCount());

        log.info("Retrieving cart items - completed");

        return "cart/details";
    }

    @GetMapping(value = "/checkout")
    public String getCheckout(Model model, Principal principal) {
        log.info("Retrieving cart details - start");

        model.addAttribute("cartTotal", cartService.getCartTotal());
        model.addAttribute("cartItemCount", cartService.getCartItemCount());
        model.addAttribute("isEmptyCart", (cartService.getCartItemCount() == 0));
        model.addAttribute("addressesDropdownListItems", getAddressDropdownListByUserName(principal.getName()));

        log.info("Retrieving cart details - completed");

        model.addAttribute("submitOrderDTO", new SubmitOrderDTO());

        return "cart/checkout";
    }

    @PostMapping(value = "/checkout")
    public String checkout(@Valid SubmitOrderDTO submitOrderDTO, Errors errors, Model model, Principal principal) {
        log.info("Creating order - start");

        boolean hasModelError = false, hasServerError = false;

        if (errors.hasErrors()) {
            hasModelError = true;

            log.info("Creating order - failed, found model error");
        }

        if (!hasModelError) {
            try {
                BeverageOrder order = orderService.createOrder(principal.getName(), submitOrderDTO.getDeliveryAddressId(), submitOrderDTO.getBillingAddressId());

                log.info("Creating order - completed");

                return "redirect:/order/" + order.getOrderNumber();
            } catch (Exception ex) {
                hasServerError = true;

                log.info("Creating order - failed, found server error");
            }
        }

        log.info("Retrieving cart details - start");

        model.addAttribute("cartTotal", cartService.getCartTotal());
        model.addAttribute("cartItemCount", cartService.getCartItemCount());
        model.addAttribute("isEmptyCart", (cartService.getCartItemCount() == 0));
        model.addAttribute("addressesDropdownListItems", getAddressDropdownListByUserName(principal.getName()));

        model.addAttribute("hasServerError", hasServerError);

        log.info("Retrieving cart details - completed");

        return "cart/checkout";
    }

    private List<DropdownListItem<Long>> getAddressDropdownListByUserName(String username) {
        return addressService.getAllByUsername(username)
                .stream()
                .map(Address::getDropdownListItem)
                .collect(Collectors.toList());
    }
}
