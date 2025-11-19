package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy
import com.example.arquiprimerparcial.strategy.ResultadoDescuento


class DescuentoNavidenoStrategy : DescuentoStrategy {
    private val porcentaje = 15.0
    private val montoMinimo = 50.0

    override fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        val esValido = subtotal >= montoMinimo
        val descuento = if (esValido) subtotal * (porcentaje / 100) else 0.0
        val total = subtotal - descuento

        val mensaje = if (esValido) {
            "Descuento Navideño aplicado: $porcentaje% OFF"
        } else {
            "Descuento Navideño requiere compra mínima de S/ $montoMinimo"
        }

        return ResultadoDescuento(
            esValido = esValido,
            mensaje = mensaje,
            subtotal = subtotal,
            descuentoAplicado = descuento,
            total = total,
            codigoDescuento = "NAVIDAD2024"
        )
    }
}