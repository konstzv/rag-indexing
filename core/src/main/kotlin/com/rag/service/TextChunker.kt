package com.rag.service

import com.rag.domain.Chunk
import com.rag.domain.Document

/**
 * Chunks documents into overlapping segments.
 *
 * Why overlap matters:
 * - Prevents losing context at chunk boundaries
 * - Example: "Kotlin is for JVM. Coroutines are powerful."
 *   Without overlap: ["Kotlin is for JVM."] ["Coroutines..."]
 *   With overlap: ["Kotlin is for JVM. Coroutines..."] ["...JVM. Coroutines are powerful."]
 *
 * @param chunkSize Maximum number of characters per chunk (default: 500)
 * @param overlapSize Number of characters to overlap between chunks (default: 150)
 */
class TextChunker(
    private val chunkSize: Int = 500,
    private val overlapSize: Int = 150
) {
    init {
        require(chunkSize > overlapSize) {
            "Chunk size ($chunkSize) must be greater than overlap ($overlapSize)"
        }
    }

    /**
     * Split a document into overlapping chunks.
     *
     * Algorithm:
     * 1. Start at position 0
     * 2. Take chunkSize characters
     * 3. Move forward by (chunkSize - overlapSize) characters
     * 4. Repeat until end of document
     *
     * Example with chunkSize=500, overlapSize=150:
     * - Chunk 0: chars 0-500
     * - Chunk 1: chars 350-850 (350 = 500-150, so 150 chars overlap)
     * - Chunk 2: chars 700-1200
     * - etc.
     *
     * @param document The document to chunk
     * @return List of Chunk objects
     */
    fun chunkDocument(document: Document): List<Chunk> {
        val chunks = mutableListOf<Chunk>()
        val text = document.content
        var startIndex = 0
        var chunkIndex = 0

        while (startIndex < text.length) {
            // Calculate end position (don't go past end of text)
            val endIndex = minOf(startIndex + chunkSize, text.length)

            // Extract the chunk content
            val chunkContent = text.substring(startIndex, endIndex)

            // Create the Chunk object
            chunks.add(
                Chunk(
                    documentId = document.id,
                    documentFilename = document.filename,
                    content = chunkContent,
                    startIndex = startIndex,
                    endIndex = endIndex,
                    chunkIndex = chunkIndex++
                )
            )

            // Move forward by (chunkSize - overlapSize)
            // This creates overlapSize overlap between consecutive chunks
            startIndex += (chunkSize - overlapSize)
        }

        return chunks
    }
}
