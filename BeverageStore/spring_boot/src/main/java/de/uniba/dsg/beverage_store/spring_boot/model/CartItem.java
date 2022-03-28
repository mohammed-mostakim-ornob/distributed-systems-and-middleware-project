package de.uniba.dsg.beverage_store.spring_boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private int cartItemId;
    private BeverageType beverageType;
    private long beverageId;
    private int quantity;
    private String name;
    private String picUrl;
    private double price;
    private int inStock;
    private double volume;
    private double volumePercent;
    private String supplier;
    private int noOfBottle;

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public double getItemTotal() {
        return price * quantity;
    }
}
