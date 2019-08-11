package io.pleo.antaeus.models

data class InvoiceChargeStatus(
        val id: Int,
        val customerId: Int,
        val invoiceId: Int,
        val transactionStatus: TransactionStatus,
        val created: String
)