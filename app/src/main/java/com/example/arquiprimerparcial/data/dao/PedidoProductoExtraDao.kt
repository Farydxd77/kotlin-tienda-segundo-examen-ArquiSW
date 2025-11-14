package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion

/**
 * 🎨 DAO para gestionar los EXTRAS de productos en pedidos
 * Implementa operaciones CRUD para la tabla pedido_producto_extra
 */
class PedidoProductoExtraDao {

    /**
     * Insertar un extra para un producto en un pedido
     */
    fun insertarExtra(
        idPedido: Int,
        idProducto: Int,
        tipoExtra: String,
        nombreExtra: String,
        precioExtra: Double
    ): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO pedido_producto_extra 
                    (id_pedido, id_producto, tipo_extra, nombre_extra, precio_extra) 
                    VALUES (?, ?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.setInt(2, idProducto)
                    ps.setString(3, tipoExtra)
                    ps.setString(4, nombreExtra)
                    ps.setDouble(5, precioExtra)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Insertar múltiples extras de una sola vez (transacción)
     */
    fun insertarExtrasLote(extras: List<ExtraData>): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                conexion.autoCommit = false

                val sql = """
                    INSERT INTO pedido_producto_extra 
                    (id_pedido, id_producto, tipo_extra, nombre_extra, precio_extra) 
                    VALUES (?, ?, ?, ?, ?)
                """

                conexion.prepareStatement(sql).use { ps ->
                    for (extra in extras) {
                        ps.setInt(1, extra.idPedido)
                        ps.setInt(2, extra.idProducto)
                        ps.setString(3, extra.tipoExtra)
                        ps.setString(4, extra.nombreExtra)
                        ps.setDouble(5, extra.precioExtra)
                        ps.addBatch()
                    }
                    val results = ps.executeBatch()
                    conexion.commit()
                    results.all { it > 0 }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtener todos los extras de un producto específico en un pedido
     */
    fun obtenerExtrasPorPedidoYProducto(idPedido: Int, idProducto: Int): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, tipo_extra, nombre_extra, precio_extra
                FROM pedido_producto_extra
                WHERE id_pedido = ? AND id_producto = ?
                ORDER BY id
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idPedido)
                ps.setInt(2, idProducto)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            mapOf(
                                "id" to rs.getInt("id"),
                                "tipoExtra" to rs.getString("tipo_extra"),
                                "nombreExtra" to rs.getString("nombre_extra"),
                                "precioExtra" to rs.getDouble("precio_extra")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    /**
     * Obtener TODOS los extras de un pedido completo
     */
    fun obtenerExtrasPorPedido(idPedido: Int): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    ppe.id,
                    ppe.id_producto,
                    ppe.tipo_extra,
                    ppe.nombre_extra,
                    ppe.precio_extra,
                    p.nombre as producto_nombre
                FROM pedido_producto_extra ppe
                JOIN producto p ON ppe.id_producto = p.id
                WHERE ppe.id_pedido = ?
                ORDER BY ppe.id_producto, ppe.id
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idPedido)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            mapOf(
                                "id" to rs.getInt("id"),
                                "idProducto" to rs.getInt("id_producto"),
                                "tipoExtra" to rs.getString("tipo_extra"),
                                "nombreExtra" to rs.getString("nombre_extra"),
                                "precioExtra" to rs.getDouble("precio_extra"),
                                "productoNombre" to rs.getString("producto_nombre")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    /**
     * Eliminar todos los extras de un pedido
     * (útil cuando se elimina un pedido completo)
     */
    fun eliminarExtrasPorPedido(idPedido: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM pedido_producto_extra WHERE id_pedido = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.executeUpdate() >= 0 // Puede ser 0 si no hay extras
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 📊 ESTADÍSTICA: Extras más vendidos
     */
    fun obtenerExtrasMasVendidos(limite: Int = 10): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    tipo_extra,
                    nombre_extra,
                    COUNT(*) as cantidad_vendida,
                    SUM(precio_extra) as ingresos_totales
                FROM pedido_producto_extra
                GROUP BY tipo_extra, nombre_extra
                ORDER BY cantidad_vendida DESC
                LIMIT ?
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            mapOf(
                                "tipoExtra" to rs.getString("tipo_extra"),
                                "nombreExtra" to rs.getString("nombre_extra"),
                                "cantidadVendida" to rs.getInt("cantidad_vendida"),
                                "ingresosTotales" to rs.getDouble("ingresos_totales")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    /**
     * 💰 ESTADÍSTICA: Total de ingresos por extras en un período
     */
    fun calcularIngresosPorExtras(fechaInicio: String, fechaFin: String): Double {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COALESCE(SUM(ppe.precio_extra), 0) as total
                    FROM pedido_producto_extra ppe
                    JOIN pedido p ON ppe.id_pedido = p.id
                    WHERE DATE(p.fecha_pedido) BETWEEN ?::date AND ?::date
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, fechaInicio)
                    ps.setString(2, fechaFin)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getDouble("total") else 0.0
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}

/**
 * Data class para facilitar inserción de extras en lote
 */
data class ExtraData(
    val idPedido: Int,
    val idProducto: Int,
    val tipoExtra: String,
    val nombreExtra: String,
    val precioExtra: Double
)