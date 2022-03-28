package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitOrderDTO {
    @MoreThanZero(message = "Delivery Address is required.")
    private long deliveryAddressId;

    @MoreThanZero(message = "Billing Address is required.")
    private long billingAddressId;
}
