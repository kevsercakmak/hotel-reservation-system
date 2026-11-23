package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "personel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad", nullable = false, length = 50)
    private String ad;

    @Column(name = "soyad", nullable = false, length = 50)
    private String soyad;

    @Column(name = "gorev", nullable = false, length = 30)
    private String gorev; // 'resepsiyon', 'kat_gorevlisi', 'yonetici'

    @Column(name = "ise_giris_tarihi", nullable = false)
    private LocalDate iseGirisTarihi;

    @PrePersist
    protected void onCreate() {
        if (this.iseGirisTarihi == null) {
            this.iseGirisTarihi = LocalDate.now();
        }
    }
}