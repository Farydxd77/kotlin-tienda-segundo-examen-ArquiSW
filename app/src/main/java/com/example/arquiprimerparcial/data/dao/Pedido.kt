package com.example.arquiprimerparcial.data.dao

import java.sql.Timestamp

data class Pedido(
    val id: Int,
    val nombreCliente: String,
    val fechaPedido: Timestamp,
    val total: Double
)