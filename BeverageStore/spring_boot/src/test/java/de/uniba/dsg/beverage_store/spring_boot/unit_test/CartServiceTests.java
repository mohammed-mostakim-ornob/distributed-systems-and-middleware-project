package de.uniba.dsg.beverage_store.spring_boot.unit_test;

import de.uniba.dsg.beverage_store.spring_boot.TestHelper;
import de.uniba.dsg.beverage_store.spring_boot.demo.DemoData;
import de.uniba.dsg.beverage_store.spring_boot.exception.InsufficientStockException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CartServiceTests {

    @Resource(name = "sessionScopedCartService")
    private CartService cartService;

    @BeforeEach
    public void init() throws NotFoundException, InsufficientStockException {
        cartService.clearCart();

        cartService.addCartItem(BeverageType.CRATE, TestHelper.getCrate().getId(), 2);
        cartService.addCartItem(BeverageType.BOTTLE, TestHelper.getBottle().getId(), 2);
    }

    @Test
    public void addCartItem_success() throws NotFoundException, InsufficientStockException {
        cartService.addCartItem(BeverageType.CRATE, TestHelper.getCrate().getId(), 2);
        cartService.addCartItem(BeverageType.BOTTLE, TestHelper.getBottle().getId(), 2);

        assertEquals(8, cartService.getCartItemCount());
        assertEquals(2, cartService.getCartItems().size());

        cartService.addCartItem(BeverageType.BOTTLE, TestHelper.getBottle().getId(), 2);

        assertEquals(10, cartService.getCartItemCount());
        assertEquals(2, cartService.getCartItems().size());
    }

    @Test
    public void addCartItem_insufficientStock() {
        assertThrows(InsufficientStockException.class, () -> cartService.addCartItem(BeverageType.CRATE, TestHelper.getCrate().getId(), TestHelper.getCrate().getAllowedInStock() + 1));
        assertThrows(InsufficientStockException.class, () -> cartService.addCartItem(BeverageType.BOTTLE, TestHelper.getBottle().getId(), TestHelper.getCrate().getAllowedInStock() + 1));

        Bottle bottle = DemoData.bottles.stream()
                .filter(x -> x.getId() == 4L)
                .findFirst()
                .orElse(null);
        assertNotNull(bottle);

        Crate crate = DemoData.crates.stream()
                .filter(x -> x.getId() == 4L)
                .findFirst()
                .orElse(null);
        assertNotNull(crate);

        assertThrows(InsufficientStockException.class, () -> cartService.addCartItem(BeverageType.CRATE, crate.getId(), crate.getAllowedInStock() + 1));
        assertThrows(InsufficientStockException.class, () -> cartService.addCartItem(BeverageType.BOTTLE, bottle.getId(), bottle.getAllowedInStock() + 1));
    }

    @Test
    public void addCartItem_invalidBeverageType() {
        assertThrows(NotFoundException.class, () -> cartService.addCartItem(null, TestHelper.getCrate().getId(), 2));
    }

    @Test
    public void removeCartItem_success() throws NotFoundException {
        CartItem cartItem = cartService.getCartItems()
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(cartItem);

        int cartItemQuantity = cartItem.getQuantity();
        double cartItemPrice = cartItem.getItemTotal();

        double totalPriceBeforeRemove = cartService.getCartTotal();
        int totalCartItemCountBeforeRemove = cartService.getCartItemCount();

        cartService.removeCartItem(cartItem.getCartItemId());

        assertEquals(1, cartService.getCartItems().size());
        assertEquals(totalPriceBeforeRemove - cartItemPrice, cartService.getCartTotal());
        assertEquals(totalCartItemCountBeforeRemove - cartItemQuantity, cartService.getCartItemCount());
    }

    @Test
    public void removeCartItem_cartItemNotFound() {
        assertThrows(NotFoundException.class, () -> cartService.removeCartItem(0));
    }

    @Test
    public void getCartItems_success() {
        assertEquals(2, cartService.getCartItems().size());
    }

    @Test
    public void getCartItemCount_success() {
        assertEquals(4, cartService.getCartItemCount());
    }

    @Test
    public void getCartTotal_success() {
        List<CartItem> cartItems = cartService.getCartItems();

        assertEquals(cartItems.stream().mapToDouble(CartItem::getItemTotal).sum(), cartService.getCartTotal());
    }

    @Test
    public void clearCart_success() {
        cartService.clearCart();

        assertEquals(0.0, cartService.getCartTotal());
        assertEquals(0, cartService.getCartItemCount());
        assertEquals(0, cartService.getCartItems().size());
    }
}
