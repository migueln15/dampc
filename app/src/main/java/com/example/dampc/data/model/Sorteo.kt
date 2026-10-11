package com.example.dampc.data.model

enum class EstadoSorteo(val etiqueta: String) {
    ABIERTO("Abierto"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    DESCONOCIDO("Desconocido");

    companion object {
        // Acepta "abierto", "en curso", "en_curso", "finalizado" (sin importar mayúsculas)
        fun from(value: String?): EstadoSorteo =
            when (value?.trim()?.lowercase()?.replace('_', ' ')) {
                "abierto" -> ABIERTO
                "en curso" -> EN_CURSO
                "finalizado" -> FINALIZADO
                else -> DESCONOCIDO
            }
    }
}

data class Sorteo(
    val id: String,
    val nombre: String,
    val fecha: String,
    val estado: EstadoSorteo
)
