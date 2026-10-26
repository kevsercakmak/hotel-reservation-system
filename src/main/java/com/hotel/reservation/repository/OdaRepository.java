package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Oda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OdaRepository extends JpaRepository<Oda, Long> {
    Optional<Oda> findByOdaNumarasi(String odaNumarasi);
    List<Oda> findByDurum(String durum);

    // Belirli tarih aralığında çakışmayan boş odaları getiren sorgu
    @Query("SELECT o FROM Oda o WHERE o.durum <> 'bakimda' AND o.id NOT IN (" +
           "SELECT r.oda.id FROM Rezervasyon r WHERE r.durum <> 'iptal' AND " +
           "(:girisTarihi < r.cikisTarihi AND :cikisTarihi > r.girisTarihi))")
    List<Oda> findMusaitOdalar(@Param("girisTarihi") LocalDate girisTarihi, 
                               @Param("cikisTarihi") LocalDate cikisTarihi);
}