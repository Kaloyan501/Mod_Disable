package com.kaloyandonev.moddisable.abstracts;

import java.nio.file.Path;

public class ConfDir {
        public static Path getConfigDir() {
            return Impl.getConfigDir();
        }

        public interface Impl {
            static Path getConfigDir() {
                throw new IllegalStateException();
            }
        }
}
