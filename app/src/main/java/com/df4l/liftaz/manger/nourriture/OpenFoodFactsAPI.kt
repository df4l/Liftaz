package com.df4l.liftaz.manger.nourriture

import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.Json

data class Nutriments(
    @Json(name = "energy-kcal_100g")
    val energyKcal100g: Float? = 0f,

    @Json(name = "proteins_100g")
    val proteins100g: Float? = 0f,

    @Json(name = "carbohydrates_100g")
    val carbohydrates100g: Float? = 0f,

    @Json(name = "fat_100g")
    val fat100g: Float? = 0f
)

data class Product(
    @Json(name = "product_name")
    val productName: String? = "",

    @Json(name = "brands")
    val brands: String? = "",

    @Json(name = "nutriments")
    val nutriments: Nutriments? = Nutriments()
)

data class OpenFoodFactsResponse(
    @Json(name = "status")
    val status: Int = 0,

    @Json(name = "product")
    val product: Product? = null
)


// --- Classe principale pour interagir avec l'API ---
class OpenFoodFactsAPI(private val userAgent: String = "Liftaz - Android - 1.0 - https://github.com/df4l/Liftaz - scan") {

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(OpenFoodFactsResponse::class.java)

    /**
     * Récupère les informations d'un produit via son code-barres
     */
    suspend fun getProduct(barcode: String): OpenFoodFactsResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://world.openfoodfacts.org/api/v2/product/$barcode.json"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext null

                adapter.fromJson(body)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

