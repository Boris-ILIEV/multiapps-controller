package com.sap.cloud.lm.sl.cf.core.helpers.v2;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.MapUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;
import com.sap.cloud.lm.sl.common.util.Tester;
import com.sap.cloud.lm.sl.common.util.Tester.Expectation;
import com.sap.cloud.lm.sl.mta.builders.v2.PropertiesChainBuilder;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ConfigurationFilterParserTest {

    private static final String NEW_SYNTAX_FILTER = "configuration";
    private static final String OLD_SYNTAX_FILTER = "mta-provides-dependency";
    private ConfigurationFilterParserTestInput input;
    private final Tester tester = Tester.forClass(getClass());

    @Mock
    private PropertiesChainBuilder testChainBuilder;
    @Mock
    private Resource testResource;

    private static class ConfigurationFilterParserTestInput {
        private String deploymentNamespace;
        private CloudTarget cloudTarget;
        private boolean isNewResourceType;
        private Map<String, Object> resourceParameters;
    }

    public static Stream<Arguments> testParse() {
        return Stream.of(
        // @formatter:off
        // (1) Parse a filter with new syntax
        Arguments.of("configuration-filter-parser-test-input-01.json", new Expectation(Expectation.Type.JSON, "configuration-filter-parser-test-output-01.json")),
        // (2) Parse a filter with old syntax
        Arguments.of("configuration-filter-parser-test-input-02.json", new Expectation(Expectation.Type.JSON, "configuration-filter-parser-test-output-02.json")),
        // (3) Deployment namespace and resource namespace differ - filter should contain the resource one
        Arguments.of("configuration-filter-parser-test-input-03.json", new Expectation(Expectation.Type.JSON, "configuration-filter-parser-test-output-03.json"))
        // @formatter:on
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testParse(String inputFileLocation, Expectation expectation) {
        parseInput(inputFileLocation);
        initMocks();

        ConfigurationFilterParser testParser = new ConfigurationFilterParser(input.cloudTarget,
                                                                             this.testChainBuilder,
                                                                             input.deploymentNamespace);
        tester.test(() -> testParser.parse(testResource), expectation);
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(testResource.getParameters()).thenReturn(input.resourceParameters);

        // only purpose of this is to mock the old vs new style of parsing (mta-provides-dependency vs configuration)
        String mockResourceType = input.isNewResourceType ? NEW_SYNTAX_FILTER : OLD_SYNTAX_FILTER;
        List<Map<String, Object>> mockResourceChain = Arrays.asList(MapUtil.asMap(SupportedParameters.TYPE, mockResourceType));
        when(testResource.getName()).thenReturn("mockedResourceName");
        when(testChainBuilder.buildResourceChain("mockedResourceName")).thenReturn(mockResourceChain);
    }

    private void parseInput(String inputFileLocation) {
        if (inputFileLocation == null) {
            fail("Test requires a configuration entry");
        }

        String testInputJson = TestUtil.getResourceAsString(inputFileLocation, getClass());
        this.input = JsonUtil.fromJson(testInputJson, new TypeReference<ConfigurationFilterParserTestInput>() {
        });
    }

}