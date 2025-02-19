package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactsRepository extends JpaRepository<Contacts, Long> {
    List<Contacts> findByUsuarioId(Long usuarioId);
}

