package com.rag

import com.rag.cli.IndexCommand
import com.rag.cli.AskCommand

/**
 * Main entry point for the RAG application.
 *
 * Handles command-line arguments and routes to appropriate commands.
 */
fun main(args: Array<String>) {
    when {
        // No arguments - show usage
        args.isEmpty() -> printUsage()

        // Index command: ./gradlew run --args="index documents/"
        args[0] == "index" && args.size >= 2 -> {
            val directoryPath = args[1]
            IndexCommand().execute(directoryPath)
        }

        // Ask with RAG: ./gradlew run --args="ask <question> [--min-similarity=0.0-1.0]"
        args[0] == "ask" && args.size >= 2 -> {
            // Parse optional --min-similarity parameter
            var minSimilarity = 0.3  // default threshold

            args.drop(1).forEach { arg ->
                if (arg.startsWith("--min-similarity=")) {
                    val value = arg.substringAfter("=").toDoubleOrNull()
                    require(value != null) { "Invalid min-similarity value. Must be a number between 0.0 and 1.0" }
                    require(value in 0.0..1.0) { "min-similarity must be between 0.0 and 1.0, got: $value" }
                    minSimilarity = value
                }
            }

            // Extract question (filter out --min-similarity parameter)
            val question = args.drop(1)
                .filterNot { it.startsWith("--") }
                .joinToString(" ")

            AskCommand().executeWithRAG(
                question = question,
                minSimilarity = minSimilarity
            )
        }

        // Ask without RAG: ./gradlew run --args="ask-direct <question>"
        args[0] == "ask-direct" && args.size >= 2 -> {
            val question = args.drop(1).joinToString(" ")
            AskCommand().executeWithoutRAG(question)
        }

        // Health check: ./gradlew run --args="health"
        args[0] == "health" -> checkHealth()

        // Unknown command
        else -> printUsage()
    }
}

/**
 * Print usage instructions
 */
private fun printUsage() {
    println("""
        RAG Document Indexing Pipeline - Kotlin/JVM

        Usage:
          ./gradlew run --args="index <directory>"                      # Index documents
          ./gradlew run --args="ask <question> [--min-similarity=0.3]"  # Ask with RAG (retrieval + context)
          ./gradlew run --args="ask-direct <question>"                  # Ask without RAG (direct LLM)
          ./gradlew run --args="health"                                 # Check Ollama status

        Options:
          --min-similarity=<0.0-1.0>  Minimum similarity threshold for filtering results (default: 0.3)

        Examples:
          ./gradlew run --args="index documents/"
          ./gradlew run --args="ask What is Kotlin?"
          ./gradlew run --args="ask What is Kotlin? --min-similarity=0.7"
          ./gradlew run --args="ask-direct What is Kotlin?"
    """.trimIndent())
}

/**
 * Check if Ollama service is running
 */
private fun checkHealth() {
    println("🏥 Checking Ollama health...")

    val ollamaClient = com.rag.client.OllamaClient()
    val isHealthy = kotlinx.coroutines.runBlocking {
        ollamaClient.healthCheck()
    }

    if (isHealthy) {
        println("✅ Ollama is running and healthy!")
        println("   URL: http://localhost:11434")
    } else {
        println("❌ Ollama is not running!")
        println("   Start it with: ollama serve")
    }
}
