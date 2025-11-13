package com.example.arquiprimerparcial.presentacion.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.databinding.ActivityListaPedidosBinding
import com.example.arquiprimerparcial.databinding.ItemPedidoEstadoBinding
import com.example.arquiprimerparcial.model.Pedido
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.example.arquiprimerparcial.state.impl.*
import kotlinx.coroutines.launch

/**
 * 🔄 LISTA DE PEDIDOS CON ESTADOS
 * Muestra todos los pedidos con su estado visual
 */
class ListaPedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaPedidosBinding
    private val pedidoServicio = PedidoServicio()
    private lateinit var adapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        cargarPedidos()
    }

    private fun initRecyclerView() {
        adapter = PedidoAdapter { pedido ->
            // Click en un pedido → Ir a detalle
            val intent = Intent(this, DetallePedidoActivity::class.java)
            intent.putExtra("idPedido", pedido.id)
            startActivity(intent)
        }

        binding.rvPedidos.layoutManager = LinearLayoutManager(this)
        binding.rvPedidos.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarPedidos()  // Recargar cuando volvemos
    }

    private fun cargarPedidos() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { pedidoServicio.listarPedidos() }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Success -> {
                    adapter.actualizarPedidos(result.data)

                    if (result.data.isEmpty()) {
                        binding.tvVacio.isVisible = true
                    } else {
                        binding.tvVacio.isVisible = false
                    }
                }
                is UiState.Error -> {
                    binding.tvVacio.text = "Error: ${result.message}"
                    binding.tvVacio.isVisible = true
                }
            }
        }
    }

    /**
     * Adapter para mostrar pedidos con estado visual
     */
    inner class PedidoAdapter(
        private val onClick: (Pedido) -> Unit
    ) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

        private val pedidos = mutableListOf<Pedido>()

        inner class ViewHolder(
            private val binding: ItemPedidoEstadoBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(pedido: Pedido) {
                // 🔄 Obtener estado para info visual
                val estado = obtenerEstadoPorNombre(pedido.estado)

                // Datos del pedido
                binding.tvNumeroPedido.text = "Pedido #${pedido.id.toString().padStart(4, '0')}"
                binding.tvNombreCliente.text = pedido.nombreCliente
                binding.tvTotal.text = "S/ ${"%.2f".format(pedido.total)}"
                binding.tvFecha.text = pedido.fechaPedido.toString().substring(0, 10)

                // 🔄 Aplicar estado visual
                binding.tvEstado.text = "${estado.obtenerIcono()} ${estado.obtenerNombre()}"

                val color = Color.parseColor(estado.obtenerColor())
                binding.cardEstado.setCardBackgroundColor(color)

                // Barra de progreso
                binding.progressBar.progress = estado.obtenerProgreso()
                binding.tvProgreso.text = "${estado.obtenerProgreso()}%"

                // Click
                binding.root.setOnClickListener {
                    onClick(pedido)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemPedidoEstadoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(pedidos[position])
        }

        override fun getItemCount() = pedidos.size

        fun actualizarPedidos(nuevosPedidos: List<Pedido>) {
            pedidos.clear()
            pedidos.addAll(nuevosPedidos)
            notifyDataSetChanged()
        }
    }

    /**
     * 🔄 Mapea nombre del estado a objeto State
     */
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