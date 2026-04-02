package com.ai.chat.test.integration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = {
        MetricsAutoConfiguration.class,
        PrometheusMetricsExportAutoConfiguration.class
    }
)
@TestPropertySource(properties = {
    "management.endpoints.web.exposure.include=health,info,metrics,prometheus",
    "management.metrics.export.prometheus.enabled=true"
})
class MetricsConfigurationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void testMeterRegistryBean() {
        assertNotNull(meterRegistry);
        assertTrue(meterRegistry instanceof PrometheusMeterRegistry);
    }

    @Test
    void testMetricTags() {
        // 测试指标标签配置
        meterRegistry.counter("test.metric", "application", "spring-ai-chat", "version", "1.0.0");

        String prometheusData = ((PrometheusMeterRegistry) meterRegistry).scrape();
        assertTrue(prometheusData.contains("application=\"spring-ai-chat\""));
        assertTrue(prometheusData.contains("version=\"1.0.0\""));
    }
}