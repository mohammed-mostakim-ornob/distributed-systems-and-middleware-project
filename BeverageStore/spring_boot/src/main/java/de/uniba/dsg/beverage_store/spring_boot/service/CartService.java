package de.uniba.dsg.beverage_store.spring_boot.service;

import de.uniba.dsg.beverage_store.spring_boot.exception.InsufficientStockException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartService {

    @Autowired
    private BeverageService beverageService;

    private int cartItemId;

    private final List<CartItem> cartItems;

    public CartService() {
        cartItemId = 0;
        cartItems = new ArrayList<>();
    }

    public CartItem addCartItem(BeverageType beverageType, Long beverageId, int quantity) throws NotFoundException, InsufficientStockException {
        CartItem cartItem;

        Optional<CartItem> cartItemOptional = getCartBeverage(beverageId, beverageType);

        if (cartItemOptional.isPresent()) {
            cartItem = cartItemOptional.get();

            if ((cartItem.getQuantity() + quantity) > cartItem.getInStock()) {
                throw new InsufficientStockException("Insufficient stock for " + beverageType.name() + " with ID: " + beverageId);
            }

            cartItem.addQuantity(quantity);

            return cartItem;
        }

        cartItem = retrieveCartItem(beverageType, beverageId, quantity);
        if (cartItem == null) {
            throw new NotFoundException();
        }

        if (cartItem.getQuantity() > cartItem.getInStock()) {
            throw new InsufficientStockException("Insufficient stock for " + beverageType.name() + " with ID: " + beverageId);
        }

        cartItem.setCartItemId(++cartItemId);
        cartItems.add(cartItem);

        return cartItem;
    }

    public void removeCartItem(Integer cartItemId) throws NotFoundException {
        Optional<CartItem> optionalCartItem = cartItems.stream()
                .filter(x -> x.getCartItemId() == cartItemId)
                .findAny();

        if (optionalCartItem.isEmpty()) {
            throw new NotFoundException();
        }

        cartItems.remove(optionalCartItem.get());
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getCartItemCount() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getCartTotal() {
        return cartItems.stream()
                .mapToDouble(CartItem::getItemTotal)
                .sum();
    }

    public void clearCart() {
        cartItemId = 0;
        cartItems.clear();
    }

    private CartItem buildBottleCartItem(Bottle bottle, int quantity) {
        return new CartItem() {
            {
                setCartItemId(0);
                setBeverageType(BeverageType.BOTTLE);
                setBeverageId(bottle.getId());
                setQuantity(quantity);
                setName(bottle.getName());
                setPicUrl(bottle.getPicUrl());
                setPrice(bottle.getPrice());
                setInStock(bottle.getInStock());
                setVolume(bottle.getVolume());
                setVolumePercent(bottle.getVolumePercent());
                setSupplier(bottle.getSupplier());
            }
        };
    }

    private CartItem buildCrateCartItem(Crate crate, int quantity) {
        return new CartItem() {
            {
                setCartItemId(0);
                setBeverageType(BeverageType.CRATE);
                setBeverageId(crate.getId());
                setQuantity(quantity);
                setName(crate.getName());
                setPicUrl(crate.getPicUrl());
                setPrice(crate.getPrice());
                setInStock(crate.getInStock());
                setNoOfBottle(crate.getNoOfBottles());
            }
        };
    }

    private Optional<CartItem> getCartBeverage(long beverageId, BeverageType beverageType) {
        return cartItems.stream()
                .filter(x -> x.getBeverageId() == beverageId && x.getBeverageType() == beverageType)
                .findAny();
    }

    private CartItem retrieveCartItem(BeverageType beverageType, Long beverageId, int quantity) throws NotFoundException {
        if (beverageType == BeverageType.BOTTLE) {
            Bottle bottle = beverageService.getBottleById(beverageId);

            return buildBottleCartItem(bottle, quantity);
        } else if (beverageType == BeverageType.CRATE) {
            Crate crate = beverageService.getCrateById(beverageId);

            return buildCrateCartItem(crate, quantity);
        }

        return null;
    }
}
