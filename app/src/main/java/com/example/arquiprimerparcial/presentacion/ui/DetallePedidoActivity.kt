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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * DETALLE DEL PEDIDO
 * Muestra información del pedido
 */
class DetallePedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePedidoBinding
    private val pedidoServicio = PedidoServicio()

    private var idPedido: Int = 0
    private var estadoActual: String = "PENDIENTE"

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

                    estadoActual = pedido.estado
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

    private fun mostrarDatos(pedido: com.example.arquiprimerparcial.data.dao.Pedido) {
        binding.tvNumeroPedido.text = "Pedido #${pedido.id.toString().padStart(4, '0')}"
        binding.tvNombreCliente.text = pedido.nombreCliente
        binding.tvTotal.text = "Total: S/ ${"%.2f".format(pedido.total)}"
        binding.tvFecha.text = pedido.fechaPedido.toString().substring(0, 10)
    }

    private fun initListeners() {
        binding.btnComenzarPreparacion.setOnClickListener {
            cambiarEstado("PREPARANDO")
        }

        binding.btnMarcarListo.setOnClickListener {
            cambiarEstado("LISTO")
        }

        binding.btnConfirmarEntrega.setOnClickListener {
            cambiarEstado("ENTREGADO")
        }

        binding.btnCancelar.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar Pedido")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí") { _, _ ->
                    cambiarEstado("CANCELADO")
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.btnVerHistorial.setOnClickListener {
            Toast.makeText(this, "Estado actual: $estadoActual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cambiarEstado(nuevoEstado: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val resultado = pedidoServicio.actualizarEstadoPedido(idPedido, nuevoEstado)

        binding.progressBar.isVisible = false

        if (resultado.isSuccess) {
            estadoActual = nuevoEstado
            Toast.makeText(
                this@DetallePedidoActivity,
                "✅ Estado actualizado: $nuevoEstado",
                Toast.LENGTH_SHORT
            ).show()
            actualizarUI()
        } else {
            Toast.makeText(
                this@DetallePedidoActivity,
                "❌ Error al actualizar estado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun actualizarUI() {
        // Información del estado
        val (icono, nombre, descripcion, color, progreso) = when (estadoActual.uppercase()) {
            "PENDIENTE" -> listOf("🛒", "PENDIENTE", "Pedido recibido, esperando preparación", "#2196F3", 25)
            "PREPARANDO" -> listOf("📦", "PREPARANDO", "Empaquetando tu pedido", "#FF9800", 50)
            "LISTO" -> listOf("✅", "LISTO", "¡Listo para recoger!", "#9C27B0", 75)
            "ENTREGADO" -> listOf("🎉", "ENTREGADO", "¡Pedido completado! Gracias 😊", "#4CAF50", 100)
            "CANCELADO" -> listOf("❌", "CANCELADO", "Pedido cancelado", "#F44336", 0)
            else -> listOf("🛒", "PENDIENTE", "Pedido recibido", "#2196F3", 25)
        }

        binding.tvEstadoActual.text = "$icono $nombre"
        binding.tvDescripcion.text = descripcion as String

        val colorInt = Color.parseColor(color as String)
        binding.cardEstado.setCardBackgroundColor(colorInt)

        binding.progressBar2.progress = progreso as Int
        binding.tvProgreso.text = "$progreso%"

        // Botones según estado
        when (estadoActual.uppercase()) {
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
}