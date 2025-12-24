package com.rag.service

import com.rag.domain.SearchResult
import com.rag.repository.JsonEmbeddingStorage
import kotlin.math.sqrt

/**
 * Service for searching similar chunks using vector similarity.
 *
 * @param embeddingStorage Storage containing indexed chunks and embeddings
 */
class VectorSearch(
    private val embeddingStorage: JsonEmbeddingStorage = JsonEmbeddingStorage()
) {
    /**
     * Compute cosine similarity between two vectors.
     *
     * Cosine similarity measures the cosine of the angle between two vectors.
     * It's a measure of orientation, not magnitude.
     *
     * Formula: cos(θ) = (A · B) / (||A|| × ||B||)
     *
     * Where:
     * - A · B = dot product (sum of element-wise multiplication)
     * - ||A|| = magnitude of vector A (Euclidean norm)
     * - ||B|| = magnitude of vector B
     *
     * Returns:
     *   1.0  = vectors point in same direction (semantically identical)
     *   0.0  = vectors are orthogonal (unrelated)
     *  -1.0  = vectors point in opposite directions (opposite meaning)
     *
     * Example:
     *   vector1 = [1.0, 0.0, 0.0]
     *   vector2 = [1.0, 0.0, 0.0]
     *   similarity = 1.0 (identical)
     *
     *   vector1 = [1.0, 0.0, 0.0]
     *   vector2 = [0.0, 1.0, 0.0]
     *   similarity = 0.0 (orthogonal)
     */
    fun cosineSimilarity(a: List<Float>, b: List<Float>): Double {
        require(a.size == b.size) {
            "Vectors must have same dimension. Got ${a.size} and ${b.size}"
        }

        // Step 1: Calculate dot product (A · B)
        // Dot product = sum of (a[i] * b[i]) for all i
        val dotProduct = a.zip(b).sumOf { (x, y) ->
            (x * y).toDouble()
        }

        // Step 2: Calculate magnitude of vector A
        // ||A|| = sqrt(sum of a[i]^2)
        val magnitudeA = sqrt(a.sumOf { (it * it).toDouble() })

        // Step 3: Calculate magnitude of vector B
        // ||B|| = sqrt(sum of b[i]^2)
        val magnitudeB = sqrt(b.sumOf { (it * it).toDouble() })

        // Step 4: Calculate cosine similarity
        // Handle edge case of zero vectors
        return if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            0.0
        } else {
            dotProduct / (magnitudeA * magnitudeB)
        }
    }

    /**
     * Search for semantically similar chunks.
     *
     * Process:
     * 1. Load all indexed chunks and embeddings
     * 2. Calculate similarity between query and each chunk
     * 3. Filter by minimum similarity threshold
     * 4. Sort by similarity (highest first)
     * 5. Return top K results
     *
     * @param queryEmbedding The query vector (768 dimensions from Ollama)
     * @param topK Number of results to return (default: 5)
     * @param minSimilarity Minimum similarity threshold (default: 0.0, range: 0.0 to 1.0)
     * @return List of search results sorted by similarity (highest first)
     * @throws IllegalStateException if no index found
     */
    suspend fun search(
        queryEmbedding: List<Float>,
        topK: Int = 5,
        minSimilarity: Double = 0.0
    ): List<SearchResult> {
        // Load the index
        val indexData = embeddingStorage.loadIndex()
            ?: throw IllegalStateException("No index found. Run indexing first with: ./gradlew run --args=\"index documents/\"")

        // Create a map for quick chunk lookup by ID
        val chunkMap = indexData.chunks.associateBy { it.id }

        // Calculate similarity for each embedding
        return indexData.embeddings
            .map { embedding ->
                // Calculate cosine similarity between query and this chunk's embedding
                val similarity = cosineSimilarity(queryEmbedding, embedding.vector)

                // Find the corresponding chunk
                val chunk = chunkMap[embedding.chunkId]
                    ?: error("Chunk not found for embedding: ${embedding.chunkId}")

                // Create search result
                SearchResult(
                    chunk = chunk,
                    similarity = similarity,
                    embedding = embedding
                )
            }
            // Filter by minimum similarity
            .filter { it.similarity >= minSimilarity }
            // Sort by similarity (highest first)
            .sortedByDescending { it.similarity }
            // Take top K results
            .take(topK)
    }
}
