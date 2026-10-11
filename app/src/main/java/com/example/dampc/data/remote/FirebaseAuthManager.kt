package com.example.dampc.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun registerUser(name: String, email: String, password: String, dob: String, phoneNumber: String): Result<Unit>{

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID del usuario")

            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "role" to "user",
                "dob" to dob,
                "phoneNumber" to phoneNumber
            )
            firestore.collection("users").document(uid).set(user).await()

            // Firebase inicia sesión automáticamente al crear la cuenta;
            // cerramos sesión para que el usuario ingrese desde el login.
            auth.signOut()
            Result.success(Unit)

        }catch (e: Exception){
            Result.failure(Exception(toFriendlyMessage(e), e))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit>{

        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(Exception(toFriendlyMessage(e), e))
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun toFriendlyMessage(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo o fue deshabilitada"
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseNetworkException -> "Sin conexión a internet. Inténtalo de nuevo"
        else -> e.message ?: "Ocurrió un error inesperado"
    }
}
