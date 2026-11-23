package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Odeme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OdemeRepository extends JpaRepository<Odeme, Long> {
    List<Odeme> findByKonaklamaId(Long konaklamaId);
}