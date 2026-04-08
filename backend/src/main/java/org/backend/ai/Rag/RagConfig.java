package org.backend.ai.Rag;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagConfig {
    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public ContentRetriever contentRetriever() {

        //1. 加载文档
        List<dev.langchain4j.data.document.Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
        //2. 分割文档
        DocumentByParagraphSplitter documentSplitter =
                new DocumentByParagraphSplitter(
                1000,
                200
        );
        //3. 定义文档加载器，将文档转换为向量并存储
        EmbeddingStoreIngestor ingestor=EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .textSegmentTransformer(textSegment -> TextSegment.from(textSegment.metadata()
                        .getString("file_name")+ "\n" + textSegment.text(),textSegment.metadata()))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        //4.加载文档
        ingestor.ingest(documents);

        //5.定义内容检索器
        EmbeddingStoreContentRetriever contentRetriever =EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)//最多5条
                .minScore(0.75)//过滤小于0.75的结果
                .build();

        return contentRetriever;

    }
}
