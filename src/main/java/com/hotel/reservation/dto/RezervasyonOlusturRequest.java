package com.hotel.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RezervasyonOlusturRequest {

    @NotNull(message = "Misafir ID boş bırakılamaz")
    private Long misafirId;

    @NotNull(message = "Oda ID boş bırakılamaz")
    private Long odaId;

    @NotNull(message = "Giriş tarihi zorunludur")
    @FutureOrPresent(message = "Giriş tarihi bugünden önce olamaz")
    private LocalDate girisTarihi;

    @NotNull(message = "Çıkış tarihi zorunludur")
    @Future(message = "Çıkış tarihi gelecekte bir tarih olmalıdır")
    private LocalDate cikisTarihi;

    @NotNull(message = "Kişi sayısı zorunludur")
    @Min(value = 1, message = "En az 1 kişi olmalıdır")
    private Integer kisiSayisi;
}