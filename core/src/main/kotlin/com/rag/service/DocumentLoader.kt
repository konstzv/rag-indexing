package com.rag.service

import com.rag.domain.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.io.File

/**
 * Service for loading documents from the filesystem.
 *
 * Currently supports only .txt files.
 */
class DocumentLoader {
    /**
     * Load a single text file as a Document.
     *
     * @param file The file to load
     * @return Document object containing the file's content
     * @throws IllegalArgumentException if file doesn't exist or isn't a .txt file
     */
    suspend fun loadDocument(file: File): Document = withContext(Dispatchers.IO) {
        // Validate the file
        require(file.exists()) { "File does not exist: ${file.absolutePath}" }
        require(file.isFile) { "Not a file: ${file.absolutePath}" }

        // For now, only support .txt files
        require(file.extension.lowercase() == "txt") {
            "Only .txt files supported for now. Got: ${file.extension}"
        }

        // Read the entire file content as a string
        val content = file.readText()

        // Create and return a Document object
        Document(
            filename = file.name,
            content = content,
            metadata = mapOf(
                "path" to file.absolutePath,
                "size" to file.length().toString(),
                "extension" to file.extension
            ),
            indexedAt = Clock.System.now()
        )
    }

    /**
     * Load all .txt files from a directory.
     *
     * Walks through the directory tree and loads all .txt files.
     *
     * @param directory The directory to scan
     * @return List of Document objects, one per .txt file found
     * @throws IllegalArgumentException if directory doesn't exist
     */
    suspend fun loadDirectory(directory: File): List<Document> = withContext(Dispatchers.IO) {
        require(directory.exists()) { "Directory does not exist: ${directory.absolutePath}" }
        require(directory.isDirectory) { "Not a directory: ${directory.absolutePath}" }

        // First, collect all .txt files
        val txtFiles = directory.walkTopDown()
            .filter { it.isFile }                    // Only files (not directories)
            .filter { it.extension == "txt" }       // Only .txt files
            .toList()

        // Then load each file as a Document (suspend function)
        txtFiles.map { file ->
            loadDocument(file)
        }
    }
}
