package ovh.tarea3.flasklogin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ovh.tarea3.flasklogin.network.ErrorUtils
import ovh.tarea3.flasklogin.network.RetrofitClient
import ovh.tarea3.flasklogin.network.UserRequest

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var apiStatusMessage by remember { mutableStateOf("Verificando conexión...") }
    var isApiOnline by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Verificación periódica de conexión con la API
    LaunchedEffect(Unit) {
        while(true) {
            try {
                val response = RetrofitClient.apiService.getHello()
                if (response.isSuccessful) {
                    apiStatusMessage = response.body()?.message ?: "API Funcionando"
                    isApiOnline = true
                } else {
                    apiStatusMessage = "Servidor responde con error"
                    isApiOnline = false
                }
            } catch (e: Exception) {
                apiStatusMessage = "Servidor Desconectado (Offline)"
                isApiOnline = false
            }
            delay(5000) // Verifica cada 5 segundos
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mostrar estado de la API
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isApiOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text(
                text = apiStatusMessage,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isApiOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }

        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it; errorMessage = "" },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.login(UserRequest(username, password))
                        if (response.isSuccessful) {
                            onLoginSuccess(username)
                        } else {
                            errorMessage = ErrorUtils.getFriendlyErrorMessage(response = response)
                        }
                    } catch (e: Exception) {
                        errorMessage = ErrorUtils.getFriendlyErrorMessage(throwable = e)
                        // Si falla el login por red, actualizamos el estado inmediatamente
                        isApiOnline = false
                        apiStatusMessage = "Servidor Desconectado (Offline)"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isApiOnline
        ) {
            Text("Entrar")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}
