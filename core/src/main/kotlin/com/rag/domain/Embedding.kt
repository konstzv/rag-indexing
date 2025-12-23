package com.rag.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents an embedding (vector) for a chunk of text.
 *
 * An embedding is a mathematical representation of text meaning.
 * The nomic-embed-text model creates 768-dimensional vectors.
 *
 * Example vector (simplified): [0.23, -0.45, 0.78, ...]
 * Similar meanings = vectors close together in 768D space
 *
 * @param chunkId ID of the chunk this embedding represents
 * @param vector List of 768 floating point numbers
 * @param model Name of the AI model used (nomic-embed-text)
 * @param createdAt Timestamp when embedding was generated
 */
@Serializable
data class Embedding(
    val chunkId: String,
    val vector: List<Float>,
    val model: String = "nomic-embed-text",
    val createdAt: Instant
)
