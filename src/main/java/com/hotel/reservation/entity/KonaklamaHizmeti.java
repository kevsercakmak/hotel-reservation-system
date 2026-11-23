package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "konaklamahizmeti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KonaklamaHizmeti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "konaklama_id", nullable = false)
    private Konaklama konaklama;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ek_hizmet_id", nullable = false)
    private EkHizmet ekHizmet;

    @Column(name = "adet", nullable = false)
    private Integer adet;

    @Column(name = "tarih", updatable = false)
    private LocalDateTime tarih;

    @PrePersist
    protected void onCreate() {
        if (this.adet == null) {
            this.adet = 1;
        }
        this.tarih = LocalDateTime.now();
    }
}