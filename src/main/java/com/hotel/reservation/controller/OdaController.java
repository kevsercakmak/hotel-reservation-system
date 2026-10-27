package com.hotel.reservation.controller;

import com.hotel.reservation.entity.Oda;
import com.hotel.reservation.service.OdaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/odalar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdaController {

    private final OdaService odaService;

    @GetMapping
    public ResponseEntity<List<Oda>> tumOdalar() {
        return ResponseEntity.ok(odaService.tumOdalar());
    }

    @GetMapping("/musait")
    public ResponseEntity<List<Oda>> musaitOdalar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate girisTarihi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cikisTarihi) {
        return ResponseEntity.ok(odaService.musaitOdalariGetir(girisTarihi, cikisTarihi));
    }
}