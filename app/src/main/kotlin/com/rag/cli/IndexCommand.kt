package com.rag.cli

import com.rag.client.OllamaClient
import com.rag.domain.IndexMetadata
import com.rag.repository.JsonEmbeddingStorage
import com.rag.service.DocumentLoader
import com.rag.service.EmbeddingService
import com.rag.service.TextChunker
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.io.File

/**
 * Command to index documents from a directory.
 *
 * This orchestrates the entire RAG indexing pipeline:
 * 1. Load documents from directory
 * 2. Chunk documents into smaller pieces
 * 3. Generate embeddings for each chunk
 * 4. Save everything to JSON file
 */
class IndexCommand(
    private val documentLoader: DocumentLoader = DocumentLoader(),
    private val textChunker: TextChunker = TextChunker(),
    private val embeddingService: EmbeddingService = EmbeddingService(OllamaClient()),
    private val embeddingStorage: JsonEmbeddingStorage = JsonEmbeddingStorage()
) {
    /**
     * Execute the indexing pipeline.
     *
     * @param directoryPath Path to directory containing .txt files
     */
    fun execute(directoryPath: String) = runBlocking {
        println("=".repeat(60))
        println("RAG Document Indexing Pipeline")
        println("=".repeat(60))
        println()

        // ========== STEP 1: Load Documents ==========
        println("📂 Loading documents from: $directoryPath")
        val directory = File(directoryPath)
        val documents = documentLoader.loadDirectory(directory)
        println("✓ Loaded ${documents.size} .txt document(s)")
        println()

        // ========== STEP 2: Chunk Documents ==========
        println("✂️  Chunking documents (size: 500, overlap: 150)...")
        val allChunks = documents.flatMap { doc ->
            textChunker.chunkDocument(doc)
        }
        println("✓ Created ${allChunks.size} chunks")
        println()

        // ========== STEP 3: Generate Embeddings ==========
        println("🧠 Generating embeddings with nomic-embed-text...")
        println("   (This may take a while - each chunk is sent to Ollama)")
        val embeddings = allChunks.mapIndexed { index, chunk ->
            // Show progress every 10 chunks
            if ((index + 1) % 10 == 0 || index == 0) {
                println("  Progress: ${index + 1}/${allChunks.size}")
            }
            embeddingService.generateEmbedding(chunk)
        }
        println("✓ Generated ${embeddings.size} embeddings (768-dim vectors)")
        println()

        // ========== STEP 4: Save Index ==========
        println("💾 Saving index...")
        val metadata = IndexMetadata(
            totalDocuments = documents.size,
            totalChunks = allChunks.size,
            model = "nomic-embed-text",
            chunkSize = 500,
            overlapSize = 150,
            createdAt = Clock.System.now()
        )

        embeddingStorage.saveIndex(allChunks, embeddings, metadata)
        println("✓ Index saved successfully!")
        println()

        println("=".repeat(60))
        println("Summary:")
        println("  Documents: ${documents.size}")
        println("  Chunks: ${allChunks.size}")
        println("  Embeddings: ${embeddings.size}")
        println("  Vector dimensions: 768")
        println("=".repeat(60))
    }
}
