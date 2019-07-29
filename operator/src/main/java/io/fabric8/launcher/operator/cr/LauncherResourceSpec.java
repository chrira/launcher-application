package io.fabric8.launcher.operator.cr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.immutables.value.Value.Enclosing;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

import javax.annotation.Nullable;

@JsonDeserialize(as = ImmutableLauncherResourceSpec.class)
@RegisterForReflection
@Immutable
@Enclosing
@Style(passAnnotations = RegisterForReflection.class)
public interface LauncherResourceSpec {

    @JsonProperty("openshift")
    OpenShift getOpenShift();

    @JsonProperty("oauth")
    OAuth getOAuth();

    @Immutable
    @JsonDeserialize(as = ImmutableLauncherResourceSpec.OpenShift.class)
    interface OpenShift {
        @Nullable
        String getConsoleUrl();
    }

    @Immutable
    @JsonDeserialize(as = ImmutableLauncherResourceSpec.OAuth.class)
    interface OAuth {
        @JsonProperty("enabled")
        boolean isEnabled();
    }
}
