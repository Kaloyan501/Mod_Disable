package com.kaloyandonev.moddisable.abstracts;

import java.nio.file.Path;

public abstract class ConfDir {
    private static Impl IMPL;

    public static void init(Impl impl) {
        IMPL = impl;
    }

    public static Path getConfigDir() {
        return IMPL.getConfigDir(); // IMPL is null → IllegalStateException
    }

    public interface Impl {
        Path getConfigDir();
    }
}
