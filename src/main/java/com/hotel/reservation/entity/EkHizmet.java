package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ekhizmet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EkHizmet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hizmet_adi", nullable = false, length = 100)
    private String hizmetAdi;

    @Column(name = "birim_fiyat", nullable = false, precision = 10, scale = 2)
    private BigDecimal birimFiyat;
}