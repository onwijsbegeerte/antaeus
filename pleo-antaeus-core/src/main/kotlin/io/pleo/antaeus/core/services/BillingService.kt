package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.TransactionStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {

    fun chargeAllInvoices(): List<Invoice> {
        var unpaidInvoices = invoiceService.fetchAll().filter { it -> it.status == InvoiceStatus.PENDING }

        while (unpaidInvoices.any()){
            unpaidInvoices.forEach { chargeInvoice(it, paymentProvider::charge, invoiceService::payInvoice) }
            unpaidInvoices = invoiceService.fetchAll().filter { it -> it.status == InvoiceStatus.PENDING }

            unpaidInvoices = InvoiceChargeLimiter(unpaidInvoices, invoiceService.fetchAllInvoiceStatuses(), 4)
        }

        return invoiceService.fetchAll().filter { it -> it.status == InvoiceStatus.PENDING }
    }

    private fun chargeInvoice(invoice: Invoice, tryPayInvoice: (Invoice) -> Boolean, updateInvoice: (Invoice) -> Invoice) {

        invoiceService.createInvoiceChargeStatus(invoice, TransactionStatus.Attempted)

        if(tryPayInvoice(invoice)){

            invoiceService.createInvoiceChargeStatus(invoice, TransactionStatus.Success)
            updateInvoice(invoice)
        }
        else{

            invoiceService.createInvoiceChargeStatus(invoice, TransactionStatus.Failed)
        }
    }

}

