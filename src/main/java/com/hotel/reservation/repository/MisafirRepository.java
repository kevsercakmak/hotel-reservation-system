package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Misafir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MisafirRepository extends JpaRepository<Misafir, Long> {
    Optional<Misafir> findByKimlikNo(String kimlikNo);
    Optional<Misafir> findByEposta(String eposta);
    boolean existsByKimlikNo(String kimlikNo);
    boolean existsByEposta(String eposta);
}