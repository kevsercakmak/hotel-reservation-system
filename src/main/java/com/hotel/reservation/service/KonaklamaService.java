package com.hotel.reservation.service;

import com.hotel.reservation.entity.*;
import com.hotel.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KonaklamaService {

    private final KonaklamaRepository konaklamaRepository;
    private final RezervasyonRepository rezervasyonRepository;
    private final PersonelRepository personelRepository;
    private final EkHizmetRepository ekHizmetRepository;
    private final KonaklamaHizmetiRepository konaklamaHizmetiRepository;

    // 1. Check-in İşlemi (Veritabanındaki trg_oda_durumu_guncelle otomatik çalışır)
    @Transactional
    public Konaklama checkInYap(Long rezervasyonId, Long personelId) {
        Rezervasyon rezervasyon = rezervasyonRepository.findById(rezervasyonId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı: " + rezervasyonId));

        if ("iptal".equals(rezervasyon.getDurum()) || "tamamlandi".equals(rezervasyon.getDurum())) {
            throw new RuntimeException("Bu rezervasyon durumu check-in için uygun değil: " + rezervasyon.getDurum());
        }

        if (konaklamaRepository.findByRezervasyonId(rezervasyonId).isPresent()) {
            throw new RuntimeException("Bu rezervasyon için zaten check-in yapılmış.");
        }

        Personel personel = personelRepository.findById(personelId)
                .orElseThrow(() -> new RuntimeException("Personel bulunamadı: " + personelId));

        Konaklama konaklama = Konaklama.builder()
                .rezervasyon(rezervasyon)
                .girisYapanPersonel(personel)
                .gercekGiris(LocalDateTime.now())
                .build();

        return konaklamaRepository.save(konaklama);
    }

    // 2. Check-out İşlemi (Oda durumunu otomatik 'temizlikte' yapar)
    @Transactional
    public Konaklama checkOutYap(Long konaklamaId, Long personelId) {
        Konaklama konaklama = konaklamaRepository.findById(konaklamaId)
                .orElseThrow(() -> new RuntimeException("Konaklama kaydı bulunamadı: " + konaklamaId));

        if (konaklama.getGercekCikis() != null) {
            throw new RuntimeException("Bu konaklama için zaten check-out yapılmış.");
        }

        Personel personel = personelRepository.findById(personelId)
                .orElseThrow(() -> new RuntimeException("Personel bulunamadı: " + personelId));

        konaklama.setGercekCikis(LocalDateTime.now());
        konaklama.setCikisYapanPersonel(personel);

        return konaklamaRepository.save(konaklama);
    }

    // 3. Konaklamaya Ek Hizmet Ekleme (Kahvaltı, Spa, Otopark vb.)
    @Transactional
    public KonaklamaHizmeti ekHizmetEkle(Long konaklamaId, Long ekHizmetId, Integer adet) {
        Konaklama konaklama = konaklamaRepository.findById(konaklamaId)
                .orElseThrow(() -> new RuntimeException("Konaklama bulunamadı: " + konaklamaId));

        EkHizmet ekHizmet = ekHizmetRepository.findById(ekHizmetId)
                .orElseThrow(() -> new RuntimeException("Ek hizmet bulunamadı: " + ekHizmetId));

        KonaklamaHizmeti kayit = KonaklamaHizmeti.builder()
                .konaklama(konaklama)
                .ekHizmet(ekHizmet)
                .adet(adet != null && adet > 0 ? adet : 1)
                .build();

        return konaklamaHizmetiRepository.save(kayit);
    }

    // 4. Saklı Yordam (sp_konaklama_tutari_hesapla) ile Toplam Hesap Dökümü
    public Map<String, Object> tutarHesapla(Long konaklamaId) {
        if (!konaklamaRepository.existsById(konaklamaId)) {
            throw new RuntimeException("Konaklama bulunamadı: " + konaklamaId);
        }
        return konaklamaRepository.hesaplaKonaklamaTutari(konaklamaId);
    }

    public List<Konaklama> aktifKonaklamalariGetir() {
        return konaklamaRepository.findByGercekCikisIsNull();
    }

    public List<EkHizmet> tumEkHizmetleriGetir() {
        return ekHizmetRepository.findAll();
    }
}