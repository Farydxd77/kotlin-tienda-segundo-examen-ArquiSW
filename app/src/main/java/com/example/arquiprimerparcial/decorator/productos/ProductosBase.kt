package com.example.arquiprimerparcial.decorator.productos

import com.example.arquiprimerparcial.decorator.ProductoComponente

/**
 * 🍗 PRODUCTO BASE: Pollo Frito
 */
class PolloFrito(
    private val precioBase: Double = 15.0
) : ProductoComponente {

    override fun obtenerNombre(): String = "Pollo Frito"

    override fun obtenerPrecio(): Double = precioBase

    override fun obtenerDescripcion(): String = "Pollo frito crujiente"
}

/**
 * 🍔 PRODUCTO BASE: Hamburguesa
 */
class Hamburguesa(
    private val precioBase: Double = 12.0
) : ProductoComponente {

    override fun obtenerNombre(): String = "Hamburguesa"

    override fun obtenerPrecio(): Double = precioBase

    override fun obtenerDescripcion(): String = "Hamburguesa clásica con carne"
}

/**
 * 🥩 PRODUCTO BASE: Lomito
 */
class Lomito(
    private val precioBase: Double = 18.0
) : ProductoComponente {

    override fun obtenerNombre(): String = "Lomito"

    override fun obtenerPrecio(): Double = precioBase

    override fun obtenerDescripcion(): String = "Lomito saltado peruano"
}