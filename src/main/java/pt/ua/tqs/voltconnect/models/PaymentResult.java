package pt.ua.tqs.voltconnect.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String message;
    private String transactionId;

    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, "Payment processed successfully", transactionId);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message, null);
    }
} 