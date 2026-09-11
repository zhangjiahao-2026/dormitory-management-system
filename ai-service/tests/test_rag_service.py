from pathlib import Path

from dataclasses import replace

from app.services.rag_service import RagService, hash_embedding, knowledge_fingerprint, load_sop_chunks


KNOWLEDGE = Path(__file__).parents[1] / "knowledge"


def test_loads_active_sop_sections():
    chunks = load_sop_chunks(KNOWLEDGE)
    assert len(chunks) == 12
    assert all(chunk.version == "1.0" for chunk in chunks)
    assert {chunk.category for chunk in chunks} >= {"ELECTRICAL", "PLUMBING", "NETWORK"}


def test_hash_embedding_is_deterministic_and_normalized():
    first = hash_embedding("插座冒烟")
    second = hash_embedding("插座冒烟")
    assert first == second
    assert abs(sum(value * value for value in first) - 1.0) < 1e-6


def test_retrieves_electrical_sop_with_sources():
    results = RagService(KNOWLEDGE).search("插座突然冒烟并且有焦味", "ELECTRICAL")
    assert results
    assert results[0].document == "宿舍电气故障处理规范"
    assert results[0].section == "3.2 插座冒烟或火花"
    assert results[0].score > 0


def test_category_filter_excludes_unrelated_sop():
    results = RagService(KNOWLEDGE).search("洗手池下面一直滴水", "PLUMBING")
    assert results
    assert all(result.category in {"PLUMBING", "EMERGENCY"} for result in results)


def test_empty_query_has_no_results():
    assert RagService(KNOWLEDGE).search("", "OTHER") == []


def test_knowledge_fingerprint_changes_when_content_changes_without_count_change():
    chunks = load_sop_chunks(KNOWLEDGE)
    changed = list(chunks)
    changed[0] = replace(changed[0], content=changed[0].content + " 修订")
    assert len(changed) == len(chunks)
    assert knowledge_fingerprint(changed) != knowledge_fingerprint(chunks)
