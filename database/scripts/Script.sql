-- 1. Misafir Tablosu
CREATE TABLE Misafir (
    id SERIAL PRIMARY KEY,
    ad VARCHAR(50) NOT NULL,
    soyad VARCHAR(50) NOT NULL,
    kimlik_no VARCHAR(20) UNIQUE NOT NULL,
    telefon VARCHAR(20) NOT NULL,
    eposta VARCHAR(100) UNIQUE NOT NULL,
    dogum_tarihi DATE NOT NULL,
    olusturma_tarihi TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Personel Tablosu
CREATE TABLE Personel (
    id SERIAL PRIMARY KEY,
    ad VARCHAR(50) NOT NULL,
    soyad VARCHAR(50) NOT NULL,
    gorev VARCHAR(30) NOT NULL CHECK (gorev IN ('resepsiyon', 'kat_gorevlisi', 'yonetici')),
    ise_giris_tarihi DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 3. Kullanici Tablosu
CREATE TABLE Kullanici (
    id SERIAL PRIMARY KEY,
    kullanici_adi VARCHAR(50) UNIQUE NOT NULL,
    sifre_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('misafir', 'resepsiyon', 'yonetici')),
    misafir_id INT REFERENCES Misafir(id) ON DELETE SET NULL,
    personel_id INT REFERENCES Personel(id) ON DELETE SET NULL,
    CONSTRAINT chk_kullanici_baglanti CHECK (
        (rol = 'misafir' AND misafir_id IS NOT NULL AND personel_id IS NULL) OR
        (rol IN ('resepsiyon', 'yonetici') AND personel_id IS NOT NULL AND misafir_id IS NULL)
    )
);

-- 4. Oda Tipi Tablosu
CREATE TABLE OdaTipi (
    id SERIAL PRIMARY KEY,
    tip_adi VARCHAR(50) UNIQUE NOT NULL,
    kapasite INT NOT NULL CHECK (kapasite > 0),
    aciklama TEXT,
    temel_fiyat NUMERIC(10, 2) NOT NULL CHECK (temel_fiyat >= 0)
);

-- 5. Oda Tablosu
CREATE TABLE Oda (
    id SERIAL PRIMARY KEY,
    oda_numarasi VARCHAR(10) UNIQUE NOT NULL,
    kat INT NOT NULL,
    oda_tipi_id INT NOT NULL REFERENCES OdaTipi(id),
    durum VARCHAR(20) NOT NULL DEFAULT 'bos' CHECK (durum IN ('bos', 'dolu', 'temizlikte', 'bakimda'))
);

-- 6. Sezon Fiyatı Tablosu
CREATE TABLE SezonFiyati (
    id SERIAL PRIMARY KEY,
    oda_tipi_id INT NOT NULL REFERENCES OdaTipi(id) ON DELETE CASCADE,
    baslangic_tarihi DATE NOT NULL,
    bitis_tarihi DATE NOT NULL,
    gecelik_fiyat NUMERIC(10, 2) NOT NULL CHECK (gecelik_fiyat > 0),
    CONSTRAINT chk_sezon_tarih CHECK (bitis_tarihi >= baslangic_tarihi)
);

-- 7. Rezervasyon Tablosu
CREATE TABLE Rezervasyon (
    id SERIAL PRIMARY KEY,
    misafir_id INT NOT NULL REFERENCES Misafir(id),
    oda_id INT NOT NULL REFERENCES Oda(id),
    giris_tarihi DATE NOT NULL,
    cikis_tarihi DATE NOT NULL,
    kisi_sayisi INT NOT NULL CHECK (kisi_sayisi > 0),
    durum VARCHAR(20) NOT NULL DEFAULT 'beklemede' CHECK (durum IN ('beklemede', 'onayli', 'iptal', 'tamamlandi')),
    olusturma_tarihi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rezervasyon_tarih CHECK (cikis_tarihi > giris_tarihi)
);

-- 8. Konaklama Tablosu
CREATE TABLE Konaklama (
    id SERIAL PRIMARY KEY,
    rezervasyon_id INT UNIQUE NOT NULL REFERENCES Rezervasyon(id),
    gercek_giris TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gercek_cikis TIMESTAMP,
    giris_yapan_personel_id INT NOT NULL REFERENCES Personel(id),
    cikis_yapan_personel_id INT REFERENCES Personel(id)
);

-- 9. Ek Hizmet Tablosu
CREATE TABLE EkHizmet (
    id SERIAL PRIMARY KEY,
    hizmet_adi VARCHAR(100) NOT NULL,
    birim_fiyat NUMERIC(10, 2) NOT NULL CHECK (birim_fiyat >= 0)
);

-- 10. Konaklama Hizmeti 
CREATE TABLE KonaklamaHizmeti (
    id SERIAL PRIMARY KEY,
    konaklama_id INT NOT NULL REFERENCES Konaklama(id) ON DELETE CASCADE,
    ek_hizmet_id INT NOT NULL REFERENCES EkHizmet(id),
    adet INT NOT NULL DEFAULT 1 CHECK (adet > 0),
    tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. Ödeme Tablosu
CREATE TABLE Odeme (
    id SERIAL PRIMARY KEY,
    konaklama_id INT NOT NULL REFERENCES Konaklama(id),
    tutar NUMERIC(10, 2) NOT NULL CHECK (tutar >= 0),
    odeme_turu VARCHAR(20) NOT NULL CHECK (odeme_turu IN ('nakit', 'kart')),
    tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. Temizlik Kaydı Tablosu
CREATE TABLE TemizlikKaydi (
    id SERIAL PRIMARY KEY,
    oda_id INT NOT NULL REFERENCES Oda(id),
    personel_id INT NOT NULL REFERENCES Personel(id),
    tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    durum VARCHAR(30) NOT NULL CHECK (durum IN ('basladi', 'tamamlandi', 'kontrol_edildi'))
);

-- 13. İşlem Log Tablosu
CREATE TABLE IslemLog (
    id SERIAL PRIMARY KEY,
    tablo_adi VARCHAR(50) NOT NULL,
    islem_turu VARCHAR(10) NOT NULL,
    islem_tarihi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    eski_veri JSONB,
    yeni_veri JSONB
);

-- 1. TRIGGER: Çakışan Rezervasyonları Engelleyen Tetikleyici

CREATE OR REPLACE FUNCTION fn_kontrol_rezervasyon_cakisma()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.durum <> 'iptal' THEN
        IF EXISTS (
            SELECT 1 FROM Rezervasyon
            WHERE oda_id = NEW.oda_id
              AND id <> COALESCE(NEW.id, 0)
              AND durum <> 'iptal'
              AND (NEW.giris_tarihi < cikis_tarihi AND NEW.cikis_tarihi > giris_tarihi)
        ) THEN
            RAISE EXCEPTION 'Bu oda seçilen tarih aralığında (% - %) zaten rezerve edilmiştir!', NEW.giris_tarihi, NEW.cikis_tarihi;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_rezervasyon_cakisma ON Rezervasyon;
CREATE TRIGGER trg_rezervasyon_cakisma
BEFORE INSERT OR UPDATE ON Rezervasyon
FOR EACH ROW
EXECUTE FUNCTION fn_kontrol_rezervasyon_cakisma();


-- 2. TRIGGER: Check-in / Check-out ile Oda Durumu Güncelleme

CREATE OR REPLACE FUNCTION fn_oda_durumu_guncelle()
RETURNS TRIGGER AS $$
DECLARE
    v_oda_id INT;
BEGIN
    SELECT oda_id INTO v_oda_id FROM Rezervasyon WHERE id = NEW.rezervasyon_id;

    -- Check-in yapıldığında oda dolu, rezervasyon tamamlandı
    IF TG_OP = 'INSERT' THEN
        UPDATE Oda SET durum = 'dolu' WHERE id = v_oda_id;
        UPDATE Rezervasyon SET durum = 'tamamlandi' WHERE id = NEW.rezervasyon_id;
    
    -- Check-out yapıldığında oda temizlikte
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.gercek_cikis IS NULL AND NEW.gercek_cikis IS NOT NULL THEN
            UPDATE Oda SET durum = 'temizlikte' WHERE id = v_oda_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_oda_durumu_guncelle ON Konaklama;
CREATE TRIGGER trg_oda_durumu_guncelle
AFTER INSERT OR UPDATE ON Konaklama
FOR EACH ROW
EXECUTE FUNCTION fn_oda_durumu_guncelle();


-- 3. TRIGGER: Rezervasyon ve Ödeme İşlemlerini Loglama

CREATE OR REPLACE FUNCTION fn_kayit_islem_log()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO IslemLog (tablo_adi, islem_turu, yeni_veri)
        VALUES (TG_TABLE_NAME, TG_OP, row_to_json(NEW));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO IslemLog (tablo_adi, islem_turu, eski_veri, yeni_veri)
        VALUES (TG_TABLE_NAME, TG_OP, row_to_json(OLD), row_to_json(NEW));
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO IslemLog (tablo_adi, islem_turu, eski_veri)
        VALUES (TG_TABLE_NAME, TG_OP, row_to_json(OLD));
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_log_rezervasyon ON Rezervasyon;
CREATE TRIGGER trg_log_rezervasyon
AFTER INSERT OR UPDATE OR DELETE ON Rezervasyon
FOR EACH ROW
EXECUTE FUNCTION fn_kayit_islem_log();

DROP TRIGGER IF EXISTS trg_log_odeme ON Odeme;
CREATE TRIGGER trg_log_odeme
AFTER INSERT OR UPDATE OR DELETE ON Odeme
FOR EACH ROW
EXECUTE FUNCTION fn_kayit_islem_log();

-- 4. STORED PROCEDURE & VIEW TANIMLARI
CREATE OR REPLACE FUNCTION sp_bos_odalari_getir(p_giris_tarihi DATE, p_cikis_tarihi DATE, p_oda_tipi_id INT DEFAULT NULL)
RETURNS TABLE (oda_id INT, oda_numarasi VARCHAR, kat INT, oda_tipi_adi VARCHAR, temel_fiyat NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT o.id, o.oda_numarasi, o.kat, ot.tip_adi, ot.temel_fiyat
    FROM Oda o
    JOIN OdaTipi ot ON o.oda_tipi_id = ot.id
    WHERE o.durum <> 'bakimda'
      AND (p_oda_tipi_id IS NULL OR o.oda_tipi_id = p_oda_tipi_id)
      AND o.id NOT IN (
          SELECT r.oda_id FROM Rezervasyon r
          WHERE r.durum <> 'iptal' AND (p_giris_tarihi < r.cikis_tarihi AND p_cikis_tarihi > r.giris_tarihi)
      );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_konaklama_tutari_hesapla(
    p_konaklama_id INT,
    OUT p_oda_tutari NUMERIC,
    OUT p_hizmet_tutari NUMERIC,
    OUT p_genel_toplam NUMERIC
) AS $$
DECLARE
    v_oda_id INT; v_giris DATE; v_cikis DATE; v_gunluk DATE; v_gecelik NUMERIC; v_oda_tipi_id INT; v_temel_fiyat NUMERIC;
BEGIN
    SELECT r.oda_id, r.giris_tarihi, r.cikis_tarihi, o.oda_tipi_id, ot.temel_fiyat
    INTO v_oda_id, v_giris, v_cikis, v_oda_tipi_id, v_temel_fiyat
    FROM Konaklama k
    JOIN Rezervasyon r ON k.rezervasyon_id = r.id
    JOIN Oda o ON r.oda_id = o.id
    JOIN OdaTipi ot ON o.oda_tipi_id = ot.id
    WHERE k.id = p_konaklama_id;

    p_oda_tutari := 0;
    v_gunluk := v_giris;
    WHILE v_gunluk < v_cikis LOOP
        SELECT COALESCE(
            (SELECT gecelik_fiyat FROM SezonFiyati WHERE oda_tipi_id = v_oda_tipi_id AND v_gunluk BETWEEN baslangic_tarihi AND bitis_tarihi LIMIT 1),
            v_temel_fiyat
        ) INTO v_gecelik;
        p_oda_tutari := p_oda_tutari + v_gecelik;
        v_gunluk := v_gunluk + INTERVAL '1 day';
    END LOOP;

    SELECT COALESCE(SUM(kh.adet * eh.birim_fiyat), 0) INTO p_hizmet_tutari
    FROM KonaklamaHizmeti kh
    JOIN EkHizmet eh ON kh.ek_hizmet_id = eh.id
    WHERE kh.konaklama_id = p_konaklama_id;

    p_genel_toplam := p_oda_tutari + p_hizmet_tutari;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sp_aylik_rapor(p_yil INT, p_ay INT)
RETURNS TABLE (toplam_rezervasyon BIGINT, toplam_gelir NUMERIC, doluluk_orani NUMERIC) AS $$
DECLARE
    v_toplam_oda INT; v_ay_gun_sayisi INT; v_dolu_oda_gece BIGINT; v_ay_baslangic DATE; v_ay_bitis DATE;
BEGIN
    v_ay_baslangic := MAKE_DATE(p_yil, p_ay, 1);
    v_ay_bitis := (v_ay_baslangic + INTERVAL '1 month' - INTERVAL '1 day')::DATE;
    v_ay_gun_sayisi := EXTRACT(DAY FROM v_ay_bitis);

    SELECT COUNT(*) INTO v_toplam_oda FROM Oda WHERE durum <> 'bakimda';
    SELECT COUNT(*) INTO toplam_rezervasyon FROM Rezervasyon WHERE durum IN ('onayli', 'tamamlandi') AND (giris_tarihi <= v_ay_bitis AND cikis_tarihi >= v_ay_baslangic);
    SELECT COALESCE(SUM(tutar), 0) INTO toplam_gelir FROM Odeme WHERE EXTRACT(YEAR FROM tarih) = p_yil AND EXTRACT(MONTH FROM tarih) = p_ay;
    SELECT COALESCE(SUM(LEAST(cikis_tarihi, v_ay_bitis + 1) - GREATEST(giris_tarihi, v_ay_baslangic)), 0) INTO v_dolu_oda_gece FROM Rezervasyon WHERE durum IN ('onayli', 'tamamlandi') AND (giris_tarihi <= v_ay_bitis AND cikis_tarihi >= v_ay_baslangic);

    IF (v_toplam_oda * v_ay_gun_sayisi) > 0 THEN
        doluluk_orani := ROUND((v_dolu_oda_gece::NUMERIC / (v_toplam_oda * v_ay_gun_sayisi)::NUMERIC) * 100, 2);
    ELSE
        doluluk_orani := 0.00;
    END IF;
    RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW vw_bugun_giris_cikis AS
SELECT 
    r.id AS rezervasyon_id,
    m.ad || ' ' || m.soyad AS misafir_adi,
    m.telefon,
    o.oda_numarasi,
    r.giris_tarihi,
    r.cikis_tarihi,
    CASE 
        WHEN r.giris_tarihi = CURRENT_DATE THEN 'Giriş (Check-in)'
        WHEN r.cikis_tarihi = CURRENT_DATE THEN 'Çıkış (Check-out)'
        ELSE 'Konaklıyor'
    END AS hareket_turu,
    r.durum
FROM Rezervasyon r
JOIN Misafir m ON r.misafir_id = m.id
JOIN Oda o ON r.oda_id = o.id
WHERE r.giris_tarihi = CURRENT_DATE OR r.cikis_tarihi = CURRENT_DATE;

CREATE OR REPLACE VIEW vw_oda_temizlik_durumu AS
SELECT 
    o.id AS oda_id,
    o.oda_numarasi,
    o.kat,
    ot.tip_adi,
    o.durum AS guncel_durum,
    MAX(t.tarih) AS son_temizlik_tarihi,
    p.ad || ' ' || p.soyad AS son_temizleyen_personel
FROM Oda o
JOIN OdaTipi ot ON o.oda_tipi_id = ot.id
LEFT JOIN TemizlikKaydi t ON o.id = t.oda_id AND t.durum = 'tamamlandi'
LEFT JOIN Personel p ON t.personel_id = p.id
GROUP BY o.id, o.oda_numarasi, o.kat, ot.tip_adi, o.durum, p.ad, p.soyad;

-- 5. İNDEKSLER
CREATE INDEX IF NOT EXISTS idx_rezervasyon_tarihler ON Rezervasyon (giris_tarihi, cikis_tarihi);
CREATE INDEX IF NOT EXISTS idx_rezervasyon_oda ON Rezervasyon (oda_id);
CREATE INDEX IF NOT EXISTS idx_oda_numarasi ON Oda (oda_numarasi);
CREATE INDEX IF NOT EXISTS idx_misafir_eposta ON Misafir (eposta);

-- 6. ÖRNEK TEST VERİLERİ (20 Oda, 50 Misafir, 200 Rezervasyon)
INSERT INTO OdaTipi (tip_adi, kapasite, aciklama, temel_fiyat) VALUES
('Standart', 2, 'Çift kişilik standart oda', 1500.00),
('Deluxe', 3, 'Geniş balkonlu ve deniz manzaralı deluxe oda', 2500.00),
('Suit', 4, 'Oturma odalı lüks suit', 4500.00)
ON CONFLICT (tip_adi) DO NOTHING;

INSERT INTO SezonFiyati (oda_tipi_id, baslangic_tarihi, bitis_tarihi, gecelik_fiyat) VALUES
(1, '2026-06-01', '2026-08-31', 2200.00),
(2, '2026-06-01', '2026-08-31', 3500.00),
(3, '2026-06-01', '2026-08-31', 6000.00);

INSERT INTO Oda (oda_numarasi, kat, oda_tipi_id, durum)
SELECT 
    (kat * 100 + oda_sira)::VARCHAR, kat,
    CASE WHEN oda_sira <= 2 THEN 1 WHEN oda_sira <= 4 THEN 2 ELSE 3 END, 'bos'
FROM generate_series(1, 4) AS kat
CROSS JOIN generate_series(1, 5) AS oda_sira
ON CONFLICT (oda_numarasi) DO NOTHING;

INSERT INTO Personel (ad, soyad, gorev, ise_giris_tarihi) VALUES
('Ahmet', 'Yılmaz', 'resepsiyon', '2024-01-15'),
('Ayşe', 'Demir', 'resepsiyon', '2024-03-01'),
('Mehmet', 'Kaya', 'kat_gorevlisi', '2024-02-10'),
('Fatma', 'Çelik', 'kat_gorevlisi', '2024-02-15'),
('Zeynep', 'Öztürk', 'yonetici', '2023-11-01');

INSERT INTO EkHizmet (hizmet_adi, birim_fiyat) VALUES
('Açık Büfe Kahvaltı', 250.00),
('Spa & Masaj', 800.00),
('Havalimanı Transferi', 600.00),
('Oda Servisi', 150.00),
('Kapalı Otopark', 100.00);

INSERT INTO Misafir (ad, soyad, kimlik_no, telefon, eposta, dogum_tarihi)
SELECT 
    'Misafir_' || i, 'Soyad_' || i,
    LPAD((10000000000 + i)::TEXT, 11, '0'),
    '0555' || LPAD(i::TEXT, 7, '0'),
    'misafir' || i || '@hoteltest.com',
    DATE '1980-01-01' + (i * 150 * INTERVAL '1 day')::INTERVAL
FROM generate_series(1, 50) AS i
ON CONFLICT (kimlik_no) DO NOTHING;

INSERT INTO Rezervasyon (misafir_id, oda_id, giris_tarihi, cikis_tarihi, kisi_sayisi, durum)
SELECT 
    ((b * 20 + o - 1) % 50) + 1, o,
    DATE '2026-01-01' + ((b * 15) * INTERVAL '1 day')::INTERVAL,
    DATE '2026-01-01' + ((b * 15 + 4) * INTERVAL '1 day')::INTERVAL,
    ((o % 3) + 1),
    CASE WHEN b < 7 THEN 'tamamlandi' WHEN b = 7 THEN 'onayli' ELSE 'beklemede' END
FROM generate_series(0, 9) AS b
CROSS JOIN generate_series(1, 20) AS o
ORDER BY b, o;


