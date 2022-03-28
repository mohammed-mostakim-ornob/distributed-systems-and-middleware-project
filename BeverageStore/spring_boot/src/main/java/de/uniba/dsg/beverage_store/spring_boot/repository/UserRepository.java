package de.uniba.dsg.beverage_store.spring_boot.repository;

import de.uniba.dsg.beverage_store.spring_boot.model.db.ApplicationUser;
import de.uniba.dsg.beverage_store.spring_boot.model.db.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Long> {
    List<ApplicationUser> findAllByRole(Role role);

    Page<ApplicationUser> findAllByRole(Pageable page, Role role);

    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmailIgnoreCase(String email);
}
