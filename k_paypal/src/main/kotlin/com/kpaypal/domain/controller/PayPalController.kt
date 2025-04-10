package com.kpaypal.domain.controller

import com.kpaypal.domain.service.PaypalService
import com.paypal.base.rest.PayPalRESTException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/paypal")
@CrossOrigin("http://localhost:63342")
class PayPalController(private val paypalService: PaypalService) {

    companion object {
        private const val SUCCESS_URL = "http://localhost:8080/paypal/success"
        private const val CANCEL_URL = "http://localhost:8080/paypal/cancel"
    }

    @PostMapping("/pay")
    fun makePayment(@RequestParam amount: Double): String {
        return try {
            val payment = paypalService.createPayment(
                amount,
                "USD",
                "paypal",
                "sale",
                "payment description",
                CANCEL_URL,
                SUCCESS_URL
            )

            payment.links.firstOrNull { it.rel == "approval_url" }?.href?.let {
                "Redirect to: $it"
            } ?: "Error processing the payment"

        } catch (e: PayPalRESTException) {
            throw RuntimeException(e)
        }
    }

    @GetMapping("/success")
    fun paymentSuccess(@RequestParam("paymentId") paymentId: String, @RequestParam("PayerID") payerId: String): String {
        return try {
            val payment = paypalService.execute(paymentId, payerId)
            if (payment.state == "approved") "payment is successfully done" else "payment failed"
        } catch (e: PayPalRESTException) {
            throw RuntimeException(e)
        }
    }
}
