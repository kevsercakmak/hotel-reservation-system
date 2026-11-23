package com.hotel.reservation.repository;

import com.hotel.reservation.entity.KonaklamaHizmeti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KonaklamaHizmetiRepository extends JpaRepository<KonaklamaHizmeti, Long> {
    List<KonaklamaHizmeti> findByKonaklamaId(Long konaklamaId);
}