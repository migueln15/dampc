package com.example.dampc.data.remote

import com.example.dampc.data.model.EstadoSorteo
import com.example.dampc.data.model.Jugada
import com.example.dampc.data.model.Sorteo
import com.example.dampc.data.model.validarNumeros
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/*
 * Estructura en Firestore:
 *   sorteos/{sorteoId}                 -> nombre, fecha, estado
 *   sorteos/{sorteoId}/jugadas/{uid}   -> sorteoId, uid, numeros
 * Usar el UID como id del documento garantiza una sola jugada por usuario.
 */
object LoteriaRepository {

    const val SORTEO_PRUEBA_ID = "sorteo-prueba"

    private val firestore = FirebaseFirestore.getInstance()

    private fun sorteoRef(sorteoId: String) =
        firestore.collection("sorteos").document(sorteoId)

    private fun jugadaRef(sorteoId: String, uid: String) =
        sorteoRef(sorteoId).collection("jugadas").document(uid)

    suspend fun getSorteo(sorteoId: String): Result<Sorteo?> {
        return try {
            val snapshot = sorteoRef(sorteoId).get().await()
            Result.success(if (snapshot.exists()) snapshot.toSorteo() else null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJugada(sorteoId: String, uid: String): Result<Jugada?> {
        return try {
            val snapshot = jugadaRef(sorteoId, uid).get().await()
            Result.success(if (snapshot.exists()) snapshot.toJugada() else null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveJugada(sorteoId: String, uid: String, numeros: List<Int>): Result<Unit> {
        validarNumeros(numeros)?.let { return Result.failure(Exception(it)) }

        return try {
            // La transacción vuelve a leer el estado del sorteo justo antes de guardar,
            // así no se puede registrar ni modificar una jugada si el sorteo ya inició.
            firestore.runTransaction { transaction ->
                val sorteo = transaction.get(sorteoRef(sorteoId))
                if (!sorteo.exists()) {
                    throw FirebaseFirestoreException(
                        "El sorteo no existe",
                        FirebaseFirestoreException.Code.NOT_FOUND
                    )
                }
                if (EstadoSorteo.from(sorteo.getString("estado")) != EstadoSorteo.ABIERTO) {
                    throw FirebaseFirestoreException(
                        "El sorteo ya inició. No se pueden registrar ni modificar jugadas",
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION
                    )
                }

                val jugada = hashMapOf(
                    "sorteoId" to sorteoId,
                    "uid" to uid,
                    "numeros" to numeros.sorted().map { it.toLong() },
                    "actualizadoEn" to FieldValue.serverTimestamp()
                )
                transaction.set(jugadaRef(sorteoId, uid), jugada)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toSorteo(): Sorteo {
        // La fecha puede ingresarse en la consola como timestamp o como texto
        val fecha = when (val value = get("fecha")) {
            is Timestamp -> SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-PE"))
                .format(value.toDate())
            is String -> value
            else -> ""
        }
        return Sorteo(
            id = id,
            nombre = getString("nombre") ?: id,
            fecha = fecha,
            estado = EstadoSorteo.from(getString("estado"))
        )
    }

    private fun DocumentSnapshot.toJugada(): Jugada {
        val numeros = (get("numeros") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?: emptyList()
        return Jugada(
            sorteoId = getString("sorteoId") ?: reference.parent.parent?.id.orEmpty(),
            uid = getString("uid") ?: id,
            numeros = numeros.sorted()
        )
    }
}
