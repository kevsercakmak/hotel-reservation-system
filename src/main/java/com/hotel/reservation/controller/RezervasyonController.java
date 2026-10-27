package com.hotel.reservation.controller;

import com.hotel.reservation.dto.RezervasyonOlusturRequest;
import com.hotel.reservation.dto.RezervasyonResponse;
import com.hotel.reservation.service.RezervasyonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rezervasyonlar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RezervasyonController {

    private final RezervasyonService rezervasyonService;

    @GetMapping
    public ResponseEntity<List<RezervasyonResponse>> tumRezervasyonlar() {
        return ResponseEntity.ok(rezervasyonService.tumRezervasyonlar());
    }

    @PostMapping
    public ResponseEntity<RezervasyonResponse> rezervasyonOlustur(@Valid @RequestBody RezervasyonOlusturRequest request) {
        RezervasyonResponse response = rezervasyonService.rezervasyonOlustur(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}