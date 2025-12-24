package com.rag.service

import com.rag.client.OllamaClient
import com.rag.domain.SearchResult

/**
 * RAG Agent implements the full Retrieval-Augmented Generation pipeline.
 *
 * Pipeline:
 * 1. Question → Generate query embedding
 * 2. Search for relevant chunks using vector similarity
 * 3. Combine question + context from chunks
 * 4. Send to LLM for answer generation
 *
 * @param embeddingService Service for generating embeddings
 * @param vectorSearch Service for searching similar chunks
 * @param ollamaClient Client for LLM text generation
 */
class RAGAgent(
    private val embeddingService: EmbeddingService = EmbeddingService(),
    private val vectorSearch: VectorSearch = VectorSearch(),
    private val ollamaClient: OllamaClient = OllamaClient()
) {
    /**
     * Answer a question using RAG (Retrieval-Augmented Generation).
     *
     * Process:
     * 1. Convert question to embedding vector
     * 2. Search for top-K most similar chunks
     * 3. Build context from retrieved chunks
     * 4. Create augmented prompt (question + context)
     * 5. Generate answer using LLM
     *
     * @param question User's question
     * @param topK Number of chunks to retrieve (default: 3)
     * @param model LLM model to use (default: "llama2")
     * @return RAGResponse with answer and retrieved chunks
     */
    suspend fun answerWithRAG(
        question: String,
        topK: Int = 3,
        model: String = "llama2"
    ): RAGResponse {
        // Step 1: Generate embedding for the question
        val queryEmbedding = embeddingService.generateQueryEmbedding(question)

        // Step 2: Search for relevant chunks
        val searchResults = vectorSearch.search(
            queryEmbedding = queryEmbedding,
            topK = topK,
            minSimilarity = 0.3  // Only use chunks with >30% similarity
        )

        // Step 3: Build context from retrieved chunks
        val context = buildContext(searchResults)

        // Step 4: Create augmented prompt
        val augmentedPrompt = buildPrompt(question, context)

        // Step 5: Generate answer using LLM
        val answer = ollamaClient.generateText(
            prompt = augmentedPrompt,
            model = model
        )

        return RAGResponse(
            question = question,
            answer = answer,
            retrievedChunks = searchResults,
            usedRAG = true
        )
    }

    /**
     * Answer a question WITHOUT RAG (no context retrieval).
     *
     * Just sends the raw question to the LLM.
     *
     * @param question User's question
     * @param model LLM model to use (default: "llama2")
     * @return RAGResponse with answer (no retrieved chunks)
     */
    suspend fun answerWithoutRAG(
        question: String,
        model: String = "llama2"
    ): RAGResponse {
        // Just ask the LLM directly without any context
        val answer = ollamaClient.generateText(
            prompt = question,
            model = model
        )

        return RAGResponse(
            question = question,
            answer = answer,
            retrievedChunks = emptyList(),
            usedRAG = false
        )
    }

    /**
     * Build context string from search results.
     *
     * Combines the content of top chunks with metadata.
     *
     * Example output:
     * ```
     * [Source: kotlin.txt, Similarity: 0.87]
     * Kotlin is a modern programming language...
     *
     * [Source: ml.txt, Similarity: 0.65]
     * Machine learning uses neural networks...
     * ```
     */
    private fun buildContext(searchResults: List<SearchResult>): String {
        if (searchResults.isEmpty()) {
            return "No relevant information found in the knowledge base."
        }

        return searchResults.joinToString("\n\n") { result ->
            """
            [Source: ${result.chunk.documentFilename}, Similarity: ${"%.2f".format(result.similarity)}]
            ${result.chunk.content.trim()}
            """.trimIndent()
        }
    }

    /**
     * Build augmented prompt combining question and context.
     *
     * The prompt instructs the LLM to answer based on the provided context.
     */
    private fun buildPrompt(question: String, context: String): String {
        return """
        You are a helpful assistant. Answer the question based on the context provided below.
        If the context doesn't contain relevant information, say so clearly.

        Context:
        $context

        Question: $question

        Answer:
        """.trimIndent()
    }
}

/**
 * Response from RAG Agent.
 *
 * @param question The original question
 * @param answer The generated answer
 * @param retrievedChunks List of chunks used for context (empty if no RAG)
 * @param usedRAG Whether RAG was used (true) or direct query (false)
 */
data class RAGResponse(
    val question: String,
    val answer: String,
    val retrievedChunks: List<SearchResult>,
    val usedRAG: Boolean
)
