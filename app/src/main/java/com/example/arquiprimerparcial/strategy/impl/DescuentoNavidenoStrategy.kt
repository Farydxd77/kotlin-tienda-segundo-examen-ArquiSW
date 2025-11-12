package com.example.arquiprimerparcial.strategy.impl

import com.example.arquiprimerparcial.strategy.DescuentoStrategy


class DescuentoNavidenoStrategy : DescuentoStrategy {
    private val porcentaje = 15.0
    private val montoMinimo = 50.0

    override fun calcularDescuento(subtotal: Double): Double {
        return subtotal * (porcentaje / 100)
    }

    override fun esValido(subtotal: Double): Boolean {
        return subtotal >= montoMinimo
    }

    override fun getMensaje(): String {
        return "Descuento Navideño aplicado: $porcentaje% OFF"
    }

    override fun getCodigoDescuento(): String = "NAVIDAD2024"
}