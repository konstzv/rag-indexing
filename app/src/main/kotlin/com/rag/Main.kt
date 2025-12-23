package com.rag

import com.rag.cli.IndexCommand

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
          ./gradlew run --args="index <directory>"     # Index documents
          ./gradlew run --args="health"                # Check Ollama status

        Examples:
          ./gradlew run --args="index documents/"
          ./gradlew run --args="index /path/to/your/documents"
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
