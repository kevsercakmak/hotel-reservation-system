package com.hotel.reservation.service;

import com.hotel.reservation.entity.Konaklama;
import com.hotel.reservation.entity.Odeme;
import com.hotel.reservation.repository.KonaklamaRepository;
import com.hotel.reservation.repository.OdemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OdemeService {

    private final OdemeRepository odemeRepository;
    private final KonaklamaRepository konaklamaRepository;

    @Transactional
    public Odeme odemeAl(Long konaklamaId, BigDecimal tutar, String odemeTuru) {
        Konaklama konaklama = konaklamaRepository.findById(konaklamaId)
                .orElseThrow(() -> new RuntimeException("Konaklama bulunamadı: " + konaklamaId));

        if (!"nakit".equalsIgnoreCase(odemeTuru) && !"kart".equalsIgnoreCase(odemeTuru)) {
            throw new RuntimeException("Geçersiz ödeme türü! Sadece 'nakit' veya 'kart' olabilir.");
        }

        Odeme odeme = Odeme.builder()
                .konaklama(konaklama)
                .tutar(tutar)
                .odemeTuru(odemeTuru.toLowerCase())
                .build();

        // Kayıt eklendiğinde PostgreSQL'deki trg_log_odeme tetikleyicisi otomatik log tutar
        return odemeRepository.save(odeme);
    }

    public List<Odeme> konaklamaOdemeleriniGetir(Long konaklamaId) {
        return odemeRepository.findByKonaklamaId(konaklamaId);
    }
}