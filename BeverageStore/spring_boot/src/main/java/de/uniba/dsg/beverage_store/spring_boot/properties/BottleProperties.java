package de.uniba.dsg.beverage_store.spring_boot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bottle")
public class BottleProperties {
    private Integer pageSize;
}
