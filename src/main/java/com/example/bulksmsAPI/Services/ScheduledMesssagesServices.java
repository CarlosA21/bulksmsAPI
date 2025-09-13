package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.ScheduledMessages;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.ScheduledMessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startTime = cal.getTime();

        cal.add(Calendar.MINUTE, 1);
        Date endTime = cal.getTime();

        Date now = new Date();
        System.out.println("Hora actual: " + now);


        List<ScheduledMessages> messagesToSend = scheduledMessagesRepository
                .findByScheduledDateBeforeAndIsSentFalse(now);

        RestTemplate restTemplate = new RestTemplate();

        for (ScheduledMessages message : messagesToSend) {
            System.out.println("Procesando mensaje ID: " + message.getId());

            if (!message.isSent()) {
                try {
                    String receiver = message.getPhoneNumbers();
                    String text = message.getMessage();
                    Long userId = message.getUserId();
                    Long creditValue = message.getCreditValue();

                    System.out.println("Intentando enviar a: " + receiver);

                    ResponseEntity<String> response = restTemplate.getForEntity(
                            horisenApiUrl + "?type=text&username=" + username + "&password=" + password +
                                    "&sender=BulkTest&receiver=" + receiver + "&dcs=GSM&text=" + text +
                                    "&dlrMask=19&dlrUrl=https://my-server.com/dlrjson.php",
                            String.class
                    );

                    if (response.getStatusCode() == HttpStatus.OK) {
                        message.setSent(true);
                        message.setStatus("successful");

                        // Descontar créditos
                        Optional<CreditAccount> optionalCreditAccount = creditAccountRepository.findByUserId(userId);
                        if (optionalCreditAccount.isPresent()) {
                            CreditAccount creditAccount = optionalCreditAccount.get();
                            Long updatedCredits = creditAccount.getBalance() - creditValue;
                            creditAccount.setBalance(Math.toIntExact(updatedCredits));
                            creditAccountRepository.save(creditAccount);
                        } else {
                            message.setStatus("credit_not_found");
                        }
                    } else {
                        message.setStatus("api_failed");
                    }
                } catch (Exception e) {
                    message.setStatus("exception_failed");
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }

                scheduledMessagesRepository.save(message);
            }
        }
    }
}
