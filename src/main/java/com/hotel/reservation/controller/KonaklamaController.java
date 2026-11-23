package com.hotel.reservation.controller;

import com.hotel.reservation.entity.EkHizmet;
import com.hotel.reservation.entity.Konaklama;
import com.hotel.reservation.entity.KonaklamaHizmeti;
import com.hotel.reservation.service.KonaklamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/konaklamalar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KonaklamaController {

    private final KonaklamaService konaklamaService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestParam Long rezervasyonId, @RequestParam Long personelId) {
        try {
            Konaklama konaklama = konaklamaService.checkInYap(rezervasyonId, personelId);
            Map<String, Object> res = new HashMap<>();
            res.put("mesaj", "Check-in işlemi başarıyla tamamlandı.");
            res.put("konaklamaId", konaklama.getId());
            res.put("gercekGiris", konaklama.getGercekGiris());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }

    @PostMapping("/{konaklamaId}/check-out")
    public ResponseEntity<?> checkOut(@PathVariable Long konaklamaId, @RequestParam Long personelId) {
        try {
            Konaklama konaklama = konaklamaService.checkOutYap(konaklamaId, personelId);
            Map<String, Object> res = new HashMap<>();
            res.put("mesaj", "Check-out işlemi tamamlandı, oda temizlik durumuna alındı.");
            res.put("konaklamaId", konaklama.getId());
            res.put("gercekCikis", konaklama.getGercekCikis());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }

    @PostMapping("/{konaklamaId}/hizmetler")
    public ResponseEntity<?> hizmetEkle(
            @PathVariable Long konaklamaId,
            @RequestParam Long ekHizmetId,
            @RequestParam(defaultValue = "1") Integer adet) {
        try {
            KonaklamaHizmeti kayit = konaklamaService.ekHizmetEkle(konaklamaId, ekHizmetId, adet);
            Map<String, Object> res = new HashMap<>();
            res.put("mesaj", "Ek hizmet konaklamaya eklendi.");
            res.put("kayitId", kayit.getId());
            res.put("adet", kayit.getAdet());
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }

    @GetMapping("/{konaklamaId}/tutar")
    public ResponseEntity<?> tutarGetir(@PathVariable Long konaklamaId) {
        try {
            return ResponseEntity.ok(konaklamaService.tutarHesapla(konaklamaId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("hata", e.getMessage()));
        }
    }

    @GetMapping("/aktif")
    public ResponseEntity<?> aktifKonaklamalar() {
        var liste = new ArrayList<>();
        for (Konaklama k : konaklamaService.aktifKonaklamalariGetir()) {
            var item = new HashMap<String, Object>();
            item.put("konaklamaId", k.getId());
            item.put("rezervasyonId", k.getRezervasyon().getId());
            item.put("odaNumarasi", k.getRezervasyon().getOda().getOdaNumarasi());
            item.put("misafirAdSoyad", k.getRezervasyon().getMisafir().getAd() + " " + k.getRezervasyon().getMisafir().getSoyad());
            item.put("gercekGiris", k.getGercekGiris());
            liste.add(item);
        }
        return ResponseEntity.ok(liste);
    }

    @GetMapping("/ek-hizmetler")
    public ResponseEntity<List<EkHizmet>> ekHizmetler() {
        return ResponseEntity.ok(konaklamaService.tumEkHizmetleriGetir());
    }
}