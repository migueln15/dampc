package com.example.dampc.presentation.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dampc.data.remote.FirebaseAuthManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private data class RegisterErrors(
    val fullName: String? = null,
    val email: String? = null,
    val dob: String? = null,
    val phoneNumber: String? = null,
    val password: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(fullName, email, dob, phoneNumber, password).any { it != null }
}

private val dobFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

private fun validate(
    fullName: String,
    email: String,
    dob: String,
    phoneNumber: String,
    password: String
): RegisterErrors {
    val dobError = try {
        val date = LocalDate.parse(dob, dobFormatter)
        if (date.isAfter(LocalDate.now())) "La fecha no puede ser futura" else null
    } catch (e: Exception) {
        "Usa el formato DD/MM/AAAA"
    }

    return RegisterErrors(
        fullName = if (fullName.isBlank()) "Ingresa tu nombre" else null,
        email = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Correo no válido" else null,
        dob = dobError,
        phoneNumber = if (!Regex("^\\d{9}$").matches(phoneNumber)) "Debe tener 9 dígitos" else null,
        password = if (password.length < 6) "Mínimo 6 caracteres" else null
    )
}

@Composable
fun RegisterScreen(navController: NavController) {

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errors by remember { mutableStateOf(RegisterErrors()) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun goToLogin() {
        navController.navigate("login") {
            popUpTo("register") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; errors = errors.copy(fullName = null) },
            label = { Text("Nombre Completo") },
            isError = errors.fullName != null,
            supportingText = errors.fullName?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim(); errors = errors.copy(email = null) },
            label = { Text("Correo") },
            isError = errors.email != null,
            supportingText = errors.email?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it; errors = errors.copy(dob = null) },
            label = { Text("Fecha Nacimiento") },
            placeholder = { Text("DD/MM/AAAA") },
            isError = errors.dob != null,
            supportingText = errors.dob?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it; errors = errors.copy(phoneNumber = null) },
            label = { Text("Número de teléfono") },
            isError = errors.phoneNumber != null,
            supportingText = errors.phoneNumber?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errors = errors.copy(password = null) },
            label = { Text("Contraseña") },
            isError = errors.password != null,
            supportingText = errors.password?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            singleLine = true
        )

        serverError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !isLoading,
            onClick = {
                serverError = null
                errors = validate(fullName, email, dob, phoneNumber, password)
                if (errors.hasErrors) return@Button

                isLoading = true
                scope.launch {
                    val result = FirebaseAuthManager
                        .registerUser(fullName.trim(), email, password, dob, phoneNumber)
                    isLoading = false
                    if(result.isSuccess){
                        Toast.makeText(context, "Cuenta creada. Inicia sesión", Toast.LENGTH_LONG).show()
                        goToLogin()
                    }else{
                        serverError = result.exceptionOrNull()?.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrar")
            }
        }

        TextButton(onClick = { goToLogin() }, enabled = !isLoading) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
