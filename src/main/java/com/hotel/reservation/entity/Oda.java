package com.hotel.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oda_numarasi", nullable = false, unique = true, length = 10)
    private String odaNumarasi;

    @Column(name = "kat", nullable = false)
    private Integer kat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "oda_tipi_id", nullable = false)
    private OdaTipi odaTipi;

    @Column(name = "durum", nullable = false, length = 20)
    private String durum; // 'bos', 'dolu', 'temizlikte', 'bakimda'
}