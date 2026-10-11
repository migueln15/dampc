package com.example.dampc.data.model

const val NUMEROS_POR_JUGADA = 6
val RANGO_NUMEROS = 1..45

data class Jugada(
    val sorteoId: String,
    val uid: String,
    val numeros: List<Int>
)

/** Devuelve un mensaje de error si la jugada no es válida, o null si es válida. */
fun validarNumeros(numeros: List<Int>): String? = when {
    numeros.size != NUMEROS_POR_JUGADA -> "Debes elegir exactamente $NUMEROS_POR_JUGADA números"
    numeros.toSet().size != numeros.size -> "Los números no pueden repetirse"
    numeros.any { it !in RANGO_NUMEROS } ->
        "Los números deben estar entre ${RANGO_NUMEROS.first} y ${RANGO_NUMEROS.last}"
    else -> null
}
