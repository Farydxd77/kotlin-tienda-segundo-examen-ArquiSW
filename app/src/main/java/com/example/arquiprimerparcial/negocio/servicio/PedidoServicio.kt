package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.Pedido
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.strategy.DescuentoContext
import com.example.arquiprimerparcial.strategy.ResultadoDescuento
import com.example.arquiprimerparcial.strategy.DescuentoStrategyFactory
import java.sql.Timestamp

/**
 * 💼 SERVICIO DE PEDIDOS
 *
 * Responsabilidades:
 * - Crear y gestionar pedidos
 * - Validar datos de negocio
 * - Orquestar DAOs
 * - USAR el Context para aplicar descuentos (NO ser el Context)
 *
 * ✅ Ahora cumple 100% con Single Responsibility Principle
 */
class PedidoServicio {

    private val pedidoDao: PedidoDao = PedidoDao()
    private val detallePedidoDao: DetallePedidoDao = DetallePedidoDao()
    private val productoDao: ProductoDao = ProductoDao()

    // ✅ USA el Context (no ES el Context)
    private val descuentoContext = DescuentoContext()

    /**
     * 🎯 APLICAR DESCUENTO usando el Context
     *
     * Flujo según diagrama:
     * 1. Client llama a este método
     * 2. Este método obtiene la Strategy del Factory
     * 3. Pasa la Strategy al Context (setStrategy)
     * 4. Context ejecuta la Strategy (doSomething)
     */
    fun aplicarDescuento(subtotal: Double, codigo: String?): ResultadoDescuento {
        // 1️⃣ Obtener estrategia del Factory
        val strategy = DescuentoStrategyFactory.obtenerStrategy(codigo)

        // 2️⃣ Establecer estrategia en el Context (setStrategy)
        descuentoContext.setStrategy(strategy)

        // 3️⃣ Ejecutar a través del Context (doSomething)
        return descuentoContext.aplicarDescuento(subtotal)
    }

    /**
     * Crear pedido primitivo (desde mapa)
     */
    fun crearPedidoPrimitivo(pedidoData: Map<String, Any>): Result<Int> {
        return try {
            val nombreCliente = pedidoData["nombreCliente"] as String
            @Suppress("UNCHECKED_CAST")
            val detalles = pedidoData["detalles"] as List<Map<String, Any>>
            val codigoDescuento = pedidoData["codigoDescuento"] as? String

            val detallesArray = detalles.map { detalle ->
                arrayOf<Any>(
                    detalle["idProducto"] as Int,
                    detalle["cantidad"] as Int,
                    detalle["precioUnitario"] as Double
                )
            }

            crearPedido(nombreCliente, detallesArray, codigoDescuento)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crear pedido con aplicación de descuento
     */
    fun crearPedido(
        nombreCliente: String,
        detalles: List<Array<Any>>,
        codigoDescuento: String? = null
    ): Result<Int> {
        return try {
            // ========================================
            // VALIDACIONES DE NEGOCIO
            // ========================================
            if (nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            if (nombreCliente.length > 100) {
                return Result.failure(Exception("El nombre del cliente no puede exceder 100 caracteres"))
            }

            if (detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

            // Validar stock de cada producto
            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = productoDao.obtenerPorId(idProducto)
                if (productoArray == null) {
                    return Result.failure(Exception("Producto no encontrado con ID: $idProducto"))
                }

                val stock = productoArray[5] as Int
                val nombreProducto = productoArray[1] as String

                if (stock < cantidad) {
                    return Result.failure(
                        Exception("Stock insuficiente para $nombreProducto. Stock disponible: $stock")
                    )
                }
            }

            // ========================================
            // 🎯 APLICAR STRATEGY DE DESCUENTO
            // ========================================
            val subtotal = calcularTotalDetalles(detalles)

            // ✅ Usar el método que implementa el patrón correctamente
            val resultadoDescuento = aplicarDescuento(subtotal, codigoDescuento)
            val total = resultadoDescuento.total

            // ========================================
            // CREAR PEDIDO EN LA BASE DE DATOS
            // ========================================
            val idPedido = pedidoDao.insertar(
                nombreCliente = nombreCliente.trim(),
                fechaPedido = Timestamp(System.currentTimeMillis()),
                total = total
            )

            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            // ========================================
            // GUARDAR DETALLES DEL PEDIDO
            // ========================================
            val detallesParaInsertar = detalles.map { detalle ->
                arrayOf<Any>(
                    idPedido,
                    detalle[0] as Int,  // idProducto
                    detalle[1] as Int,  // cantidad
                    detalle[2] as Double // precioUnitario
                )
            }

            val resultadoDetalles = detallePedidoDao.insertarLote(detallesParaInsertar)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

            // ========================================
            // ACTUALIZAR STOCK DE PRODUCTOS
            // ========================================
            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = productoDao.obtenerPorId(idProducto)!!
                val stockActual = productoArray[5] as Int
                val nuevoStock = stockActual - cantidad

                productoDao.actualizarStock(idProducto, nuevoStock)
            }

            Result.success(idPedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcular total de detalles (sin descuento)
     */
    fun calcularTotalDetalles(detalles: List<Array<Any>>): Double {
        var total = 0.0
        for (detalle in detalles) {
            val cantidad = detalle[1] as Int
            val precioUnitario = detalle[2] as Double
            total += cantidad * precioUnitario
        }
        return total
    }

    /**
     * Obtener pedido por ID
     */
    fun obtenerPedidoPorId(idPedido: Int): Pedido? {
        return pedidoDao.obtenerPorId(idPedido)
    }

    /**
     * Listar todos los pedidos
     */
    fun listarPedidos(): List<Pedido> {
        return pedidoDao.listarTodos()
    }
}