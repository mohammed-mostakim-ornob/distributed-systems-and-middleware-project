package de.uniba.dsg.beverage_store.spring_boot.helper;

import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Address;
import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrderItem;
import de.uniba.dsg.models.Invoice;
import de.uniba.dsg.models.InvoiceAddress;
import de.uniba.dsg.models.InvoiceItem;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.ObjectError;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {
    public static String generateOrderNumber(Long orderId) {
        LocalDate nowDate = LocalDate.now();

        return ("ORD" + (nowDate.getYear() % 100) +  String.format("%02d", nowDate.getMonthValue()) + String.format("%05d", orderId));
    }

    public static String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    public static String constructErrorMessage(List<ObjectError> errors) {
        return errors
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
    }

    public static Invoice constructOrderInvoice(BeverageOrder order, ApplicationUser customer, Address deliveryAddress, Address billingAddress, List<BeverageOrderItem> orderItems) {
        return new Invoice(
                order.getOrderNumber(),
                order.getDate(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                new InvoiceAddress(
                        deliveryAddress.getStreet(),
                        deliveryAddress.getHouseNumber(),
                        deliveryAddress.getPostalCode()
                ),
                new InvoiceAddress(
                        billingAddress.getStreet(),
                        billingAddress.getHouseNumber(),
                        billingAddress.getPostalCode()
                ),
                orderItems.stream()
                        .map(x -> new InvoiceItem(
                                x.getPosition(),
                                x.getBeverageType() == BeverageType.BOTTLE
                                        ? x.getBottle().getName()
                                        : x.getCrate().getName(),
                                x.getBeverageType().name(),
                                x.getQuantity(),
                                x.getBeverageType() == BeverageType.BOTTLE
                                        ? x.getBottle().getPrice()
                                        : x.getCrate().getPrice()

                        ))
                        .collect(Collectors.toList())
        );
    }
}
