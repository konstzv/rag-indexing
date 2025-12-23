# Privacy & Security Fixes

This document outlines the changes made to ensure the project can be shared publicly without exposing sensitive information.

## Issues Fixed

### 1. ✅ Hardcoded Personal Paths Removed

**Before:**
- `/Users/konstantinzagulin/IdeaProjects/RAG/...` (hardcoded in multiple files)
- `/Users/konstantinzagulin/Documents/my-notes` (in examples)

**After:**
- Relative paths: `./output`, `./documents`
- Generic placeholders: `/path/to/your/documents`

### 2. ✅ Configuration Made Flexible

**JsonEmbeddingStorage.kt** - Now accepts custom output directory:

```kotlin
// Before (hardcoded):
File("/Users/konstantinzagulin/IdeaProjects/RAG/output")

// After (configurable):
class JsonEmbeddingStorage(
    private val outputDir: String = "./output"
)
```

**Benefits:**
- Users can configure their own paths
- Works on any system without modification
- No personal information exposed

### 3. ✅ .gitignore Created

Added comprehensive `.gitignore` to prevent committing:
- Build artifacts (`build/`, `.gradle/`)
- IDE files (`.idea/`, `*.iml`)
- Generated indices (`output/`, `*.json`)
- Temporary files (`*.tmp`, `*.log`)
- macOS files (`.DS_Store`)

## Files Modified

| File | Changes |
|------|---------|
| `.gitignore` | ✨ Created |
| `core/src/main/kotlin/com/rag/repository/JsonEmbeddingStorage.kt` | Made output path configurable |
| `app/src/main/kotlin/com/rag/Main.kt` | Replaced personal path examples |
| `README.md` | Replaced all personal paths with generic ones |

## Files to Exclude from Public Sharing

Make sure these are in `.gitignore`:

```
# Already ignored:
output/                  # Contains generated embeddings.json
build/                   # Build artifacts
.gradle/                 # Gradle cache
.idea/                   # IDE settings

# Should NOT be ignored:
documents/               # Keep sample documents for demo
gradle/                  # Gradle wrapper (needed for builds)
gradlew                  # Gradle wrapper script
src/                     # Source code
README.md               # Documentation
```

## Verification Checklist

Before sharing publicly:

- [x] No API keys or secrets in code
- [x] No personal file paths in source code
- [x] No usernames in examples
- [x] `.gitignore` created and comprehensive
- [x] Build still works with changes
- [x] README uses generic paths
- [x] Configuration is flexible/customizable

## Testing After Changes

```bash
# Clean build
./gradlew clean build

# Test with relative path (from project root)
./gradlew run --args="index documents/"

# Verify output location
ls -la ./output/embeddings.json
```

## For Future Contributors

### Adding New Features

When adding new features that use file paths:

1. **Use relative paths** when possible (`./output/file.json`)
2. **Make paths configurable** via constructor parameters
3. **Avoid hardcoding** system-specific paths
4. **Test on different systems** (Windows, macOS, Linux)

### Example - Good vs Bad

❌ **Bad:**
```kotlin
val file = File("/Users/username/Documents/data.json")
```

✅ **Good:**
```kotlin
class MyService(
    private val dataDir: String = "./data"
) {
    val file = File(dataDir, "data.json")
}
```

## Privacy Score

**Before:** ⚠️ Not ready for public sharing
**After:** ✅ Safe to share publicly

All personal information has been removed or made configurable.

---

**Last Updated:** December 23, 2025
