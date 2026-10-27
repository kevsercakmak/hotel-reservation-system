package com.hotel.reservation.service;

import com.hotel.reservation.dto.RezervasyonOlusturRequest;
import com.hotel.reservation.dto.RezervasyonResponse;
import com.hotel.reservation.entity.Misafir;
import com.hotel.reservation.entity.Oda;
import com.hotel.reservation.entity.Rezervasyon;
import com.hotel.reservation.repository.MisafirRepository;
import com.hotel.reservation.repository.OdaRepository;
import com.hotel.reservation.repository.RezervasyonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RezervasyonService {

    private final RezervasyonRepository rezervasyonRepository;
    private final MisafirRepository misafirRepository;
    private final OdaRepository odaRepository;

    @Transactional
    public RezervasyonResponse rezervasyonOlustur(RezervasyonOlusturRequest request) {
        if (!request.getCikisTarihi().isAfter(request.getGirisTarihi())) {
            throw new IllegalArgumentException("Çıkış tarihi giriş tarihinden sonra olmalıdır.");
        }

        Misafir misafir = misafirRepository.findById(request.getMisafirId())
                .orElseThrow(() -> new RuntimeException("Misafir bulunamadı: " + request.getMisafirId()));

        Oda oda = odaRepository.findById(request.getOdaId())
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + request.getOdaId()));

        // Servis seviyesinde tarih çakışma kontrolü
        boolean cakismaVar = rezervasyonRepository.existsCakisma(
                request.getOdaId(), 
                request.getGirisTarihi(), 
                request.getCikisTarihi()
        );

        if (cakismaVar) {
            throw new IllegalStateException("Seçilen tarihler arasında bu oda zaten doludur.");
        }

        Rezervasyon rezervasyon = Rezervasyon.builder()
                .misafir(misafir)
                .oda(oda)
                .girisTarihi(request.getGirisTarihi())
                .cikisTarihi(request.getCikisTarihi())
                .kisiSayisi(request.getKisiSayisi())
                .durum("beklemede")
                .build();

        Rezervasyon kaydedilen = rezervasyonRepository.save(rezervasyon);

        return mapToResponse(kaydedilen);
    }

    public List<RezervasyonResponse> tumRezervasyonlar() {
        return rezervasyonRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RezervasyonResponse mapToResponse(Rezervasyon r) {
        return RezervasyonResponse.builder()
                .id(r.getId())
                .misafirAdSoyad(r.getMisafir().getAd() + " " + r.getMisafir().getSoyad())
                .odaNumarasi(r.getOda().getOdaNumarasi())
                .odaTipi(r.getOda().getOdaTipi().getTipAdi())
                .girisTarihi(r.getGirisTarihi())
                .cikisTarihi(r.getCikisTarihi())
                .kisiSayisi(r.getKisiSayisi())
                .durum(r.getDurum())
                .build();
    }
}