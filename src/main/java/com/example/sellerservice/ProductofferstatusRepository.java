package com.example.sellerservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductofferstatusRepository extends JpaRepository<Productofferstatus, String> {

    @Query(value = "select * from productofferstatuses p where p.offerid = ?1",nativeQuery = true)
    public Optional<List<Productofferstatus>> findByOfferid(String offerid);

}