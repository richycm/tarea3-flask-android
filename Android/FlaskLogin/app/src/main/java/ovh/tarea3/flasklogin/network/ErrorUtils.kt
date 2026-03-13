package ovh.tarea3.flasklogin.network

import retrofit2.Response
import java.io.IOException

object ErrorUtils {
    fun getFriendlyErrorMessage(response: Response<*>? = null, throwable: Throwable? = null): String {
        if (throwable != null) {
            return if (throwable is IOException) {
                "No se pudo conectar al servidor. Verifica tu conexión a internet."
            } else {
                "Ocurrió un error inesperado. Intenta nuevamente."
            }
        }

        return when (response?.code()) {
            401 -> "Usuario o contraseña incorrectos."
            400 -> "Los datos ingresados no son válidos."
            else -> "Ocurrió un error inesperado. Intenta nuevamente."
        }
    }
}
