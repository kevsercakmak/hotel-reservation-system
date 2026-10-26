package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "misafir")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Misafir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad", nullable = false, length = 50)
    private String ad;

    @Column(name = "soyad", nullable = false, length = 50)
    private String soyad;

    @Column(name = "kimlik_no", nullable = false, unique = true, length = 20)
    private String kimlikNo;

    @Column(name = "telefon", nullable = false, length = 20)
    private String telefon;

    @Column(name = "eposta", nullable = false, unique = true, length = 100)
    private String eposta;

    @Column(name = "dogum_tarihi", nullable = false)
    private LocalDate dogumTarihi;

    @Column(name = "olusturma_tarihi", updatable = false)
    private LocalDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        this.olusturmaTarihi = LocalDateTime.now();
    }
}