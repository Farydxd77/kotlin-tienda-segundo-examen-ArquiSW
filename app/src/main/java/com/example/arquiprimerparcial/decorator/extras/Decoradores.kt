package com.example.arquiprimerparcial.decorator.extras

import com.example.arquiprimerparcial.decorator.ProductoComponente
import com.example.arquiprimerparcial.decorator.ProductoDecorador

/**
 * 🍟 DECORADOR: Papas Fritas
 */
class ConPapas(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${super.obtenerNombre()} + Papas"  // ✅ Llama a super
    }

    override fun obtenerPrecio(): Double {
        return super.obtenerPrecio() + precioExtra()  // ✅ Llama a super primero
    }

    override fun obtenerDescripcion(): String {
        return "${super.obtenerDescripcion()}, papas fritas crujientes"  // ✅ Llama a super
    }

    /**
     * Método extra que agrega funcionalidad adicional
     */
    private fun precioExtra(): Double = 3.0
}

/**
 * 🥤 DECORADOR: Refresco
 */
class ConRefresco(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${super.obtenerNombre()} + Refresco"  // ✅ Llama a super
    }

    override fun obtenerPrecio(): Double {
        return super.obtenerPrecio() + precioExtra()  // ✅ Llama a super primero
    }

    override fun obtenerDescripcion(): String {
        return "${super.obtenerDescripcion()}, refresco de 500ml"  // ✅ Llama a super
    }

    /**
     * Método extra que agrega funcionalidad adicional
     */
    private fun precioExtra(): Double = 2.5
}

/**
 * 🍚 DECORADOR: Arroz
 */
class ConArroz(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${super.obtenerNombre()} + Arroz"  // ✅ Llama a super
    }

    override fun obtenerPrecio(): Double {
        return super.obtenerPrecio() + precioExtra()  // ✅ Llama a super primero
    }

    override fun obtenerDescripcion(): String {
        return "${super.obtenerDescripcion()}, porción de arroz blanco"  // ✅ Llama a super
    }

    /**
     * Método extra que agrega funcionalidad adicional
     */
    private fun precioExtra(): Double = 2.0
}

/**
 * 🧀 DECORADOR: Queso
 */
class ConQueso(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${super.obtenerNombre()} + Queso"  // ✅ Llama a super
    }

    override fun obtenerPrecio(): Double {
        return super.obtenerPrecio() + precioExtra()  // ✅ Llama a super primero
    }

    override fun obtenerDescripcion(): String {
        return "${super.obtenerDescripcion()}, queso derretido"  // ✅ Llama a super
    }

    /**
     * Método extra que agrega funcionalidad adicional
     */
    private fun precioExtra(): Double = 1.5
}

/**
 * 🥓 DECORADOR: Tocino
 */
class ConTocino(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${super.obtenerNombre()} + Tocino"  // ✅ Llama a super
    }

    override fun obtenerPrecio(): Double {
        return super.obtenerPrecio() + precioExtra()  // ✅ Llama a super primero
    }

    override fun obtenerDescripcion(): String {
        return "${super.obtenerDescripcion()}, tocino crocante"  // ✅ Llama a super
    }

    /**
     * Método extra que agrega funcionalidad adicional
     */
    private fun precioExtra(): Double = 2.5
}