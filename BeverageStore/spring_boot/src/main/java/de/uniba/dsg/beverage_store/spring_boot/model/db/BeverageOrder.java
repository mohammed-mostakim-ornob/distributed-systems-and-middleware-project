package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@NamedEntityGraph(
        name = "Order.orders",
        attributeNodes = {
                @NamedAttributeNode(value = "user"),
                @NamedAttributeNode(value = "deliveryAddress"),
                @NamedAttributeNode(value = "billingAddress")
        }
)
public class BeverageOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @NotNull(message = "Date is required.")
    private LocalDate date;

    @MoreThanZero(message = "Price must be more than zero.")
    private double price;

    //Entity Relations
    @ManyToOne
    @JsonIgnoreProperties({"addresses", "orders"})
    private ApplicationUser user;

    @ManyToOne
    @JsonIgnoreProperties({"user", "deliveryAddressOrders", "billingAddressOrders"})
    private Address deliveryAddress;

    @ManyToOne
    @JsonIgnoreProperties({"user", "deliveryAddressOrders", "billingAddressOrders"})
    private Address billingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE)
    @JsonBackReference
    private Set<BeverageOrderItem> orderItems;
}
