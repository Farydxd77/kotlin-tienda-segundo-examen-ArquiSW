package com.example.arquiprimerparcial.decorator.extras

import com.example.arquiprimerparcial.decorator.ProductoComponente
import com.example.arquiprimerparcial.decorator.ProductoDecorador

/**
 * 🍟 DECORADOR: Papas Fritas
 */
class ConPapas(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${producto.obtenerNombre()} + Papas"
    }

    override fun obtenerPrecio(): Double {
        return producto.obtenerPrecio() + 3.0
    }

    override fun obtenerDescripcion(): String {
        return "${producto.obtenerDescripcion()}, papas fritas crujientes"
    }
}

/**
 * 🥤 DECORADOR: Refresco
 */
class ConRefresco(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${producto.obtenerNombre()} + Refresco"
    }

    override fun obtenerPrecio(): Double {
        return producto.obtenerPrecio() + 2.5
    }

    override fun obtenerDescripcion(): String {
        return "${producto.obtenerDescripcion()}, refresco de 500ml"
    }
}

/**
 * 🍚 DECORADOR: Arroz
 */
class ConArroz(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${producto.obtenerNombre()} + Arroz"
    }

    override fun obtenerPrecio(): Double {
        return producto.obtenerPrecio() + 2.0
    }

    override fun obtenerDescripcion(): String {
        return "${producto.obtenerDescripcion()}, porción de arroz blanco"
    }
}

/**
 * 🧀 DECORADOR: Queso
 */
class ConQueso(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${producto.obtenerNombre()} + Queso"
    }

    override fun obtenerPrecio(): Double {
        return producto.obtenerPrecio() + 1.5
    }

    override fun obtenerDescripcion(): String {
        return "${producto.obtenerDescripcion()}, queso derretido"
    }
}

/**
 * 🥓 DECORADOR: Tocino
 */
class ConTocino(producto: ProductoComponente) : ProductoDecorador(producto) {

    override fun obtenerNombre(): String {
        return "${producto.obtenerNombre()} + Tocino"
    }

    override fun obtenerPrecio(): Double {
        return producto.obtenerPrecio() + 2.5
    }

    override fun obtenerDescripcion(): String {
        return "${producto.obtenerDescripcion()}, tocino crocante"
    }
}