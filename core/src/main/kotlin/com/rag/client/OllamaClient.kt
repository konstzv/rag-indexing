package com.rag.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Client for communicating with Ollama API to generate embeddings.
 *
 * Ollama is a local AI service that runs on http://localhost:11434.
 * We use it to convert text into 768-dimensional vectors.
 *
 * @param baseUrl URL of the Ollama service (default: http://localhost:11434)
 * @param httpClient HTTP client for making requests (uses Ktor)
 */
class OllamaClient(
    private val baseUrl: String = "http://localhost:11434",
    private val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Ignore JSON fields we don't need
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000  // 60 second timeout
        }
    }
) {
    /**
     * Request body sent to Ollama API
     */
    @Serializable
    data class EmbeddingRequest(
        val model: String,    // e.g., "nomic-embed-text"
        val prompt: String    // The text to embed
    )

    /**
     * Response from Ollama API
     */
    @Serializable
    data class EmbeddingResponse(
        val embedding: List<Float>  // The 768-dimensional vector
    )

    /**
     * Generate an embedding vector for the given text.
     *
     * Example:
     *   Input: "Kotlin is great"
     *   Output: [0.23, -0.45, 0.78, ... ] (768 numbers)
     *
     * @param text The text to convert into a vector
     * @return A list of 768 floating-point numbers
     */
    suspend fun generateEmbedding(text: String): List<Float> {
        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.post("$baseUrl/api/embeddings") {
                    contentType(ContentType.Application.Json)
                    setBody(EmbeddingRequest(
                        model = "nomic-embed-text",
                        prompt = text
                    ))
                }

                val embeddingResponse = response.body<EmbeddingResponse>()
                embeddingResponse.embedding
            } catch (e: Exception) {
                throw Exception("Failed to generate embedding: ${e.message}", e)
            }
        }
    }

    /**
     * Check if Ollama service is running.
     *
     * @return true if Ollama is healthy, false otherwise
     */
    suspend fun healthCheck(): Boolean {
        return try {
            httpClient.get("$baseUrl/api/tags")
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Close the HTTP client when done.
     */
    fun close() {
        httpClient.close()
    }
}
