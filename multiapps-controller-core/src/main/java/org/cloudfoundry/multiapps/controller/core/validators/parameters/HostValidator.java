package org.cloudfoundry.multiapps.controller.core.validators.parameters;

import org.cloudfoundry.multiapps.controller.core.Messages;
import org.cloudfoundry.multiapps.controller.core.model.SupportedParameters;

public class HostValidator extends RoutePartValidator {

    @Override
    public String getParameterName() {
        return SupportedParameters.HOST;
    }

    @Override
    protected String getErrorMessage() {
        return Messages.COULD_NOT_CREATE_VALID_HOST;
    }

    @Override
    protected int getRoutePartMaxLength() {
        return 63;
    }

    @Override
    protected String getRoutePartIllegalCharacters() {
        return "[^a-z0-9\\-]";
    }

    @Override
    protected String getRoutePartPattern() {
        return "^([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])|\\*$";
    }

}
