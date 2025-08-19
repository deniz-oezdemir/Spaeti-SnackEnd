package ecommerce.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentMethod(
    @JsonValue val id: String,
) {
    PM_CARD_VISA("pm_card_visa"),
    PM_CARD_CHARGE_DECLINED("pm_card_chargeDeclined"),
    PM_CARD_CHARGE_CUSTOMER_FAIL("pm_card_chargeCustomerFail"),
}
