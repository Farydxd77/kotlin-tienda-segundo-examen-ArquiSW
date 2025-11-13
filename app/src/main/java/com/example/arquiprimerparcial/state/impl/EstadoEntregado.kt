package com.example.arquiprimerparcial.state.impl

import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.EstadoPedido

class EstadoEntregado : EstadoPedido {

    override fun comenzarPreparacion(contexto: ContextoPedido) {
        // Ya fue entregado
    }

    override fun marcarListo(contexto: ContextoPedido) {
        // Ya fue entregado
    }

    override fun confirmarEntrega(contexto: ContextoPedido) {
        // Ya está entregado
    }

    override fun cancelar(contexto: ContextoPedido) {
        // No se puede cancelar lo entregado
    }

    override fun obtenerNombre() = "ENTREGADO"
    override fun obtenerDescripcion() = "¡Pedido completado! Gracias 😊"
    override fun obtenerIcono() = "🎉"
    override fun obtenerColor() = "#4CAF50"  // Verde
    override fun obtenerProgreso() = 100
}