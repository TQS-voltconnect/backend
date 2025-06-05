package pt.ua.tqs.voltconnect.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentMethod;  // "CARD" ou "MBWAY"
} 