package com.example.dampc.presentation.seguimiento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dampc.data.model.Bolilla
import com.example.dampc.data.model.NUMEROS_POR_JUGADA
import com.example.dampc.data.model.Sorteo
import com.example.dampc.data.remote.FirebaseAuthManager
import com.example.dampc.data.remote.LoteriaRepository
import com.example.dampc.domain.Resultado
import com.example.dampc.domain.contarAciertos
import com.example.dampc.domain.determinarResultado
import com.example.dampc.presentation.components.ColorAcierto
import com.example.dampc.presentation.components.NumeroBola
import com.example.dampc.presentation.components.SorteoCard

@Composable
fun SeguimientoScreen(navController: NavController) {

    val sorteoId = LoteriaRepository.SORTEO_PRUEBA_ID
    val uid = FirebaseAuthManager.currentUserId
    val email = FirebaseAuthManager.currentUserEmail

    var sorteo by remember { mutableStateOf<Sorteo?>(null) }
    var sorteoCargado by remember { mutableStateOf(false) }
    var bolillas by remember { mutableStateOf<List<Bolilla>>(emptyList()) }
    var jugada by remember { mutableStateOf<List<Int>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // La jugada no cambia una vez iniciado el sorteo, así que basta con leerla una vez
    LaunchedEffect(uid) {
        if (uid == null) {
            error = "No hay una sesión activa"
            return@LaunchedEffect
        }
        LoteriaRepository.getJugada(sorteoId, uid)
            .onSuccess { jugada = it?.numeros }
            .onFailure { error = "No se pudo cargar tu jugada: ${it.message}" }
    }

    // Escucha en tiempo real el estado del sorteo y sus bolillas mientras la pantalla esté abierta.
    // Al salir de la pantalla, onDispose quita ambos listeners para no dejarlos activos ni duplicados.
    DisposableEffect(sorteoId) {
        val sorteoListener = LoteriaRepository.listenSorteo(
            sorteoId = sorteoId,
            onChange = {
                sorteo = it
                sorteoCargado = true
            },
            onError = { error = "Error al escuchar el sorteo: ${it.message}" }
        )
        val bolillasListener = LoteriaRepository.listenBolillas(
            sorteoId = sorteoId,
            onChange = { bolillas = it },
            onError = { error = "Error al escuchar las bolillas: ${it.message}" }
        )
        onDispose {
            sorteoListener.remove()
            bolillasListener.remove()
        }
    }

    // Cálculos: se rehacen automáticamente cada vez que llega un cambio desde Firestore
    val numerosJugada = jugada.orEmpty()
    val numerosSorteados = bolillas.map { it.numero }.toSet()
    val aciertos = contarAciertos(numerosJugada, bolillas)
    val resultado = sorteo?.let { determinarResultado(it.estado, bolillas.size, aciertos) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Seguimiento del Sorteo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        email?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        val s = sorteo
        when {
            !sorteoCargado -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))

            s == null -> Text(
                text = "No existe el sorteo \"$sorteoId\" en Firestore",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            else -> {
                SorteoCard(s)

                Spacer(modifier = Modifier.height(16.dp))

                resultado?.let { ResultadoCard(it, tieneJugada = jugada != null) }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bolillas publicadas: ${bolillas.size} de $NUMEROS_POR_JUGADA",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Se muestran en orden de salida; los espacios con "?" son bolillas pendientes
                FilaNumeros(
                    numeros = List(NUMEROS_POR_JUGADA) { i -> bolillas.getOrNull(i)?.numero },
                    resaltar = { it in numerosJugada }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Tu jugada", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (jugada == null) {
                    Text(
                        text = "No registraste una jugada para este sorteo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    FilaNumeros(
                        numeros = numerosJugada,
                        resaltar = { it in numerosSorteados }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aciertos: $aciertos de $NUMEROS_POR_JUGADA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun FilaNumeros(numeros: List<Int?>, resaltar: (Int) -> Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        numeros.forEach { n ->
            NumeroBola(numero = n, resaltado = n != null && resaltar(n))
        }
    }
}

@Composable
private fun ResultadoCard(resultado: Resultado, tieneJugada: Boolean) {
    val (fondo, texto) = when (resultado) {
        Resultado.GANADOR -> ColorAcierto to Color.White
        Resultado.SIGUE_INTENTANDO -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val mensaje = if (resultado.esDefinitivo && !tieneJugada) "Sorteo finalizado" else resultado.texto

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto)
    ) {
        Text(
            text = mensaje,
            style = if (resultado.esDefinitivo) MaterialTheme.typography.headlineMedium
            else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}
