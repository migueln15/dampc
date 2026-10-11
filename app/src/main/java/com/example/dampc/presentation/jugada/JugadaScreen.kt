package com.example.dampc.presentation.jugada

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dampc.data.model.EstadoSorteo
import com.example.dampc.data.model.NUMEROS_POR_JUGADA
import com.example.dampc.data.model.RANGO_NUMEROS
import com.example.dampc.data.model.Sorteo
import com.example.dampc.data.remote.FirebaseAuthManager
import com.example.dampc.data.remote.LoteriaRepository
import kotlinx.coroutines.launch

@Composable
fun JugadaScreen(navController: NavController) {

    val sorteoId = LoteriaRepository.SORTEO_PRUEBA_ID
    val uid = FirebaseAuthManager.currentUserId
    val email = FirebaseAuthManager.currentUserEmail

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var sorteo by remember { mutableStateOf<Sorteo?>(null) }
    var jugadaGuardada by remember { mutableStateOf<List<Int>?>(null) }
    var seleccion by remember { mutableStateOf(setOf<Int>()) }
    var message by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Carga el sorteo y la jugada del usuario de la sesión al abrir la pantalla
    LaunchedEffect(uid) {
        if (uid == null) {
            loadError = "No hay una sesión activa"
            isLoading = false
            return@LaunchedEffect
        }
        val sorteoResult = LoteriaRepository.getSorteo(sorteoId)
        val jugadaResult = LoteriaRepository.getJugada(sorteoId, uid)

        sorteo = sorteoResult.getOrNull()
        jugadaGuardada = jugadaResult.getOrNull()?.numeros
        seleccion = jugadaGuardada?.toSet() ?: emptySet()

        loadError = when {
            sorteoResult.isFailure -> "No se pudo cargar el sorteo: ${sorteoResult.exceptionOrNull()?.message}"
            jugadaResult.isFailure -> "No se pudo cargar tu jugada: ${jugadaResult.exceptionOrNull()?.message}"
            sorteo == null -> "No existe el sorteo \"$sorteoId\" en Firestore"
            else -> null
        }
        isLoading = false
    }

    val abierto = sorteo?.estado == EstadoSorteo.ABIERTO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Registro de Jugada",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        email?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))

            loadError != null -> Text(
                text = loadError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            else -> {
                val s = sorteo!!
                SorteoCard(s)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        abierto && jugadaGuardada == null -> "Elige $NUMEROS_POR_JUGADA números distintos del ${RANGO_NUMEROS.first} al ${RANGO_NUMEROS.last}"
                        abierto -> "Ya registraste tu jugada. Puedes modificarla mientras el sorteo esté abierto"
                        jugadaGuardada == null -> "El sorteo ya inició y no registraste una jugada"
                        else -> "El sorteo ya inició. Tu jugada no se puede modificar"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                NumberGrid(
                    selected = seleccion,
                    enabled = abierto && !isSaving,
                    onToggle = { n ->
                        message = null
                        seleccion = when {
                            n in seleccion -> seleccion - n
                            seleccion.size < NUMEROS_POR_JUGADA -> seleccion + n
                            else -> {
                                message = "Ya elegiste $NUMEROS_POR_JUGADA números. Quita uno para cambiarlo"
                                seleccion
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Seleccionados: ${seleccion.size}/$NUMEROS_POR_JUGADA" +
                            if (seleccion.isNotEmpty()) "  ·  ${seleccion.sorted().joinToString(", ")}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                if (abierto) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        enabled = !isSaving
                                && seleccion.size == NUMEROS_POR_JUGADA
                                && seleccion.sorted() != jugadaGuardada,
                        onClick = {
                            message = null
                            isSaving = true
                            scope.launch {
                                val numeros = seleccion.sorted()
                                val result = LoteriaRepository.saveJugada(sorteoId, uid!!, numeros)
                                isSaving = false
                                if (result.isSuccess) {
                                    jugadaGuardada = numeros
                                    Toast.makeText(context, "Jugada guardada", Toast.LENGTH_SHORT).show()
                                } else {
                                    message = result.exceptionOrNull()?.message
                                    // Si el sorteo cambió de estado, se recarga para reflejarlo
                                    LoteriaRepository.getSorteo(sorteoId).getOrNull()?.let { sorteo = it }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (jugadaGuardada == null) "Guardar jugada" else "Actualizar jugada")
                        }
                    }

                    OutlinedButton(
                        enabled = !isSaving && seleccion.isNotEmpty(),
                        onClick = { seleccion = emptySet(); message = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Limpiar selección")
                    }
                }

                // TODO: botón para pasar a la pantalla de seguimiento del sorteo
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            enabled = !isSaving,
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun SorteoCard(sorteo: Sorteo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = sorteo.nombre, style = MaterialTheme.typography.titleLarge)
            if (sorteo.fecha.isNotEmpty()) {
                Text(text = "Fecha: ${sorteo.fecha}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Estado: ${sorteo.estado.etiqueta}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NumberGrid(selected: Set<Int>, enabled: Boolean, onToggle: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        RANGO_NUMEROS.chunked(9).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { n ->
                    val isSelected = n in selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(enabled = enabled) { onToggle(n) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$n",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
