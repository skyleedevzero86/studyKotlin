package com.kpaypal.domain.service

import com.paypal.api.payments.*
import com.paypal.base.rest.APIContext
import org.springframework.stereotype.Service

@Service
class PaypalService(private val apiContext: APIContext) {

    fun createPayment(
        total: Double,
        currency: String,
        method: String,
        intent: String,
        description: String,
        cancelUrl: String,
        successUrl: String
    ): Payment {
        val amount = Amount().apply {
            this.currency = currency
            this.total = String.format("%.2f", total)
        }

        val transaction = Transaction().apply {
            this.description = description
            this.amount = amount
        }

        val payer = Payer().apply { paymentMethod = method.uppercase() }

        val redirectUrls = RedirectUrls().apply {
            this.cancelUrl = cancelUrl
            this.returnUrl = successUrl
        }

        return Payment().apply {
            this.intent = intent
            this.payer = payer
            this.transactions = listOf(transaction)
            this.redirectUrls = redirectUrls
        }.create(apiContext)
    }

    fun execute(paymentId: String, payerId: String): Payment {
        val payment = Payment().apply { id = paymentId }
        val paymentExecution = PaymentExecution().apply { this.payerId = payerId }
        return payment.execute(apiContext, paymentExecution)
    }
}
