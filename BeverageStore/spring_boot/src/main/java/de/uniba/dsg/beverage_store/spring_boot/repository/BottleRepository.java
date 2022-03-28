package de.uniba.dsg.beverage_store.spring_boot.repository;

import de.uniba.dsg.beverage_store.spring_boot.model.db.Bottle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BottleRepository extends JpaRepository<Bottle, Long> {
    Page<Bottle> findByOrderByNameAsc(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Bottle b set b.inStock = (b.inStock - :quantity) where b.id = :id")
    void decreaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);
}
