package com.hotel.reservation.controller;

import com.hotel.reservation.entity.Odeme;
import com.hotel.reservation.service.OdemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/odemeler")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdemeController {

    private final OdemeService odemeService;

    @PostMapping
    public ResponseEntity<?> odemeYap(
            @RequestParam Long konaklamaId,
            @RequestParam BigDecimal tutar,
            @RequestParam String odemeTuru) {
        try {
            Odeme odeme = odemeService.odemeAl(konaklamaId, tutar, odemeTuru);
            Map<String, Object> res = new HashMap<>();
            res.put("mesaj", "Ödeme başarıyla alındı ve loglandı.");
            res.put("odemeId", odeme.getId());
            res.put("tutar", odeme.getTutar());
            res.put("odemeTuru", odeme.getOdemeTuru());
            res.put("tarih", odeme.getTarih());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }

    @GetMapping("/konaklama/{konaklamaId}")
    public ResponseEntity<?> konaklamaOdemeleri(@PathVariable Long konaklamaId) {
        var odemeler = new ArrayList<>();
        for (Odeme o : odemeService.konaklamaOdemeleriniGetir(konaklamaId)) {
            var item = new HashMap<String, Object>();
            item.put("id", o.getId());
            item.put("tutar", o.getTutar());
            item.put("odemeTuru", o.getOdemeTuru());
            item.put("tarih", o.getTarih());
            odemeler.add(item);
        }
        return ResponseEntity.ok(odemeler);
    }
}