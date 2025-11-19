package com.example.arquiprimerparcial.decorator

/**
 *  PATRÓN DECORATOR - Decorador Abstracto
 * Clase base para todos los decoradores
 */
abstract class ProductoDecorador(
    protected val producto: ProductoComponente
) : ProductoComponente {

    override fun obtenerNombre(): String = producto.obtenerNombre()

    override fun obtenerPrecio(): Double = producto.obtenerPrecio()

    override fun obtenerDescripcion(): String = producto.obtenerDescripcion()
}