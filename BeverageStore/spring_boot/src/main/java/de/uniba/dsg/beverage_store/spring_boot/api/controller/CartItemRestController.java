package de.uniba.dsg.beverage_store.spring_boot.api.controller;

import de.uniba.dsg.beverage_store.spring_boot.exception.InsufficientStockException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.dto.CartItemDTO;
import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/api/cart-items")
public class CartItemRestController {

    @Resource(name = "sessionScopedCartService")
    CartService cartService;

    @GetMapping(value = "/count")
    public ResponseEntity<Integer> getCount() {
        log.info("Retrieving cart item count - start");

        int cartItemCount = cartService.getCartItemCount();

        log.info("Retrieving cart item count - completed");

        return new ResponseEntity<>(cartItemCount, HttpStatus.OK);
    }

    @GetMapping(value = "/total-price")
    public ResponseEntity<Double> getTotalPrice() {
        log.info("Retrieving cart item total - start");

        double cartTotal = cartService.getCartTotal();

        log.info("Retrieving cart item total - completed");

        return new ResponseEntity<>(cartTotal, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addCartItem(@RequestBody @Valid CartItemDTO cartItemDTO, Errors errors) {
        log.info("Adding cart item - start");

        if (errors.hasErrors()) {
            log.info("Adding cart item - failed, found model error");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Helper.constructErrorMessage(errors.getAllErrors()));
        }

        try {
            CartItem newCartItem = cartService.addCartItem(cartItemDTO.getBeverageType(), cartItemDTO.getBeverageId(), cartItemDTO.getQuantity());

            log.info("Adding cart item - completed");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(newCartItem);
        } catch (NotFoundException e) {
            log.info("Adding cart item - failed, found not found exception");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (InsufficientStockException e) {
            log.info("Adding cart item - failed, found insufficient stock exception");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping(value = "/{cart-item-id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable(name = "cart-item-id") Integer cartItemId) {
        log.info("Removing cart item with ID: " + cartItemId + " - start");

        try {
            cartService.removeCartItem(cartItemId);

            log.info("Removing cart item with ID: " + cartItemId + " - completed");

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (NotFoundException ex) {
            log.info("Removing cart item with ID: " + cartItemId + " - failed, found not found exception");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No Cart Item found with ID: " + cartItemId);
        }
    }
}
