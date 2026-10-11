package com.example.dampc.domain

import com.example.dampc.data.model.Bolilla
import com.example.dampc.data.model.EstadoSorteo
import com.example.dampc.data.model.NUMEROS_POR_JUGADA

/*
 * Cálculos del sorteo. Se hacen en la app a partir de la jugada guardada y de las
 * bolillas recibidas desde Firestore; el resultado nunca se guarda en Firestore.
 */

enum class Resultado(val texto: String, val esDefinitivo: Boolean) {
    ESPERANDO_INICIO("Esperando el inicio del sorteo", false),
    EN_CURSO("Sorteo en curso", false),
    ESPERANDO_RESULTADOS("Esperando resultados", false),
    GANADOR("GANADOR", true),
    SIGUE_INTENTANDO("SIGUE INTENTANDO", true),
    ESTADO_DESCONOCIDO("Estado del sorteo no reconocido", false)
}

/** Cantidad de números distintos de la jugada que salieron entre las bolillas. El orden no importa. */
fun contarAciertos(jugada: List<Int>, bolillas: List<Bolilla>): Int {
    val sorteados = bolillas.map { it.numero }.toSet()
    return jugada.toSet().count { it in sorteados }
}

/**
 * El resultado definitivo solo se muestra cuando el sorteo está finalizado
 * y ya se recibieron las seis bolillas; si falta alguna, se sigue esperando.
 */
fun determinarResultado(estado: EstadoSorteo, bolillasPublicadas: Int, aciertos: Int): Resultado =
    when (estado) {
        EstadoSorteo.ABIERTO -> Resultado.ESPERANDO_INICIO
        EstadoSorteo.EN_CURSO -> Resultado.EN_CURSO
        EstadoSorteo.FINALIZADO -> when {
            bolillasPublicadas < NUMEROS_POR_JUGADA -> Resultado.ESPERANDO_RESULTADOS
            aciertos == NUMEROS_POR_JUGADA -> Resultado.GANADOR
            else -> Resultado.SIGUE_INTENTANDO
        }
        EstadoSorteo.DESCONOCIDO -> Resultado.ESTADO_DESCONOCIDO
    }
