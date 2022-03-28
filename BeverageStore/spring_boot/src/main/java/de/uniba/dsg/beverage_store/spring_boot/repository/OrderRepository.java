package de.uniba.dsg.beverage_store.spring_boot.repository;

import de.uniba.dsg.beverage_store.spring_boot.model.db.BeverageOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<BeverageOrder, Long> {
    @EntityGraph(value = "Order.orders")
    Page<BeverageOrder> findAllByOrderByOrderNumber(Pageable pageable);

    @EntityGraph(value = "Order.orders")
    List<BeverageOrder> findAllByUserUsernameOrderByOrderNumber(String userName);

    @EntityGraph(value = "Order.orders")
    Page<BeverageOrder> findAllByUserUsernameOrderByOrderNumber(String userName, Pageable pageable);

    @EntityGraph(value = "Order.orders")
    Optional<BeverageOrder> findByOrderNumber(String orderNumber);
}
