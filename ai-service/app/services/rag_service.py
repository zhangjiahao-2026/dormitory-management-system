from __future__ import annotations

import hashlib
import math
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence


@dataclass(frozen=True)
class SopChunk:
    chunk_id: str
    document_id: str
    document: str
    section: str
    category: str
    department: str
    version: str
    content: str
    keywords: Sequence[str]


@dataclass(frozen=True)
class SearchResult:
    chunk_id: str
    document: str
    section: str
    category: str
    department: str
    version: str
    content: str
    score: float


def _front_matter(text: str) -> tuple[Dict[str, str], str]:
    if not text.startswith("---\n"):
        return {}, text
    end = text.find("\n---\n", 4)
    if end < 0:
        return {}, text
    metadata: Dict[str, str] = {}
    for line in text[4:end].splitlines():
        if ":" in line:
            key, value = line.split(":", 1)
            metadata[key.strip()] = value.strip()
    return metadata, text[end + 5 :]


def _tokens(text: str) -> List[str]:
    normalized = re.sub(r"\s+", "", text.lower())
    chinese = re.findall(r"[\u4e00-\u9fff]", normalized)
    bigrams = ["".join(chinese[i : i + 2]) for i in range(len(chinese) - 1)]
    words = re.findall(r"[a-z0-9_]+", normalized)
    return chinese + bigrams + words


def hash_embedding(text: str, dimensions: int = 384) -> List[float]:
    vector = [0.0] * dimensions
    for token in _tokens(text):
        digest = hashlib.sha256(token.encode("utf-8")).digest()
        index = int.from_bytes(digest[:4], "big") % dimensions
        sign = 1.0 if digest[4] % 2 == 0 else -1.0
        vector[index] += sign
    norm = math.sqrt(sum(value * value for value in vector)) or 1.0
    return [value / norm for value in vector]


def _cosine(left: Sequence[float], right: Sequence[float]) -> float:
    return sum(a * b for a, b in zip(left, right))


def load_sop_chunks(knowledge_dir: Path) -> List[SopChunk]:
    chunks: List[SopChunk] = []
    for path in sorted(knowledge_dir.glob("*.md")):
        metadata, body = _front_matter(path.read_text(encoding="utf-8"))
        if metadata.get("active", "true").lower() != "true":
            continue
        headings = list(re.finditer(r"^##\s+(.+)$", body, flags=re.MULTILINE))
        for index, heading in enumerate(headings):
            start = heading.end()
            end = headings[index + 1].start() if index + 1 < len(headings) else len(body)
            content = body[start:end].strip()
            if not content:
                continue
            section_key = heading.group(1).strip()
            chunk_id = f"{metadata.get('document_id', path.stem)}-{index + 1}"
            keywords = tuple(
                item.strip() for item in metadata.get("keywords", "").split(",") if item.strip()
            )
            chunks.append(
                SopChunk(
                    chunk_id=chunk_id,
                    document_id=metadata.get("document_id", path.stem),
                    document=metadata.get("title", path.stem),
                    section=section_key,
                    category=metadata.get("category", "OTHER"),
                    department=metadata.get("department", "综合维修组"),
                    version=metadata.get("version", "1.0"),
                    content=content,
                    keywords=keywords,
                )
            )
    return chunks


class HashEmbeddingFunction:
    def __call__(self, input: Iterable[str]) -> List[List[float]]:
        return [hash_embedding(text) for text in input]


class RagService:
    def __init__(self, knowledge_dir: Path, persist_dir: Optional[Path] = None):
        self.knowledge_dir = knowledge_dir
        self.persist_dir = persist_dir
        self.chunks = load_sop_chunks(knowledge_dir)
        self._collection = None
        self._initialize_chroma()

    def _initialize_chroma(self) -> None:
        if self.persist_dir is None:
            return
        try:
            import chromadb

            self.persist_dir.mkdir(parents=True, exist_ok=True)
            client = chromadb.PersistentClient(path=str(self.persist_dir))
            self._collection = client.get_or_create_collection(
                name="repair_sop",
                embedding_function=HashEmbeddingFunction(),
                metadata={"hnsw:space": "cosine"},
            )
            if self._collection.count() != len(self.chunks):
                if self._collection.count():
                    client.delete_collection("repair_sop")
                    self._collection = client.create_collection(
                        name="repair_sop",
                        embedding_function=HashEmbeddingFunction(),
                        metadata={"hnsw:space": "cosine"},
                    )
                self._collection.add(
                    ids=[chunk.chunk_id for chunk in self.chunks],
                    documents=[chunk.content for chunk in self.chunks],
                    metadatas=[self._metadata(chunk) for chunk in self.chunks],
                )
        except Exception:
            self._collection = None

    @staticmethod
    def _metadata(chunk: SopChunk) -> Dict[str, str]:
        return {
            "document": chunk.document,
            "section": chunk.section,
            "category": chunk.category,
            "department": chunk.department,
            "version": chunk.version,
            "keywords": ",".join(chunk.keywords),
        }

    def search(self, query: str, category: Optional[str] = None, top_k: int = 3) -> List[SearchResult]:
        candidates = self._chroma_candidates(query) if self._collection is not None else self.chunks
        query_tokens = set(_tokens(query))
        query_vector = hash_embedding(query)
        scored: List[tuple[float, SopChunk]] = []
        for chunk in candidates:
            if category and category != "OTHER" and chunk.category not in {category, "EMERGENCY"}:
                continue
            semantic = max(0.0, _cosine(query_vector, hash_embedding(chunk.content)))
            keyword_hits = sum(1 for keyword in chunk.keywords if keyword and keyword in query)
            token_overlap = len(query_tokens.intersection(_tokens(chunk.content))) / max(len(query_tokens), 1)
            score = min(1.0, semantic * 0.55 + token_overlap * 0.25 + min(keyword_hits, 3) * 0.1)
            scored.append((score, chunk))
        scored.sort(key=lambda item: (-item[0], item[1].chunk_id))
        return [self._result(chunk, score) for score, chunk in scored[:top_k] if score > 0]

    def _chroma_candidates(self, query: str) -> List[SopChunk]:
        count = min(5, len(self.chunks))
        if count == 0:
            return []
        try:
            result = self._collection.query(query_texts=[query], n_results=count)
            ids = result.get("ids", [[]])[0]
            by_id = {chunk.chunk_id: chunk for chunk in self.chunks}
            return [by_id[item] for item in ids if item in by_id]
        except Exception:
            return self.chunks

    @staticmethod
    def _result(chunk: SopChunk, score: float) -> SearchResult:
        return SearchResult(
            chunk_id=chunk.chunk_id,
            document=chunk.document,
            section=chunk.section,
            category=chunk.category,
            department=chunk.department,
            version=chunk.version,
            content=chunk.content,
            score=round(score, 4),
        )
