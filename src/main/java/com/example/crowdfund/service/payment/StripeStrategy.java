package com.example.crowdfund.service.payment;


import com.example.crowdfund.entity.Contribution;
import com.example.crowdfund.entity.Payment;
import com.example.crowdfund.enums.Currency;
import com.example.crowdfund.enums.PaymentStatus;
import com.example.crowdfund.enums.PaymentProvider;
import com.example.crowdfund.repository.ContributionRepository;
import com.example.crowdfund.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class StripeStrategy implements PaymentStrategy {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;
    private final ContributionRepository contributionRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    @Override
    public Session createCheckoutSession(Contribution contribution, String successUrl, String cancelUrl) throws StripeException {
        log.info("Creating Stripe Checkout session for contribution: {}", contribution.getId());

        long amountInCents = contribution.getAmount().multiply(new BigDecimal(100)).longValue();
        
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(contribution.getCurrency().toLowerCase())
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Campaign Contribution")
                                        .setDescription("Contribution for campaign ID: " + contribution.getCampaignId())
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("contribution_id", contribution.getId().toString())
                .putMetadata("campaign_id", contribution.getCampaignId().toString())
                .build();

        Session session = Session.create(params);

        contribution.setPaymentIntentId(session.getId());
        contribution.setPaymentStatus(PaymentStatus.PENDING);
        contribution.setPaymentProvider(PaymentProvider.STRIPE);
        contributionRepository.save(contribution);

        Payment payment = Payment.builder()
                .contributionId(contribution.getId())
                .amount(contribution.getAmount())
                .currency(Currency.valueOf(contribution.getCurrency()))
                .paymentProvider(PaymentProvider.STRIPE)
                .paymentStatus(PaymentStatus.PENDING)
                .externalPaymentId(session.getId())
                .paymentIntentId(session.getId())
                .build();
        
        paymentRepository.save(payment);
        
        log.info("Created Stripe Checkout session: {} for contribution: {}", session.getId(), contribution.getId());
        return session;
    }

}
