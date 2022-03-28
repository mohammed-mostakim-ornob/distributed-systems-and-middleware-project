package de.uniba.dsg.beverage_store.spring_boot.model.dto;

import de.uniba.dsg.validation.annotation.FieldMatch;
import de.uniba.dsg.validation.annotation.InPast;
import de.uniba.dsg.validation.annotation.LaterThanOrEqualTo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(first = "repeatPassword", second = "password", message = "The passwords must match")
public class CustomerDTO {
    @NotNull(message = "First Name is required.")
    @NotEmpty(message = "First Name cannot be empty.")
    private String firstName;

    @NotNull(message = "Last Name is required.")
    @NotEmpty(message = "Last Name cannot be empty.")
    private String lastName;

    @NotNull(message = "Username is required.")
    @Length(min = 5, message = "Minimum Username length is 5.")
    private String username;

    @NotNull(message = "Email is required.")
    @NotEmpty(message = "Email is required.")
    @Email(message = "Valid Email is required.")
    private String email;

    @NotNull(message = "Password is required.")
    @Length(min = 5, message = "Minimum password length is 5.")
    private String password;

    @NotNull(message = "Repeat Password is required.")
    @Length(min = 5, message = "Minimum password length is 5.")
    private String repeatPassword;

    @InPast
    @LaterThanOrEqualTo(year = "1990", month = "01", dayOfMonth = "01", message = "Date must be later than 01-01-1990")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
