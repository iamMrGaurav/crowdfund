package com.example.crowdfund.service.notificationKafka;

import com.example.crowdfund.dto.response.PaymentNotificationResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "payment-notification", groupId = "payment-notification")
    public void getNotification(PaymentNotificationResponse paymentNotificationResponse){
        System.out.println("Notification Received");
    }
}
