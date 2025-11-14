package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy
import com.example.arquiprimerparcial.strategy.ResultadoDescuento


class DescuentoBlackFridayStrategy : DescuentoStrategy {
    private val porcentajeBase = 30.0
    private val porcentajeExtra = 10.0
    private val umbralExtra = 500.0

    override fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        val porcentaje = if (subtotal >= umbralExtra) {
            porcentajeBase + porcentajeExtra // 40%
        } else {
            porcentajeBase // 30%
        }

        val descuento = subtotal * (porcentaje / 100)
        val total = subtotal - descuento

        val mensaje = if (subtotal >= umbralExtra) {
            "🔥 Black Friday: ${porcentaje.toInt()}% OFF (¡40% por compra mayor a S/ $umbralExtra!)"
        } else {
            "🔥 Black Friday: ${porcentaje.toInt()}% OFF"
        }

        return ResultadoDescuento(
            esValido = true,
            mensaje = mensaje,
            subtotal = subtotal,
            descuentoAplicado = descuento,
            total = total,
            codigoDescuento = "BLACKFRIDAY"
        )
    }
}