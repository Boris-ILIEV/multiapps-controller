package org.cloudfoundry.multiapps.controller.process.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.cloudfoundry.multiapps.common.SLException;
import org.cloudfoundry.multiapps.common.util.Tester.Expectation;
import org.cloudfoundry.multiapps.controller.core.helpers.MtaDescriptorPropertiesResolver;
import org.cloudfoundry.multiapps.controller.core.util.DescriptorTestUtil;
import org.cloudfoundry.multiapps.controller.process.Constants;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.cloudfoundry.multiapps.mta.model.DeploymentDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ProcessDescriptorStepTest extends SyncFlowableStepTest<ProcessDescriptorStep> {

    private static final Integer MTA_MAJOR_SCHEMA_VERSION = 2;

    private static final DeploymentDescriptor DEPLOYMENT_DESCRIPTOR = DescriptorTestUtil.loadDeploymentDescriptor("node-hello-mtad.yaml",
                                                                                                                  ProcessDescriptorStepTest.class);

    private class ProcessDescriptorStepMock extends ProcessDescriptorStep {

        @Override
        protected MtaDescriptorPropertiesResolver getMtaDescriptorPropertiesResolver(ProcessContext context) {
            return resolver;
        }

    }

    @Mock
    private MtaDescriptorPropertiesResolver resolver;

    @Before
    public void setUp() throws Exception {
        prepareContext();
    }

    private void prepareContext() {
        context.setVariable(Variables.DEPLOYMENT_DESCRIPTOR_WITH_SYSTEM_PARAMETERS, DEPLOYMENT_DESCRIPTOR);

        context.setVariable(Variables.SERVICE_ID, Constants.DEPLOY_SERVICE_ID);
        context.setVariable(Variables.MTA_NAMESPACE, null);
        context.setVariable(Variables.APPLY_NAMESPACE, false);

        context.setVariable(Variables.MTA_MAJOR_SCHEMA_VERSION, MTA_MAJOR_SCHEMA_VERSION);
    }

    @Test
    public void testExecute1() {
        when(resolver.resolve(any())).thenAnswer((invocation) -> invocation.getArguments()[0]);

        step.execute(execution);

        assertStepFinishedSuccessfully();

        tester.test(() -> context.getVariable(Variables.SUBSCRIPTIONS_TO_CREATE), new Expectation("[]"));

        tester.test(() -> context.getVariable(Variables.COMPLETE_DEPLOYMENT_DESCRIPTOR),
                    new Expectation(Expectation.Type.JSON, "node-hello-mtad-1.yaml.json"));
    }

    @Test(expected = SLException.class)
    public void testExecute2() {
        when(resolver.resolve(any())).thenThrow(new SLException("Error!"));

        step.execute(execution);
    }

    @Test(expected = SLException.class)
    public void testWithInvalidModulesSpecifiedForDeployment() {
        when(resolver.resolve(any())).thenReturn(DEPLOYMENT_DESCRIPTOR);
        when(context.getVariable(Variables.MODULES_FOR_DEPLOYMENT)).thenReturn(Arrays.asList("foo", "bar"));

        step.execute(execution);
    }

    @Override
    protected ProcessDescriptorStep createStep() {
        return new ProcessDescriptorStepMock();
    }

}
