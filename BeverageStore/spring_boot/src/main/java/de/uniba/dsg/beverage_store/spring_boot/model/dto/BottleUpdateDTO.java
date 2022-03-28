package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BottleUpdateDTO extends BeverageUpdateDTO {
    public BottleUpdateDTO(String name, String picUrl, double price, double volume, double volumePercent, String supplier) {
        super(name, picUrl, price);

        this.volume = volume;
        this.volumePercent = volumePercent;
        this.supplier = supplier;
    }

    @MoreThanZero(message = "Volume must be more than zero.")
    private double volume;

    @Min(value = 0, message = "Volume Percent must be more then or equal to zero.")
    private double volumePercent;

    @NotNull(message = "Supplier is required.")
    @NotEmpty(message = "Supplier can not be empty.")
    private String supplier;
}
