package io.fabric8.launcher.operator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.launcher.operator.cr.LauncherResource;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class LauncherOperator {

    @Inject
    KubernetesClient client;

    @Inject
    LauncherResourceCache cache;

    void onStartup(@Observes StartupEvent _ev) {
        new Thread(this::runWatch).start();
    }

    private void runWatch() {
        cache.listThenWatch(this::handleEvent);
    }

    private void handleEvent(Watcher.Action action, String uid) {
        try {
            LauncherResource resource = cache.get(uid);
            if (resource == null) {
                // Resource was deleted
                return;
            }
            System.out.println("INSTALL CUSTOM RESOURCE: " + resource);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
