package com.nullpointer.squad.util

import java.math.RoundingMode

fun Double.convertPrice(
    currency: String,
    usdToInrRate: Double
): Double {
    return if (currency.equals("INR", ignoreCase = true)) {
        (this * usdToInrRate).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
    } else {
        this
    }
}





