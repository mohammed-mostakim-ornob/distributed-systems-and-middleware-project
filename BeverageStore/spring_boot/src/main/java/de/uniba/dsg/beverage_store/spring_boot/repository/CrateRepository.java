package de.uniba.dsg.beverage_store.spring_boot.repository;

import de.uniba.dsg.beverage_store.spring_boot.model.db.Crate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CrateRepository extends JpaRepository<Crate, Long> {
    @Override
    @EntityGraph(value = "Crate.crates")
    Optional<Crate> findById(Long aLong);

    @EntityGraph(value = "Crate.crates")
    Page<Crate> findByOrderByNameAsc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Crate c SET c.inStock = (c.inStock - :quantity) WHERE c.id = :id")
    void decreaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);
}
