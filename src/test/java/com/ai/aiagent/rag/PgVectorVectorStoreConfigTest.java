package com.ai.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PgVectorVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(
                new Document("您在创建虚拟交换机时没有选择正确的专有网络。请确保您已经创建了专有网络并且选择的专有网络与您的虚拟交换机在同一区域内。", Map.of("meta1", "meta1")),
                new Document("如果您在迁移服务器的过程中遇到这个问题，可能是因为在迁移设置时需要选择迁移后的专有网络和虚拟交换机，但您选择的专有网络中没有可用的虚拟交换机。"),
                new Document("今天晚上吃什么", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("为什么虚拟交换机无法启动").topK(5).build());
        Assertions.assertNotNull(results);
    }
}
