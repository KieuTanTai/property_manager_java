package com.example.contract.models.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


import com.example.shared.enums.EInvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "monthly_invoice")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder 
public class MonthlyInvoiceModel {
    @Id 
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    public MonthlyInvoiceModel(UUID invoiceContractId) {
        this.invoiceContractId = invoiceContractId;
    }

    public MonthlyInvoiceModel(UUID invoiceId, UUID invoiceContractId, LocalDateTime invoicePaymentDate,
            LocalDateTime invoiceDueDate, BigDecimal invoiceTotalAmount, EInvoiceStatus invoiceStatus) {
        this.invoiceId = invoiceId;
        this.invoiceContractId = invoiceContractId;
        this.invoicePaymentDate = invoicePaymentDate;
        this.invoiceDueDate = invoiceDueDate;
        this.invoiceTotalAmount = invoiceTotalAmount;
        this.invoiceStatus = invoiceStatus;
    }

    @Column(name = "invoice_contract_id", nullable = false)
    private UUID invoiceContractId;

    @Column(name = "invoice_payment_date", nullable = false)
    private LocalDateTime invoicePaymentDate;

    @Column(name = "invoice_due_date", nullable = false)
    private LocalDateTime invoiceDueDate;

    @Column(name = "invoice_total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal invoiceTotalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false)
    private EInvoiceStatus invoiceStatus;

    @Column(name = "invoice_created_at")
    private LocalDateTime invoiceCreatedAt;

    @Column(name = "invoice_updated_at")
    private LocalDateTime invoiceUpdatedAt;
}
