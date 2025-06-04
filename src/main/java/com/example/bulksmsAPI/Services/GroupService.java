package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.DTO.GroupsDTO;
import com.example.bulksmsAPI.Models.Groups;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.GroupRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    public GroupService(GroupRepository groupRepository) {

        this.groupRepository = groupRepository;
    }
    public Groups saveGroup(GroupsDTO groupDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new SecurityException("Invalid authentication principal");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Groups group = new Groups();
        group.setGroup_name(groupDTO.getGroup_name());
        group.setUser(user);

        return groupRepository.save(group);
    }

    public Groups findById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        groupRepository.deleteById(id);
    }

    public Groups update(Groups group) {
        return groupRepository.save(group);
    }

   public List<Groups> getTagsByUserId (Long userId) {
        return groupRepository.findByUserId(userId);
    }

    public List<Groups> getAllGroups() {
        return groupRepository.findAll();
    }


}
