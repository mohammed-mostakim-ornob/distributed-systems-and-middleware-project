package de.uniba.dsg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.validation.annotation.MoreThanZero;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class InvoiceItem {
    @Min(value = 1, message = "Minimum Position is 1.")
    private int position;

    @NotNull(message = "Name is required.")
    @NotEmpty(message = "Name cannot be empty.")
    private String name;

    @NotNull(message = "Type is required.")
    @NotEmpty(message = "Type cannot be empty.")
    private String type;

    @Min(value = 1, message = "Minimum Quantity is 1.")
    private int quantity;

    @MoreThanZero(message = "Price should be more than zero.")
    private double price;

    public InvoiceItem(int position, String name, String type, int quantity, double price) {
        this.position = position;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
    }

    public InvoiceItem() {}

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @JsonIgnore
    public double getItemTotal() {
        return quantity * price;
    }
}
