package com.sewagealert.community.repository;

import com.sewagealert.community.model.NgoExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NgoExpenseRecordRepository extends JpaRepository<NgoExpenseRecord, Long> {
    List<NgoExpenseRecord> findByNgoOrganizationIdOrderByExpenseDateDesc(Long ngoOrganizationId);
    List<NgoExpenseRecord> findByFundRecordId(Long fundRecordId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM NgoExpenseRecord e WHERE e.fundRecordId = :fundId")
    BigDecimal sumExpensesByFundRecord(@Param("fundId") Long fundRecordId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM NgoExpenseRecord e WHERE e.ngoOrganizationId = :ngoId")
    BigDecimal sumTotalExpensesByNgo(@Param("ngoId") Long ngoOrganizationId);
}
