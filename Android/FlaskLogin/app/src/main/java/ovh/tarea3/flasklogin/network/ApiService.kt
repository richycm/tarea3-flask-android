package ovh.tarea3.flasklogin.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/")
    suspend fun getHello(): Response<HelloResponse>

    @POST("/register")
    suspend fun register(@Body request: UserRequest): Response<AuthResponse>

    @POST("/login")
    suspend fun login(@Body request: UserRequest): Response<AuthResponse>

    @GET("/users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @PUT("/users/{id}")
    suspend fun updateUserPassword(@Path("id") id: Int, @Body request: UpdatePasswordRequest): Response<AuthResponse>

    @DELETE("/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<AuthResponse>

    @PUT("/users/{id}/username")
    suspend fun updateUsername(@Path("id") id: Int, @Body request: UsernameRequest): Response<AuthResponse>

    @PUT("/users/{id}/password")
    suspend fun updatePassword(
        @Path("id") id: Int,
        @Body request: ChangePasswordRequest
    ): Response<AuthResponse>
}
