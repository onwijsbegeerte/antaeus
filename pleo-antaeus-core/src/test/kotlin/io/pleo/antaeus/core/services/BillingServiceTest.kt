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
            every { createInvoiceChargeStatus(any(), any(), any() )} returns InvoiceChargeStatus(Random.nextInt(), Random.nextInt(), Random.nextInt(), TransactionStatus.Attempted, LocalDate.now().toString())
            every { payInvoice(any())} returns Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID )
            every { fetchAllInvoiceChargeStatuses()} returns listOf()
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
    fun `Attempting to charging customers, with some unpaid invoices, should attempt to charge customer`() {

        val invoice = Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING )
        val dal = mockk<AntaeusDal> {
            every { fetchInvoices() } returns listOf(invoice) andThen listOf()
            every { createInvoiceChargeStatus(any(), any(), any() )} returns InvoiceChargeStatus(Random.nextInt(), Random.nextInt(), Random.nextInt(), TransactionStatus.Attempted, LocalDate.now().toString())
            every { payInvoice(any())} returns Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID )
            every { fetchAllInvoiceChargeStatuses()} returns listOf()
        }

        val mockPaymentProvider = mockk<PaymentProvider>{
            every { charge(any()) } returns true
        }

        val billingService = BillingService(mockPaymentProvider, InvoiceService(dal))

        billingService.chargeAllInvoices()

        verify(exactly = 1) { mockPaymentProvider.charge(invoice) }
        confirmVerified(mockPaymentProvider)
    }

    @Test
    fun `Attempting to charging customers, with some unpaid invoices charged success, should add attempted and success`() {

        val dal = mockk<AntaeusDal> {
            every { fetchInvoices() } returns listOf(Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PENDING )) andThen listOf()
            every { createInvoiceChargeStatus(any(), any(), any() )} returns InvoiceChargeStatus(Random.nextInt(), Random.nextInt(), Random.nextInt(), TransactionStatus.Attempted, LocalDate.now().toString())
            every { payInvoice(any())} returns Invoice(1, 2, Money(10.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID )
            every { fetchAllInvoiceChargeStatuses()} returns listOf()
        }

        val mockPaymentProvider = mockk<PaymentProvider>{
            every { charge(any()) } returns true
        }

        val billingService = BillingService(mockPaymentProvider, InvoiceService(dal))

        billingService.chargeAllInvoices()

        verify(exactly = 1) { dal.createInvoiceChargeStatus(2, TransactionStatus.Attempted, 1) }
        verify(exactly = 1) { dal.createInvoiceChargeStatus(2, TransactionStatus.Success, 1) }
    }
}