package com.example.arquiprimerparcial.strategy


data class ResultadoDescuento(
    val esValido: Boolean,
    val mensaje: String,
    val subtotal: Double,
    val descuentoAplicado: Double,
    val total: Double,
    val codigoDescuento: String
)