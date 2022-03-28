package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import de.uniba.dsg.validation.annotation.MoreThanZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CrateUpdateDTO extends BeverageUpdateDTO {
    public CrateUpdateDTO(String name, String picUrl, double price, int noOfBottles, long bottleId) {
        super(name, picUrl, price);

        this.noOfBottles = noOfBottles;
        this.bottleId = bottleId;
    }

    @MoreThanZero(message = "No of Bottles must be more then zero.")
    private int noOfBottles;

    @MoreThanZero(message = "Bottle is required.")
    private long bottleId;
}
