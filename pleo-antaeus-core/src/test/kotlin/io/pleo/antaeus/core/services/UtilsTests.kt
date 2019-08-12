package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test

class UtilsTest {
    @Test
    fun `InvoiceChargeLimiter with invoices below limit, should return unpaid invoice`() {
        val invoices = listOf(
                Invoice(1, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING),
                Invoice(2, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING),
                Invoice(3, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING)
        )

        val invoiceChargeStatuses = listOf(
                InvoiceChargeStatus(1, 1, 1, TransactionStatus.Failed, ""),
                InvoiceChargeStatus(2, 1, 2, TransactionStatus.Failed, ""),
                InvoiceChargeStatus(3, 1, 3, TransactionStatus.Failed, "")
        )

        val actual = InvoiceChargeLimiter(invoices, invoiceChargeStatuses, 2).count()
        assert(actual == 3)
    }

    @Test
    fun `InvoiceChargeLimiter with invoices above limit, should not return unpaid invoices`() {
        val invoices = listOf(
                Invoice(1, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING),
                Invoice(2, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING),
                Invoice(3, 1, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING)
        )

        val invoiceChargeStatuses = listOf(
                InvoiceChargeStatus(1, 1, 1, TransactionStatus.Failed, ""),
                InvoiceChargeStatus(2, 1, 1, TransactionStatus.Failed, ""),
                InvoiceChargeStatus(3, 1, 1, TransactionStatus.Failed, "")
        )

        val actual = InvoiceChargeLimiter(invoices, invoiceChargeStatuses, 3)
        assert(actual.isEmpty())
    }
}