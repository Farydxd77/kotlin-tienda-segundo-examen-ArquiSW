package com.example.arquiprimerparcial.state.impl


import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.EstadoPedido

class EstadoPreparando : EstadoPedido {

    override fun comenzarPreparacion(contexto: ContextoPedido) {
        // Ya está en preparación
    }

    override fun marcarListo(contexto: ContextoPedido) {
        // ✅ TRANSICIÓN: PREPARANDO → LISTO
        contexto.cambiarEstado(EstadoListo())
    }

    override fun confirmarEntrega(contexto: ContextoPedido) {
        // Debe marcar listo primero
    }

    override fun cancelar(contexto: ContextoPedido) {
        // ✅ TRANSICIÓN: PREPARANDO → CANCELADO
        contexto.cambiarEstado(EstadoCancelado())
    }

    override fun obtenerNombre() = "PREPARANDO"
    override fun obtenerDescripcion() = "Empaquetando tu pedido"
    override fun obtenerIcono() = "📦"
    override fun obtenerColor() = "#FF9800"  // Naranja
    override fun obtenerProgreso() = 50
}