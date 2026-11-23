package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "odeme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Odeme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "konaklama_id", nullable = false)
    private Konaklama konaklama;

    @Column(name = "tutar", nullable = false, precision = 10, scale = 2)
    private BigDecimal tutar;

    @Column(name = "odeme_turu", nullable = false, length = 20)
    private String odemeTuru; // 'nakit', 'kart'

    @Column(name = "tarih", updatable = false)
    private LocalDateTime tarih;

    @PrePersist
    protected void onCreate() {
        this.tarih = LocalDateTime.now();
    }
}
