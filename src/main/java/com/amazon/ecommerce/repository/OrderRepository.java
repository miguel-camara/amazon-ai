package com.amazon.ecommerce.repository;

import com.amazon.ecommerce.entity.Order;
import com.amazon.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
