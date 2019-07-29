package io.fabric8.launcher.operator;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.launcher.operator.cr.LauncherResource;
import io.fabric8.launcher.operator.cr.LauncherResourceDoneable;
import io.fabric8.launcher.operator.cr.LauncherResourceList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@ApplicationScoped
class LauncherResourceCache {
    private final Map<String, LauncherResource> cache = new ConcurrentHashMap<>();

    @Inject
    NonNamespaceOperation<LauncherResource, LauncherResourceList, LauncherResourceDoneable, Resource<LauncherResource, LauncherResourceDoneable>> crClient;

    private final Executor executor = Executors.newSingleThreadExecutor();

    LauncherResource get(String uid) {
        return cache.get(uid);
    }

    void listThenWatch(BiConsumer<Watcher.Action, String> callback) {
        try {
            // list
            crClient
                    .list()
                    .getItems()
                    .forEach(resource -> {
                                cache.put(resource.getMetadata().getUid(), resource);
                                String uid = resource.getMetadata().getUid();
                                executor.execute(() -> callback.accept(Watcher.Action.ADDED, uid));
                            }
                    );

            // watch
            crClient.watch(new Watcher<LauncherResource>() {
                @Override
                public void eventReceived(Action action, LauncherResource resource) {
                    try {
                        String uid = resource.getMetadata().getUid();
                        if (cache.containsKey(uid)) {
                            int knownResourceVersion = Integer.parseInt(cache.get(uid).getMetadata().getResourceVersion());
                            int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
                            if (knownResourceVersion > receivedResourceVersion) {
                                return;
                            }
                        }
                        System.out.println("received " + action + " for resource " + resource);
                        if (action == Action.ADDED || action == Action.MODIFIED) {
                            cache.put(uid, resource);
                        } else if (action == Action.DELETED) {
                            cache.remove(uid);
                        } else {
                            System.err.println("Received unexpected " + action + " event for " + resource);
                            System.exit(-1);
                        }
                        executor.execute(() -> callback.accept(action, uid));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                    cause.printStackTrace();
                    System.exit(-1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}