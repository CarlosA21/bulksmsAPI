package com.example.bulksmsAPI.Models.DTO;

public class GroupInfoDTO {
    private String groupName;
    private String phoneNumbers;

    public GroupInfoDTO(String groupName, String phoneNumbers) {
        this.groupName = groupName;
        this.phoneNumbers = phoneNumbers;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
