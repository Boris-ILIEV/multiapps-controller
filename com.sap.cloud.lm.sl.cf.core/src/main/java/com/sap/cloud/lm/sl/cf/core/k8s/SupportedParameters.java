package com.sap.cloud.lm.sl.cf.core.k8s;

public class SupportedParameters {

    public static final String CONTAINER_IMAGE = "container-image";
    public static final String CONTAINER_IMAGE_CREDENTIALS = "container-image-credentials";

    public static class ContainerImageCredentialsSchema {

        public static final String REGISTRY = "registry";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

    }

}