package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.DTO.PlanDTO;
import com.example.bulksmsAPI.Models.Plans;
import com.example.bulksmsAPI.Repositories.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public PlanDTO createPlan(PlanDTO planDTO) {
        Plans plan = new Plans();
        plan.setName(planDTO.getName());
        plan.setPrice(planDTO.getPrice());
        plan.setCredits(planDTO.getCredits());
        Plans savedPlan = planRepository.save(plan);
        return convertToDTO(savedPlan);
    }
    public List<PlanDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PlanDTO updatePlan(Long id, PlanDTO planDTO) {
        return planRepository.findById(id).map(plan -> {
            plan.setName(planDTO.getName());
            plan.setPrice(planDTO.getPrice());
            plan.setCredits(planDTO.getCredits());
            Plans updatedPlan = planRepository.save(plan);
            return convertToDTO(updatedPlan);
        }).orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    private PlanDTO convertToDTO(Plans plan) {
        PlanDTO planDTO = new PlanDTO();
        planDTO.setId(plan.getId());
        planDTO.setName(plan.getName());
        planDTO.setPrice(plan.getPrice());
        planDTO.setCredits(plan.getCredits());
        return planDTO;
    }
}
