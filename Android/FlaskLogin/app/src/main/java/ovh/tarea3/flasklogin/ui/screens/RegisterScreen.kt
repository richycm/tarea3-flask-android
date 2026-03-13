package ovh.tarea3.flasklogin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ovh.tarea3.flasklogin.network.ErrorUtils
import ovh.tarea3.flasklogin.network.RetrofitClient
import ovh.tarea3.flasklogin.network.UserRequest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessScreen by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    if (showSuccessScreen) {
        // Pantalla de Usuario Creado
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", fontSize = 48.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "¡Usuario Creado!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tu cuenta se ha registrado correctamente. Ya puedes iniciar sesión.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRegisterSuccess,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Ir al Login")
            }
        }
    } else {
        // Formulario de Registro
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Registro de Usuario", style = MaterialTheme.typography.headlineMedium)
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
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it; errorMessage = "" },
                label = { Text("Confirmar Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    if (password != passwordConfirm) {
                        errorMessage = "Las contraseñas no coinciden"
                        return@Button
                    }
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.register(
                                UserRequest(username, password, passwordConfirm)
                            )
                            if (response.isSuccessful) {
                                showSuccessScreen = true
                            } else {
                                // Aquí se captura el error si el usuario ya existe
                                // Si el backend devuelve un 400 con un mensaje, ErrorUtils lo procesará
                                val errorBody = response.errorBody()?.string()
                                if (errorBody?.contains("already exists", ignoreCase = true) == true || 
                                    errorBody?.contains("existe", ignoreCase = true) == true) {
                                    errorMessage = "Datos inválidos, usuario ya existente."
                                } else {
                                    errorMessage = ErrorUtils.getFriendlyErrorMessage(response = response)
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = ErrorUtils.getFriendlyErrorMessage(throwable = e)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }

            TextButton(onClick = onBackToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
