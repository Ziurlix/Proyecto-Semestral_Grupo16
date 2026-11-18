package com.example.data.remote

import com.example.appworkchat_grupo16.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.POST

/** Contrato esperado para la API REST de prueba del Dashboard. */
interface ApiService {
    @GET("api/v1/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/v1/clients")
    suspend fun getClients(): List<ClientDto>

    @GET("api/v1/orders")
    suspend fun getOrders(): List<OrderDto>

    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): OrderDto

    @GET("api/v1/deliveries")
    suspend fun getDeliveries(): List<DeliveryDto>

    @GET("api/v1/sales")
    suspend fun getSales(): List<SaleDto>

    @GET("api/v1/routes")
    suspend fun getRoutes(): List<RouteDto>

    @GET("api/v1/indicators")
    suspend fun getIndicators(): IndicatorsDto

    @PUT("api/v1/deliveries/{id}/status")
    suspend fun updateDeliveryStatus(
        @Path("id") id: String,
        @Body request: DeliveryStatusRequest
    ): DeliveryDto
}

data class ProductDto(
    val id: Int = 0,
    val name: String = "",
    val category: String = "",
    val price: Int = 0,
    val stock: Int = 0
)

data class CreateOrderRequest(
    val id: String,
    val clientName: String,
    val productName: String,
    val quantity: Int,
    val priority: String,
    val status: String,
    val date: String
)

data class ClientDto(
    val id: Int = 0,
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val address: String = ""
)

data class OrderDto(
    val id: String = "",
    val clientName: String = "",
    val productName: String = "",
    val quantity: Int = 1,
    val priority: String = "Normal",
    val status: String = "Pendiente",
    val date: String = ""
)

data class DeliveryDto(
    val id: String = "",
    val orderId: String = "",
    val recipient: String = "",
    val address: String = "",
    val driver: String = "",
    val status: String = "Pendiente",
    val scheduledTime: String = ""
)

data class SaleDto(
    val id: String = "",
    val orderId: String = "",
    val amount: Int = 0,
    val date: String = "",
    val seller: String = ""
)

data class RouteDto(
    val id: String = "",
    val name: String = "",
    val zone: String = "",
    val driver: String = "",
    val status: String = "",
    val deliveriesCount: Int = 0
)

data class IndicatorsDto(
    val salesAmount: Int = 0,
    val goalCompletionPercent: Int = 0,
    val pendingOrders: Int = 0,
    val completedDeliveries: Int = 0
)

data class DeliveryStatusRequest(val status: String)

object RetrofitClient {
    // Cambiar API_BASE_URL por la URL entregada por el docente o por el backend del equipo.
    // La URL debe terminar en '/'. El valor por defecto permite abrir la app en modo demostración.
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()
    @Volatile private var instance: ApiService? = null

    fun create(baseUrl: String = BuildConfig.API_BASE_URL): ApiService {
        return instance ?: synchronized(this) {
            instance ?: Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
                .also { instance = it }
        }
    }
}
