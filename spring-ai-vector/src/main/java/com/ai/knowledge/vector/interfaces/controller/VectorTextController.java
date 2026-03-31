package com.ai.knowledge.vector.interfaces.controller;

import com.ai.common.http.WebResult;
import com.ai.knowledge.vector.application.service.VectorApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量文本控制器
 * 遵循DDD规范，负责接收用户请求并返回响应
 *
 * @author kiturone
 * @date 2025/5/17 10:34
 */
@RestController
@RequestMapping("/vector/text")
public class VectorTextController {

    private final VectorApplicationService vectorApplicationService;

    public VectorTextController(VectorApplicationService vectorApplicationService) {
        this.vectorApplicationService = vectorApplicationService;
    }

    /**
     * 文本向量化
     *
     * @param text 文本
     * @return 向量表示
     */
    @GetMapping("/v1/embedding")
    public WebResult embedding(@RequestParam("text") String text) {
        return WebResult.buildSuccess(vectorApplicationService.embedding(text));
    }

    /**
     * 文本存储
     *
     * @param text 文本
     * @return 向量表示
     */
    @GetMapping("/v1/store")
    public WebResult store(@RequestParam("text") String text) {
        return WebResult.buildSuccess(vectorApplicationService.store(text));
    }

    /**
     * 自动存储
     *
     * @param text 文本
     * @return 向量表示
     */
    @GetMapping("/v1/auto/store")
    public WebResult autoStore(@RequestParam("text") String text) {
        return WebResult.buildSuccess(vectorApplicationService.autoStore(text));
    }

    /**
     * 混合搜索接口
     *
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param semanticWeight 语义搜索权重
     * @param keywordWeight 关键词搜索权重
     * @return 搜索结果
     */
    @GetMapping("/v1/hybrid/search")
    public WebResult hybridSearch(@RequestParam("query") String query,
                                 @RequestParam(value = "topK", defaultValue = "5") Integer topK,
                                 @RequestParam(value = "semanticWeight", defaultValue = "0.7") Double semanticWeight,
                                 @RequestParam(value = "keywordWeight", defaultValue = "0.3") Double keywordWeight) {

        List<org.springframework.ai.document.Document> results =
            vectorApplicationService.hybridRetrieval(query, topK, semanticWeight, keywordWeight, new HashMap<>());
        return WebResult.buildSuccess(results);
    }

    /**
     * 查询改写接口
     *
     * @param query 待改写的查询
     * @return 改写后的查询
     */
    @GetMapping("/v1/query/rewrite")
    public WebResult queryRewrite(@RequestParam("query") String query) {
        String rewrittenQuery = vectorApplicationService.rewriteQuery(query);
        return WebResult.buildSuccess(rewrittenQuery);
    }

    /**
     * 查询分解接口
     *
     * @param query 待分解的复杂查询
     * @return 分解的子查询数组
     */
    @GetMapping("/v1/query/decompose")
    public WebResult queryDecompose(@RequestParam("query") String query) {
        String[] subQueries = vectorApplicationService.decomposeQuery(query);
        return WebResult.buildSuccess(subQueries);
    }

    /**
     * 关键词扩展接口
     *
     * @param query 待扩展的查询
     * @return 扩展的关键词数组
     */
    @GetMapping("/v1/query/expand")
    public WebResult queryExpand(@RequestParam("query") String query) {
        String[] keywords = vectorApplicationService.expandKeywords(query);
        return WebResult.buildSuccess(keywords);
    }

    /**
     * 使用改写查询的混合搜索接口
     *
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param semanticWeight 语义搜索权重
     * @param keywordWeight 关键词搜索权重
     * @return 搜索结果
     */
    @GetMapping("/v1/hybrid/search-with-rewrite")
    public WebResult hybridSearchWithRewrite(@RequestParam("query") String query,
                                            @RequestParam(value = "topK", defaultValue = "5") Integer topK,
                                            @RequestParam(value = "semanticWeight", defaultValue = "0.7") Double semanticWeight,
                                            @RequestParam(value = "keywordWeight", defaultValue = "0.3") Double keywordWeight) {

        List<org.springframework.ai.document.Document> results =
            vectorApplicationService.hybridRetrievalWithRewrite(query, topK, semanticWeight, keywordWeight, new HashMap<>());
        return WebResult.buildSuccess(results);
    }

    /**
     * 带元数据过滤的检索接口
     *
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param author 作者过滤
     * @param category 分类过滤
     * @param source 来源过滤
     * @return 搜索结果
     */
    @GetMapping("/v1/retrieval/metadata-filter")
    public WebResult retrievalWithMetadata(@RequestParam("query") String query,
                                         @RequestParam(value = "topK", defaultValue = "5") Integer topK,
                                         @RequestParam(value = "author", required = false) String author,
                                         @RequestParam(value = "category", required = false) String category,
                                         @RequestParam(value = "source", required = false) String source) {

        List<org.springframework.ai.document.Document> results =
            vectorApplicationService.retrievalWithMetadata(query, topK, author, category, source);
        return WebResult.buildSuccess(results);
    }

    /**
     * 高级过滤检索接口
     *
     * @param query 查询文本
     * @param topK 返回结果数量
     * @param filterCriteria 过滤条件JSON
     * @return 搜索结果
     */
    @PostMapping("/v1/retrieval/advanced-filter")
    public WebResult advancedFilteredRetrieval(@RequestParam("query") String query,
                                             @RequestParam(value = "topK", defaultValue = "5") Integer topK,
                                             @RequestBody Map<String, Object> filterCriteria) {

        List<org.springframework.ai.document.Document> results =
            vectorApplicationService.advancedFilteredRetrieval(query, topK, filterCriteria);
        return WebResult.buildSuccess(results);
    }
}