package ovh.tarea3.flasklogin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ovh.tarea3.flasklogin.network.ChangePasswordRequest
import ovh.tarea3.flasklogin.network.RetrofitClient
import ovh.tarea3.flasklogin.network.UserResponse
import ovh.tarea3.flasklogin.network.UsernameRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<UserResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingUser by remember { mutableStateOf<UserResponse?>(null) }
    var changingPasswordUser by remember { mutableStateOf<UserResponse?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun fetchUsers() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getUsers()
                if (response.isSuccessful) {
                    users = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onEdit = { editingUser = user },
                            onChangePassword = { changingPasswordUser = user },
                            onDelete = {
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.deleteUser(user.id)
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                                            fetchUsers()
                                        } else {
                                            Toast.makeText(context, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        editingUser?.let { user ->
            EditUserDialog(
                user = user,
                onDismiss = { editingUser = null },
                onConfirm = { newUsername ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.updateUsername(
                                user.id,
                                UsernameRequest(newUsername)
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                                editingUser = null
                                fetchUsers()
                            } else {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        changingPasswordUser?.let { user ->
            ChangePasswordDialog(
                user = user,
                onDismiss = { changingPasswordUser = null },
                onConfirm = { oldPassword, newPassword, confirmPassword ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.updatePassword(
                                user.id,
                                ChangePasswordRequest(oldPassword, newPassword, confirmPassword)
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                changingPasswordUser = null
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Error al actualizar"
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun UserItem(
    user: UserResponse, 
    onEdit: () -> Unit, 
    onChangePassword: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onChangePassword) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cambiar Contraseña",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Nombre",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: UserResponse,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(user.username) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column {
                Text("Ingresa el nuevo nombre de usuario:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newUsername) },
                enabled = newUsername.isNotBlank() && newUsername != user.username
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    user: UserResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña: ${user.username}") },
        text = {
            Column {
                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it; error = "" },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    label = { Text("Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = "" },
                    label = { Text("Confirmar Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (password == confirm) {
                        onConfirm(oldPassword, password, confirm)
                    } else {
                        error = "Las contraseñas nuevas no coinciden"
                    }
                },
                enabled = oldPassword.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty()
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
