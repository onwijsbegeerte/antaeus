/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceChargeStatus
import io.pleo.antaeus.models.TransactionStatus

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun payInvoice(invoice: Invoice): Invoice {
        return dal.payInvoice(invoice) ?: throw InvoiceNotFoundException(invoice.id)
    }

    fun fetchAllInvoiceStatuses(): List<InvoiceChargeStatus> {
        return dal.fetchInvoiceChargeStatuses()
    }

    fun fetchInvoiceChargeStatus(id: Int) : InvoiceChargeStatus? {
        return dal.fetchInvoiceChargeStatus(id)
    }

    fun createInvoiceChargeStatus(invoice: Invoice, transactionStatus : TransactionStatus) : InvoiceChargeStatus? {
        return dal.createInvoiceChargeStatus(invoice.customerId, transactionStatus, invoice.id)
    }
}
