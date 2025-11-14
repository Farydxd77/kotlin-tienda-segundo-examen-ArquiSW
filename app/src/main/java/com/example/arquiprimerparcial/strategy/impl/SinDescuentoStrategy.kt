package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy
import com.example.arquiprimerparcial.strategy.ResultadoDescuento


class SinDescuentoStrategy : DescuentoStrategy {
    override fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        return ResultadoDescuento(
            esValido = true,
            mensaje = "Sin descuento aplicado",
            subtotal = subtotal,
            descuentoAplicado = 0.0,
            total = subtotal,
            codigoDescuento = ""
        )
    }
}