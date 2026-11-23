package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "konaklama")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Konaklama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rezervasyon_id", nullable = false, unique = true)
    private Rezervasyon rezervasyon;

    @Column(name = "gercek_giris", nullable = false)
    private LocalDateTime gercekGiris;

    @Column(name = "gercek_cikis")
    private LocalDateTime gercekCikis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giris_yapan_personel_id", nullable = false)
    private Personel girisYapanPersonel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cikis_yapan_personel_id")
    private Personel cikisYapanPersonel;

    @PrePersist
    protected void onCreate() {
        if (this.gercekGiris == null) {
            this.gercekGiris = LocalDateTime.now();
        }
    }
}