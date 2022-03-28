package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeverageUpdateDTO {
    @NotNull(message = "Name is required.")
    @NotEmpty(message = "Name can not be empty.")
    private String name;

    @NotNull(message = "Picture URL is required")
    @Pattern(regexp = "(https://).*\\.(?:jpg|gif|png)", message = "Picture URL Must be a valid URL to a picture.")
    private String picUrl;

    @MoreThanZero(message = "Price must be more than zero.")
    private double price;
}
