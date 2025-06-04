package com.example.bulksmsAPI.Models.DTO;



public class PlanDTO {
    private Long id;
    private String name;
    private double price;
    private int credits;

    public PlanDTO() {
    }

    public PlanDTO(Long id, String name, double price, int credits) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.credits = credits;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}
