package com.sap.cloud.lm.sl.cf.process.steps;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ConfigurationEntryService;
import com.sap.cloud.lm.sl.cf.core.security.serialization.SecureSerialization;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;

@Named("deleteDiscontinuedConfigurationEntriesStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteDiscontinuedConfigurationEntriesStep extends SyncFlowableStep {

    @Inject
    private ConfigurationEntryService configurationEntryService;

    @Inject
    private FlowableFacade flowableFacade;

    @Override
    protected StepPhase executeStep(ProcessContext context) {
        getStepLogger().debug(Messages.DELETING_PUBLISHED_DEPENDENCIES);

        List<ConfigurationEntry> entriesToDelete = getEntriesToDelete(context);
        deleteConfigurationEntries(entriesToDelete, context);

        getStepLogger().debug(Messages.PUBLISHED_DEPENDENCIES_DELETED);
        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return Messages.ERROR_DELETING_PUBLISHED_DEPENDENCIES;
    }

    private List<ConfigurationEntry> getEntriesToDelete(ProcessContext context) {
        List<ConfigurationEntry> publishedEntries = StepsUtil.getPublishedEntriesFromSubProcesses(context, flowableFacade);

        List<ConfigurationEntry> allEntriesForCurrentMta = getEntries(context);

        List<Long> publishedEntryIds = getEntryIds(publishedEntries);

        return allEntriesForCurrentMta.stream()
                                      .filter(entry -> !publishedEntryIds.contains(entry.getId()))
                                      .collect(Collectors.toList());
    }

    private void deleteConfigurationEntries(List<ConfigurationEntry> entriesToDelete, ProcessContext context) {        
        for (ConfigurationEntry entry : entriesToDelete) {
            getStepLogger().info(MessageFormat.format(Messages.DELETING_DISCONTINUED_DEPENDENCY_0, entry.getProviderId()));
            int deletedEntries = configurationEntryService.createQuery()
                                                          .id(entry.getId())
                                                          .delete();
            if (deletedEntries == 0) {
                getStepLogger().warn(Messages.COULD_NOT_DELETE_PROVIDED_DEPENDENCY, entry.getProviderId());
            }
        }
        getStepLogger().debug(Messages.DELETED_ENTRIES, SecureSerialization.toJson(entriesToDelete));
        context.setVariable(Variables.DELETED_ENTRIES, entriesToDelete);
    }

    private List<ConfigurationEntry> getEntries(ProcessContext context) {
        String mtaId = context.getVariable(Variables.MTA_ID);
        String spaceGuid =  context.getVariable(Variables.SPACE_GUID);
        String namespace = context.getVariable(Variables.MTA_NAMESPACE);

        return configurationEntryService.createQuery()
                                        .providerNid(ConfigurationEntriesUtil.PROVIDER_NID)
                                        .spaceId(spaceGuid)
                                        .mtaId(mtaId)
                                        .providerNamespace(namespace, true)
                                        .list();
    }

    private List<Long> getEntryIds(List<ConfigurationEntry> configurationEntries) {
        return configurationEntries.stream()
                                   .map(ConfigurationEntry::getId)
                                   .collect(Collectors.toList());
    }

}