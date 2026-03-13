package ovh.tarea3.flasklogin.network

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String = ""
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)

data class UpdatePasswordRequest(
    @SerializedName("password") val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String
)

data class UsernameRequest(
    @SerializedName("username") val username: String
)

data class AuthResponse(
    @SerializedName("message") val message: String
)

data class HelloResponse(
    @SerializedName("message") val message: String
)

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String
)