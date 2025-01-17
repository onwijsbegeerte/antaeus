/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)
    }

    fun payInvoice(invoice: Invoice): Invoice? {
        transaction(db) {
            InvoiceTable.update({ InvoiceTable.id eq invoice.id }) {
                it[this.status] = InvoiceStatus.PAID.toString()
            }
        }

        return fetchInvoice(invoice.id)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)
    }

    fun fetchInvoiceChargeStatuses(id: Int): List<InvoiceChargeStatus> {
        return transaction(db) {
            // Returns the first InvoiceChargeStatus with matching id.
            InvoiceChargeStatusTable
                    .select { InvoiceChargeStatusTable.invoiceId.eq(id) }
                    .map {it.toInvoiceChargeStatus()}
        }
    }

    fun fetchChargeStatus(id: Int): InvoiceChargeStatus? {
        return transaction(db) {
            // Returns the first InvoiceChargeStatus with matching id.
            InvoiceChargeStatusTable
                    .select { InvoiceChargeStatusTable.id.eq(id) }
                    .firstOrNull()
                    ?.toInvoiceChargeStatus()
        }
    }

    fun createInvoiceChargeStatus(customerId : Int, event : TransactionStatus, invoiceId : Int) : InvoiceChargeStatus? {
        val id = transaction(db) {
            InvoiceChargeStatusTable
                    .insert {
                        it[this.created] = DateTime.now()
                        it[this.customerId] = customerId
                        it[this.transactionStatus] = event.toString()
                        it[this.invoiceId] = invoiceId
                    } get InvoiceChargeStatusTable.id
        }
        return fetchChargeStatus(id!!)
    }

    fun fetchAllInvoiceChargeStatuses(): List<InvoiceChargeStatus> {
        return transaction(db) {
            InvoiceChargeStatusTable
                    .selectAll()
                    .map { it.toInvoiceChargeStatus() }
        }
    }
}
