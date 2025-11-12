package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy


class SinDescuentoStrategy : DescuentoStrategy {
    override fun calcularDescuento(subtotal: Double): Double = 0.0

    override fun esValido(subtotal: Double): Boolean = true

    override fun getMensaje(): String = "Sin descuento aplicado"

    override fun getCodigoDescuento(): String = ""
}