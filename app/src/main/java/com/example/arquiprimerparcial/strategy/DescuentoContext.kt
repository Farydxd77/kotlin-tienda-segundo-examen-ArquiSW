package com.example.arquiprimerparcial.strategy

import com.example.arquiprimerparcial.strategy.impl.SinDescuentoStrategy

/**
 * 🎯 CONTEXT del patrón Strategy (según diagrama teórico)
 *
 * Responsabilidades:
 * 1. Mantener referencia a una Strategy
 * 2. Permitir cambiar la Strategy (setStrategy)
 * 3. Delegar la ejecución a la Strategy (doSomething)
 *
 * ✅ Cumple 100% con el diagrama de estructura del patrón Strategy
 */
class DescuentoContext {

    // ✅ - strategy: Strategy (atributo privado como en el diagrama)
    private var strategy: DescuentoStrategy = SinDescuentoStrategy()

    /**
     * ✅ + setStrategy(strategy: Strategy)
     * Permite cambiar la estrategia en tiempo de ejecución
     */
    fun setStrategy(strategy: DescuentoStrategy) {
        this.strategy = strategy
    }

    /**
     * ✅ + doSomething()
     * Ejecuta la estrategia actual
     * En el diagrama: strategy.execute(data)
     */
    fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        return strategy.aplicarDescuento(subtotal)
    }
}