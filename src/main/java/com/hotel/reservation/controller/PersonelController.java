package com.hotel.reservation.controller;

import com.hotel.reservation.entity.Personel;
import com.hotel.reservation.service.PersonelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personeller")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PersonelController {

    private final PersonelService personelService;

    @GetMapping
    public ResponseEntity<List<Personel>> tumPersoneller() {
        return ResponseEntity.ok(personelService.tumPersonelleriGetir());
    }

    @GetMapping("/gorev/{gorev}")
    public ResponseEntity<List<Personel>> goreveGorePersoneller(@PathVariable String gorev) {
        return ResponseEntity.ok(personelService.goreveGoreGetir(gorev));
    }
}