package com.example.arquiprimerparcial.strategy

import com.example.arquiprimerparcial.strategy.impl.SinDescuentoStrategy


class DescuentoContext {

    // - strategy: Strategy (atributo privado como en el diagrama)
    private var strategy: DescuentoStrategy = SinDescuentoStrategy()

    fun setStrategy(strategy: DescuentoStrategy) {
        this.strategy = strategy
    }

    fun aplicarDescuento(subtotal: Double): ResultadoDescuento {
        return strategy.aplicarDescuento(subtotal)
    }
}