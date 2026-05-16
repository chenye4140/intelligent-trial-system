package com.intelligent.trial.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.document.dto.ParseResultDTO;
import com.intelligent.trial.document.entity.DocParagraphVector;
import com.intelligent.trial.document.mapper.DocParagraphVectorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VectorStorageService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class VectorStorageServiceTest {

    @InjectMocks
    private VectorStorageService vectorStorageService;

    @Mock
    private DocParagraphVectorMapper vectorMapper;

    // ==================== cosineSimilarity Tests ====================

    @Test
    void cosineSimilarity_identicalVectors_shouldReturn1() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {1.0f, 0.0f, 0.0f};
        double result = VectorStorageService.cosineSimilarity(a, b);
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void cosineSimilarity_orthogonalVectors_shouldReturn0() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {0.0f, 1.0f, 0.0f};
        double result = VectorStorageService.cosineSimilarity(a, b);
        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void cosineSimilarity_oppositeVectors_shouldReturnNegative1() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {-1.0f, 0.0f, 0.0f};
        double result = VectorStorageService.cosineSimilarity(a, b);
        assertEquals(-1.0, result, 0.0001);
    }

    @Test
    void cosineSimilarity_partialSimilarity_shouldReturnBetween0And1() {
        float[] a = {1.0f, 1.0f, 0.0f};
        float[] b = {1.0f, 0.0f, 0.0f};
        double result = VectorStorageService.cosineSimilarity(a, b);
        // cos(45°) ≈ 0.707
        assertEquals(0.7071, result, 0.001);
    }

    @Test
    void cosineSimilarity_nullVectors_shouldReturn0() {
        assertEquals(0.0, VectorStorageService.cosineSimilarity(null, new float[]{1.0f}));
        assertEquals(0.0, VectorStorageService.cosineSimilarity(new float[]{1.0f}, null));
        assertEquals(0.0, VectorStorageService.cosineSimilarity(null, null));
    }

    @Test
    void cosineSimilarity_differentLengths_shouldReturn0() {
        float[] a = {1.0f, 0.0f};
        float[] b = {1.0f, 0.0f, 0.0f};
        assertEquals(0.0, VectorStorageService.cosineSimilarity(a, b));
    }

    @Test
    void cosineSimilarity_zeroVector_shouldReturn0() {
        float[] a = {0.0f, 0.0f, 0.0f};
        float[] b = {1.0f, 2.0f, 3.0f};
        assertEquals(0.0, VectorStorageService.cosineSimilarity(a, b));
    }

    @Test
    void cosineSimilarity_highDimensional_shouldWork() {
        float[] a = new float[100];
        float[] b = new float[100];
        Arrays.fill(a, 0.1f);
        Arrays.fill(b, 0.1f);
        double result = VectorStorageService.cosineSimilarity(a, b);
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void cosineSimilarity_normalizedVectors_shouldWork() {
        // Two normalized vectors
        float[] a = {0.57735027f, 0.57735027f, 0.57735027f}; // 1/sqrt(3)
        float[] b = {0.57735027f, 0.57735027f, 0.57735027f};
        double result = VectorStorageService.cosineSimilarity(a, b);
        assertEquals(1.0, result, 0.001);
    }

    // ==================== storeVectors Tests ====================

    @Test
    void storeVectors_shouldReturnEmptyList_whenVectorsEmpty() {
        List<Long> result = vectorStorageService.storeVectors(1L, Arrays.asList("text"),
                Collections.emptyList(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    void storeVectors_shouldReturnEmptyList_whenVectorsNull() {
        List<Long> result = vectorStorageService.storeVectors(1L, Arrays.asList("text"),
                null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void storeVectors_shouldInsertVectorsAndReturnIds() {
        List<String> texts = Arrays.asList("段落一", "段落二", "段落三");
        List<float[]> vectors = Arrays.asList(
                new float[]{0.1f, 0.2f, 0.3f},
                new float[]{0.4f, 0.5f, 0.6f},
                new float[]{0.7f, 0.8f, 0.9f}
        );

        List<ParseResultDTO.ParagraphDTO> paragraphs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ParseResultDTO.ParagraphDTO p = new ParseResultDTO.ParagraphDTO();
            p.setContent(texts.get(i));
            p.setCategory("总则");
            p.setLawLevel("条");
            paragraphs.add(p);
        }

        // Simulate auto-generated IDs
        when(vectorMapper.insert(any(DocParagraphVector.class))).thenAnswer(invocation -> {
            DocParagraphVector entity = invocation.getArgument(0);
            // Simulate auto-increment ID
            entity.setId((long) (invocation.getArguments().length + 100));
            return 1;
        });

        List<Long> result = vectorStorageService.storeVectors(1L, texts, vectors, paragraphs);

        assertEquals(3, result.size());
        verify(vectorMapper, times(3)).insert(any(DocParagraphVector.class));

        // Verify vector data was set correctly
        ArgumentCaptor<DocParagraphVector> captor = ArgumentCaptor.forClass(DocParagraphVector.class);
        verify(vectorMapper, times(3)).insert(captor.capture());

        List<DocParagraphVector> captured = captor.getAllValues();
        assertEquals(1L, captured.get(0).getTaskId());
        assertEquals(0, captured.get(0).getParagraphIndex());
        assertEquals("段落一", captured.get(0).getContent());
        assertEquals("总则", captured.get(0).getCategory());
        assertEquals("条", captured.get(0).getLawLevel());
        assertEquals(3, captured.get(0).getVectorDimension());
    }

    @Test
    void storeVectors_shouldBackfillVectorIdToParagraphs() {
        List<String> texts = Arrays.asList("段落一");
        List<float[]> vectors = Arrays.asList(new float[]{0.1f, 0.2f});

        ParseResultDTO.ParagraphDTO p = new ParseResultDTO.ParagraphDTO();
        p.setContent("段落一");
        List<ParseResultDTO.ParagraphDTO> paragraphs = Collections.singletonList(p);

        when(vectorMapper.insert(any(DocParagraphVector.class))).thenAnswer(invocation -> {
            DocParagraphVector entity = invocation.getArgument(0);
            entity.setId(42L);
            return 1;
        });

        vectorStorageService.storeVectors(1L, texts, vectors, paragraphs);

        assertEquals("42", p.getVectorId());
    }

    @Test
    void storeVectors_shouldHandleNullParagraphs() {
        List<String> texts = Arrays.asList("段落一", "段落二");
        List<float[]> vectors = Arrays.asList(
                new float[]{0.1f, 0.2f},
                new float[]{0.3f, 0.4f}
        );

        when(vectorMapper.insert(any(DocParagraphVector.class))).thenReturn(1);

        List<Long> result = vectorStorageService.storeVectors(1L, texts, vectors, null);

        assertEquals(2, result.size());
        verify(vectorMapper, times(2)).insert(any(DocParagraphVector.class));
    }

    @Test
    void storeVectors_shouldHandleMoreTextsThanVectors() {
        List<String> texts = Arrays.asList("段落一", "段落二", "段落三");
        List<float[]> vectors = Arrays.asList(
                new float[]{0.1f},
                new float[]{0.2f}
        );

        when(vectorMapper.insert(any(DocParagraphVector.class))).thenReturn(1);

        List<Long> result = vectorStorageService.storeVectors(1L, texts, vectors, null);

        assertEquals(2, result.size());
        // Third text should not be stored since there's no matching vector
        verify(vectorMapper, times(2)).insert(any(DocParagraphVector.class));
    }

    @Test
    void storeVectors_shouldHandleEmptyTexts() {
        List<String> texts = new ArrayList<>();
        List<float[]> vectors = Arrays.asList(
                new float[]{0.1f},
                new float[]{0.2f}
        );

        when(vectorMapper.insert(any(DocParagraphVector.class))).thenReturn(1);

        List<Long> result = vectorStorageService.storeVectors(1L, texts, vectors, null);

        assertEquals(2, result.size());
        // Empty text should still be stored
        ArgumentCaptor<DocParagraphVector> captor = ArgumentCaptor.forClass(DocParagraphVector.class);
        verify(vectorMapper, times(2)).insert(captor.capture());
        assertEquals("", captor.getAllValues().get(0).getContent());
    }

    // ==================== searchSimilar Tests ====================

    @Test
    void searchSimilar_shouldReturnEmpty_whenQueryVectorNull() {
        List<VectorStorageService.SimilarParagraphResult> result =
                vectorStorageService.searchSimilar(null, 10, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchSimilar_shouldReturnEmpty_whenQueryVectorEmpty() {
        List<VectorStorageService.SimilarParagraphResult> result =
                vectorStorageService.searchSimilar(new float[0], 10, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchSimilar_shouldReturnSortedResults() {
        float[] queryVector = {1.0f, 0.0f, 0.0f};

        // Create candidate vectors with different similarities
        List<DocParagraphVector> candidates = new ArrayList<>();

        DocParagraphVector v1 = new DocParagraphVector();
        v1.setId(1L);
        v1.setTaskId(10L);
        v1.setParagraphIndex(0);
        v1.setContent("Similar content");
        v1.setVectorData("[0.8, 0.1, 0.1]"); // High similarity

        DocParagraphVector v2 = new DocParagraphVector();
        v2.setId(2L);
        v2.setTaskId(10L);
        v2.setParagraphIndex(1);
        v2.setContent("Less similar content");
        v2.setVectorData("[0.3, 0.3, 0.3]"); // Lower similarity

        DocParagraphVector v3 = new DocParagraphVector();
        v3.setId(3L);
        v3.setTaskId(10L);
        v3.setParagraphIndex(2);
        v3.setContent("Orthogonal content");
        v3.setVectorData("[0.0, 1.0, 0.0]"); // Zero similarity

        candidates.add(v1);
        candidates.add(v2);
        candidates.add(v3);

        when(vectorMapper.searchSimilar(anyString(), anyInt(), isNull())).thenReturn(candidates);

        List<VectorStorageService.SimilarParagraphResult> result =
                vectorStorageService.searchSimilar(queryVector, 10, null);

        assertEquals(3, result.size());
        // Results should be sorted by similarity descending
        assertTrue(result.get(0).getSimilarity() >= result.get(1).getSimilarity());
        assertTrue(result.get(1).getSimilarity() >= result.get(2).getSimilarity());
        // Most similar should be first
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void searchSimilar_shouldLimitResults() {
        float[] queryVector = {1.0f, 0.0f};

        List<DocParagraphVector> candidates = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DocParagraphVector v = new DocParagraphVector();
            v.setId((long) (i + 1));
            v.setTaskId(10L);
            v.setParagraphIndex(i);
            v.setContent("Content " + i);
            v.setVectorData("[0.5, 0.5]");
            candidates.add(v);
        }

        when(vectorMapper.searchSimilar(anyString(), anyInt(), isNull())).thenReturn(candidates);

        List<VectorStorageService.SimilarParagraphResult> result =
                vectorStorageService.searchSimilar(queryVector, 5, null);

        assertEquals(5, result.size());
        // Should have fetched 25 candidates (5 * 5)
        verify(vectorMapper).searchSimilar(anyString(), eq(25), isNull());
    }

    @Test
    void searchSimilar_shouldFilterByCategory() {
        float[] queryVector = {1.0f, 0.0f};

        when(vectorMapper.searchSimilar(anyString(), anyInt(), eq("总则"))).thenReturn(new ArrayList<>());

        vectorStorageService.searchSimilar(queryVector, 10, "总则");

        verify(vectorMapper).searchSimilar(anyString(), anyInt(), eq("总则"));
    }

    @Test
    void searchSimilar_shouldSkipInvalidVectors() {
        float[] queryVector = {1.0f, 0.0f};

        DocParagraphVector valid = new DocParagraphVector();
        valid.setId(1L);
        valid.setTaskId(10L);
        valid.setParagraphIndex(0);
        valid.setContent("Valid");
        valid.setVectorData("[0.8, 0.2]");

        DocParagraphVector invalid = new DocParagraphVector();
        invalid.setId(2L);
        invalid.setTaskId(10L);
        invalid.setParagraphIndex(1);
        invalid.setContent("Invalid dimension");
        invalid.setVectorData("[0.8, 0.2, 0.1]"); // Different dimension

        when(vectorMapper.searchSimilar(anyString(), anyInt(), isNull()))
                .thenReturn(Arrays.asList(valid, invalid));

        List<VectorStorageService.SimilarParagraphResult> result =
                vectorStorageService.searchSimilar(queryVector, 10, null);

        // Only the valid vector should be returned
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ==================== getByTaskId Tests ====================

    @Test
    void getByTaskId_shouldReturnOrderedVectors() {
        List<DocParagraphVector> expected = new ArrayList<>();
        DocParagraphVector v1 = new DocParagraphVector();
        v1.setId(1L);
        v1.setTaskId(5L);
        v1.setParagraphIndex(0);
        v1.setContent("First");
        expected.add(v1);

        DocParagraphVector v2 = new DocParagraphVector();
        v2.setId(2L);
        v2.setTaskId(5L);
        v2.setParagraphIndex(1);
        v2.setContent("Second");
        expected.add(v2);

        when(vectorMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(expected);

        List<DocParagraphVector> result = vectorStorageService.getByTaskId(5L);

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getParagraphIndex());
        assertEquals(1, result.get(1).getParagraphIndex());
    }
}
