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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service

public class ScheduledMesssagesServices {
    @Autowired
    private ScheduledMessagesRepository scheduledMessagesRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    // Create a new scheduled message
    public ScheduledMessages createScheduledMessage(ScheduledMessages scheduledMessage) {
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
            message.setScheduledTime(updatedMessage.getScheduledTime());
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
    private final String horisenApiUrl = "https://api.horisen.com/sendSms";
    private final String username = "yourUsername";
    private final String password = "yourPassword";

    @Scheduled(fixedRate = 60000) // Runs every minute
    public void sendScheduledMessages() {
        // Get the current date and time
        Date currentDate = new Date();
        String currentTime = new java.text.SimpleDateFormat("HH:mm").format(currentDate);

        // Fetch messages matching the current date and time
        List<ScheduledMessages> messagesToSend = scheduledMessagesRepository.findByScheduledDateAndScheduledTime(currentDate, currentTime);
        RestTemplate restTemplate = new RestTemplate();

        for (ScheduledMessages message : messagesToSend) {
            if (!message.isSent()) {
                try {
                    String receiver = message.getPhoneNumbers();
                    String text = message.getMessage();
                    Long userId = message.getUserId();
                    Long creditValue = message.getCreditValue();

                    ResponseEntity<String> response = restTemplate.getForEntity(
                            horisenApiUrl + "?type=text&username=" + username + "&password=" + password +
                                    "&sender=BulkTest&receiver=" + receiver + "&dcs=GSM&text=" + text +
                                    "&dlrMask=19&dlrUrl=https://my-server.com/dlrjson.php",
                            String.class
                    );

                    if (response.getStatusCode() == HttpStatus.OK) {
                        message.setSent(true);
                        message.setStatus("successful");

                        // Deduct the credit value from the user's CreditAccount
                        Optional<CreditAccount> optionalCreditAccount = creditAccountRepository.findByUserId(userId);
                        if (optionalCreditAccount.isPresent()) {
                            CreditAccount creditAccount = optionalCreditAccount.get();
                            Long updatedCredits = creditAccount.getBalance() - creditValue;
                            creditAccount.setBalance(Math.toIntExact(updatedCredits));
                            creditAccountRepository.save(creditAccount);
                        } else {
                            message.setStatus("failed");
                        }
                    } else {
                        message.setStatus("failed");
                    }
                } catch (Exception e) {
                    message.setStatus("failed");
                }
                scheduledMessagesRepository.save(message);
            }
        }
    }
}
