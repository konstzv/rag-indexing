package com.rag.repository

import com.rag.domain.Chunk
import com.rag.domain.Embedding
import com.rag.domain.IndexMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Container for all index data.
 *
 * This is what gets saved to the JSON file.
 *
 * @param chunks All text chunks
 * @param embeddings All embedding vectors (matches chunks 1:1)
 * @param metadata Information about the index
 * @param version Schema version (for future compatibility)
 */
@Serializable
data class IndexData(
    val chunks: List<Chunk>,
    val embeddings: List<Embedding>,
    val metadata: IndexMetadata,
    val version: String = "1.0"
)

/**
 * Storage layer for saving/loading embeddings to/from JSON files.
 *
 * File location: ./output/embeddings.json (relative to project root)
 *
 * @param outputDir Custom output directory path (default: "./output")
 */
class JsonEmbeddingStorage(
    private val outputDir: String = "./output"
) {
    /**
     * JSON serializer configuration
     */
    private val json = Json {
        prettyPrint = true              // Make JSON human-readable
        ignoreUnknownKeys = true        // Ignore unknown fields when loading
    }

    /**
     * Directory where we store the index file
     */
    private val storageDir: File by lazy {
        File(outputDir).apply {
            if (!exists()) mkdirs()  // Create directory if it doesn't exist
        }
    }

    /**
     * The actual index file
     */
    private val indexFile: File by lazy {
        File(storageDir, "embeddings.json")
    }

    /**
     * Save the index to disk.
     *
     * Process:
     * 1. Combine chunks, embeddings, and metadata into IndexData
     * 2. Serialize to JSON string
     * 3. Write to temp file
     * 4. Rename temp file to final file (atomic operation)
     *
     * Why atomic write? If the program crashes during write, we don't
     * corrupt the existing index file.
     *
     * @param chunks All text chunks to save
     * @param embeddings All embedding vectors to save
     * @param metadata Index metadata
     */
    suspend fun saveIndex(
        chunks: List<Chunk>,
        embeddings: List<Embedding>,
        metadata: IndexMetadata
    ) = withContext(Dispatchers.IO) {
        // Create the container object
        val indexData = IndexData(
            chunks = chunks,
            embeddings = embeddings,
            metadata = metadata
        )

        // Convert to JSON string
        val jsonContent = json.encodeToString(indexData)

        // Atomic write using temp file
        val tempFile = File(storageDir, "embeddings.json.tmp")
        tempFile.writeText(jsonContent)
        tempFile.renameTo(indexFile)

        println("Index saved to: ${indexFile.absolutePath}")
    }

    /**
     * Load the index from disk.
     *
     * @return IndexData if file exists and is valid, null otherwise
     */
    suspend fun loadIndex(): IndexData? = withContext(Dispatchers.IO) {
        try {
            if (!indexFile.exists()) {
                println("No index file found at: ${indexFile.absolutePath}")
                return@withContext null
            }

            // Read JSON file
            val jsonContent = indexFile.readText()

            // Parse JSON into IndexData object
            json.decodeFromString<IndexData>(jsonContent)
        } catch (e: Exception) {
            println("Error loading index: ${e.message}")
            null
        }
    }
}
