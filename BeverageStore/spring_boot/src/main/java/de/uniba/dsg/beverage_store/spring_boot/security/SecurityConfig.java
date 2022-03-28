package de.uniba.dsg.beverage_store.spring_boot.security;

import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import de.uniba.dsg.beverage_store.spring_boot.service.UserService;
import org.apache.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer, HandlerInterceptor {

    private final UserService userService;

    private final String[] anonymousPages = {
            "/register/customer"
    };

    private final String[] authenticatedPages = {
            "/",
            "/home"
    };

    private final String[] permitAllPages = {
            "/scripts/**",
            "/stylesheets/**"
    };

    private final String[] managerCustomerPages = {
            "/beverage/bottle",
            "/beverage/crate",
            "/order/**",
            "/api/invoice/order/**"
    };

    private final String[] customerPages = {
            "/cart",
            "/cart/checkout",
            "/address/**",
            "/api/cart-items/**"
    };

    private final String[] managerPages = {
            "/beverage/bottle/add",
            "/beverage/bottle/edit/**",
            "/beverage/crate/add",
            "/beverage/crate/edit/**",
            "/customer/**",
            "/api/bottles/{\\\\d+}/stock",
            "/api/crates/{\\\\d+}/stock"
    };

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder createEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);

        auth.userDetailsService(userService).passwordEncoder(createEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(managerPages)
                    .hasRole(Role.MANAGER.name())
                .antMatchers(customerPages)
                    .hasRole(Role.CUSTOMER.name())
                .antMatchers(managerCustomerPages)
                    .hasAnyRole(Role.MANAGER.name(), Role.CUSTOMER.name())
                .antMatchers(authenticatedPages)
                    .authenticated()
                .antMatchers(permitAllPages)
                    .permitAll()
                .antMatchers(anonymousPages)
                    .anonymous()
                .and()

                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/home")
                .and()

                .csrf()
                    .ignoringAntMatchers("/api/**")
                .and()

                .headers()
                    .frameOptions()
                    .sameOrigin()
                .and()

                .logout()
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessUrl("/login");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();

        if (urlPathHelper.getLookupPathForRequest(request).equalsIgnoreCase("/login") && isAuthenticated()) {
            String encodedRedirectURL = response.encodeRedirectURL(request.getContextPath() + "/");

            response.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
            response.setHeader("Location", encodedRedirectURL);

            return false;
        } else {
            return true;
        }
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
