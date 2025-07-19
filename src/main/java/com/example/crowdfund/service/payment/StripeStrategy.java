package com.example.crowdfund.service.payment;

import com.example.crowdfund.entity.Contribution;
import com.example.crowdfund.entity.Payment;
import com.example.crowdfund.enums.Currency;
import com.example.crowdfund.enums.PaymentStatus;
import com.example.crowdfund.enums.PaymentProvider;
import com.example.crowdfund.repository.ContributionRepository;
import com.example.crowdfund.repository.PaymentRepository;
import com.example.crowdfund.repository.UserRepository;
import com.example.crowdfund.repository.CampaignRepository;
import com.stripe.exception.StripeException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Slf4j
@Service
@RequiredArgsConstructor
public class StripeStrategy implements PaymentStrategy {

    private final PaymentRepository paymentRepository;
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

    @Transactional
    public Session createCheckoutSession(Contribution contribution, String successUrl, String cancelUrl) throws StripeException {
        log.info("Creating Stripe Checkout session for contribution: {}", contribution.getId());

        long amountInCents = contribution.getAmount().multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams paymentIntentParams = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(contribution.getCurrency().toLowerCase())
                .putMetadata("contribution_id", contribution.getId().toString())
                .putMetadata("campaign_id", contribution.getCampaignId().toString())
                .build();
        
        PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);
        log.info("Created PaymentIntent: {} for contribution: {}", paymentIntent.getId(), contribution.getId());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("contribution_id", contribution.getId().toString())
                        .putMetadata("campaign_id", contribution.getCampaignId().toString())
                        .build()
                )
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
                .build();
        
        paymentRepository.save(payment);
        
        log.info("Created Stripe Checkout session: {} for contribution: {}", session.getId(), contribution.getId());
        return session;
    }

    @Transactional
    @Override
    public Session createStripeCheckoutSessionDestinationCharges(Contribution contribution, String successUrl, String cancelUrl) throws StripeException {
        log.info("Creating destination charge session for contribution: {}", contribution.getId());

        Long campaignOwnerId = campaignRepository.findCreatorIdByCampaignId(contribution.getCampaignId());
        if (campaignOwnerId == null) {
            throw new InvalidRequestException("Campaign not found or has no owner", null, null, "400", null, null);
        }

        String campaignOwnerAccountId = userRepository.findStripeAccountIdByUserId(campaignOwnerId);
        if (campaignOwnerAccountId == null) {
            throw new InvalidRequestException("Campaign owner doesn't have a Stripe account", null, null, "400", null, null);
        }
        
        long amountInCents = contribution.getAmount().multiply(new BigDecimal(100)).longValue();
        long applicationFeeInCents = calculateApplicationFee(amountInCents);

        SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(contribution.getCurrency().toLowerCase())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Campaign Contribution")
                                                                .setDescription("Contribution to campaign ID: " + contribution.getCampaignId())
                                                                .build()
                                                )
                                                .setUnitAmount(amountInCents)
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setApplicationFeeAmount(applicationFeeInCents)
                                .setTransferData(
                                        SessionCreateParams.PaymentIntentData.TransferData.builder()
                                                .setDestination(campaignOwnerAccountId)
                                                .build()
                                )
                                .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("contribution_id", contribution.getId().toString())
                .putMetadata("campaign_id", contribution.getCampaignId().toString())
                .putMetadata("campaign_owner_account", campaignOwnerAccountId)
                .putMetadata("platform_fee", String.valueOf(applicationFeeInCents))
                .build();

        Session session = Session.create(sessionCreateParams);
        contribution.setPaymentStatus(PaymentStatus.PENDING);
        contribution.setPaymentProvider(PaymentProvider.STRIPE);


        Payment payment = Payment.builder()
                .contributionId(contribution.getId())
                .amount(contribution.getAmount())
                .currency(Currency.valueOf(contribution.getCurrency()))
                .paymentProvider(PaymentProvider.STRIPE)
                .paymentStatus(PaymentStatus.PENDING)
                .externalPaymentId(session.getId())
                .build();

        if(contribution.getIsAnonymous()){
            contribution.setUserId(null);
        }

        contributionRepository.save(contribution);
        paymentRepository.save(payment);
        
        log.info("Created destination charge session: {} for contribution: {}, platform fee: {}", 
                session.getId(), contribution.getId(), applicationFeeInCents);

        return session;
    }

    private long calculateApplicationFee(long amountInCents) {
        return Math.round(amountInCents * 0.03);
    }

    public String createStripeAccount(String email) throws StripeException {
        log.info("Creating Stripe Connect account for user: {}", email);
        
        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry("US") // TODO: MAKE THIS DYNAMIC
                .setEmail(email)
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(
                                        AccountCreateParams.Capabilities.CardPayments.builder()
                                                .setRequested(true)
                                                .build()
                                )
                                .setTransfers(
                                        AccountCreateParams.Capabilities.Transfers.builder()
                                                .setRequested(true)
                                                .build()
                                )
                                .build()
                )
                .build();

        Account account = Account.create(params);
        
        log.info("Created Stripe Connect account: {} for user email: {}", account.getId(), email);
        return account.getId();
    }

    public String createOnboardingLink(String stripeAccountId, String refreshUrl, String returnUrl) throws StripeException {
        log.info("Creating onboarding link for account: {}", stripeAccountId);
        
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(stripeAccountId)
                .setRefreshUrl(refreshUrl)
                .setReturnUrl(returnUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        
        log.info("Created onboarding link for account: {}", stripeAccountId);
        return accountLink.getUrl();
    }

    public boolean isAccountOnboarded(String stripeAccountId) throws StripeException {
        Account account = Account.retrieve(stripeAccountId);
        return account.getDetailsSubmitted() && account.getChargesEnabled();
    }
}
