package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeverageStockAddDTO {
    @Min(value = 1, message = "Least quantity addition must be 1.")
    private int quantity;
}
