package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy


class DescuentoBlackFridayStrategy : DescuentoStrategy {
    private val porcentajeBase = 30.0
    private val porcentajeExtra = 10.0
    private val umbralExtra = 500.0

    override fun calcularDescuento(subtotal: Double): Double {
        val porcentaje = if (subtotal >= umbralExtra) {
            porcentajeBase + porcentajeExtra // 40% total
        } else {
            porcentajeBase // 30%
        }
        return subtotal * (porcentaje / 100)
    }

    override fun esValido(subtotal: Double): Boolean {
        return true // Siempre válido
    }

    override fun getMensaje(): String {
        return "Black Friday: 30% OFF (40% si compras más de \$500)"
    }

    override fun getCodigoDescuento(): String = "BLACKFRIDAY"
}