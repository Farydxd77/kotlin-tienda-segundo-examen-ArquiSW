package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy


class DescuentoBienvenidaStrategy : DescuentoStrategy {
    private val montoFijo = 10.0
    private val montoMinimo = 30.0

    override fun calcularDescuento(subtotal: Double): Double {
        return montoFijo
    }

    override fun esValido(subtotal: Double): Boolean {
        return subtotal >= montoMinimo
    }

    override fun getMensaje(): String {
        return "Descuento de Bienvenida: -\$$montoFijo"
    }

    override fun getCodigoDescuento(): String = "BIENVENIDA"
}