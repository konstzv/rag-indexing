package com.rag.service

import com.rag.client.OllamaClient
import com.rag.domain.Chunk
import com.rag.domain.Embedding
import kotlinx.datetime.Clock

/**
 * Service for generating embeddings (vectors) from text chunks.
 *
 * Uses OllamaClient to communicate with the Ollama API.
 *
 * @param ollamaClient Client for calling Ollama API
 */
class EmbeddingService(
    private val ollamaClient: OllamaClient = OllamaClient()
) {
    /**
     * Generate embeddings for multiple chunks.
     *
     * For each chunk:
     * 1. Send the chunk text to Ollama
     * 2. Receive a 768-dimensional vector
     * 3. Create an Embedding object
     *
     * @param chunks List of text chunks to embed
     * @return List of Embedding objects (same order as input chunks)
     */
    suspend fun generateEmbeddings(chunks: List<Chunk>): List<Embedding> {
        return chunks.map { chunk ->
            // Generate vector for this chunk
            val vector = ollamaClient.generateEmbedding(chunk.content)

            // Create Embedding object
            Embedding(
                chunkId = chunk.id,
                vector = vector,
                createdAt = Clock.System.now()
            )
        }
    }

    /**
     * Generate an embedding for a single chunk.
     *
     * @param chunk The text chunk to embed
     * @return Embedding object with the vector
     */
    suspend fun generateEmbedding(chunk: Chunk): Embedding {
        val vector = ollamaClient.generateEmbedding(chunk.content)

        return Embedding(
            chunkId = chunk.id,
            vector = vector,
            createdAt = Clock.System.now()
        )
    }

    /**
     * Generate an embedding for raw query text.
     *
     * Used when searching - we embed the user's query to compare
     * it with our indexed chunks.
     *
     * @param query The search query text
     * @return Vector representation of the query (768 floats)
     */
    suspend fun generateQueryEmbedding(query: String): List<Float> {
        return ollamaClient.generateEmbedding(query)
    }
}
