package org.cloudfoundry.multiapps.controller.web.configuration.bean.factory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.cloudfoundry.multiapps.controller.core.persistence.service.ConfigurationSubscriptionService;
import org.cloudfoundry.multiapps.controller.core.persistence.service.ConfigurationSubscriptionService.ConfigurationSubscriptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;

@Named("configurationSubscriptionService")
public class ConfigurationSubscriptionServiceFactoryBean implements FactoryBean<ConfigurationSubscriptionService>, InitializingBean {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSubscriptionServiceFactoryBean.class);
    @Inject
    protected EntityManagerFactory entityManagerFactory;
    @Inject
    @Qualifier("configurationSubscriptionMapper")
    protected ConfigurationSubscriptionMapper entryMapper;

    protected ConfigurationSubscriptionService configurationSubscriptionService;

    @Override
    public void afterPropertiesSet() {
        LOGGER.warn("entryMapper: " + entryMapper);
        if (entryMapper != null) {
            LOGGER.warn("entryMapper class: " + entryMapper.getClass());
        }
        this.configurationSubscriptionService = new ConfigurationSubscriptionService(entityManagerFactory, entryMapper);
    }

    @Override
    public ConfigurationSubscriptionService getObject() {
        return configurationSubscriptionService;
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigurationSubscriptionService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
