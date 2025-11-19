package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy
import com.example.arquiprimerparcial.strategy.ResultadoDescuento


class DescuentoBienvenidaStrategy : DescuentoStrategy {
    private val montoFijo = 10.0
    private val montoMinimo = 30.0

    override fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        val esValido = subtotal >= montoMinimo
        val descuento = if (esValido) montoFijo else 0.0
        val total = subtotal - descuento

        val mensaje = if (esValido) {
            " Descuento de Bienvenida: -S/ $montoFijo"
        } else {
            " Descuento de Bienvenida requiere compra mínima de S/ $montoMinimo"
        }

        return ResultadoDescuento(
            esValido = esValido,
            mensaje = mensaje,
            subtotal = subtotal,
            descuentoAplicado = descuento,
            total = total,
            codigoDescuento = "BIENVENIDA"
        )
    }
}