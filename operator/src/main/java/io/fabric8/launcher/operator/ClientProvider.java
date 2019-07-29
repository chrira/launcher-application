package io.fabric8.launcher.operator;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.launcher.operator.cr.LauncherResource;
import io.fabric8.launcher.operator.cr.LauncherResourceDoneable;
import io.fabric8.launcher.operator.cr.LauncherResourceList;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class ClientProvider {

    @Produces
    @Singleton
    KubernetesClient newClient(@ConfigProperty(name = "WATCH_NAMESPACE", defaultValue = "default") String namespace) {
        // TODO: Remove when new quarkus is released
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    NonNamespaceOperation<LauncherResource, LauncherResourceList, LauncherResourceDoneable, Resource<LauncherResource, LauncherResourceDoneable>>
        makeCustomResourceClient(KubernetesClient defaultClient) {

        KubernetesDeserializer.registerCustomKind("launcher.fabric8.io/v1alpha1", "Launcher", LauncherResource.class);
        CustomResourceDefinition crd = defaultClient
                .customResourceDefinitions()
                .list()
                .getItems()
                .stream()
                .filter(d -> "launchers.launcher.fabric8.io".equals(d.getMetadata().getName()))
                .findAny()
                .orElseThrow(
                        () -> new RuntimeException("Deployment error: Custom resource definition launcher.fabric8.io not found."));
        return defaultClient
                .customResources(crd, LauncherResource.class, LauncherResourceList.class, LauncherResourceDoneable.class)
                .inNamespace(defaultClient.getNamespace());
    }
}