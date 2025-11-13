package com.example.arquiprimerparcial.state

/**
 * 🔄 CONTEXTO DEL PEDIDO
 * Solo mantiene el estado actual y DELEGA operaciones
 */
class ContextoPedido(
    val idPedido: Int,
    val nombreCliente: String,
    val total: Double,
    private var estado: EstadoPedido
) {

    private val historial = mutableListOf<String>()

    init {
        historial.add(estado.obtenerNombre())
    }

    // ✅ SOLO DELEGA - No decide transiciones
    fun comenzarPreparacion() {
        estado.comenzarPreparacion(this)
    }

    fun marcarListo() {
        estado.marcarListo(this)
    }

    fun confirmarEntrega() {
        estado.confirmarEntrega(this)
    }

    fun cancelar() {
        estado.cancelar(this)
    }

    // Permite que estados cambien el estado interno
    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        this.estado = nuevoEstado
        historial.add(nuevoEstado.obtenerNombre())
    }

    fun obtenerEstadoActual(): EstadoPedido = estado

    fun obtenerHistorial(): List<String> = historial.toList()
}