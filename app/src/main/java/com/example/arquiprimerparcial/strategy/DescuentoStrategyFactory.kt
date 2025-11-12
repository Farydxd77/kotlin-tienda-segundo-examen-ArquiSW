package com.example.arquiprimerparcial.strategy

import com.example.arquiprimerparcial.strategy.impl.DescuentoBienvenidaStrategy
import com.example.arquiprimerparcial.strategy.impl.DescuentoBlackFridayStrategy
import com.example.arquiprimerparcial.strategy.impl.DescuentoNavidenoStrategy
import com.example.arquiprimerparcial.strategy.impl.SinDescuentoStrategy

object DescuentoStrategyFactory {

    fun obtenerStrategy(codigo: String?): DescuentoStrategy {
        return when (codigo?.uppercase()?.trim()) {
            "NAVIDAD2024", "NAVIDAD" -> DescuentoNavidenoStrategy()
            "BLACKFRIDAY", "BLACK" -> DescuentoBlackFridayStrategy()
            "BIENVENIDA" -> DescuentoBienvenidaStrategy()
            else -> SinDescuentoStrategy()
        }
    }

    fun obtenerDescuentosDisponibles(): List<DescuentoStrategy> {
        return listOf(
            DescuentoNavidenoStrategy(),
            DescuentoBlackFridayStrategy(),
            DescuentoBienvenidaStrategy()
        )
    }
}