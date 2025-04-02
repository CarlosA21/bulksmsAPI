package com.example.bulksmsAPI.Controller;

import com.example.bulksmsAPI.Models.DTO.GroupInfoDTO;
import com.example.bulksmsAPI.Models.DTO.GroupsDTO;
import com.example.bulksmsAPI.Models.Groups;
import com.example.bulksmsAPI.Services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {
    @Autowired
    private GroupService groupsService;

    /*
    // Create a new group
    @PostMapping
    public ResponseEntity<Groups> createGroup(@RequestBody GroupsDTO groupDTO) {
        Groups createdGroup = groupsService.saveGroup(groupDTO);
        return ResponseEntity.ok(createdGroup);
    }

    // Get all groups
    @GetMapping
    public ResponseEntity<List<GroupsDTO>> getAllGroups() {
        List<GroupsDTO> groups = groupsService();
        return ResponseEntity.<List<GroupsDTO>>ok(groups);
    }

    // Get group by ID
    @GetMapping("/{id}")
    public ResponseEntity<GroupInfoDTO> getGroupById(@PathVariable Long id) {
        List<GroupInfoDTO> group = groupsService.getGroupsByUserId(id);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update group
    @PutMapping("/{id}")
    public ResponseEntity<GroupsDTO> updateGroup(@PathVariable Long id, @RequestBody GroupsDTO groupDTO) {
        GroupsDTO updatedGroup = groupsService.updateGroup(id, groupDTO);
        return ResponseEntity.ok(updatedGroup);
    }

    // Delete group
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupsService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
    */

}
