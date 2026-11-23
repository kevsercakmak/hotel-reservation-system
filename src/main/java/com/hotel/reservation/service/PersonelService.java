package com.hotel.reservation.service;

import com.hotel.reservation.entity.Personel;
import com.hotel.reservation.repository.PersonelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonelService {

    private final PersonelRepository personelRepository;

    public List<Personel> tumPersonelleriGetir() {
        return personelRepository.findAll();
    }

    public List<Personel> goreveGoreGetir(String gorev) {
        return personelRepository.findByGorev(gorev);
    }
}