package com.rag.domain

/**
 * Represents a search result with similarity score.
 *
 * @param chunk The matched text chunk
 * @param similarity Cosine similarity score (0.0 to 1.0)
 *   - 1.0 = identical meaning
 *   - 0.7+ = very similar
 *   - 0.5 = somewhat related
 *   - 0.0 = unrelated
 * @param embedding The embedding vector of this chunk
 */
data class SearchResult(
    val chunk: Chunk,
    val similarity: Double,
    val embedding: Embedding
)
