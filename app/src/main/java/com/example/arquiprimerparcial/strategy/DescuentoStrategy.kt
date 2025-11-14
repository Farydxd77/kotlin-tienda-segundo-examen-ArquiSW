package com.example.arquiprimerparcial.strategy

interface DescuentoStrategy {

    fun aplicarDescuento(subtotal: Double): ResultadoDescuento

}