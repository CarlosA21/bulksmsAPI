package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.ScheduledMessages;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.ScheduledMessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service

public class ScheduledMesssagesServices {
    @Autowired
    private ScheduledMessagesRepository scheduledMessagesRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    // Crear un nuevo mensaje agendado
    public ScheduledMessages createScheduledMessage(ScheduledMessages scheduledMessage) {
        // Redondea la fecha para poner segundos y milisegundos en cero
        Calendar cal = Calendar.getInstance();
        cal.setTime(scheduledMessage.getScheduledDate());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        scheduledMessage.setScheduledDate(cal.getTime());

        return scheduledMessagesRepository.save(scheduledMessage);
    }
    public List<ScheduledMessages> getScheduledMessagesByUserId(Long userId) {
        return scheduledMessagesRepository.findByUserId(userId);
    }


    // Retrieve a scheduled message by ID
    public ScheduledMessages getScheduledMessageById(Long id) {
        Optional<ScheduledMessages> scheduledMessage = scheduledMessagesRepository.findById(id);
        return scheduledMessage.orElse(null);
    }

    // Retrieve all scheduled messages
    public List<ScheduledMessages> getAllScheduledMessages() {
        return scheduledMessagesRepository.findAll();
    }

    // Update an existing scheduled message
    public ScheduledMessages updateScheduledMessage(Long id, ScheduledMessages updatedMessage) {
        Optional<ScheduledMessages> existingMessage = scheduledMessagesRepository.findById(id);
        if (existingMessage.isPresent()) {
            ScheduledMessages message = existingMessage.get();
            message.setMessage(updatedMessage.getMessage());
            message.setPhoneNumbers(updatedMessage.getPhoneNumbers());
            message.setScheduledDate(updatedMessage.getScheduledDate());
            message.setSent(updatedMessage.isSent());
            message.setStatus(updatedMessage.getStatus());
            message.setUserId(updatedMessage.getUserId());
            return scheduledMessagesRepository.save(message);
        }
        return null;
    }

    // Delete a scheduled message by ID
    public boolean deleteScheduledMessage(Long id) {
        if (scheduledMessagesRepository.existsById(id)) {
            scheduledMessagesRepository.deleteById(id);
            return true;
        }
        return false;
    }
    private final String horisenApiUrl = "http://194.0.137.123:32112/bulk/sendsms";
    private final String username = "15525_BRMRetail";
    private final String password = "HNvdMRqZ";


    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    public void sendScheduledMessages() {
        System.out.println("Ejecutando tarea programada para enviar mensajes...");

        Date now = new Date();
        System.out.println("Hora actual: " + now);

        List<ScheduledMessages> messagesToSend = scheduledMessagesRepository
                .findByScheduledDateBeforeAndIsSentFalse(now);

        for (ScheduledMessages message : messagesToSend) {
            System.out.println("Procesando mensaje ID: " + message.getId());

            if (!message.isSent()) {
                try {
                    String receiver = message.getPhoneNumbers();
                    String text = message.getMessage();
                    Long userId = message.getUserId();
                    Long creditValue = message.getCreditValue();

                    System.out.println("Intentando enviar a: " + receiver);

                    // Create JSON body structure
                    Map<String, Object> body = new HashMap<>();
                    body.put("type", "text");

                    Map<String, String> auth = new HashMap<>();
                    auth.put("username", username);
                    auth.put("password", password);
                    body.put("auth", auth);

                    body.put("sender", "BulkTest");
                    body.put("receiver", receiver);
                    body.put("dcs", "GSM");
                    body.put("text", text);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                    ResponseEntity<String> response = new RestTemplate().postForEntity(
                            horisenApiUrl,
                            request,
                            String.class
                    );

                    System.out.println("Horisen API response: " + response.getBody());

                    if (response.getStatusCode().is2xxSuccessful()) {
                        message.setSent(true);
                        message.setStatus("successful");
                        System.out.println("SMS sent successfully.");

                        // Descontar créditos
                        Optional<CreditAccount> optionalCreditAccount = creditAccountRepository.findByUserId(userId);
                        if (optionalCreditAccount.isPresent()) {
                            CreditAccount creditAccount = optionalCreditAccount.get();
                            long updatedCredits = creditAccount.getBalance() - creditValue;
                            creditAccount.setBalance((int) updatedCredits);
                            creditAccountRepository.save(creditAccount);
                        } else {
                            message.setStatus("credit_not_found");
                        }
                    } else {
                        message.setStatus("api_failed");
                        System.out.println("SMS sending failed.");
                    }
                } catch (Exception e) {
                    message.setStatus("exception_failed");
                    System.out.println("SMS sending failed. Exception: " + e.getMessage());
                    e.printStackTrace();
                }

                scheduledMessagesRepository.save(message);
            }
        }
    }
}
