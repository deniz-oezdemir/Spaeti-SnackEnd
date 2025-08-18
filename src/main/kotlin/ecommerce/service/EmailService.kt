package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Order

interface EmailService {
    fun sendOrderConfirmation(member: Member, order: Order)
    fun sendOrderFailureNotification(member: Member, reason: String)
}
