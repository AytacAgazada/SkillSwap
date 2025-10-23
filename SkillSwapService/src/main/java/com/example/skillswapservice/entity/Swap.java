package com.example.skillswapservice.entity;

import com.example.skillswapservice.enumeration.SwapStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * İki istifadəçi arasındakı təsdiqlənmiş mübadilə əməliyyatını təmsil edir.
 */
@Entity
@Table(name = "swaps")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Swap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Təklifi edən istifadəçi (Offeror)
    private UUID user1Id;

    // Təklifə cavab verən istifadəçi (Requested)
    private UUID user2Id;

    // Əməliyyatın vəziyyəti
    @Enumerated(EnumType.STRING)
    private SwapStatus status = SwapStatus.REQUESTED;

    // Əlaqədar SwapOfferEntity-nin ID-si (optional)
    private Long swapOfferId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
