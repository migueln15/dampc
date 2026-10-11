package com.example.dampc.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dampc.data.model.Sorteo

val ColorAcierto = Color(0xFF2E7D32)

@Composable
fun SorteoCard(sorteo: Sorteo) {
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

/** Círculo con un número. Si [numero] es null se dibuja un espacio vacío (bolilla pendiente). */
@Composable
fun NumeroBola(numero: Int?, resaltado: Boolean, size: Dp = 44.dp) {
    val fondo = when {
        numero == null -> Color.Transparent
        resaltado -> ColorAcierto
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fondo)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numero?.toString() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (resaltado) FontWeight.Bold else FontWeight.Normal,
            color = when {
                numero == null -> MaterialTheme.colorScheme.outline
                resaltado -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
