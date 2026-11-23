package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Konaklama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface KonaklamaRepository extends JpaRepository<Konaklama, Long> {
    
    Optional<Konaklama> findByRezervasyonId(Long rezervasyonId);
    
    // Henüz check-out yapmamış (aktif konaklayan) misafirleri getirir
    List<Konaklama> findByGercekCikisIsNull();

    // Veritabanındaki sp_konaklama_tutari_hesapla fonksiyonunu çağırır
    @Query(value = "SELECT * FROM sp_konaklama_tutari_hesapla(:konaklamaId)", nativeQuery = true)
    Map<String, Object> hesaplaKonaklamaTutari(@Param("konaklamaId") Long konaklamaId);
}