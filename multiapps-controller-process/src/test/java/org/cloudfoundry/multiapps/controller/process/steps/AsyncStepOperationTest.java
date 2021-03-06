package org.cloudfoundry.multiapps.controller.process.steps;

import java.util.List;

import org.junit.Test;

public abstract class AsyncStepOperationTest<AsyncStep extends SyncFlowableStep> extends SyncFlowableStepTest<AsyncStep> {

    protected abstract List<AsyncExecution> getAsyncOperations(ProcessContext wrapper);

    @Test
    public void testExecuteOperations() {
        step.initializeStepLogger(execution);
        ProcessContext wrapper = step.createProcessContext(execution);

        for (AsyncExecution operation : getAsyncOperations(wrapper)) {
            AsyncExecutionState result = operation.execute(wrapper);
            validateOperationExecutionResult(result);
        }

    }

    protected abstract void validateOperationExecutionResult(AsyncExecutionState result);
}
