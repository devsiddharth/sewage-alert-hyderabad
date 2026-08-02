package com.sewagealert.community.repository;

import com.sewagealert.community.model.TreatmentPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentPlantRepository extends JpaRepository<TreatmentPlant, Long> {
}
