package com.rag.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a document loaded from a file.
 *
 * @param id Unique identifier (auto-generated UUID)
 * @param filename Name of the source file
 * @param content Full text content of the document
 * @param metadata Additional info (file path, size, etc.)
 * @param indexedAt Timestamp when document was indexed
 */
@Serializable
data class Document(
    val id: String = UUID.randomUUID().toString(),
    val filename: String,
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val indexedAt: Instant
)
