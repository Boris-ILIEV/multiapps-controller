package org.cloudfoundry.multiapps.controller.process.flowable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.api.model.Operation;
import org.cloudfoundry.multiapps.controller.core.model.HistoricOperationEvent.EventType;
import org.cloudfoundry.multiapps.controller.core.persistence.service.OperationService;
import org.cloudfoundry.multiapps.controller.process.util.HistoricOperationEventPersister;
import org.cloudfoundry.multiapps.controller.process.util.ProcessConflictPreventer;

@Named
public class AbortProcessAction extends ProcessAction {

    public static final String ACTION_ID_ABORT = "abort";

    private HistoricOperationEventPersister historicEventPersister;
    private OperationService operationService;

    @Inject
    public AbortProcessAction(FlowableFacade flowableFacade, List<AdditionalProcessAction> additionalProcessActions,
                              HistoricOperationEventPersister historicEventPersister, OperationService operationService) {
        super(flowableFacade, additionalProcessActions);
        this.historicEventPersister = historicEventPersister;
        this.operationService = operationService;
    }

    @Override
    public void executeActualProcessAction(String user, String superProcessInstanceId) {
        flowableFacade.setAbortVariable(superProcessInstanceId);
        releaseOperationLock(superProcessInstanceId, Operation.State.ABORTED);
        historicEventPersister.add(superProcessInstanceId, EventType.ABORTED);
    }

    private void releaseOperationLock(String superProcessInstanceId, Operation.State state) {
        getProcessConflictPreventer().releaseLock(superProcessInstanceId, state);
    }

    protected ProcessConflictPreventer getProcessConflictPreventer() {
        return new ProcessConflictPreventer(operationService);
    }

    @Override
    public String getActionId() {
        return ACTION_ID_ABORT;
    }

}
