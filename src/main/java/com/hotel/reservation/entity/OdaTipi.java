package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "oditipi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdaTipi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tip_adi", nullable = false, unique = true, length = 50)
    private String tipAdi;

    @Column(name = "kapasite", nullable = false)
    private Integer kapasite;

    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String aciklama;

    @Column(name = "temel_fiyat", nullable = false)
    private BigDecimal temelFiyat;
}