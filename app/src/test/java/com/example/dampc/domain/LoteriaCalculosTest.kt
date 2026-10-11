package com.example.dampc.domain

import com.example.dampc.data.model.Bolilla
import com.example.dampc.data.model.EstadoSorteo
import org.junit.Assert.assertEquals
import org.junit.Test

class LoteriaCalculosTest {

    private val jugadaA = listOf(5, 12, 18, 23, 34, 41)
    private val jugadaB = listOf(5, 12, 18, 23, 34, 42)

    private fun bolillas(vararg numeros: Int) =
        numeros.mapIndexed { i, n -> Bolilla(numero = n, orden = i + 1) }

    @Test
    fun aciertos_noDependenDelOrden() {
        assertEquals(3, contarAciertos(jugadaA, bolillas(41, 5, 18)))
    }

    @Test
    fun despuesDeLaQuintaBolilla_ambosTienenCincoAciertosYSinResultado() {
        val publicadas = bolillas(5, 12, 18, 23, 34)
        val aciertosA = contarAciertos(jugadaA, publicadas)
        val aciertosB = contarAciertos(jugadaB, publicadas)

        assertEquals(5, aciertosA)
        assertEquals(5, aciertosB)
        assertEquals(Resultado.EN_CURSO, determinarResultado(EstadoSorteo.EN_CURSO, publicadas.size, aciertosA))
    }

    @Test
    fun finalizadoConSeisBolillas_AGanaYBSigueIntentando() {
        val publicadas = bolillas(5, 12, 18, 23, 34, 41)

        val aciertosA = contarAciertos(jugadaA, publicadas)
        val aciertosB = contarAciertos(jugadaB, publicadas)

        assertEquals(6, aciertosA)
        assertEquals(5, aciertosB)
        assertEquals(Resultado.GANADOR, determinarResultado(EstadoSorteo.FINALIZADO, 6, aciertosA))
        assertEquals(Resultado.SIGUE_INTENTANDO, determinarResultado(EstadoSorteo.FINALIZADO, 6, aciertosB))
    }

    @Test
    fun finalizadoSinLasSeisBolillas_esperaResultados() {
        assertEquals(Resultado.ESPERANDO_RESULTADOS, determinarResultado(EstadoSorteo.FINALIZADO, 5, 5))
    }

    @Test
    fun sorteoAbierto_muestraEspera() {
        assertEquals(Resultado.ESPERANDO_INICIO, determinarResultado(EstadoSorteo.ABIERTO, 0, 0))
    }
}
