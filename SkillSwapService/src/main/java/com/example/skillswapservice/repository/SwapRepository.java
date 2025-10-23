package com.example.skillswapservice.repository;

import com.example.skillswapservice.entity.Swap;
import com.example.skillswapservice.enumeration.SwapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SwapRepository extends JpaRepository<Swap, Long> {

    /**
     * Verilmiş Swap ID-si üçün statusu yeniləyir.
     */
    @Modifying
    @Query("UPDATE Swap s SET s.status = :status, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :swapId")
    void updateStatus(@Param("swapId") Long swapId, @Param("status") SwapStatus status);
}
