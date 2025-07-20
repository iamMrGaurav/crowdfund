package com.example.crowdfund.service.notificationKafka;

import com.example.crowdfund.dto.response.PaymentNotificationResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "payment-notification", groupId = "payment-notification")
    public void getNotification(@Header(KafkaHeaders.RECEIVED_KEY) Long ownerId, PaymentNotificationResponse paymentNotificationResponse){
        System.out.println("Notification Received");
        System.out.println("Donor name" + paymentNotificationResponse.getDonorName());
        System.out.println("Message" + paymentNotificationResponse.getMessage());
        System.out.println("Amount" + paymentNotificationResponse.getAmount());
        System.out.println("owner id" + ownerId);

    }
}
