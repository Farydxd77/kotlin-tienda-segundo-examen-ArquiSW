package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.PedidoProductoExtraDao
import com.example.arquiprimerparcial.data.dao.ExtraData

/**
 * 🎨 SERVICIO DE NEGOCIO para gestionar EXTRAS de productos
 *
 * ✅ RESPETA ARQUITECTURA 3 CAPAS:
 * - Capa de NEGOCIO
 * - Contiene validaciones y lógica
 * - Orquesta llamadas al DAO
 */
class PedidoExtraServicio {

    private val pedidoProductoExtraDao = PedidoProductoExtraDao()

    /**
     * 🎨 DECORATOR PATTERN - Guardar extras de productos decorados
     *
     * @param idPedido ID del pedido
     * @param detallesConExtras Lista de detalles que contienen extras
     * @return Result con éxito o error
     */
    fun guardarExtrasDeProductosDecorados(
        idPedido: Int,
        detallesConExtras: List<Map<String, Any>>
    ): Result<Boolean> {
        return try {
            // ✅ VALIDACIÓN DE NEGOCIO
            if (idPedido <= 0) {
                return Result.failure(Exception("ID de pedido inválido"))
            }

            if (detallesConExtras.isEmpty()) {
                return Result.success(true) // Sin extras, pero no es error
            }

            // ✅ TRANSFORMACIÓN DE DATOS (Lógica de negocio)
            val todosLosExtras = mutableListOf<ExtraData>()

            for (detalle in detallesConExtras) {
                val esDecorado = detalle["esDecorado"] as? Boolean ?: false

                if (esDecorado) {
                    val idProducto = detalle["idProducto"] as? Int

                    if (idProducto == null || idProducto <= 0) {
                        continue // Skip este detalle si el ID es inválido
                    }

                    @Suppress("UNCHECKED_CAST")
                    val extras = detalle["extras"] as? List<Map<String, Any>> ?: emptyList()

                    for (extra in extras) {
                        val tipoExtra = extra["tipo"] as? String ?: continue
                        val nombreExtra = extra["nombre"] as? String ?: continue
                        val precioExtra = extra["precio"] as? Double ?: 0.0

                        // ✅ VALIDACIÓN de cada extra
                        if (precioExtra <= 0) {
                            return Result.failure(Exception("Precio de extra inválido: $nombreExtra"))
                        }

                        todosLosExtras.add(
                            ExtraData(
                                idPedido = idPedido,
                                idProducto = idProducto,
                                tipoExtra = tipoExtra,
                                nombreExtra = nombreExtra,
                                precioExtra = precioExtra
                            )
                        )
                    }
                }
            }

            // ✅ Delegar operación de datos al DAO
            if (todosLosExtras.isNotEmpty()) {
                val resultado = pedidoProductoExtraDao.insertarExtrasLote(todosLosExtras)

                if (resultado) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Error al guardar extras en la base de datos"))
                }
            } else {
                Result.success(true) // No hay extras pero no es error
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error al procesar extras: ${e.message}"))
        }
    }

    /**
     * Obtener extras de un producto específico en un pedido
     */
    fun obtenerExtrasPorPedidoYProducto(idPedido: Int, idProducto: Int): List<Map<String, Any>> {
        return try {
            if (idPedido <= 0 || idProducto <= 0) {
                return emptyList()
            }
            pedidoProductoExtraDao.obtenerExtrasPorPedidoYProducto(idPedido, idProducto)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtener todos los extras de un pedido completo
     */
    fun obtenerExtrasPorPedido(idPedido: Int): List<Map<String, Any>> {
        return try {
            if (idPedido <= 0) {
                return emptyList()
            }
            pedidoProductoExtraDao.obtenerExtrasPorPedido(idPedido)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 🎨 LÓGICA DE NEGOCIO - Extraer extras del nombre del producto decorado
     *
     * Parsea un nombre como "Pollo Frito + Papas + Refresco" y extrae los extras
     */
    fun extraerExtrasDeNombre(nombreCompleto: String, precioBase: Double): List<Map<String, Any>> {
        val extras = mutableListOf<Map<String, Any>>()

        // Si no tiene "+", no hay extras
        if (!nombreCompleto.contains("+")) {
            return extras
        }

        // Dividir por "+"
        val partes = nombreCompleto.split("+").map { it.trim() }

        // La primera parte es el producto base, el resto son extras
        for (i in 1 until partes.size) {
            val nombreExtra = partes[i]

            // ✅ LÓGICA DE NEGOCIO: Mapear nombre a tipo y precio
            val (tipo, precio) = when {
                nombreExtra.contains("Papas", ignoreCase = true) ->
                    Pair("PAPAS", 3.0)
                nombreExtra.contains("Refresco", ignoreCase = true) ->
                    Pair("REFRESCO", 2.5)
                nombreExtra.contains("Arroz", ignoreCase = true) ->
                    Pair("ARROZ", 2.0)
                nombreExtra.contains("Queso", ignoreCase = true) ->
                    Pair("QUESO", 1.5)
                nombreExtra.contains("Tocino", ignoreCase = true) ->
                    Pair("TOCINO", 2.5)
                else -> Pair("OTRO", 0.0)
            }

            extras.add(
                mapOf(
                    "tipo" to tipo,
                    "nombre" to nombreExtra,
                    "precio" to precio
                )
            )
        }

        return extras
    }

    /**
     * 📊 ESTADÍSTICA: Extras más vendidos
     */
    fun obtenerExtrasMasVendidos(limite: Int = 10): List<Map<String, Any>> {
        return try {
            if (limite <= 0) {
                return emptyList()
            }
            pedidoProductoExtraDao.obtenerExtrasMasVendidos(limite)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 💰 ESTADÍSTICA: Ingresos por extras en un período
     */
    fun calcularIngresosPorExtras(fechaInicio: String, fechaFin: String): Double {
        return try {
            pedidoProductoExtraDao.calcularIngresosPorExtras(fechaInicio, fechaFin)
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Eliminar extras de un pedido (cuando se elimina el pedido)
     */
    fun eliminarExtrasPorPedido(idPedido: Int): Result<Boolean> {
        return try {
            if (idPedido <= 0) {
                return Result.failure(Exception("ID de pedido inválido"))
            }

            val resultado = pedidoProductoExtraDao.eliminarExtrasPorPedido(idPedido)

            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar extras"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}