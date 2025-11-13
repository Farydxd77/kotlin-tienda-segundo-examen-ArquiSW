import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.EstadoPedido

class EstadoCancelado : EstadoPedido {

    override fun comenzarPreparacion(contexto: ContextoPedido) {
        // Está cancelado
    }

    override fun marcarListo(contexto: ContextoPedido) {
        // Está cancelado
    }

    override fun confirmarEntrega(contexto: ContextoPedido) {
        // Está cancelado
    }

    override fun cancelar(contexto: ContextoPedido) {
        // Ya está cancelado
    }

    override fun obtenerNombre() = "CANCELADO"
    override fun obtenerDescripcion() = "Pedido cancelado"
    override fun obtenerIcono() = "❌"
    override fun obtenerColor() = "#F44336"  // Rojo
    override fun obtenerProgreso() = 0
}