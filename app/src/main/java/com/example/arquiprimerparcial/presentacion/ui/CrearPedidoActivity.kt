package com.example.arquiprimerparcial.presentacion.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityCrearPedidoBinding
import com.example.arquiprimerparcial.databinding.ItemsProductoBinding
import com.example.arquiprimerparcial.databinding.ItemsPedidoDetalleBinding
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CrearPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPedidoBinding
    private val pedidoServicio: PedidoServicio = PedidoServicio()
    private val productoServicio: ProductoServicio = ProductoServicio()

    private var listaProductos = mutableListOf<Map<String, Any>>()
    private var detallesPedido = mutableListOf<Map<String, Any>>()

    // 🎯 STRATEGY - Variables para descuento
    private var subtotalOriginal = 0.0
    private var descuentoAplicado = 0.0
    private var totalConDescuento = 0.0
    private var codigoDescuentoActual: String? = null
    private var cantidadTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initUI()
        initListeners()
        cargarProductos()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Crear nuevo pedido"
            navigationIcon = AppCompatResources.getDrawable(
                this@CrearPedidoActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.includeToolbar.ibAccion.isVisible = false
    }

    private fun initUI() {
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = ProductoAdapter()
        }

        binding.rvDetallesPedido.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = DetalleAdapter()
        }

        actualizarResumen()
    }

    private fun initListeners() {
        binding.btnFinalizarPedido.setOnClickListener {
            if (validarPedido()) {
                confirmarPedido()
            }
        }

        binding.btnLimpiarPedido.setOnClickListener {
            limpiarPedido()
        }

        // 🎯 NUEVO: Botón para aplicar descuento
        binding.btnAplicarDescuento.setOnClickListener {
            mostrarDialogoDescuentos()
        }

        binding.etBuscarProducto.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                cargarProductos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ==========================================
    // 🎯 STRATEGY PATTERN - Aplicar Descuentos
    // ==========================================

    private fun mostrarDialogoDescuentos() {
        if (detallesPedido.isEmpty()) {
            mostrarError("Agrega productos al pedido primero")
            return
        }

        val opciones = arrayOf(
            "🎄 NAVIDAD2024 - 15% OFF (mínimo S/ 50)",
            "🔥 BLACKFRIDAY - 30-40% OFF",
            "🎉 BIENVENIDA - S/ 10 OFF (mínimo S/ 30)",
            "❌ Quitar descuento"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Aplicar Código de Descuento")
            .setItems(opciones) { _, which ->
                val codigo = when (which) {
                    0 -> "NAVIDAD2024"
                    1 -> "BLACKFRIDAY"
                    2 -> "BIENVENIDA"
                    else -> null // Sin descuento
                }

                aplicarDescuento(codigo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aplicarDescuento(codigo: String?) {
        codigoDescuentoActual = codigo

        // Calcular subtotal sin descuento
        subtotalOriginal = 0.0
        for (detalle in detallesPedido) {
            subtotalOriginal += (detalle["subtotal"] as Double)
        }

        // 🎯 Usar el STRATEGY PATTERN
        val resultado = pedidoServicio.aplicarDescuento(subtotalOriginal, codigo)

        if (resultado.esValido) {
            descuentoAplicado = resultado.descuentoAplicado
            totalConDescuento = resultado.total

            // Mostrar info del descuento
            val mensaje = buildString {
                append("✅ ${resultado.mensaje}\n\n")
                append("Subtotal: S/ ${"%.2f".format(resultado.subtotal)}\n")
                append("Descuento: -S/ ${"%.2f".format(resultado.descuentoAplicado)}\n")
                append("─────────────\n")
                append("TOTAL: S/ ${"%.2f".format(resultado.total)}")
            }

            binding.tvDescuentoInfo.text = mensaje
            binding.tvDescuentoInfo.isVisible = true

            android.widget.Toast.makeText(
                this,
                "✅ Descuento aplicado",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            descuentoAplicado = 0.0
            totalConDescuento = subtotalOriginal

            binding.tvDescuentoInfo.text = "⚠️ ${resultado.mensaje}"
            binding.tvDescuentoInfo.isVisible = true

            android.widget.Toast.makeText(
                this,
                "⚠️ ${resultado.mensaje}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        actualizarResumen()
    }

    // ==========================================
    // ADAPTADORES (sin cambios)
    // ==========================================

    inner class ProductoAdapter : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

        inner class ProductoViewHolder(private val binding: ItemsProductoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun enlazar(producto: Map<String, Any>) {
                val id = producto["id"] as Int
                val nombre = producto["nombre"] as String
                val precio = producto["precio"] as Double
                val stock = producto["stock"] as Int

                binding.tvNombre.text = nombre
                binding.tvPrecio.text = "S/ ${"%.2f".format(precio)}"
                binding.tvStock.text = "Stock: $stock"

                binding.btnSeleccionar.isEnabled = stock > 0
                binding.btnSeleccionar.text = if (stock > 0) "Agregar" else "Sin Stock"

                binding.btnSeleccionar.setOnClickListener {
                    if (stock > 0) {
                        mostrarDialogoCantidad(producto)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
            return ProductoViewHolder(
                ItemsProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = listaProductos.size

        override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
            holder.enlazar(listaProductos[position])
        }
    }

    inner class DetalleAdapter : RecyclerView.Adapter<DetalleAdapter.DetalleViewHolder>() {

        inner class DetalleViewHolder(private val binding: ItemsPedidoDetalleBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun enlazar(detalle: Map<String, Any>) {
                val idProducto = detalle["idProducto"] as Int
                val nombreProducto = detalle["nombreProducto"] as String
                val precioUnitario = detalle["precioUnitario"] as Double
                val cantidad = detalle["cantidad"] as Int
                val subtotal = detalle["subtotal"] as Double

                binding.tvNombreProducto.text = nombreProducto
                binding.tvPrecioUnitario.text = "S/ ${"%.2f".format(precioUnitario)}"
                binding.tvCantidad.text = cantidad.toString()
                binding.tvSubtotal.text = "S/ ${"%.2f".format(subtotal)}"

                binding.btnMenos.setOnClickListener {
                    if (cantidad > 1) {
                        modificarCantidad(idProducto, cantidad - 1)
                    }
                }

                binding.btnMas.setOnClickListener {
                    modificarCantidad(idProducto, cantidad + 1)
                }

                binding.btnEliminar.setOnClickListener {
                    eliminarDetalle(idProducto)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
            return DetalleViewHolder(
                ItemsPedidoDetalleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = detallesPedido.size

        override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
            holder.enlazar(detallesPedido[position])
        }
    }

    private fun mostrarDialogoCantidad(producto: Map<String, Any>) {
        val nombre = producto["nombre"] as String
        val stock = producto["stock"] as Int

        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Cantidad"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar $nombre")
            .setMessage("Stock disponible: $stock")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val cantidadStr = input.text.toString()
                if (cantidadStr.isNotEmpty()) {
                    val cantidad = cantidadStr.toIntOrNull() ?: 0
                    if (cantidad > 0 && cantidad <= stock) {
                        agregarDetalle(producto, cantidad)
                    } else {
                        mostrarError("Cantidad inválida o mayor al stock disponible")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarDetalle(producto: Map<String, Any>, cantidad: Int) {
        val idProducto = producto["id"] as Int
        val nombre = producto["nombre"] as String
        val precio = producto["precio"] as Double

        val detalleExistente = detallesPedido.find { it["idProducto"] == idProducto }

        if (detalleExistente != null) {
            val cantidadActual = detalleExistente["cantidad"] as Int
            modificarCantidad(idProducto, cantidadActual + cantidad)
        } else {
            val detalle = mapOf(
                "idProducto" to idProducto,
                "nombreProducto" to nombre,
                "precioUnitario" to precio,
                "cantidad" to cantidad,
                "subtotal" to (precio * cantidad)
            )
            detallesPedido.add(detalle)
        }

        // 🎯 Recalcular descuento si hay uno aplicado
        if (codigoDescuentoActual != null) {
            aplicarDescuento(codigoDescuentoActual)
        }

        actualizarUI()
    }

    private fun modificarCantidad(idProducto: Int, nuevaCantidad: Int) {
        val index = detallesPedido.indexOfFirst { it["idProducto"] == idProducto }
        if (index != -1) {
            val detalle = detallesPedido[index].toMutableMap()
            val precio = detalle["precioUnitario"] as Double
            detalle["cantidad"] = nuevaCantidad
            detalle["subtotal"] = precio * nuevaCantidad
            detallesPedido[index] = detalle

            // 🎯 Recalcular descuento
            if (codigoDescuentoActual != null) {
                aplicarDescuento(codigoDescuentoActual)
            }

            actualizarUI()
        }
    }

    private fun eliminarDetalle(idProducto: Int) {
        detallesPedido.removeAll { it["idProducto"] == idProducto }

        // 🎯 Recalcular descuento
        if (detallesPedido.isEmpty()) {
            codigoDescuentoActual = null
            descuentoAplicado = 0.0
            binding.tvDescuentoInfo.isVisible = false
        } else if (codigoDescuentoActual != null) {
            aplicarDescuento(codigoDescuentoActual)
        }

        actualizarUI()
    }

    private fun cargarProductos(filtro: String = "") = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        makeCall { productoServicio.listarProductosPrimitivos(filtro) }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    listaProductos.clear()
                    listaProductos.addAll(result.data)
                    binding.rvProductos.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun actualizarUI() {
        binding.rvDetallesPedido.adapter?.notifyDataSetChanged()
        actualizarResumen()
    }

    private fun actualizarResumen() {
        // Calcular subtotal
        subtotalOriginal = 0.0
        cantidadTotal = 0

        for (detalle in detallesPedido) {
            subtotalOriginal += (detalle["subtotal"] as Double)
            cantidadTotal += (detalle["cantidad"] as Int)
        }

        // Si no hay descuento aplicado, el total es el subtotal
        if (codigoDescuentoActual == null) {
            totalConDescuento = subtotalOriginal
            descuentoAplicado = 0.0
        }

        // 🎯 Mostrar con descuento si existe
        if (descuentoAplicado > 0) {
            binding.tvTotalPedido.text = buildString {
                append("Subtotal: S/ ${"%.2f".format(subtotalOriginal)}\n")
                append("Descuento: -S/ ${"%.2f".format(descuentoAplicado)}\n")
                append("TOTAL: S/ ${"%.2f".format(totalConDescuento)}")
            }
        } else {
            binding.tvTotalPedido.text = "Total: S/ ${"%.2f".format(totalConDescuento)}"
        }

        binding.tvCantidadProductos.text = "$cantidadTotal productos"
        binding.btnFinalizarPedido.isEnabled = detallesPedido.isNotEmpty()
    }

    private fun validarPedido(): Boolean {
        val nombreCliente = binding.etNombreCliente.text.toString().trim()

        when {
            nombreCliente.isEmpty() -> {
                binding.etNombreCliente.error = "Ingrese el nombre del cliente"
                return false
            }
            detallesPedido.isEmpty() -> {
                mostrarError("Agregue al menos un producto al pedido")
                return false
            }
        }
        return true
    }

//    private fun confirmarPedido() = lifecycleScope.launch {
//        binding.progressBar.isVisible = true
//        val nombreCliente = binding.etNombreCliente.text.toString().trim()
//
//        val pedidoData = mapOf(
//            "nombreCliente" to nombreCliente,
//            "detalles" to detallesPedido,
//            // 🎯 IMPORTANTE: Usar el total CON descuento
//            "total" to totalConDescuento,
//            "codigoDescuento" to (codigoDescuentoActual ?: ""),
//            "subtotal" to subtotalOriginal,
//            "descuento" to descuentoAplicado
//        )
//
//        makeCall { pedidoServicio.crearPedidoPrimitivo(pedidoData) }.let { result ->
//            binding.progressBar.isVisible = false
//
//            when (result) {
//                is UiState.Error -> mostrarError(result.message)
//                is UiState.Success -> {
//                    if (result.data.isSuccess) {
//                        val pedidoId = result.data.getOrNull() ?: 0
//
//                        val mensaje = buildString {
//                            append("✅ Pedido #$pedidoId creado exitosamente\n\n")
//                            if (descuentoAplicado > 0) {
//                                append("💰 Ahorro: S/ ${"%.2f".format(descuentoAplicado)}\n")
//                            }
//                            append("Total pagado: S/ ${"%.2f".format(totalConDescuento)}")
//                        }
//
//                        mostrarExito(mensaje)
//                        limpiarPedido()
//                    } else {
//                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error al crear pedido")
//                    }
//                }
//            }
//        }
//    }

    private fun limpiarPedido() {
        detallesPedido.clear()
        binding.etNombreCliente.text?.clear()
        codigoDescuentoActual = null
        descuentoAplicado = 0.0
        subtotalOriginal = 0.0
        totalConDescuento = 0.0
        binding.tvDescuentoInfo.isVisible = false
        actualizarUI()
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarExito(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Éxito")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    // 🔥 AGREGAR EN CrearPedidoActivity al final del método confirmarPedido()

    private fun confirmarPedido() = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        val nombreCliente = binding.etNombreCliente.text.toString().trim()

        val pedidoData = mapOf(
            "nombreCliente" to nombreCliente,
            "detalles" to detallesPedido,
            "total" to totalConDescuento,
            "codigoDescuento" to (codigoDescuentoActual ?: ""),
            "subtotal" to subtotalOriginal,
            "descuento" to descuentoAplicado,
            "estado" to "PENDIENTE"  // 🔄 IMPORTANTE: Estado inicial
        )

        makeCall { pedidoServicio.crearPedidoPrimitivo(pedidoData) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        val pedidoId = result.data.getOrNull() ?: 0

                        val mensaje = buildString {
                            append("✅ Pedido #$pedidoId creado\n\n")
                            append("Estado: 🛒 PENDIENTE\n")
                            if (descuentoAplicado > 0) {
                                append("Ahorro: S/ ${"%.2f".format(descuentoAplicado)}\n")
                            }
                            append("Total: S/ ${"%.2f".format(totalConDescuento)}")
                        }

                        MaterialAlertDialogBuilder(this@CrearPedidoActivity)
                            .setTitle("Pedido Creado")
                            .setMessage(mensaje)
                            .setPositiveButton("Ver Pedidos") { _, _ ->
                                // 🔥 IR A LISTA DE PEDIDOS
                                val intent = Intent(this@CrearPedidoActivity, ListaPedidosActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .setNegativeButton("Crear Otro") { _, _ ->
                                limpiarPedido()
                            }
                            .show()
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error al crear pedido")
                    }
                }
            }
        }
    }

}