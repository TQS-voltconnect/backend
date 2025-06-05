package pt.ua.tqs.voltconnect.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResult {

    private final boolean success;
    private final String transactionId;
    private final String errorMessage;

    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, null);
    }

    public static PaymentResult failure(String errorMessage) {
        return new PaymentResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }
}
