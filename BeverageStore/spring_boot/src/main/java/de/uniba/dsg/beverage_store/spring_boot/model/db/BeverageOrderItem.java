package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.uniba.dsg.beverage_store.spring_boot.model.BeverageType;
import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@NamedEntityGraph(
        name = "OrderItem.orderItems",
        attributeNodes = {
                @NamedAttributeNode(value = "bottle"),
                @NamedAttributeNode(value = "crate")
        }
)
public class BeverageOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Beverage Type is required.")
    private BeverageType beverageType;

    @MoreThanZero(message = "Quantity must be greater than zero.")
    private int quantity;

    @MoreThanZero(message = "Position must be greater than zero.")
    private int position;

    //Entity Relations
    @ManyToOne
    private Bottle bottle;

    @ManyToOne
    private Crate crate;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonBackReference
    private BeverageOrder order;

    //Validation
    @PrePersist
    @PreUpdate
    public void validate() {
        if (beverageType == BeverageType.BOTTLE) {
            if (bottle == null) {
                throw new ValidationException("Bottle is required for Beverage Type Bottle");
            }

            if (crate != null) {
                throw new ValidationException("Crate should be null for Beverage Type Bottle");
            }
        } else if (beverageType == BeverageType.CRATE) {
            if (crate == null) {
                throw new ValidationException("Crate is required for Beverage Type Crate");
            }

            if (bottle != null) {
                throw new ValidationException("Bottle should be null for Beverage Type Crate");
            }
        }
    }
}
