package com.example.arquiprimerparcial.state

/**
 * 🔄 PATRÓN STATE - Interfaz
 * Define las operaciones que cada estado debe manejar
 */
interface EstadoPedido {

    // Operaciones de transición
    fun comenzarPreparacion(contexto: ContextoPedido)
    fun marcarListo(contexto: ContextoPedido)
    fun confirmarEntrega(contexto: ContextoPedido)
    fun cancelar(contexto: ContextoPedido)

    // Consultas
    fun obtenerNombre(): String
    fun obtenerDescripcion(): String
    fun obtenerIcono(): String
    fun obtenerColor(): String
    fun obtenerProgreso(): Int
}