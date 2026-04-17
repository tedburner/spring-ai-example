package com.ai.chat.application.service;

import com.ai.chat.domain.entity.SearchResult;
import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Tavily 网络搜索服务 - 独立的搜索服务，避免循环依赖
 *
 * @author kiturone
 * @date 2026/04/13
 */
@Service
public class TavilyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TavilyService.class);

    private final RestClient restClient;

    @Value("${tavily.api.key:}")
    private String tavilyApiKey;

    public TavilyService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tavily.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Tavily 网络搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    public TavilySearchResponse tavilySearch(TavilySearchRequest request) {
        LOGGER.info("Tavily 搜索：{}", request.getQuery());

        if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
            throw new IllegalStateException("Tavily API Key 未配置，请在 application.yml 中设置 tavily.api.key");
        }

        Map<String, Object> body = Map.of(
                "query", request.getQuery(),
                "api_key", tavilyApiKey,
                "max_results", request.getMaxResults(),
                "search_depth", request.getSearchDepth(),
                "include_answer", request.getIncludeAnswer()
        );

        try {
            ResponseEntity<TavilySearchResponse> response = restClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(TavilySearchResponse.class);

            return response.getBody();
        } catch (Exception e) {
            LOGGER.error("Tavily 搜索失败", e);
            throw new RuntimeException("Tavily 搜索失败：" + e.getMessage(), e);
        }
    }

    /**
     * 简化的 Tavily 搜索方法（供 Tool 调用）
     *
     * @param query 查询词
     * @return 搜索结果文本
     */
    public String searchWeb(String query) {
        try {
            TavilySearchRequest request = TavilySearchRequest.builder()
                    .query(query)
                    .maxResults(3)
                    .searchDepth("basic")
                    .includeAnswer(true)
                    .build();

            TavilySearchResponse response = tavilySearch(request);

            StringBuilder result = new StringBuilder();
            if (response.getAnswer() != null && !response.getAnswer().isEmpty()) {
                result.append("答案：").append(response.getAnswer()).append("\n\n");
            }

            if (response.getResults() != null && !response.getResults().isEmpty()) {
                result.append("相关链接：\n");
                for (int i = 0; i < Math.min(3, response.getResults().size()); i++) {
                    SearchResult r = response.getResults().get(i);
                    result.append(String.format("%d. [%s](%s)\n   %s\n",
                            i + 1, r.getTitle(), r.getUrl(), r.getContent()));
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }
}