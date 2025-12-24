package com.rag.cli

import com.rag.service.RAGAgent
import kotlinx.coroutines.runBlocking

/**
 * Command to ask questions using RAG Agent.
 *
 * Supports two modes:
 * - WITH RAG: Question → Search → Context → LLM
 * - WITHOUT RAG: Question → LLM (direct)
 */
class AskCommand(
    private val ragAgent: RAGAgent = RAGAgent()
) {
    /**
     * Ask a question with RAG (retrieval + context).
     *
     * @param question The user's question
     * @param topK Number of chunks to retrieve (default: 3)
     * @param model LLM model to use (default: "llama2")
     */
    fun executeWithRAG(
        question: String,
        topK: Int = 3,
        model: String = "llama2"
    ) = runBlocking {
        println("=".repeat(60))
        println("🟢 RAG Mode (WITH Context Retrieval)")
        println("=".repeat(60))
        println()
        println("Question: \"$question\"")
        println()

        // Get answer with RAG
        val response = ragAgent.answerWithRAG(
            question = question,
            topK = topK,
            model = model
        )

        // Display retrieved chunks
        if (response.retrievedChunks.isNotEmpty()) {
            println("📚 Retrieved Context:")
            println()

            response.retrievedChunks.forEachIndexed { index, result ->
                println("${index + 1}. ${result.chunk.documentFilename} " +
                        "(similarity: ${"%.2f".format(result.similarity)})")
                println("   Chunk #${result.chunk.chunkIndex}: " +
                        "${result.chunk.startIndex}-${result.chunk.endIndex}")
                println()
                println("   ${result.chunk.content.trim()}")
                println()
                println("-".repeat(60))
                println()
            }
        } else {
            println("⚠️  No relevant chunks found (similarity < 0.3)")
            println()
        }

        // Display answer
        println("=".repeat(60))
        println("Answer:")
        println("=".repeat(60))
        println()
        println(response.answer)
        println()
    }

    /**
     * Ask a question without RAG (direct LLM query).
     *
     * @param question The user's question
     * @param model LLM model to use (default: "llama2")
     */
    fun executeWithoutRAG(
        question: String,
        model: String = "llama2"
    ) = runBlocking {
        println("=".repeat(60))
        println("🔴 Direct Mode (WITHOUT RAG)")
        println("=".repeat(60))
        println()
        println("Question: \"$question\"")
        println()

        // Get answer without RAG
        val response = ragAgent.answerWithoutRAG(
            question = question,
            model = model
        )

        // Display answer
        println("=".repeat(60))
        println("Answer:")
        println("=".repeat(60))
        println()
        println(response.answer)
        println()
    }
}
