package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.HistorialPedidosDao
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ SERVICIO DE NEGOCIO - Capa de NEGOCIO
 *
 * Responsabilidades:
 * - Obtener historial de pedidos
 * - Formatear datos para presentación
 * - Construir mensajes detallados
 * - Validaciones de negocio
 */
class HistorialPedidosServicio {

    // ✅ Solo accede a DAOs (Capa de DATOS)
    private val historialPedidosDao = HistorialPedidosDao()
    private val pedidoExtraServicio = PedidoExtraServicio() // ✅ Usa otro servicio

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Obtener todos los pedidos con sus detalles y extras
     */
    fun obtenerTodosPedidosPrimitivos(): List<Map<String, Any>> {
        return try {
            val pedidosArray = historialPedidosDao.listarTodos()
            val resultado = mutableListOf<Map<String, Any>>()

            for (pedido in pedidosArray) {
                val id = pedido[0] as Int
                val nombreCliente = pedido[1] as String
                val fechaPedido = pedido[2] as java.sql.Timestamp
                val total = pedido[3] as Double

                // Obtener detalles del pedido
                val detallesArray = historialPedidosDao.obtenerDetallesPedido(id)
                val detallesPrimitivos = mutableListOf<Map<String, Any>>()
                var cantidadTotal = 0

                for (detalle in detallesArray) {
                    val idProducto = detalle[1] as Int
                    val cantidad = detalle[2] as Int
                    val precioUnitario = detalle[3] as Double
                    val nombreProducto = detalle[4] as String
                    cantidadTotal += cantidad

                    // ✅ USAR SERVICIO para obtener extras (respeta arquitectura)
                    val extras = pedidoExtraServicio.obtenerExtrasPorPedidoYProducto(id, idProducto)

                    detallesPrimitivos.add(
                        mapOf(
                            "idProducto" to idProducto,
                            "nombreProducto" to nombreProducto,
                            "cantidad" to cantidad,
                            "precioUnitario" to precioUnitario,
                            "subtotal" to (cantidad.toDouble() * precioUnitario),
                            "extras" to extras // ✅ EXTRAS desde BD
                        )
                    )
                }

                resultado.add(
                    mapOf(
                        "id" to id,
                        "nombreCliente" to nombreCliente,
                        "fecha" to dateFormat.format(Date(fechaPedido.time)),
                        "total" to total,
                        "cantidadProductos" to cantidadTotal,
                        "detalles" to detallesPrimitivos
                    )
                )
            }

            resultado
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtener estadísticas del día
     */
    fun obtenerEstadisticasDia(): Pair<Double, Int> {
        val ventas = historialPedidosDao.calcularVentasDia()
        val totalPedidos = historialPedidosDao.contarPedidosHoy()
        return Pair(ventas, totalPedidos)
    }

    /**
     * Eliminar pedido completo (con extras)
     */
    fun eliminarPedidoCompleto(id: Int): Result<Boolean> {
        return try {
            // ✅ VALIDACIÓN DE NEGOCIO
            if (id <= 0) {
                return Result.failure(Exception("ID inválido"))
            }

            // ✅ Usar servicio para eliminar extras primero (respeta arquitectura)
            val resultadoExtras = pedidoExtraServicio.eliminarExtrasPorPedido(id)

            if (resultadoExtras.isFailure) {
                return Result.failure(
                    Exception("Error al eliminar extras: ${resultadoExtras.exceptionOrNull()?.message}")
                )
            }

            // Luego eliminar el pedido
            val resultado = historialPedidosDao.eliminarPedido(id)

            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el pedido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🎨 Construir mensaje detallado del pedido
     * Muestra productos con sus extras
     */
    fun construirMensajeDetallePrimitivo(
        id: Int,
        nombreCliente: String,
        fecha: String,
        total: Double,
        detalles: List<Map<String, Any>>
    ): String {
        return buildString {
            append("📦 Pedido #$id\n\n")
            append("👤 Cliente: $nombreCliente\n")
            append("📅 Fecha: $fecha\n\n")
            append("🛒 Productos:\n\n")

            for (detalle in detalles) {
                val nombreProducto = detalle["nombreProducto"] as String
                val cantidad = detalle["cantidad"] as Int
                val precioUnitario = detalle["precioUnitario"] as Double
                val subtotal = detalle["subtotal"] as Double

                @Suppress("UNCHECKED_CAST")
                val extras = detalle["extras"] as? List<Map<String, Any>> ?: emptyList()

                // ✅ MOSTRAR PRODUCTO
                if (extras.isNotEmpty()) {
                    append("🎨 $nombreProducto\n")

                    // ✅ MOSTRAR EXTRAS
                    append("   Extras:\n")
                    for (extra in extras) {
                        val nombreExtra = extra["nombreExtra"] as String
                        val precioExtra = extra["precioExtra"] as Double
                        append("      • $nombreExtra (+S/ ${formatearPrecio(precioExtra)})\n")
                    }
                } else {
                    append("• $nombreProducto\n")
                }

                // Cantidad y precio
                append("   $cantidad x S/ ${formatearPrecio(precioUnitario)}")
                append(" = S/ ${formatearPrecio(subtotal)}\n\n")
            }

            append("─────────────────\n")
            append("💰 Total: S/ ${formatearPrecio(total)}")
        }
    }

    /**
     * Formatear precio con 2 decimales
     */
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }
}