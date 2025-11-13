package com.example.arquiprimerparcial.decorator.productos

import com.example.arquiprimerparcial.decorator.ProductoComponente

/**
 * 🎨 ADAPTER: Convierte productos de BD en ProductoComponente
 *
 * Este es el "producto base REAL" que viene de la base de datos.
 * El Decorator Pattern lo envuelve con funcionalidad extra.
 */
class ProductoBaseBD(
    private val productoArray: Array<Any>  // Producto de BD
) : ProductoComponente {

    // Extraer datos del array
    private val id: Int = productoArray[0] as Int
    private val nombre: String = productoArray[1] as String
    private val descripcion: String = productoArray[2] as String
    private val precio: Double = productoArray[4] as Double

    override fun obtenerNombre(): String = nombre

    override fun obtenerPrecio(): Double = precio

    override fun obtenerDescripcion(): String = descripcion

    /**
     * Obtener ID del producto original (útil para pedidos)
     */
    fun obtenerId(): Int = id
}