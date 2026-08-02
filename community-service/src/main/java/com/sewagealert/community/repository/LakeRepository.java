package com.sewagealert.community.repository;

import com.sewagealert.community.model.Lake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LakeRepository extends JpaRepository<Lake, Long> {

    // findByConnectedStpId: Finds all lakes connected to a specific treatment plant
    List<Lake> findByConnectedStpId(Long connectedStpId);
}
