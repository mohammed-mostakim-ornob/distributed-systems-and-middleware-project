package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    @NotNull(message = "Name is required.")
    @NotEmpty(message = "Name can not be empty.")
    private String name;

    @NotNull(message = "Street is required.")
    @NotEmpty(message = "Street can not be empty.")
    private String street;

    @NotNull(message = "House Number is required.")
    @NotEmpty(message = "House Number can not be empty.")
    private String houseNumber;

    @NotNull(message = "Postal Code is required.")
    @Pattern(regexp = "\\b\\d{5}\\b", message = "Postal Code must be a 5 digit number.")
    private String postalCode;
}
