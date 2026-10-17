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
    
    -- Check-out yapıldığında (gercek_cikis dolduğunda) oda temizlikte
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



