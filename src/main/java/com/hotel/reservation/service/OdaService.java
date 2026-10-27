package com.hotel.reservation.service;

import com.hotel.reservation.entity.Oda;
import com.hotel.reservation.repository.OdaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OdaService {

    private final OdaRepository odaRepository;

    public List<Oda> tumOdalar() {
        return odaRepository.findAll();
    }

    public List<Oda> musaitOdalariGetir(LocalDate girisTarihi, LocalDate cikisTarihi) {
        if (cikisTarihi.isBefore(girisTarihi) || cikisTarihi.isEqual(girisTarihi)) {
            throw new IllegalArgumentException("Çıkış tarihi giriş tarihinden sonra olmalıdır.");
        }
        return odaRepository.findMusaitOdalar(girisTarihi, cikisTarihi);
    }
}