package com.example.phuongstore.customer.repository;

import com.example.phuongstore.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
        SELECT c FROM Customer c
        WHERE LOWER(c.fullName)    LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR c.phoneNumber        LIKE CONCAT('%', :keyword, '%')
           OR LOWER(c.facebookUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    java.util.Optional<Customer> findByPhoneNumber(String phoneNumber);
}
