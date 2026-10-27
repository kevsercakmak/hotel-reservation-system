package com.hotel.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RezervasyonResponse {
    private Long id;
    private String misafirAdSoyad;
    private String odaNumarasi;
    private String odaTipi;
    private LocalDate girisTarihi;
    private LocalDate cikisTarihi;
    private Integer kisiSayisi;
    private String durum;
}