package com.example.arquiprimerparcial.decorator

/**
 * 🎨 PATRÓN DECORATOR - Componente Base
 * Define la interfaz común para productos base y decorados
 */
interface ProductoComponente {
    fun obtenerNombre(): String
    fun obtenerPrecio(): Double
    fun obtenerDescripcion(): String
}