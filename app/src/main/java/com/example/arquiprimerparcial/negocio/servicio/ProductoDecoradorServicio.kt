package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.decorator.ProductoComponente
import com.example.arquiprimerparcial.decorator.productos.ProductoBaseBD
import com.example.arquiprimerparcial.decorator.extras.*

/**
 * 🎨 SERVICIO DE DECORACIÓN DE PRODUCTOS REALES
 *
 * Implementación CORRECTA del patrón Decorator:
 * 1. Toma productos existentes de BD
 * 2. Los envuelve con decoradores (extras)
 * 3. Devuelve el producto decorado con precio y descripción actualizados
 */
class ProductoDecoradorServicio(
    private val productoServicio: ProductoServicio
) {

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
     *  DECORATOR PATTERN - Aplicar decoradores a un producto real
     *
     * @param idProducto ID del producto en la base de datos
     * @param extras Lista de extras a agregar
     * @return ProductoComponente decorado con precio actualizado
     */
    fun decorarProductoDeBD(idProducto: Int, extras: List<TipoExtra>): ProductoComponente? {
        //  Obtener producto real de BD
        val productoArray = productoServicio.obtenerProductoPorId(idProducto) ?: return null

        // Convertir a ProductoComponente (base para decorar)
        var productoDecorado: ProductoComponente = ProductoBaseBD(productoArray)

        //  Aplicar cada decorador en secuencia
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
     * Listar productos decorables de la BD
     * (puedes filtrar por categoría, ej: "COMIDA")
     */
    fun listarProductosDecorables(): List<Map<String, Any>> {
        return productoServicio.listarProductosPrimitivos("")
            .filter { it["activo"] as Boolean } // Solo activos
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

    /**
     * Formatear precio para mostrar
     */
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }
}