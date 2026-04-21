package com.example.jetpack_compose_foot_calendar.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Login screen composable.
 *
 * Displays a centered form with username and password fields, an optional inline error
 * message, and a submit button that shows a loading indicator while the request is in flight.
 *
 * A [LaunchedEffect] watches [AuthViewModel.isAuthenticated] and calls [onLoginSuccess]
 * as soon as the flag becomes `true`, triggering the navigation to the calendar screen.
 *
 * @param authViewModel  The [AuthViewModel] that drives authentication state.
 *                       Defaults to the default [viewModel] instance.
 * @param onLoginSuccess Callback invoked when the user is successfully authenticated.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    // On observe les StateFlow du ViewModel
    // collectAsState() = ton watch() Vue — re-render quand ça change
    val isLoading by authViewModel.isLoading.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val error by authViewModel.error.collectAsState()

    // État local de l'écran — persiste uniquement pendant que l'écran est affiché
    // C'est l'équivalent de tes ref() dans un composant Vue
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // LaunchedEffect = ton watch(isAuthenticated) Vue
    // Se déclenche quand isAuthenticated change
    // Le paramètre clé (isAuthenticated) décide quand l'effet se relance
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚽ Football Calendar",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OutlinedTextField = ton <input v-model="username" />
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },   // équivalent de v-model
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), // masque le texte
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Affichage de l'erreur — équivalent de ton v-if="error"
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading  // désactivé pendant le chargement
        ) {
            // v-if="isLoading" en Compose = if dans le corps du composable
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Connexion")
            }
        }
    }
}