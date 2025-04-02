package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Groups, Long> {
    List<Groups> findByUserId(Long userId);

}
