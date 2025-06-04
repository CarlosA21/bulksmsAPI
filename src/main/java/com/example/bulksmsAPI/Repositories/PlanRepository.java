package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository  extends JpaRepository<Plans, Long> {

}
