import com.example.arquiprimerparcial.state.ContextoPedido
import com.example.arquiprimerparcial.state.EstadoPedido

class EstadoPendiente : EstadoPedido {

    override fun comenzarPreparacion(contexto: ContextoPedido) {
        // ✅ TRANSICIÓN: PENDIENTE → PREPARANDO
        contexto.cambiarEstado(EstadoPreparando())
    }

    override fun marcarListo(contexto: ContextoPedido) {
        // No se puede marcar listo sin preparar primero
    }

    override fun confirmarEntrega(contexto: ContextoPedido) {
        // No se puede entregar sin preparar
    }

    override fun cancelar(contexto: ContextoPedido) {
        // ✅ TRANSICIÓN: PENDIENTE → CANCELADO
        contexto.cambiarEstado(EstadoCancelado())
    }

    override fun obtenerNombre() = "PENDIENTE"
    override fun obtenerDescripcion() = "Pedido recibido, esperando preparación"
    override fun obtenerIcono() = "🛒"
    override fun obtenerColor() = "#2196F3"  // Azul
    override fun obtenerProgreso() = 25
}