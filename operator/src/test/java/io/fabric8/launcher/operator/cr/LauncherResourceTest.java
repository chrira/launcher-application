package io.fabric8.launcher.operator.cr;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class LauncherResourceTest {

    @Test
    void should_deserialize_cr() throws Exception {
        File contents = new File("example/launcher_cr.yaml");
        LauncherResource resource = Serialization.yamlMapper().readValue(contents, LauncherResource.class);
        assertThat(resource).isNotNull();
        assertThat(resource.getSpec().getOAuth().isEnabled()).isTrue();
        assertThat(resource.getSpec().getOpenShift().getConsoleUrl()).isNullOrEmpty();

    }

}