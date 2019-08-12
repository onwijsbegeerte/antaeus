package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceChargeStatus
import io.pleo.antaeus.models.TransactionStatus

fun InvoiceChargeLimiter (unpaidInvoices : List<Invoice>, invoiceChargeResults : List<InvoiceChargeStatus>, chargeAttempts : Int) : List<Invoice>{

    var invoiceEligibleForRetry = invoiceChargeResults
            .filter { it.transactionStatus == TransactionStatus.Failed }
            .groupBy { it -> it.invoiceId }
            .filter { it -> it.value.count() <= chargeAttempts - 1}

    return unpaidInvoices.filter { it -> invoiceEligibleForRetry.containsKey(it.id) }
}