package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivitySeleccionarProductoDecoradorBinding
import com.example.arquiprimerparcial.databinding.ItemsProductoBinding
import com.example.arquiprimerparcial.negocio.servicio.ProductoDecoradorServicio
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * 🎨 ACTIVIDAD: Seleccionar producto REAL y decorarlo
 *
 * FLUJO:
 * 1. Ver productos de tu BD
 * 2. Seleccionar uno (ej: Pollo Frito)
 * 3. Decorarlo con extras (Papas, Arroz, Queso, etc.)
 * 4. Agregarlo al pedido
 */
class SeleccionarProductoDecoradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarProductoDecoradorBinding
    private val productoServicio = ProductoServicio()
    private val decoradorServicio = ProductoDecoradorServicio(productoServicio)

    private lateinit var adapter: ProductoAdapter
    private var productoSeleccionado: Map<String, Any>? = null
    private val extrasSeleccionados = mutableListOf<ProductoDecoradorServicio.TipoExtra>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarProductoDecoradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initUI()
        cargarProductos()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "🎨 Decorar tu Producto"
            navigationIcon = AppCompatResources.getDrawable(
                this@SeleccionarProductoDecoradorActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.includeToolbar.ibAccion.isVisible = false
    }

    private fun initUI() {
        // RecyclerView para productos REALES
        adapter = ProductoAdapter { producto ->
            productoSeleccionado = producto
            mostrarOpcionesDecorado(producto)
        }

        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@SeleccionarProductoDecoradorActivity)
            adapter = this@SeleccionarProductoDecoradorActivity.adapter
        }

        binding.btnLimpiarSeleccion.setOnClickListener {
            productoSeleccionado = null
            extrasSeleccionados.clear()
            binding.chipGroupExtras.removeAllViews()
            actualizarResumen()
        }

        binding.btnAgregarAlPedido.setOnClickListener {
            if (productoSeleccionado != null) {
                confirmarAgregarProducto()
            } else {
                Toast.makeText(this, "⚠️ Selecciona un producto primero", Toast.LENGTH_SHORT).show()
            }
        }

        actualizarResumen()
    }

    private fun cargarProductos() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { productoServicio.listarProductosPrimitivos("") }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Success -> {
                    if (result.data.isEmpty()) {
                        Toast.makeText(
                            this@SeleccionarProductoDecoradorActivity,
                            "No hay productos disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        adapter.actualizarProductos(result.data)
                    }
                }
                is UiState.Error -> {
                    Toast.makeText(
                        this@SeleccionarProductoDecoradorActivity,
                        "Error: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun mostrarOpcionesDecorado(producto: Map<String, Any>) {
        val nombre = producto["nombre"] as String

        Toast.makeText(this, "✅ $nombre seleccionado", Toast.LENGTH_SHORT).show()

        // Limpiar extras previos
        extrasSeleccionados.clear()
        binding.chipGroupExtras.removeAllViews()

        // Agregar chips de extras
        val extras = decoradorServicio.listarExtrasDisponibles()

        for (extra in extras) {
            val chip = Chip(this).apply {
                text = "${extra["icono"]} ${extra["nombre"]} (+S/ ${decoradorServicio.formatearPrecio(extra["precio"] as Double)})"
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    val tipoExtra = extra["tipo"] as ProductoDecoradorServicio.TipoExtra
                    if (isChecked) {
                        extrasSeleccionados.add(tipoExtra)
                    } else {
                        extrasSeleccionados.remove(tipoExtra)
                    }
                    actualizarResumen()
                }
            }
            binding.chipGroupExtras.addView(chip)
        }

        binding.cardExtras.isVisible = true
        actualizarResumen()

        // Hacer scroll a los extras
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.cardExtras.top)
        }
    }

    private fun actualizarResumen() {
        if (productoSeleccionado == null) {
            binding.tvResumenProducto.text = "👆 Selecciona un producto de la lista para decorarlo"
            binding.tvPrecioTotal.text = "S/ 0.00"
            binding.btnAgregarAlPedido.isEnabled = false
            binding.cardExtras.isVisible = false
            return
        }

        binding.cardExtras.isVisible = true

        val nombre = productoSeleccionado!!["nombre"] as String
        val precioBase = productoSeleccionado!!["precio"] as Double

        var precioTotal = precioBase
        val descripcion = StringBuilder()
        descripcion.append("📦 $nombre\n")
        descripcion.append("   Precio base: S/ ${decoradorServicio.formatearPrecio(precioBase)}\n\n")

        if (extrasSeleccionados.isNotEmpty()) {
            descripcion.append("➕ Extras agregados:\n")
            for (extra in extrasSeleccionados) {
                val precioExtra = decoradorServicio.obtenerPrecioExtra(extra)
                precioTotal += precioExtra
                descripcion.append("   • ${getNombreExtra(extra)} (+S/ ${decoradorServicio.formatearPrecio(precioExtra)})\n")
            }
        } else {
            descripcion.append("⬇️ Selecciona extras abajo o agrégalo así")
        }

        binding.tvResumenProducto.text = descripcion.toString()
        binding.tvPrecioTotal.text = "S/ ${decoradorServicio.formatearPrecio(precioTotal)}"
        binding.btnAgregarAlPedido.isEnabled = true
    }

    private fun getNombreExtra(tipo: ProductoDecoradorServicio.TipoExtra): String {
        return when (tipo) {
            ProductoDecoradorServicio.TipoExtra.PAPAS -> "🍟 Papas Fritas"
            ProductoDecoradorServicio.TipoExtra.REFRESCO -> "🥤 Refresco"
            ProductoDecoradorServicio.TipoExtra.ARROZ -> "🍚 Arroz"
            ProductoDecoradorServicio.TipoExtra.QUESO -> "🧀 Queso"
            ProductoDecoradorServicio.TipoExtra.TOCINO -> "🥓 Tocino"
        }
    }

    private fun confirmarAgregarProducto() {
        val producto = productoSeleccionado ?: return
        val nombre = producto["nombre"] as String
        val precioBase = producto["precio"] as Double

        var precioTotal = precioBase
        val descripcion = StringBuilder()
        descripcion.append(nombre)

        if (extrasSeleccionados.isNotEmpty()) {
            for (extra in extrasSeleccionados) {
                val precioExtra = decoradorServicio.obtenerPrecioExtra(extra)
                precioTotal += precioExtra
                descripcion.append(" + ${getNombreExtra(extra)}")
            }
        }

        val mensaje = buildString {
            append("¿Agregar al pedido?\n\n")
            append("${descripcion}\n\n")
            append("Total: S/ ${decoradorServicio.formatearPrecio(precioTotal)}")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Producto Decorado")
            .setMessage(mensaje)
            .setPositiveButton("✅ Agregar") { _, _ ->
                agregarAlPedido(descripcion.toString(), precioTotal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarAlPedido(nombreCompleto: String, precioTotal: Double) {
        val producto = productoSeleccionado ?: return
        val idProducto = producto["id"] as Int

        val intent = android.content.Intent()
        intent.putExtra("producto_id", idProducto)  // ✅ IMPORTANTE: Agregar ID
        intent.putExtra("producto_nombre", nombreCompleto)
        intent.putExtra("producto_precio", precioTotal)
        intent.putExtra("producto_descripcion", "Producto personalizado con decoradores")
        setResult(RESULT_OK, intent)

        Toast.makeText(
            this,
            "✅ $nombreCompleto agregado al pedido",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    /**
     * Adapter para mostrar productos REALES de la BD
     */
    inner class ProductoAdapter(
        private val onClick: (Map<String, Any>) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapter.ViewHolder>() {

        private val productos = mutableListOf<Map<String, Any>>()

        inner class ViewHolder(private val binding: ItemsProductoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(producto: Map<String, Any>) {
                val nombre = producto["nombre"] as String
                val precio = producto["precio"] as Double
                val stock = producto["stock"] as Int
                val categoria = producto["categoriaNombre"] as? String ?: "Sin categoría"

                binding.tvNombre.text = nombre
                binding.tvTitulo.text = nombre
                binding.tvPrecio.text = "S/ ${"%.2f".format(precio)}"
                binding.tvStock.text = "Stock: $stock"
                binding.tvCategoria.text = "🏷️ $categoria"

                binding.btnSeleccionar.text = "🎨 Decorar"
                binding.btnSeleccionar.isEnabled = stock > 0

                // Ocultar botones de editar/eliminar
                binding.ibEditar.isVisible = false
                binding.ibEliminar.isVisible = false

                binding.btnSeleccionar.setOnClickListener {
                    if (stock > 0) {
                        onClick(producto)
                    }
                }

                binding.root.setOnClickListener {
                    if (stock > 0) {
                        onClick(producto)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemsProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount() = productos.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(productos[position])
        }

        fun actualizarProductos(nuevosProductos: List<Map<String, Any>>) {
            productos.clear()
            productos.addAll(nuevosProductos)
            notifyDataSetChanged()
        }
    }
}