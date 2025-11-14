package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.Pedido
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.model.ResultadoDescuento
import com.example.arquiprimerparcial.strategy.DescuentoStrategy
import com.example.arquiprimerparcial.strategy.DescuentoStrategyFactory
import com.example.arquiprimerparcial.strategy.impl.SinDescuentoStrategy
import java.sql.Timestamp

class PedidoServicio {

    private val pedidoDao: PedidoDao = PedidoDao()
    private val detallePedidoDao: DetallePedidoDao = DetallePedidoDao()
    private val productoDao: ProductoDao = ProductoDao()

    // 🎯 PATRÓN STRATEGY - Estrategia de descuento actual
    // ✅ CORRECTO: Mantiene UNA referencia a la estrategia
    private var descuentoStrategy: DescuentoStrategy = SinDescuentoStrategy()

    /**
     * 🎯 STRATEGY PATTERN - Cambiar estrategia de descuento dinámicamente
     * ✅ CORRECTO: Permite cambiar la estrategia en tiempo de ejecución
     */
    fun establecerEstrategiaDescuento(codigoDescuento: String?) {
        descuentoStrategy = DescuentoStrategyFactory.obtenerStrategy(codigoDescuento)
    }

    /**
     * 🎯 STRATEGY PATTERN - Aplicar descuento usando la estrategia actual
     * ✅ CORRECTO: USA la estrategia almacenada, NO crea una nueva
     */
    fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        // ✅ USA descuentoStrategy almacenado
        if (!descuentoStrategy.esValido(subtotal)) {
            return ResultadoDescuento(
                esValido = false,
                mensaje = "El código de descuento requiere una compra mínima mayor",
                subtotal = subtotal,
                descuentoAplicado = 0.0,
                total = subtotal,
                codigoDescuento = descuentoStrategy.getCodigoDescuento()
            )
        }

        val descuento = descuentoStrategy.calcularDescuento(subtotal)
        val total = subtotal - descuento

        return ResultadoDescuento(
            esValido = true,
            mensaje = descuentoStrategy.getMensaje(),
            subtotal = subtotal,
            descuentoAplicado = descuento,
            total = total,
            codigoDescuento = descuentoStrategy.getCodigoDescuento()
        )
    }

    /**
     * 🎯 STRATEGY PATTERN - Versión simplificada que establece y aplica en un solo paso
     * Para mantener compatibilidad con código existente
     */
    fun aplicarDescuentoConCodigo(subtotal: Double, codigoDescuento: String?): ResultadoDescuento {
        // Establece la estrategia según el código
        establecerEstrategiaDescuento(codigoDescuento)

        // Aplica el descuento usando la estrategia establecida
        return aplicarDescuento(subtotal)
    }

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

    fun crearPedido(
        nombreCliente: String,
        detalles: List<Array<Any>>,
        codigoDescuento: String? = null
    ): Result<Int> {
        return try {
            if (nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            if (nombreCliente.length > 100) {
                return Result.failure(Exception("El nombre del cliente no puede exceder 100 caracteres"))
            }

            if (detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

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
                    return Result.failure(Exception("Stock insuficiente para $nombreProducto. Stock disponible: $stock"))
                }
            }

            // 🎯 APLICAR STRATEGY DE DESCUENTO
            val subtotal = calcularTotalDetalles(detalles)

            // ✅ CORRECTO: Establece la estrategia y aplica el descuento
            val resultadoDescuento = aplicarDescuentoConCodigo(subtotal, codigoDescuento)
            val total = resultadoDescuento.total

            val idPedido = pedidoDao.insertar(
                nombreCliente = nombreCliente.trim(),
                fechaPedido = Timestamp(System.currentTimeMillis()),
                total = total
            )

            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            val detallesParaInsertar = detalles.map { detalle ->
                arrayOf<Any>(
                    idPedido,
                    detalle[0] as Int,
                    detalle[1] as Int,
                    detalle[2] as Double
                )
            }

            val resultadoDetalles = detallePedidoDao.insertarLote(detallesParaInsertar)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

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