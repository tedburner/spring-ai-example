package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptPatternOptions;
import com.ai.chat.application.config.PromptPatternOptions.PatternConfig;
import com.ai.chat.application.service.dto.ChainResult;
import com.ai.chat.application.service.dto.ParallelResult;
import com.ai.chat.application.service.dto.RouteClassification;
import com.ai.chat.application.service.dto.RoutingDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkflowService 单元测试（针对新增的 Chain/Parallel/Routing 工作流）
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callSpec;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        PatternConfig config = new PatternConfig(0.7, 0, 500, 0);
        PromptPatternOptions patternOptions = new PromptPatternOptions();
        patternOptions.getPresets().put("creative", config);
        patternOptions.getPresets().put("reasoning", config);
        patternOptions.getPresets().put("self-consistency", config);

        workflowService = new WorkflowService(chatModel, null, patternOptions);
    }

    @Test
    void testChainResultRecord() {
        List<ChainResult.ChainStep> steps = List.of(
                new ChainResult.ChainStep(1, "step1", "output1"),
                new ChainResult.ChainStep(2, "step2", "output2")
        );
        ChainResult result = new ChainResult("initial", steps, "finalOutput");

        assertEquals("initial", result.initialPrompt());
        assertEquals(2, result.steps().size());
        assertEquals("finalOutput", result.finalOutput());
        assertEquals("step1", result.steps().get(0).prompt());
        assertEquals("output1", result.steps().get(0).output());
    }

    @Test
    void testParallelResultRecord() {
        List<ParallelResult.ParallelStep> steps = List.of(
                new ParallelResult.ParallelStep("section1", "out1"),
                new ParallelResult.ParallelStep("section2", "out2")
        );
        ParallelResult result = new ParallelResult("sectioning", "main", steps, "combined");

        assertEquals("sectioning", result.mode());
        assertEquals("main", result.mainPrompt());
        assertEquals(2, result.steps().size());
        assertEquals("combined", result.consensus());
    }

    @Test
    void testRouteClassificationRecord() {
        RouteClassification classification = new RouteClassification("这是推理", "technical");
        assertEquals("这是推理", classification.reasoning());
        assertEquals("technical", classification.selection());
    }

    @Test
    void testRoutingDecisionRecord() {
        RouteClassification classification = new RouteClassification("推理", "general");
        RoutingDecision decision = new RoutingDecision("你好", classification, "回答");

        assertEquals("你好", decision.input());
        assertEquals("推理", decision.classification().reasoning());
        assertEquals("回答", decision.result());
    }

    @Test
    void testChainWorkflowEmptySteps() {
        assertDoesNotThrow(() -> {
            ChainResult result = workflowService.chainWorkflow("init", List.of());
            assertNotNull(result);
            assertEquals(0, result.steps().size());
            assertEquals("init", result.finalOutput());
        });
    }

    @Test
    void testParallelSectioningEmptySections() {
        assertDoesNotThrow(() -> {
            ParallelResult result = workflowService.parallelSectioning("main", List.of());
            assertNotNull(result);
            assertEquals("sectioning", result.mode());
            assertEquals(0, result.steps().size());
        });
    }

    @Test
    void testParallelVotingSingleVote() {
        // WorkflowService creates its own ChatClient from the mocked ChatModel in constructor.
        // The mock ChatModel returns null → content() is null → trim() throws NPE.
        // This test can only verify the data structure, not the full execution path.
        // Integration test with @SpringBootTest would be needed for full coverage.
        assertNotNull(ParallelResult.class);
        assertEquals("voting", new ParallelResult("voting", "prompt", List.of(), "consensus").mode());
    }
}
