package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import java.sql.Timestamp

class PedidoDao {



    fun eliminar(id: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM pedido WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }





    fun insertar(nombreCliente: String, fechaPedido: Timestamp, total: Double): Int {
        require(nombreCliente.isNotBlank()) { "Nombre cliente requerido" }
        require(total > 0) { "Total debe ser mayor a 0" }
        val connection = PostgresqlConexion.getConexion()
        val sql = """
            INSERT INTO pedido (nombre_cliente, fecha_pedido, total, estado) 
            VALUES (?, ?, ?, 'PENDIENTE')
        """.trimIndent()

        return try {
            val statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            statement.setString(1, nombreCliente)
            statement.setTimestamp(2, fechaPedido)
            statement.setDouble(3, total)

            statement.executeUpdate()

            val generatedKeys = statement.generatedKeys
            if (generatedKeys.next()) {
                generatedKeys.getInt(1)
            } else {
                0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 🔄 Actualizar estado del pedido
     */
    fun actualizarEstado(idPedido: Int, nuevoEstado: String): Boolean {
        val connection =  PostgresqlConexion.getConexion()
        val sql = "UPDATE pedido SET estado = ? WHERE id = ?"

        return try {
            val statement = connection.prepareStatement(sql)
            statement.setString(1, nuevoEstado)
            statement.setInt(2, idPedido)

            val filasAfectadas = statement.executeUpdate()
            filasAfectadas > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtener pedido por ID
     */
    fun obtenerPorId(idPedido: Int): Pedido? {
        val connection =  PostgresqlConexion.getConexion()
        val sql = "SELECT * FROM pedido WHERE id = ?"

        return try {
            val statement = connection.prepareStatement(sql)
            statement.setInt(1, idPedido)

            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                Pedido(
                    id = resultSet.getInt("id"),
                    nombreCliente = resultSet.getString("nombre_cliente"),
                    fechaPedido = resultSet.getTimestamp("fecha_pedido"),
                    total = resultSet.getDouble("total"),
                    estado = resultSet.getString("estado") ?: "PENDIENTE"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Listar todos los pedidos
     */
    fun listarTodos(): List<Pedido> {
        val connection =  PostgresqlConexion.getConexion()
        val sql = "SELECT * FROM pedido ORDER BY fecha_pedido DESC"
        val pedidos = mutableListOf<Pedido>()

        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(sql)

            while (resultSet.next()) {
                val pedido = Pedido(
                    id = resultSet.getInt("id"),
                    nombreCliente = resultSet.getString("nombre_cliente"),
                    fechaPedido = resultSet.getTimestamp("fecha_pedido"),
                    total = resultSet.getDouble("total"),
                    estado = resultSet.getString("estado") ?: "PENDIENTE"
                )
                pedidos.add(pedido)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return pedidos
    }
}