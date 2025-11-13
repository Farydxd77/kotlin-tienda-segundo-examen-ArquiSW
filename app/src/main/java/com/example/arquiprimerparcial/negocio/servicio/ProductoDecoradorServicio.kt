package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.decorator.ProductoComponente
import com.example.arquiprimerparcial.decorator.productos.Hamburguesa
import com.example.arquiprimerparcial.decorator.productos.Lomito
import com.example.arquiprimerparcial.decorator.productos.PolloFrito
import com.example.arquiprimerparcial.decorator.extras.*

/**
 * 🎨 SERVICIO DE PRODUCTOS DECORADOS
 * Gestiona la creación de productos base y sus decoradores
 */
class ProductoDecoradorServicio {

    /**
     * Tipos de productos base disponibles
     */
    enum class TipoProductoBase {
        POLLO_FRITO,
        HAMBURGUESA,
        LOMITO
    }

    /**
     * Tipos de extras/decoradores disponibles
     */
    enum class TipoExtra {
        PAPAS,
        REFRESCO,
        ARROZ,
        QUESO,
        TOCINO
    }

    /**
     * Obtener precio base de un producto
     */
    fun obtenerPrecioBase(tipo: TipoProductoBase): Double {
        return when (tipo) {
            TipoProductoBase.POLLO_FRITO -> 15.0
            TipoProductoBase.HAMBURGUESA -> 12.0
            TipoProductoBase.LOMITO -> 18.0
        }
    }

    /**
     * Obtener precio de un extra
     */
    fun obtenerPrecioExtra(tipo: TipoExtra): Double {
        return when (tipo) {
            TipoExtra.PAPAS -> 3.0
            TipoExtra.REFRESCO -> 2.5
            TipoExtra.ARROZ -> 2.0
            TipoExtra.QUESO -> 1.5
            TipoExtra.TOCINO -> 2.5
        }
    }

    /**
     * Crear producto base sin decorar
     */
    fun crearProductoBase(tipo: TipoProductoBase): ProductoComponente {
        return when (tipo) {
            TipoProductoBase.POLLO_FRITO -> PolloFrito()
            TipoProductoBase.HAMBURGUESA -> Hamburguesa()
            TipoProductoBase.LOMITO -> Lomito()
        }
    }

    /**
     * 🎨 APLICAR DECORADORES - Patrón Decorator en acción
     *
     * Aplica múltiples decoradores a un producto base
     *
     * Ejemplo:
     * - Base: Lomito ($18)
     * - Extras: [PAPAS, REFRESCO, QUESO]
     * - Resultado: Lomito + Papas + Refresco + Queso ($25)
     */
    fun aplicarDecoradores(
        productoBase: ProductoComponente,
        extras: List<TipoExtra>
    ): ProductoComponente {
        var productoDecorado: ProductoComponente = productoBase

        // Aplicar cada decorador en secuencia
        for (extra in extras) {
            productoDecorado = when (extra) {
                TipoExtra.PAPAS -> ConPapas(productoDecorado)
                TipoExtra.REFRESCO -> ConRefresco(productoDecorado)
                TipoExtra.ARROZ -> ConArroz(productoDecorado)
                TipoExtra.QUESO -> ConQueso(productoDecorado)
                TipoExtra.TOCINO -> ConTocino(productoDecorado)
            }
        }

        return productoDecorado
    }

    /**
     * Crear producto decorado completo desde tipos
     */
    fun crearProductoDecorado(
        tipoBase: TipoProductoBase,
        extras: List<TipoExtra>
    ): ProductoComponente {
        val base = crearProductoBase(tipoBase)
        return aplicarDecoradores(base, extras)
    }

    /**
     * Listar todos los productos base disponibles
     */
    fun listarProductosBase(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "tipo" to TipoProductoBase.POLLO_FRITO,
                "nombre" to "Pollo Frito",
                "descripcion" to "Pollo frito crujiente",
                "precio" to 15.0,
                "icono" to "🍗"
            ),
            mapOf(
                "tipo" to TipoProductoBase.HAMBURGUESA,
                "nombre" to "Hamburguesa",
                "descripcion" to "Hamburguesa clásica con carne",
                "precio" to 12.0,
                "icono" to "🍔"
            ),
            mapOf(
                "tipo" to TipoProductoBase.LOMITO,
                "nombre" to "Lomito",
                "descripcion" to "Lomito saltado peruano",
                "precio" to 18.0,
                "icono" to "🥩"
            )
        )
    }

    /**
     * Listar todos los extras disponibles
     */
    fun listarExtrasDisponibles(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "tipo" to TipoExtra.PAPAS,
                "nombre" to "Papas Fritas",
                "precio" to 3.0,
                "icono" to "🍟"
            ),
            mapOf(
                "tipo" to TipoExtra.REFRESCO,
                "nombre" to "Refresco",
                "precio" to 2.5,
                "icono" to "🥤"
            ),
            mapOf(
                "tipo" to TipoExtra.ARROZ,
                "nombre" to "Arroz",
                "precio" to 2.0,
                "icono" to "🍚"
            ),
            mapOf(
                "tipo" to TipoExtra.QUESO,
                "nombre" to "Queso",
                "precio" to 1.5,
                "icono" to "🧀"
            ),
            mapOf(
                "tipo" to TipoExtra.TOCINO,
                "nombre" to "Tocino",
                "precio" to 2.5,
                "icono" to "🥓"
            )
        )
    }

    /**
     * Formatear precio para mostrar
     */
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }

    /**
     * Obtener resumen de un producto decorado
     */
    fun obtenerResumenProducto(producto: ProductoComponente): Map<String, Any> {
        return mapOf(
            "nombre" to producto.obtenerNombre(),
            "descripcion" to producto.obtenerDescripcion(),
            "precio" to producto.obtenerPrecio(),
            "precioFormateado" to "S/ ${formatearPrecio(producto.obtenerPrecio())}"
        )
    }
}