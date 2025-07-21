package com.example.crowdfund.service.notificationKafka;

import com.example.crowdfund.dto.response.PaymentNotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "payment-notification", groupId = "payment-notification")
    public void getPaymentNotification(@Header(KafkaHeaders.RECEIVED_KEY) Long ownerId, PaymentNotificationResponse paymentNotificationResponse) throws Exception {
        notifyAboutDonation(paymentNotificationResponse);
    }

    void notifyAboutDonation(PaymentNotificationResponse paymentNotificationResponse){
        try {
            messagingTemplate.convertAndSend("/topic/campaign/"+ paymentNotificationResponse.getCampaignId() +"/contributions/", paymentNotificationResponse);
            messagingTemplate.convertAndSendToUser(paymentNotificationResponse.getCampaignOwnerId().toString(),"/topic/campaign/contributions/user/" +
                    paymentNotificationResponse.getCampaignOwnerId(), paymentNotificationResponse );

            System.out.println("WebSocket notification sent for campaign: " + paymentNotificationResponse.getCampaignId());
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }
}
