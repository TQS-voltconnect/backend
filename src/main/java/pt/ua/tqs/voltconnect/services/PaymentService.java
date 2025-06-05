package pt.ua.tqs.voltconnect.services;

import pt.ua.tqs.voltconnect.models.PaymentResult;

public interface PaymentService {
    PaymentResult processPayment(String paymentMethod, Double amount, String currency);
} 