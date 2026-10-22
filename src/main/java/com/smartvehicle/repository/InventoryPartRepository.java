package com.smartvehicle.repository;

import com.smartvehicle.model.InventoryPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryPartRepository extends JpaRepository<InventoryPart, Long> {
}
