package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoFundRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NgoFundRecordRepository extends JpaRepository<NgoFundRecord, Long> {
    List<NgoFundRecord> findByNgoOrganizationIdOrderByReceivedDateDesc(Long ngoOrganizationId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM NgoFundRecord f WHERE f.ngoOrganizationId = :ngoId")
    BigDecimal sumTotalFundsByNgo(@Param("ngoId") Long ngoOrganizationId);

    @Query("SELECT COALESCE(SUM(f.allocatedAmount), 0) FROM NgoFundRecord f WHERE f.ngoOrganizationId = :ngoId")
    BigDecimal sumAllocatedFundsByNgo(@Param("ngoId") Long ngoOrganizationId);
}
