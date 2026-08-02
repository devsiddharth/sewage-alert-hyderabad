package com.sewagealert.community.repository;

import com.sewagealert.community.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    // findByLocalityContainingIgnoreCase: Finds pipelines serving a specific locality
    List<Pipeline> findByLocalityContainingIgnoreCase(String locality);

    // findByOperationalStatus: Filters pipelines by their operational status
    List<Pipeline> findByOperationalStatus(Pipeline.OperationalStatus status);
}
