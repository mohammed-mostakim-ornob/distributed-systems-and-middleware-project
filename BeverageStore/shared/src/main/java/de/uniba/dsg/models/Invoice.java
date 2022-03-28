package de.uniba.dsg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.uniba.dsg.deserializers.LocalDateDeserializer;
import de.uniba.dsg.serializers.LocalDateSerializer;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class Invoice {
    @NotNull(message = "Order Number is required.")
    @NotEmpty(message = "Order Number cannot be empty.")
    private String orderNumber;

    @NotNull(message = "Order Date is required.")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate orderDate;

    @NotNull(message = "Customer Name is required.")
    @NotEmpty(message = "Customer Name cannot be empty.")
    private String customerName;

    @Email(message="Please provide a valid Customer Email ID.")
    private String customerEmailId;

    @Valid
    @NotNull(message = "Delivery Address is required.")
    private InvoiceAddress deliveryAddress;

    @Valid
    @NotNull(message = "Billing Address is required.")
    private InvoiceAddress billingAddress;

    @Valid
    @NotEmpty(message = "At least one Order Item is required.")
    private List<InvoiceItem> items;

    public Invoice() {}

    public Invoice(String orderNumber, LocalDate orderDate, String customerName, String customerEmailId, InvoiceAddress deliveryAddress, InvoiceAddress billingAddress, List<InvoiceItem> items) {
        this.setOrderNumber(orderNumber);
        this.setOrderDate(orderDate);
        this.setCustomerName(customerName);
        this.setCustomerEmailId(customerEmailId);
        this.setDeliveryAddress(deliveryAddress);
        this.setBillingAddress(billingAddress);
        this.setItems(items);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmailId() {
        return customerEmailId;
    }

    public void setCustomerEmailId(String customerEmailId) {
        this.customerEmailId = customerEmailId;
    }

    public InvoiceAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(InvoiceAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public InvoiceAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(InvoiceAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    @JsonIgnore
    public double getTotalPrice() {
        return getItems().stream()
                .map(x -> x.getPrice() * x.getQuantity())
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
