package de.uniba.dsg.beverage_store.spring_boot.demo;

import de.uniba.dsg.beverage_store.spring_boot.helper.Helper;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.db.*;
import de.uniba.dsg.beverage_store.spring_boot.repository.*;
import de.uniba.dsg.beverage_store.spring_boot.service.FireStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
public class DemoData {

    private final FireStoreService fireStoreService;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CrateRepository crateRepository;
    private final BottleRepository bottleRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;

    private final String picUrl = "https://www.google.com/logos/doodles/2020/december-holidays-day-1-6753651837108829.4-law.gif" ;

    public static List<Crate> crates = new ArrayList<>();
    public static List<Bottle> bottles = new ArrayList<>();
    public static List<Address> addresses = new ArrayList<>();
    public static List<BeverageOrder> orders = new ArrayList<>();
    public static List<BeverageOrderItem> orderItems = new ArrayList<>();
    public static List<ApplicationUser> applicationUsers = new ArrayList<>();

    @Autowired
    public DemoData(FireStoreService fireStoreService,
                    UserRepository userRepository,
                    OrderRepository orderRepository,
                    CrateRepository crateRepository,
                    BottleRepository bottleRepository,
                    AddressRepository addressRepository,
                    OrderItemRepository orderItemRepository) {
        this.fireStoreService = fireStoreService;

        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.crateRepository = crateRepository;
        this.bottleRepository = bottleRepository;
        this.addressRepository = addressRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @EventListener
    public void createDemoData(ApplicationReadyEvent event) {
        log.info("Creating demo data - start");

        addUser("manager", "Manager", "manager@beverage.store.de", "manager", Role.ROLE_MANAGER);

        ApplicationUser customerUser1 = addUser("customer1", "Customer 1", "customer1@beverage.store.de", "customer1", Role.ROLE_CUSTOMER);
        ApplicationUser customerUser2 = addUser("customer2", "Customer 2", "customer2@beverage.store.de", "customer2", Role.ROLE_CUSTOMER);

        Address customer1Address1 = addAddress("Address 1", "Pestalozzistraße", "9f", "96052", customerUser1);
        Address customer1Address2 = addAddress("Address 2", "Kapellenstraße", "23", "96050", customerUser1);

        Address customer2Address1 = addAddress("Address 1", "Pestalozzistraße", "9f", "96052", customerUser2);
        Address customer2Address2 = addAddress("Address 2", "Kapellenstraße", "23", "96050", customerUser2);

        Bottle cocaCola = addBottle("Coca-Cola");
        Bottle sprite = addBottle("Sprite");
        Bottle pepsi = addBottle("Pepsi");
        Bottle sevenUp = addBottle("7Up");

        Crate cocaColaCrate = addCrate("Coca-Cola Crate", cocaCola);
        Crate spriteCrate = addCrate("Sprite Crate", sprite);
        Crate pepsiCrate = addCrate("Pepsi Crate", pepsi);
        Crate sevenUpCrate = addCrate("7Up Crate", sevenUp);

        addOrder(customerUser1, customer1Address1, customer1Address2, cocaCola, sprite, pepsiCrate, sevenUpCrate);
        addOrder(customerUser2, customer2Address1, customer2Address2, pepsi, sevenUp, cocaColaCrate, spriteCrate);

        log.info("Creating demo data - completed");
    }

    private ApplicationUser addUser(String username, String firstName, String email, String password, Role role) {
        ApplicationUser user = new ApplicationUser(null, username, firstName, "User", email, Helper.encryptPassword(password), LocalDate.of(1990, 1, 1), role, null, null);

        userRepository.save(user);

        applicationUsers.add(user);

        return user;
    }

    private Address addAddress(String name, String street, String houseNo, String postalCode, ApplicationUser customer) {
        Address address = new Address(null, name, street, houseNo, postalCode, customer, null, null);

        addressRepository.save(address);

        addresses.add(address);

        return address;
    }

    private Bottle addBottle(String name) {
        Bottle bottle = new Bottle(null, name, picUrl, 1.0, 0.0, 1.0, "Coca-cola Limited", 10, null, null);

        bottleRepository.save(bottle);

        bottles.add(bottle);

        return bottle;
    }

    private Crate addCrate(String name, Bottle bottle) {
        Crate crate = new Crate(null, name, picUrl, 10, 10.0, 10, bottle, null);

        crateRepository.save(crate);

        crates.add(crate);

        return crate;
    }

    private void addOrder(ApplicationUser customer, Address deliveryAddress, Address billingAddress, Bottle bottle1, Bottle bottle2, Crate crate1, Crate crate2) {
        int quantity = 2;
        double total = (bottle1.getPrice() + bottle2.getPrice() + crate1.getPrice() + crate2.getPrice()) * quantity;

        BeverageOrder order = new BeverageOrder(null, null, LocalDate.now(), total, customer, deliveryAddress, billingAddress, null);
        orderRepository.save(order);
        order.setOrderNumber(Helper.generateOrderNumber(order.getId()));
        orderRepository.save(order);

        BeverageOrderItem orderItem1 = new BeverageOrderItem(null, BeverageType.BOTTLE, quantity, 1, bottle1, null, order);
        BeverageOrderItem orderItem2 = new BeverageOrderItem(null, BeverageType.BOTTLE, quantity, 2, bottle2, null, order);
        BeverageOrderItem orderItem3 = new BeverageOrderItem(null, BeverageType.CRATE, quantity, 3, null, crate1, order);
        BeverageOrderItem orderItem4 = new BeverageOrderItem(null, BeverageType.CRATE, quantity, 4, null, crate2, order);

        orderItemRepository.saveAll(Arrays.asList(orderItem1, orderItem2, orderItem3, orderItem4));

        order.setOrderItems(new HashSet<>(Arrays.asList(orderItem1, orderItem2, orderItem3, orderItem4)));

        orders.add(order);
        orderItems.addAll(Arrays.asList(orderItem1, orderItem2, orderItem3, orderItem4));

        fireStoreService.storeOrder(Helper.constructOrderInvoice(order, customer, deliveryAddress, billingAddress, Arrays.asList(orderItem1, orderItem2, orderItem3, orderItem4)));
    }
}
