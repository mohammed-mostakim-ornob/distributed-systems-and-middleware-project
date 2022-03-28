package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@NamedEntityGraph(
        name = "Crate.crates",
        attributeNodes = {
                @NamedAttributeNode(value = "bottle")
        }
)
public class Crate extends Beverage {
    public Crate(Long id, String name, String picUrl, int noOfBottles, double price, int inStock, Bottle bottle, Set<BeverageOrderItem> orderItems) {
        super(id, name, picUrl, price, inStock, inStock);

        this.noOfBottles = noOfBottles;
        this.bottle = bottle;
        this.orderItems = orderItems;
    }

    @MoreThanZero(message = "No of Bottles must be more than zero.")
    private int noOfBottles;

    //Entity Relations
    @ManyToOne(cascade = CascadeType.MERGE)
    private Bottle bottle;

    @OneToMany(mappedBy = "crate")
    @JsonBackReference
    private Set<BeverageOrderItem> orderItems;
}
