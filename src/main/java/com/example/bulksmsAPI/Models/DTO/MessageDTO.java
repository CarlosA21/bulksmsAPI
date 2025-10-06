package com.example.bulksmsAPI.Models.DTO;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MessageDTO {
    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("phoneNumbers")
    private List<String> phoneNumbers; // Para múltiples números

    private List<String> phone_number_list; // Campo interno para manejar phone_number como array

    @JsonProperty("date")
    private Date date;

    @JsonProperty("user")
    private String user; // now holds the email

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("creditvalue")
    private long creditvalue;

    @JsonProperty("status")
    private String status;

    @JsonProperty("denialReason")
    private String denialReason;

    // Constructor específico para debugging
    public MessageDTO(String message, String phone_number) {
        this.message = message;
        if (phone_number != null) {
            this.phone_number_list = List.of(phone_number);
        }
    }

    // Setter personalizado para phone_number que maneja tanto String como Array
    @JsonSetter("phone_number")
    public void setPhone_number(Object phone_number) {
        if (phone_number instanceof String) {
            // Si es un string, convertir a lista
            this.phone_number_list = List.of((String) phone_number);
        } else if (phone_number instanceof List) {
            // Si es una lista, usar directamente
            this.phone_number_list = (List<String>) phone_number;
        } else if (phone_number instanceof JsonNode) {
            // Manejar JsonNode (en caso de deserialización compleja)
            JsonNode node = (JsonNode) phone_number;
            this.phone_number_list = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    this.phone_number_list.add(item.asText());
                }
            } else {
                this.phone_number_list.add(node.asText());
            }
        }
    }

    // Getter para phone_number (retorna el primer número)
    public String getPhone_number() {
        if (phone_number_list != null && !phone_number_list.isEmpty()) {
            return phone_number_list.get(0);
        }
        return null;
    }

    // Método para obtener todos los números de teléfono
    public List<String> getPhoneNumbers() {
        // Prioridad: phoneNumbers > phone_number_list
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            return phoneNumbers;
        }
        if (phone_number_list != null && !phone_number_list.isEmpty()) {
            return phone_number_list;
        }
        return new ArrayList<>();
    }
}
