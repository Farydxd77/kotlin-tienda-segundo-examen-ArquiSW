package com.example.arquiprimerparcial.strategy

interface DescuentoStrategy {
    fun calcularDescuento(subtotal: Double): Double
    fun esValido(subtotal: Double): Boolean
    fun getMensaje(): String
    fun getCodigoDescuento(): String
}