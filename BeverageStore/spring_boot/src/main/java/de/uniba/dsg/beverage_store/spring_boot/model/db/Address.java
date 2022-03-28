package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.beverage_store.spring_boot.model.DropdownListItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name is required.")
    @NotEmpty(message = "Name can not be empty.")
    private String name;

    @NotNull(message = "Street is required.")
    @NotEmpty(message = "Street can not be empty.")
    private String street;

    @NotNull(message = "Street is required.")
    @NotEmpty(message = "Street can not be empty.")
    private String houseNumber;

    @NotNull(message = "Postal Code is required.")
    @Pattern(regexp = "\\b\\d{5}\\b", message = "Postal Code must be a 5 digit number.")
    private String postalCode;

    // Entity Relations
    @ManyToOne
    @JsonIgnore
    private ApplicationUser user;

    @OneToMany(mappedBy = "deliveryAddress")
    @JsonIgnore
    private Set<BeverageOrder> deliveryAddressOrders;

    @OneToMany(mappedBy = "billingAddress")
    @JsonIgnore
    private Set<BeverageOrder> billingAddressOrders;

    public DropdownListItem<Long> getDropdownListItem() {
        return new DropdownListItem<>(getId(), getName(), String.format("%s %s, %s", getStreet(), getHouseNumber(), getPostalCode()));
    }
}
