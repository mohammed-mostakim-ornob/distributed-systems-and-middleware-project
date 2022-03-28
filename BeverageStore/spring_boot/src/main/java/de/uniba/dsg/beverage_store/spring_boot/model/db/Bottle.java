package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.uniba.dsg.beverage_store.spring_boot.model.DropdownListItem;
import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Bottle extends Beverage {
    public Bottle (Long id, String name, String picUrl, double volume, double volumePercent, double price, String supplier, int inStock, Set<Crate> crates, Set<BeverageOrderItem> orderItems) {
        super(id, name, picUrl, price, inStock, inStock);

        this.volume = volume;
        this.volumePercent = volumePercent;
        this.supplier = supplier;
        this.crates = crates;
        this.orderItems = orderItems;
    }

    @MoreThanZero(message = "Volume must be more than zero.")
    private double volume;

    @Min(value = 0, message = "Volume Percent must be more then or equal to zero.")
    private double volumePercent;

    @NotNull(message = "Supplier is required.")
    @NotEmpty(message = "Supplier can not be empty.")
    private String supplier;

    //Entity Relations
    @OneToMany(mappedBy = "bottle", cascade = CascadeType.REMOVE)
    @JsonBackReference
    private Set<Crate> crates;

    @OneToMany(mappedBy = "bottle")
    @JsonBackReference
    private Set<BeverageOrderItem> orderItems;

    public DropdownListItem<Long> getDropdownListItem() {
        return new DropdownListItem<>(getId(), getName(), getName());
    }
}
