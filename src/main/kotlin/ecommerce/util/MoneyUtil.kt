package ecommerce.util

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyUtil {
    fun toMinorUnits(
        unitPrice: Double,
        qty: Long,
    ): Long =
        BigDecimal.valueOf(unitPrice)
            .multiply(BigDecimal.valueOf(qty))
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
}
