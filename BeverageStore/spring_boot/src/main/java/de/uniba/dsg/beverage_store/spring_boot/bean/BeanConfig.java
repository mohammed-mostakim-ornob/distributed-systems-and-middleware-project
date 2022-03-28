package de.uniba.dsg.beverage_store.spring_boot.bean;

import de.uniba.dsg.beverage_store.spring_boot.service.CartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class BeanConfig {

    @Bean
    @SessionScope
    public CartService sessionScopedCartService() {
        return new CartService();
    }
}
