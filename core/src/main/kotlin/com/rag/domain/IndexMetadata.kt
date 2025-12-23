package com.rag.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Metadata about the entire index.
 *
 * This tells us information about how the index was built.
 *
 * @param totalDocuments Number of documents indexed
 * @param totalChunks Total number of chunks created
 * @param model AI model used for embeddings
 * @param chunkSize Maximum size of each chunk in characters
 * @param overlapSize Number of overlapping characters between chunks
 * @param createdAt Timestamp when index was created
 */
@Serializable
data class IndexMetadata(
    val totalDocuments: Int,
    val totalChunks: Int,
    val model: String,
    val chunkSize: Int,
    val overlapSize: Int,
    val createdAt: Instant
)
