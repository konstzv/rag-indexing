package com.rag.domain

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a piece/chunk of a document.
 *
 * Example: A 1000-char document might be split into 3 chunks of 500 chars each,
 * with 150 chars overlap between consecutive chunks to preserve context.
 *
 * @param id Unique identifier for this chunk
 * @param documentId ID of the parent document
 * @param documentFilename Name of the source file (for display)
 * @param content The actual text content of this chunk
 * @param startIndex Starting character position in the original document
 * @param endIndex Ending character position in the original document
 * @param chunkIndex Sequential index (0, 1, 2, ...)
 */
@Serializable
data class Chunk(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val documentFilename: String,
    val content: String,
    val startIndex: Int,
    val endIndex: Int,
    val chunkIndex: Int
)
