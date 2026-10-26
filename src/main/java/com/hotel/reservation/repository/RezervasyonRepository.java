package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Rezervasyon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RezervasyonRepository extends JpaRepository<Rezervasyon, Long> {
    List<Rezervasyon> findByMisafirId(Long misafirId);
    List<Rezervasyon> findByDurum(String durum);

    // Veritabanı seviyesindeki trigger'ın yanı sıra servis katmanında da çakışma kontrolü için
    @Query("SELECT COUNT(r) > 0 FROM Rezervasyon r WHERE r.oda.id = :odaId AND r.durum <> 'iptal' " +
           "AND (:girisTarihi < r.cikisTarihi AND :cikisTarihi > r.girisTarihi)")
    boolean existsCakisma(@Param("odaId") Long odaId, 
                          @Param("girisTarihi") LocalDate girisTarihi, 
                          @Param("cikisTarihi") LocalDate cikisTarihi);
}