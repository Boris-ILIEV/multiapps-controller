package org.cloudfoundry.multiapps.controller.process.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.multiapps.common.util.MapUtil;
import org.cloudfoundry.multiapps.controller.api.model.ProcessType;
import org.cloudfoundry.multiapps.controller.core.cf.CloudControllerClientProvider;
import org.cloudfoundry.multiapps.controller.core.model.HookPhase;
import org.cloudfoundry.multiapps.controller.core.model.Phase;
import org.cloudfoundry.multiapps.controller.core.model.SubprocessPhase;
import org.cloudfoundry.multiapps.controller.process.mock.MockDelegateExecution;
import org.cloudfoundry.multiapps.controller.process.steps.ProcessContext;
import org.cloudfoundry.multiapps.controller.process.steps.StepsUtil;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.cloudfoundry.multiapps.mta.model.Hook;
import org.cloudfoundry.multiapps.mta.model.Module;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ModuleHooksAggregatorTest {

    private final ProcessContext context = createContext();
    @Mock
    private ProcessTypeParser processTypeParser;

    ModuleHooksAggregatorTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testAggregateHooksNoCurrentAndNoHooksForExecution() {
        Module moduleToDeploy = createModule("test-module", Collections.emptyList());
        Mockito.when(processTypeParser.getProcessType(context.getExecution()))
               .thenReturn(ProcessType.DEPLOY);
        ModuleHooksAggregator moduleHooksAggregator = createModuleHooksAggregator(moduleToDeploy);
        List<Hook> aggregatedHooks = moduleHooksAggregator.aggregateHooks(Collections.emptyList());
        Assertions.assertTrue(aggregatedHooks.isEmpty());
    }

    @Test
    void testAggregateHooksNoAlreadyExecutedHooks() {
        List<Hook> hooksForModule = Collections.singletonList(createHook("test-hook",
                                                                         Collections.singletonList("deploy.application.after-stop")));
        Module moduleToDeploy = createModule("test-module", hooksForModule);
        List<HookPhase> currentHookPhasesForExecution = Collections.singletonList(HookPhase.DEPLOY_APPLICATION_AFTER_STOP);
        Mockito.when(processTypeParser.getProcessType(context.getExecution()))
               .thenReturn(ProcessType.DEPLOY);
        ModuleHooksAggregator moduleHooksAggregator = createModuleHooksAggregator(moduleToDeploy);
        List<Hook> aggregatedHooks = moduleHooksAggregator.aggregateHooks(currentHookPhasesForExecution);
        Assertions.assertEquals(hooksForModule, aggregatedHooks);
    }

    @Test
    void testAggregateHooksIfTheHookForExecutionIsAlreadyExecuted() {
        List<Hook> hooksForModule = Arrays.asList(createHook("hook1", Collections.singletonList("blue-green.application.before-stop.live")),
                                                  createHook("hook2", Collections.singletonList("blue-green.application.after-stop.live")));
        Module moduleToDeploy = createModule("test-module", hooksForModule);
        List<HookPhase> currentHookPhasesForExecutions = Collections.singletonList(HookPhase.BEFORE_STOP);
        prepareExecutedHooks("test-module", MapUtil.asMap("hook1", Collections.singletonList("blue-green.application.before-stop.live")));
        Mockito.when(processTypeParser.getProcessType(context.getExecution()))
               .thenReturn(ProcessType.BLUE_GREEN_DEPLOY);
        ModuleHooksAggregator moduleHooksAggregator = createModuleHooksAggregator(moduleToDeploy);
        List<Hook> aggregatedHooks = moduleHooksAggregator.aggregateHooks(currentHookPhasesForExecutions);
        Assertions.assertTrue(aggregatedHooks.isEmpty());
    }

    @Test
    void testAggregateHooksIfThereAreSomeExecutedPhases() {
        Hook hookForExecution = createHook("hook1", Collections.singletonList("blue-green.application.before-start.live"));
        List<Hook> hooksForModule = Arrays.asList(hookForExecution,
                                                  createHook("hook2",
                                                             Collections.singletonList("blue-green.application.before-start.idle")));
        Module moduleToDeploy = createModule("test-module", hooksForModule);
        List<HookPhase> currentHookPhasesForExecutions = Collections.singletonList(HookPhase.BLUE_GREEN_APPLICATION_BEFORE_START_LIVE);
        prepareExecutedHooks("test-module", MapUtil.asMap("hook1", Collections.singletonList("blue-green.application.before-start.idle")));
        Mockito.when(processTypeParser.getProcessType(context.getExecution()))
               .thenReturn(ProcessType.BLUE_GREEN_DEPLOY);
        context.setVariable(Variables.SUBPROCESS_PHASE, SubprocessPhase.BEFORE_APPLICATION_START);
        context.setVariable(Variables.PHASE, Phase.AFTER_RESUME);
        ModuleHooksAggregator moduleHooksAggregator = createModuleHooksAggregator(moduleToDeploy);
        List<Hook> aggregatedHooks = moduleHooksAggregator.aggregateHooks(currentHookPhasesForExecutions);
        Assertions.assertEquals(Collections.singletonList(hookForExecution), aggregatedHooks);
    }

    private void prepareExecutedHooks(String moduleName, Map<String, List<String>> executedHooks) {
        StepsUtil.setExecutedHooksForModule(context.getExecution(), moduleName, executedHooks);
    }

    private ModuleHooksAggregator createModuleHooksAggregator(Module moduleToDeploy) {
        return new ModuleHooksAggregator(context.getExecution(), moduleToDeploy);
    }

    private Hook createHook(String name, List<String> phases) {
        return Hook.createV3()
                   .setName(name)
                   .setPhases(phases);
    }

    private Module createModule(String moduleName, List<Hook> hooks) {
        return Module.createV3()
                     .setHooks(hooks)
                     .setName(moduleName);
    }

    private ProcessContext createContext() {
        DelegateExecution delegateExecution = MockDelegateExecution.createSpyInstance();
        StepLogger stepLogger = Mockito.mock(StepLogger.class);
        CloudControllerClientProvider cloudControllerClientProvider = Mockito.mock(CloudControllerClientProvider.class);
        return new ProcessContext(delegateExecution, stepLogger, cloudControllerClientProvider);
    }
}
