package pt.ua.tqs.voltconnect.services;

import org.junit.jupiter.api.Test;
import pt.ua.tqs.voltconnect.models.PaymentResult;
import pt.ua.tqs.voltconnect.services.impl.MockPaymentService;

import static org.junit.jupiter.api.Assertions.*;

class MockPaymentServiceTest {

    private final PaymentService paymentService = new MockPaymentService();

    @Test
    void testSuccessfulPayment() {
        PaymentResult result = paymentService.processPayment("CREDIT_CARD", 20.0, "EUR");

        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertNull(result.getErrorMessage());
    }

    @Test
    void testPaymentWithInvalidAmount() {
        PaymentResult result = paymentService.processPayment("CREDIT_CARD", -10.0, "EUR");

        assertFalse(result.isSuccess());
        assertNull(result.getTransactionId());
        assertEquals("Invalid amount", result.getErrorMessage());
    }

    @Test
    void testUnsupportedCurrency() {
        PaymentResult result = paymentService.processPayment("CREDIT_CARD", 20.0, "USD");

        assertFalse(result.isSuccess());
        assertNull(result.getTransactionId());
        assertEquals("Only EUR currency is supported", result.getErrorMessage());
    }

    @Test
    void testInterruptedPayment() {
        Thread current = Thread.currentThread();
        current.interrupt(); // Simula interrupção

        PaymentResult result = paymentService.processPayment("CREDIT_CARD", 20.0, "EUR");

        assertFalse(result.isSuccess());
        assertEquals("Payment processing was interrupted", result.getErrorMessage());

        // Clear interrupted status for future tests
        assertTrue(Thread.interrupted());
    }
}
