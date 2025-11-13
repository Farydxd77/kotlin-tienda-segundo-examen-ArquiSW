package com.example.arquiprimerparcial.presentacion.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.arquiprimerparcial.databinding.ActivityDetallePedidoBinding
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.impl.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * 🔄 DETALLE DEL PEDIDO
 * Aquí se cambian los estados usando STATE pattern
 */
class DetallePedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePedidoBinding
    private val pedidoServicio = PedidoServicio()

    private var contextoPedido: ContextoPedido? = null
    private var idPedido: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPedido = intent.getIntExtra("idPedido", 0)

        cargarPedido()
        initListeners()
    }

    private fun cargarPedido() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { pedidoServicio.obtenerPedidoPorId(idPedido) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Success -> {
                    val pedido = result.data

                    if (pedido == null) {
                        Toast.makeText(this@DetallePedidoActivity, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
                        finish()
                        return@let
                    }

                    // Crear contexto con estado actual
                    val estadoActual = obtenerEstadoPorNombre(pedido.estado)

                    contextoPedido = ContextoPedido(
                        idPedido = pedido.id,
                        nombreCliente = pedido.nombreCliente,
                        total = pedido.total,
                        estado = estadoActual
                    )

                    mostrarDatos(pedido)
                    actualizarUI()
                }
                is UiState.Error -> {
                    Toast.makeText(this@DetallePedidoActivity, result.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun mostrarDatos(pedido: com.example.arquiprimerparcial.model.Pedido) {
        binding.tvNumeroPedido.text = "Pedido #${pedido.id.toString().padStart(4, '0')}"
        binding.tvNombreCliente.text = pedido.nombreCliente
        binding.tvTotal.text = "Total: S/ ${"%.2f".format(pedido.total)}"
        binding.tvFecha.text = pedido.fechaPedido.toString().substring(0, 10)
    }

    private fun initListeners() {
        // 🔄 BOTÓN: Comenzar Preparación
        binding.btnComenzarPreparacion.setOnClickListener {
            ejecutarOperacion("comenzar preparación") { ctx ->
                ctx.comenzarPreparacion()
            }
        }

        // 🔄 BOTÓN: Marcar como Listo
        binding.btnMarcarListo.setOnClickListener {
            ejecutarOperacion("marcar como listo") { ctx ->
                ctx.marcarListo()
            }
        }

        // 🔄 BOTÓN: Confirmar Entrega
        binding.btnConfirmarEntrega.setOnClickListener {
            ejecutarOperacion("confirmar entrega") { ctx ->
                ctx.confirmarEntrega()
            }
        }

        // 🔄 BOTÓN: Cancelar
        binding.btnCancelar.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí") { _, _ ->
                    ejecutarOperacion("cancelar") { ctx ->
                        ctx.cancelar()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Ver historial
        binding.btnVerHistorial.setOnClickListener {
            mostrarHistorial()
        }
    }

    /**
     * 🔄 Ejecuta una operación delegando al contexto
     * El contexto delega al estado actual
     * El estado decide si puede hacerla
     */
    private fun ejecutarOperacion(
        nombreOperacion: String,
        operacion: (ContextoPedido) -> Unit
    ) {
        val ctx = contextoPedido ?: return

        val estadoAntes = ctx.obtenerEstadoActual().obtenerNombre()

        // ✅ DELEGAR al contexto → contexto delega al estado
        operacion(ctx)

        val estadoDespues = ctx.obtenerEstadoActual().obtenerNombre()

        // Si cambió, actualizar en BD
        if (estadoAntes != estadoDespues) {
            lifecycleScope.launch {
                val resultado = pedidoServicio.actualizarEstadoPedido(idPedido, estadoDespues)

                if (resultado.isSuccess) {
                    Toast.makeText(
                        this@DetallePedidoActivity,
                        "✅ $estadoDespues",
                        Toast.LENGTH_SHORT
                    ).show()

                    actualizarUI()
                } else {
                    Toast.makeText(
                        this@DetallePedidoActivity,
                        "❌ Error al actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                this,
                "⚠️ No se puede $nombreOperacion en este estado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 🎨 Actualizar UI según el estado actual
     */
    private fun actualizarUI() {
        val ctx = contextoPedido ?: return
        val estado = ctx.obtenerEstadoActual()

        // Mostrar estado actual
        binding.tvEstadoActual.text = "${estado.obtenerIcono()} ${estado.obtenerNombre()}"
        binding.tvDescripcion.text = estado.obtenerDescripcion()

        // Color del card
        val color = Color.parseColor(estado.obtenerColor())
        binding.cardEstado.setCardBackgroundColor(color)

        // Barra de progreso
        binding.progressBar2.progress = estado.obtenerProgreso()
        binding.tvProgreso.text = "${estado.obtenerProgreso()}%"

        // Botones según estado
        when (estado.obtenerNombre()) {
            "PENDIENTE" -> {
                binding.btnComenzarPreparacion.isVisible = true
                binding.btnMarcarListo.isVisible = false
                binding.btnConfirmarEntrega.isVisible = false
                binding.btnCancelar.isEnabled = true
            }
            "PREPARANDO" -> {
                binding.btnComenzarPreparacion.isVisible = false
                binding.btnMarcarListo.isVisible = true
                binding.btnConfirmarEntrega.isVisible = false
                binding.btnCancelar.isEnabled = true
            }
            "LISTO" -> {
                binding.btnComenzarPreparacion.isVisible = false
                binding.btnMarcarListo.isVisible = false
                binding.btnConfirmarEntrega.isVisible = true
                binding.btnCancelar.isEnabled = false
            }
            "ENTREGADO" -> {
                binding.btnComenzarPreparacion.isVisible = false
                binding.btnMarcarListo.isVisible = false
                binding.btnConfirmarEntrega.isVisible = false
                binding.btnCancelar.isEnabled = false
            }
            "CANCELADO" -> {
                binding.btnComenzarPreparacion.isVisible = false
                binding.btnMarcarListo.isVisible = false
                binding.btnConfirmarEntrega.isVisible = false
                binding.btnCancelar.isEnabled = false
            }
        }
    }

    private fun mostrarHistorial() {
        val ctx = contextoPedido ?: return
        val historial = ctx.obtenerHistorial()

        val mensaje = buildString {
            append("📋 HISTORIAL\n\n")
            historial.forEachIndexed { index, estado ->
                append("${index + 1}. $estado\n")
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Historial de Estados")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun obtenerEstadoPorNombre(nombre: String): com.example.arquiprimerparcial.state.EstadoPedido {
        return when (nombre.uppercase()) {
            "PENDIENTE" -> EstadoPendiente()
            "PREPARANDO" -> EstadoPreparando()
            "LISTO" -> EstadoListo()
            "ENTREGADO" -> EstadoEntregado()
            "CANCELADO" -> EstadoCancelado()
            else -> EstadoPendiente()
        }
    }
}