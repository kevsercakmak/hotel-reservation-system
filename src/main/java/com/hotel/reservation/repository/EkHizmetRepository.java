package com.hotel.reservation.repository;

import com.hotel.reservation.entity.EkHizmet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EkHizmetRepository extends JpaRepository<EkHizmet, Long> {
}