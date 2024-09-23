package com.example.magichouse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.ByteBuffer
import javax.net.ssl.HttpsURLConnection

private val CardFetcherTag: String = "CardFetcher"

class CardFetcher(private val context: Context) {

    private val notFoundBitmap: Bitmap
    private val backsideBitmap: Bitmap

    private val cardWidth: Int = 512
    private val cardHeight: Int = 715

    init {
        BitmapFactory.decodeStream(context.assets.open("images/magic_card_back.png")).apply {
            Bitmap.createScaledBitmap(this, cardWidth, cardHeight, false).apply {
                backsideBitmap = this
            }
        }
        BitmapFactory.decodeStream(context.assets.open("images/not_found.png")).apply {
            Bitmap.createScaledBitmap(this, cardWidth, cardHeight,false).apply {
                notFoundBitmap = this
            }
        }
    }

    private fun downloadImageAsBitmap(imageURL: String?): Bitmap? {
        return imageURL?.run{
            try {
                val url = URL(imageURL)
                with(url.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        val inputStream: InputStream = inputStream
                        BitmapFactory.decodeStream(inputStream)
                    }
                    else {
                        null
                    }
                }
            } catch (e: IOException) {
                Log.e(CardFetcherTag, "IOException: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e(CardFetcherTag, "Exception: ${e.message}", e)
                null
            }
        }
    }

    private fun parseImageUrl(jsonResponse: JSONObject?): List<String>? {

        // There are Multi Face Cards, find both sides
        (jsonResponse?.opt("card_faces") as? JSONArray)?.run {
            val imageURLs = mutableListOf<String>()
            for (i in 0 until length()) {
                val faceObject = optJSONObject(i)
                faceObject?.optString("png")?.let { pngUrl ->
                    imageURLs.add(pngUrl)  // Collect the "png" URLs
                }
            }
            return imageURLs.toList()
        }

        jsonResponse?.optJSONObject("image_uris")?.optString("png")?.run {
            return listOf<String>(this)
        }

        return null
    }

    private fun queryRandomCard(): JSONObject? {
        return try {
            val url = URL("https://api.scryfall.com/cards/random")
            with(url.openConnection() as HttpsURLConnection) {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                val responseCode = responseCode
                Log.d(CardFetcherTag, "Response Code: $responseCode")
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    reader.useLines { lines ->
                        lines.forEach { response.append(it) }
                    }
                    Log.d(CardFetcherTag, "Response: $response")
                    JSONObject(response.toString())
                } else {
                    Log.e(CardFetcherTag, "Error in GET request: $responseCode")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(CardFetcherTag, "IOException: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(CardFetcherTag, "Exception: ${e.message}", e)
            null
        }
    }

    fun GetRandomCard(): Pair<Bitmap, Bitmap> {
        var frontFace: Bitmap = notFoundBitmap
        var backFace: Bitmap = backsideBitmap

        queryRandomCard()?.run {
            parseImageUrl(this)?.run {
                when (size) {
                    1 -> { // Only one face, populate the other face with default magic back
                        downloadImageAsBitmap(this[0])?.run{
                            Bitmap.createScaledBitmap(this, cardWidth, cardHeight, false).apply {
                                frontFace = this
                            }
                        }
                    }
                    2 -> { // Get both sides of the card
                        downloadImageAsBitmap(this[0])?.run{
                            Bitmap.createScaledBitmap(this, cardWidth, cardHeight, false).apply {
                                frontFace = this
                            }
                        }
                        downloadImageAsBitmap(this[1])?.run {
                            Bitmap.createScaledBitmap(this, cardWidth, cardHeight, false).apply {
                                backFace = this
                            }
                        }
                    }
                    else -> { /* do nothing, handled below */ }
                }
            }
        }
        return Pair<Bitmap, Bitmap>(frontFace, backFace)
    }
}