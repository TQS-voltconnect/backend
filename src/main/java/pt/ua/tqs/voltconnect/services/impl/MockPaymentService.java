package pt.ua.tqs.voltconnect.services.impl;

import org.springframework.stereotype.Service;
import pt.ua.tqs.voltconnect.models.PaymentResult;
import pt.ua.tqs.voltconnect.services.PaymentService;
import java.util.UUID;

@Service
public class MockPaymentService implements PaymentService {

    @Override
    public PaymentResult processPayment(String paymentMethod, Double amount, String currency) {
        // Simulate payment processing
        try {
            // Simulate some processing time
            Thread.sleep(1000);
            
            // Generate a mock transaction ID
            String transactionId = UUID.randomUUID().toString();
            
            // Simple validation
            if (amount <= 0) {
                return PaymentResult.failure("Invalid amount");
            }
            
            if (!"EUR".equalsIgnoreCase(currency)) {
                return PaymentResult.failure("Only EUR currency is supported");
            }
            
            // Simulate successful payment
            return PaymentResult.success(transactionId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.failure("Payment processing was interrupted");
        }
    }
} 