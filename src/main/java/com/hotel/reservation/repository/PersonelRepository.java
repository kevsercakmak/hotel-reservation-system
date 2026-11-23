package com.hotel.reservation.repository;

import com.hotel.reservation.entity.Personel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonelRepository extends JpaRepository<Personel, Long> {
    List<Personel> findByGorev(String gorev);
}