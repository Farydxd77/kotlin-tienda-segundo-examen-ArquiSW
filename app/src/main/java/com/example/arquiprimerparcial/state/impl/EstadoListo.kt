package com.example.arquiprimerparcial.state.impl


import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.EstadoPedido

class EstadoListo : EstadoPedido {

    override fun comenzarPreparacion(contexto: ContextoPedido) {
        // Ya fue preparado
    }

    override fun marcarListo(contexto: ContextoPedido) {
        // Ya está listo
    }

    override fun confirmarEntrega(contexto: ContextoPedido) {
        // ✅ TRANSICIÓN: LISTO → ENTREGADO
        contexto.cambiarEstado(EstadoEntregado())
    }

    override fun cancelar(contexto: ContextoPedido) {
        // No se puede cancelar cuando ya está listo
    }

    override fun obtenerNombre() = "LISTO"
    override fun obtenerDescripcion() = "¡Listo para recoger!"
    override fun obtenerIcono() = "✅"
    override fun obtenerColor() = "#9C27B0"  // Morado
    override fun obtenerProgreso() = 75
}