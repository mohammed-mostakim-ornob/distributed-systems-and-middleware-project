package de.uniba.dsg.beverage_store.spring_boot.service;

import de.uniba.dsg.beverage_store.spring_boot.exception.InvalidOperationException;
import de.uniba.dsg.beverage_store.spring_boot.exception.NotFoundException;
import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.CartItem;
import de.uniba.dsg.beverage_store.spring_boot.model.db.*;
import de.uniba.dsg.beverage_store.spring_boot.repository.BottleRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.CrateRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.OrderItemRepository;
import de.uniba.dsg.beverage_store.spring_boot.repository.OrderRepository;
import de.uniba.dsg.models.Invoice;
import de.uniba.dsg.models.InvoiceAddress;
import de.uniba.dsg.models.InvoiceItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final UserService userService;
    private final AddressService addressService;
    private final InvoiceService invoiceService;
    private final BeverageService beverageService;
    private final FireStoreService fireStoreService;

    private final CrateRepository crateRepository;
    private final BottleRepository bottleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Resource(name = "sessionScopedCartService")
    private CartService cartService;

    @Autowired
    public OrderService(UserService userService,
                        AddressService addressService,
                        BeverageService beverageService,
                        InvoiceService invoiceService,
                        FireStoreService fireStoreService,
                        CrateRepository crateRepository,
                        BottleRepository bottleRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository) {
        this.userService = userService;
        this.addressService = addressService;
        this.invoiceService = invoiceService;
        this.beverageService = beverageService;
        this.fireStoreService = fireStoreService;

        this.crateRepository = crateRepository;
        this.bottleRepository = bottleRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public BeverageOrder getOrderByOrderNumber(String orderNumber) throws NotFoundException {
        Optional<BeverageOrder> orderOptional = orderRepository.findByOrderNumber(orderNumber);

        if (orderOptional.isEmpty()) {
            throw new NotFoundException("No Order found with Order Number: " + orderNumber);
        }

        return orderOptional.get();
    }

    public Page<BeverageOrder> getPagedOrders(int page, int pageSize) {
        return orderRepository.findAllByOrderByOrderNumber(PageRequest.of(page - 1, pageSize));
    }

    public Page<BeverageOrder> getPagedOrdersByUsername(String username, int page, int pageSize) {
        return orderRepository.findAllByUserUsernameOrderByOrderNumber(username, PageRequest.of(page - 1, pageSize));
    }

    public List<BeverageOrder> getOrdersByUsername(String username) {
        return orderRepository.findAllByUserUsernameOrderByOrderNumber(username);
    }

    public List<BeverageOrderItem> getOrderItemsByOrderNumber(String orderNumber) {
        return orderItemRepository.findAllByOrderOrderNumber(orderNumber);
    }

    @Transactional
    public BeverageOrder createOrder(String userName, Long deliveryAddressId, Long billingAddressId) throws NotFoundException, InvalidOperationException {
        if (cartService.getCartItemCount() == 0)
            throw new InvalidOperationException("At least one Cart Item is required for checkout.");

        ApplicationUser customer = userService.getUserByUserName(userName);
        Address deliveryAddress = addressService.getAddressById(deliveryAddressId);
        Address billingAddress = addressService.getAddressById(billingAddressId);

        BeverageOrder order = new BeverageOrder(null, null, LocalDate.now(), cartService.getCartTotal(), customer, deliveryAddress, billingAddress, null);
        orderRepository.save(order);

        int count = 0;
        List<BeverageOrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem: cartService.getCartItems()) {
            int quantity = cartItem.getQuantity();
            Long beverageId = cartItem.getBeverageId();
            BeverageType beverageType = cartItem.getBeverageType();

            orderItems.add(buildOrderItem(order, beverageType, beverageId, quantity, ++count));

            if (beverageType == BeverageType.CRATE) {
                crateRepository.decreaseQuantity(beverageId, quantity);
            } else if (beverageType == BeverageType.BOTTLE) {
                bottleRepository.decreaseQuantity(beverageId, quantity);
            }
        }

        orderItemRepository.saveAll(orderItems);

        order.setOrderNumber(Helper.generateOrderNumber(order.getId()));
        orderRepository.save(order);

        cartService.clearCart();

        Invoice invoice = Helper.constructOrderInvoice(order, customer, deliveryAddress, billingAddress, orderItems);

        fireStoreService.storeOrder(invoice);
        invoiceService.generateInvoice(invoice);

        return order;
    }

    private BeverageOrderItem buildOrderItem(BeverageOrder order, BeverageType beverageType, Long beverageId, int quantity, int position) throws NotFoundException {
        return new BeverageOrderItem(
                null,
                beverageType,
                quantity,
                position,
                beverageType == BeverageType.BOTTLE
                        ? beverageService.getBottleById(beverageId)
                        : null,
                beverageType == BeverageType.CRATE
                        ? beverageService.getCrateById(beverageId)
                        : null,
                order
        );
    }
}
