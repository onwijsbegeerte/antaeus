package io.pleo.antaeus.core.services

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import io.pleo.antaeus.models.Currency
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.random.Random


class BillingServiceTest {
    @Test
    fun `Attempting to charging customers, with no unpaid invoices, should not charge customers`() {

        val dal = mockk<AntaeusDal> {
            every { fetchInvoices() } returns listOf(Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID ))
            every { createInvoiceChargeStatus(any<Int>(), any(), any<Int>() )} returns InvoiceChargeStatus(Random.nextInt(), Random.nextInt(), Random.nextInt(), TransactionStatus.Attempted, LocalDate.now().toString())
            every { payInvoice(any())} returns Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID )
        }

        val mockPaymentProvider = mockk<PaymentProvider>{
            every { charge(any()) } returns true
        }

        val billingService = BillingService(mockPaymentProvider, InvoiceService(dal))

        billingService.chargeAllInvoices()

        verify(exactly = 0) { mockPaymentProvider.charge(any()) }
        confirmVerified(mockPaymentProvider)
    }

    @Test
    fun `Attempting to charging customers, some unpaid invoices, should attempt to charge customer`() {

        val dal = mockk<AntaeusDal> {
            every { fetchInvoices() } returns listOf(Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING )) andThen listOf()
            every { createInvoiceChargeStatus(any<Int>(), any(), any<Int>() )} returns InvoiceChargeStatus(Random.nextInt(), Random.nextInt(), Random.nextInt(), TransactionStatus.Attempted, LocalDate.now().toString())
            every { payInvoice(any())} returns Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID )
        }

        val mockPaymentProvider = mockk<PaymentProvider>{
            every { charge(any()) } returns true
        }

        val billingService = BillingService(mockPaymentProvider, InvoiceService(dal))

        billingService.chargeAllInvoices()

        verify(exactly = 1) { mockPaymentProvider.charge(any()) }
        confirmVerified(mockPaymentProvider)
    }
}