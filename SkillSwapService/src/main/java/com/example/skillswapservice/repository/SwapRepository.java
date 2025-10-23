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

    @Modifying
    @Query("UPDATE Swap s SET s.status = :status, s.meetingDateTime = :meetingDateTime, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :swapId")
    void updateStatusAndMeetingTime(@Param("swapId") Long swapId, @Param("status") SwapStatus status, @Param("meetingDateTime") java.time.LocalDateTime meetingDateTime);

    @Query("SELECT s FROM Swap s WHERE s.status = 'ACCEPTED' AND FUNCTION('DATE', s.meetingDateTime) = :date")
    java.util.List<com.example.skillswapservice.entity.Swap> findSwapsForTomorrow(@Param("date") java.time.LocalDate date);
}
