package com.sewagealert.community.repository;

import com.sewagealert.community.model.Ngo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NgoRepository extends JpaRepository<Ngo, Long> {
}
