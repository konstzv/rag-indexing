# RAG Document Indexing Pipeline

A **Retrieval Augmented Generation (RAG)** document indexing system built with Kotlin/JVM. This project loads text documents, splits them into chunks, generates vector embeddings using Ollama AI, and saves them for semantic search.

## What is RAG?

RAG (Retrieval Augmented Generation) is a technique that enhances AI responses by:
1. **Indexing** your documents into searchable vectors (embeddings)
2. **Retrieving** relevant chunks based on semantic similarity
3. **Augmenting** AI prompts with relevant context

This project implements the **indexing** part - converting documents into searchable vector embeddings.

## Features

- 📂 **Document Loading** - Load `.txt` files from any directory
- ✂️ **Smart Chunking** - Split documents with overlapping chunks to preserve context
- 🧠 **AI Embeddings** - Generate 768-dimensional vectors using Ollama's nomic-embed-text model
- 💾 **JSON Storage** - Save index to a portable JSON file
- 🏥 **Health Checks** - Verify Ollama service is running

## Architecture

### Project Structure

```
RAG/
├── core/                           # Core library module
│   └── src/main/kotlin/com/rag/
│       ├── domain/                 # Data models
│       │   ├── Document.kt
│       │   ├── Chunk.kt
│       │   ├── Embedding.kt
│       │   └── IndexMetadata.kt
│       ├── service/                # Business logic
│       │   ├── DocumentLoader.kt
│       │   ├── TextChunker.kt
│       │   └── EmbeddingService.kt
│       ├── repository/             # Storage layer
│       │   └── JsonEmbeddingStorage.kt
│       └── client/                 # External API clients
│           └── OllamaClient.kt
├── app/                            # CLI application module
│   └── src/main/kotlin/com/rag/
│       ├── Main.kt                 # Entry point
│       └── cli/
│           └── IndexCommand.kt     # Indexing command
├── documents/                      # Place your .txt files here
│   ├── kotlin.txt
│   └── ml.txt
└── output/                         # Generated index files
    └── embeddings.json
```

### Tech Stack

- **Language**: Kotlin 1.9.22
- **Build**: Gradle with Kotlin DSL
- **JDK**: 17+
- **Dependencies**:
  - Ktor Client 2.3.7 (HTTP client for Ollama API)
  - Kotlinx Serialization 1.6.2 (JSON handling)
  - Kotlinx Coroutines 1.7.3 (Async operations)
  - Kotlinx DateTime 0.5.0 (Timestamps)

## Prerequisites

1. **Java 17+** installed
2. **Ollama** installed and running
3. **nomic-embed-text** model pulled

### Install Ollama

```bash
# macOS
brew install ollama

# Start Ollama service
ollama serve

# In another terminal, pull the embedding model
ollama pull nomic-embed-text
```

## Quick Start

### 1. Clone and Navigate

```bash
cd /path/to/RAG
```

### 2. Build the Project

```bash
./gradlew build
```

### 3. Check Ollama Health

```bash
./gradlew run --args="health"
```

Expected output:
```
✅ Ollama is running and healthy!
   URL: http://localhost:11434
```

### 4. Index Your Documents

```bash
./gradlew run --args="index documents/"
# Or use absolute path:
./gradlew run --args="index /absolute/path/to/RAG/documents"
```

Expected output:
```
============================================================
RAG Document Indexing Pipeline
============================================================

📂 Loading documents from: documents/
✓ Loaded 2 .txt document(s)

✂️  Chunking documents (size: 500, overlap: 150)...
✓ Created 5 chunks

🧠 Generating embeddings with nomic-embed-text...
   (This may take a while - each chunk is sent to Ollama)
  Progress: 1/5
✓ Generated 5 embeddings (768-dim vectors)

💾 Saving index...
Index saved to: ./output/embeddings.json
✓ Index saved successfully!

============================================================
Summary:
  Documents: 2
  Chunks: 5
  Embeddings: 5
  Vector dimensions: 768
============================================================
```

## How It Works

### The Indexing Pipeline

```
📄 Text Documents
    ↓
1. DocumentLoader
    ↓
📋 Document Objects
    ↓
2. TextChunker (500 chars, 150 overlap)
    ↓
📦 Chunks
    ↓
3. EmbeddingService + OllamaClient
    ↓
🧮 768-dimensional Vectors
    ↓
4. JsonEmbeddingStorage
    ↓
💾 embeddings.json
```

### 1. Document Loading

The `DocumentLoader` service reads `.txt` files and creates `Document` objects:

```kotlin
Document(
    id = "uuid",
    filename = "kotlin.txt",
    content = "Full text content...",
    metadata = mapOf("path" to "/path/to/file", "size" to "1024"),
    indexedAt = Instant.now()
)
```

### 2. Text Chunking

The `TextChunker` splits documents into overlapping segments:

```
Original text (1000 chars):
"Kotlin is a modern language. Coroutines are powerful..."

Chunk 0: chars 0-500
"Kotlin is a modern language. Coroutines are powerful..."

Chunk 1: chars 350-850 (150 chars overlap)
"...Coroutines are powerful. Type safety is built-in..."

Chunk 2: chars 700-1200
"...Type safety is built-in. Data classes are useful..."
```

**Why overlap?** Ensures context isn't lost at chunk boundaries.

### 3. Embedding Generation

The `EmbeddingService` sends each chunk to Ollama and receives a 768-dimensional vector:

```
Input:  "Kotlin is a modern language"
        ↓ Ollama API
Output: [0.234, -0.456, 0.789, ... ] (768 numbers)
```

These vectors capture the **semantic meaning** of the text. Similar texts produce similar vectors!

### 4. Storage

Everything is saved to `output/embeddings.json`:

```json
{
  "chunks": [ /* All text chunks */ ],
  "embeddings": [ /* All 768-dim vectors */ ],
  "metadata": {
    "totalDocuments": 2,
    "totalChunks": 5,
    "model": "nomic-embed-text",
    "chunkSize": 500,
    "overlapSize": 150,
    "createdAt": "2025-12-23T01:44:00Z"
  },
  "version": "1.0"
}
```

## Usage Examples

### Index a Single Directory

```bash
./gradlew run --args="index /path/to/documents"
```

### Index Your Personal Notes

```bash
./gradlew run --args="index /path/to/your/notes"
```

### Add New Documents

1. Drop new `.txt` files into the `documents/` folder
2. Re-run the index command
3. The index is completely rebuilt with all documents

## Key Concepts

### Embeddings

An **embedding** is a mathematical representation of text meaning:
- Text → AI Model → List of 768 numbers
- Similar meanings = vectors close together in 768D space
- NOT keyword matching - understands **semantic similarity**

Example:
```
"dog" and "puppy" → very similar vectors
"dog" and "car"   → very different vectors
```

### Chunking

Large documents are split into smaller pieces because:
- AI models have token limits
- Smaller chunks = more precise retrieval
- Overlapping chunks preserve context at boundaries

**Settings:**
- `chunkSize: 500` characters
- `overlapSize: 150` characters

### Cosine Similarity

How we compare vectors:
```
similarity = cos(θ) = (A · B) / (||A|| × ||B||)

1.0  = identical meaning
0.7+ = very similar
0.5  = somewhat related
0.0  = unrelated
```

## Configuration

Edit settings in the source code:

### Chunk Size

In `TextChunker` (core/src/main/kotlin/com/rag/service/TextChunker.kt):
```kotlin
class TextChunker(
    private val chunkSize: Int = 500,      // Change this
    private val overlapSize: Int = 150     // And this
)
```

### Storage Location

In `JsonEmbeddingStorage` (core/src/main/kotlin/com/rag/repository/JsonEmbeddingStorage.kt):
```kotlin
class JsonEmbeddingStorage(
    private val outputDir: String = "./output"  // Change this
)
```

### Ollama URL

In `OllamaClient` (core/src/main/kotlin/com/rag/client/OllamaClient.kt):
```kotlin
class OllamaClient(
    private val baseUrl: String = "http://localhost:11434"  // Change if needed
)
```

## Troubleshooting

### Ollama Not Running

```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# If not, start it
ollama serve
```

### Model Not Found

```bash
# Pull the embedding model
ollama pull nomic-embed-text

# Verify it's installed
ollama list
```

### Build Errors

```bash
# Clean and rebuild
./gradlew clean build

# Check Java version (needs 17+)
java -version
```

### Out of Memory

If indexing large documents:
```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx4g"
./gradlew run --args="index /path/to/documents"
```

## Future Enhancements

This project currently implements **document loading, chunking, and embedding**. Future additions could include:

### Search Functionality
- Implement vector similarity search
- Return top-K most relevant chunks
- Add relevance scoring

### More Document Formats
- PDF support (Apache PDFBox)
- Markdown files
- Word documents
- HTML/Web pages

### Better Chunking
- Sentence-aware splitting
- Paragraph-based chunking
- Recursive chunking strategies

### Performance
- Parallel embedding generation
- Batch processing
- Caching frequently used embeddings

### Storage Upgrades
- Replace JSON with Qdrant (vector database)
- Add HNSW indexing for fast search
- Implement incremental updates

### Web Interface
- Ktor REST API
- React/Vue frontend
- Real-time search UI

## Project Details

- **Created**: December 23, 2025
- **Language**: Kotlin 1.9.22
- **JDK**: 17+
- **Build Tool**: Gradle 8.14
- **License**: MIT (or your choice)

## Contributing

To add features:

1. Core logic goes in `core/` module
2. CLI commands go in `app/cli/`
3. Keep services in `core/src/main/kotlin/com/rag/service/`
4. Add tests in `core/src/test/kotlin/`

## Resources

- [Ollama Documentation](https://github.com/ollama/ollama)
- [Nomic Embed Text Model](https://huggingface.co/nomic-ai/nomic-embed-text-v1)
- [RAG Explained](https://www.pinecone.io/learn/retrieval-augmented-generation/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Ktor Documentation](https://ktor.io/)

## Contact

For questions or issues, please check:
- Source code documentation in each file
- Ollama logs: `ollama logs`
- [Ollama GitHub Issues](https://github.com/ollama/ollama/issues)

---

**Happy Indexing!** 🚀
