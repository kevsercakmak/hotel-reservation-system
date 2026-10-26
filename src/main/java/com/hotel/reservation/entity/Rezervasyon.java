package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rezervasyon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rezervasyon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "misafir_id", nullable = false)
    private Misafir misafir;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oda_id", nullable = false)
    private Oda oda;

    @Column(name = "giris_tarihi", nullable = false)
    private LocalDate girisTarihi;

    @Column(name = "cikis_tarihi", nullable = false)
    private LocalDate cikisTarihi;

    @Column(name = "kisi_sayisi", nullable = false)
    private Integer kisiSayisi;

    @Column(name = "durum", nullable = false, length = 20)
    private String durum; // 'beklemede', 'onayli', 'iptal', 'tamamlandi'

    @Column(name = "olusturma_tarihi", updatable = false)
    private LocalDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        this.olusturmaTarihi = LocalDateTime.now();
        if (this.durum == null) {
            this.durum = "beklemede";
        }
    }
}