package com.ai.knowledge.vector.domain.rag.query.impl;

import com.ai.knowledge.vector.domain.rag.query.QueryRewriteDomainService;
import com.ai.knowledge.vector.domain.rag.query.QueryRewriteResult;
import com.ai.knowledge.vector.domain.rag.query.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询改写领域服务实现
 * 遵循DDD规范，使用AI模型进行智能化查询改写
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryRewriteDomainServiceImpl implements QueryRewriteDomainService {

    private final ChatModel chatModel;

    @Override
    public SearchQuery rewriteQuery(SearchQuery searchQuery) {
        log.info("开始改写查询: {}", searchQuery.getOriginalQuery());

        String promptTemplate = """
            请分析并优化以下搜索查询，使其更清晰、更具体，更适合搜索引擎理解：

            原始查询：%s

            要求：
            1. 保持原意不变
            2. 使查询更加具体和明确
            3. 添加相关的关键词或同义词
            4. 如果查询模糊，请提出更精确的表述
            5. 直接返回优化后的查询，不要有任何解释
            优化后的查询：
            """;

        String prompt = String.format(promptTemplate, searchQuery.getOriginalQuery());

        try {
            var response = chatModel.call(new Prompt(prompt, OllamaChatOptions.builder().temperature(0.1).build()));
            String rewrittenQuery = response.getResult().getOutput().getText().trim();

            log.info("查询改写完成: {} -> {}", searchQuery.getOriginalQuery(), rewrittenQuery);
            return searchQuery.withRewrite(rewrittenQuery);
        } catch (Exception e) {
            log.error("查询改写出错，使用原始查询: {}", e.getMessage());
            return searchQuery; // 出错时返回原始查询
        }
    }

    @Override
    public QueryRewriteResult decomposeQuery(SearchQuery searchQuery) {
        log.info("分解复杂查询: {}", searchQuery.getOriginalQuery());

        String promptTemplate = """
            请将以下复杂查询分解为多个简单的子查询，每个子查询都应该聚焦于一个特定的概念或主题：

            复杂查询：%s

            要求：
            1. 分解为3-5个独立的子查询
            2. 每个子查询应该简洁明了
            3. 每行一个子查询
            4. 不要包含任何解释，只输出子查询
            5. 按重要性排序

            子查询列表：
            """;

        String prompt = String.format(promptTemplate, searchQuery.getOriginalQuery());

        try {
            var response = chatModel.call(new Prompt(prompt, OllamaChatOptions.builder().temperature(0.1).build()));
            String[] subQueries = response.getResult().getOutput().getText()
                    .trim()
                    .split("\\r?\\n"); // 按行分割

            // 过滤掉空行
            List<String> filteredSubQueries = Arrays.stream(subQueries)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            return new QueryRewriteResult(
                    searchQuery.getOriginalQuery(),
                    searchQuery.getRewrittenQuery(),
                    filteredSubQueries,
                    Collections.singletonList(searchQuery.getOriginalQuery())
            );
        } catch (Exception e) {
            log.error("查询分解出错: {}", e.getMessage());
            // 出错时返回原始查询作为单一元素列表
            return new QueryRewriteResult(
                    searchQuery.getOriginalQuery(),
                    searchQuery.getRewrittenQuery(),
                    Collections.singletonList(searchQuery.getOriginalQuery()),
                    Collections.singletonList(searchQuery.getOriginalQuery())
            );
        }
    }

    @Override
    public QueryRewriteResult expandKeywords(SearchQuery searchQuery) {
        log.info("扩展查询关键词: {}", searchQuery.getOriginalQuery());

        String promptTemplate = """
            请为以下查询扩展相关的关键词和同义词：

            查询：%s

            要求：
            1. 提供与查询相关的关键词和同义词
            2. 包括概念相近的术语
            3. 每行一个关键词
            4. 不要包含任何解释，只输出关键词列表
            5. 按相关性排序

            扩展关键词列表：
            """;

        String prompt = String.format(promptTemplate, searchQuery.getOriginalQuery());

        try {
            var response = chatModel.call(new Prompt(prompt, OllamaChatOptions.builder().temperature(0.1).build()));
            String[] keywords = response.getResult().getOutput().getText()
                    .trim()
                    .split("\\r?\\n"); // 按行分割

            // 过滤掉空行并添加原始查询
            List<String> keywordList = Arrays.stream(keywords)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            keywordList.add(0, searchQuery.getOriginalQuery()); // 将原始查询放在首位

            return new QueryRewriteResult(
                    searchQuery.getOriginalQuery(),
                    searchQuery.getRewrittenQuery(),
                    Collections.singletonList(searchQuery.getOriginalQuery()),
                    keywordList
            );
        } catch (Exception e) {
            log.error("关键词扩展出错: {}", e.getMessage());
            // 出错时返回原始查询
            return new QueryRewriteResult(
                    searchQuery.getOriginalQuery(),
                    searchQuery.getRewrittenQuery(),
                    Collections.singletonList(searchQuery.getOriginalQuery()),
                    Collections.singletonList(searchQuery.getOriginalQuery())
            );
        }
    }

    @Override
    public QueryRewriteResult completeRewrite(SearchQuery searchQuery) {
        log.info("执行完整查询改写流程: {}", searchQuery.getOriginalQuery());

        // 首先改写查询
        SearchQuery rewrittenQuery = rewriteQuery(searchQuery);

        // 然后分解查询
        QueryRewriteResult decomposedResult = decomposeQuery(rewrittenQuery);

        // 最后扩展关键词
        QueryRewriteResult expandedResult = expandKeywords(rewrittenQuery);

        // 组合结果
        return new QueryRewriteResult(
                searchQuery.getOriginalQuery(),
                rewrittenQuery.getRewrittenQuery(),
                decomposedResult.getDecomposedQueries(),
                expandedResult.getExpandedKeywords()
        );
    }
}