package de.uniba.dsg.beverage_store.spring_boot.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uniba.dsg.validation.annotation.LaterThanOrEqualTo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        })
})
public class ApplicationUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Username is required.")
    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotNull(message = "First Name is required.")
    @NotEmpty(message = "First Name cannot be empty.")
    private String firstName;

    @NotNull(message = "Last Name is required.")
    @NotEmpty(message = "Last Name cannot be empty.")
    private String lastName;

    @Email(message = "Valid Email is required.")
    private String email;

    @NotNull(message = "Password is required.")
    @NotEmpty(message = "Password cannot be empty.")
    private String password;

    @NotNull
    @LaterThanOrEqualTo(year = "1990", month = "01", dayOfMonth = "01")
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required.")
    private Role role;

    // Entity Relations
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Address> addresses;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<BeverageOrder> orders;

    // Authentication
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
