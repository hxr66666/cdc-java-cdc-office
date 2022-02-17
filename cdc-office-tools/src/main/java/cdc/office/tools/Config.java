package cdc.office.tools;

import java.util.Optional;
import java.util.jar.Manifest;

import cdc.util.lang.ManifestUtils;

public final class Config {
    public static final String VERSION;

    static {
        final Optional<Manifest> manifest = ManifestUtils.getManifest(Config.class);
        VERSION = ManifestUtils.getValue(manifest, ManifestUtils.IMPLEMENTATION_VERSION, "SNAPSHOT");
    }

    private Config() {
    }
}