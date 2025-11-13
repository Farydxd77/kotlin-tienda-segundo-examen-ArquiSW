package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivitySeleccionarProductoDecoradorBinding
import com.example.arquiprimerparcial.decorator.ProductoComponente
import com.example.arquiprimerparcial.negocio.servicio.ProductoDecoradorServicio
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 🎨 ACTIVITY: Seleccionar y Decorar Productos
 * Permite elegir un producto base y agregarle extras usando el patrón Decorator
 */
class SeleccionarProductoDecoradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarProductoDecoradorBinding
    private val decoradorServicio = ProductoDecoradorServicio()

    private var productoBaseSeleccionado: ProductoDecoradorServicio.TipoProductoBase? = null
    private val extrasSeleccionados = mutableListOf<ProductoDecoradorServicio.TipoExtra>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarProductoDecoradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initUI()
        initListeners()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "🎨 Decorar Producto"
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
        // Cargar productos base
        cargarProductosBase()

        // Cargar extras disponibles
        cargarExtrasDisponibles()

        actualizarResumen()
    }

    private fun cargarProductosBase() {
        val productos = decoradorServicio.listarProductosBase()

        binding.chipGroupProductosBase.removeAllViews()

        for (producto in productos) {
            val chip = Chip(this).apply {
                text = "${producto["icono"]} ${producto["nombre"]} - S/ ${decoradorServicio.formatearPrecio(producto["precio"] as Double)}"
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        productoBaseSeleccionado = producto["tipo"] as ProductoDecoradorServicio.TipoProductoBase
                        // Desmarcar otros chips
                        for (i in 0 until binding.chipGroupProductosBase.childCount) {
                            val otherChip = binding.chipGroupProductosBase.getChildAt(i) as Chip
                            if (otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                        actualizarResumen()
                    } else if (productoBaseSeleccionado == producto["tipo"]) {
                        productoBaseSeleccionado = null
                        actualizarResumen()
                    }
                }
            }
            binding.chipGroupProductosBase.addView(chip)
        }
    }

    private fun cargarExtrasDisponibles() {
        val extras = decoradorServicio.listarExtrasDisponibles()

        binding.chipGroupExtras.removeAllViews()

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
    }

    private fun initListeners() {
        binding.btnAgregarAlPedido.setOnClickListener {
            if (productoBaseSeleccionado == null) {
                Toast.makeText(this, "⚠️ Selecciona un producto base", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            confirmarAgregarProducto()
        }

        binding.btnLimpiarSeleccion.setOnClickListener {
            limpiarSeleccion()
        }
    }

    private fun actualizarResumen() {
        if (productoBaseSeleccionado == null) {
            binding.tvResumenProducto.text = "Selecciona un producto base"
            binding.tvPrecioTotal.text = "S/ 0.00"
            binding.btnAgregarAlPedido.isEnabled = false
            return
        }

        // 🎨 USAR EL PATRÓN DECORATOR
        val productoDecorado = decoradorServicio.crearProductoDecorado(
            productoBaseSeleccionado!!,
            extrasSeleccionados
        )

        val resumen = decoradorServicio.obtenerResumenProducto(productoDecorado)

        binding.tvResumenProducto.text = buildString {
            append("📦 ${resumen["nombre"]}\n\n")
            append("${resumen["descripcion"]}")
        }

        binding.tvPrecioTotal.text = resumen["precioFormateado"] as String
        binding.btnAgregarAlPedido.isEnabled = true
    }

    private fun confirmarAgregarProducto() {
        val productoDecorado = decoradorServicio.crearProductoDecorado(
            productoBaseSeleccionado!!,
            extrasSeleccionados
        )

        val mensaje = buildString {
            append("¿Agregar al pedido?\n\n")
            append("${productoDecorado.obtenerNombre()}\n")
            append("${productoDecorado.obtenerDescripcion()}\n\n")
            append("Total: S/ ${decoradorServicio.formatearPrecio(productoDecorado.obtenerPrecio())}")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Producto")
            .setMessage(mensaje)
            .setPositiveButton("Agregar") { _, _ ->
                agregarAlPedido(productoDecorado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarAlPedido(producto: ProductoComponente) {
        // Aquí enviarías el producto al pedido
        // Por ahora solo mostramos un mensaje

        val intent = intent
        intent.putExtra("producto_nombre", producto.obtenerNombre())
        intent.putExtra("producto_precio", producto.obtenerPrecio())
        intent.putExtra("producto_descripcion", producto.obtenerDescripcion())
        setResult(RESULT_OK, intent)

        Toast.makeText(
            this,
            "✅ ${producto.obtenerNombre()} agregado al pedido",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun limpiarSeleccion() {
        // Limpiar producto base
        for (i in 0 until binding.chipGroupProductosBase.childCount) {
            (binding.chipGroupProductosBase.getChildAt(i) as Chip).isChecked = false
        }
        productoBaseSeleccionado = null

        // Limpiar extras
        for (i in 0 until binding.chipGroupExtras.childCount) {
            (binding.chipGroupExtras.getChildAt(i) as Chip).isChecked = false
        }
        extrasSeleccionados.clear()

        actualizarResumen()
    }
}