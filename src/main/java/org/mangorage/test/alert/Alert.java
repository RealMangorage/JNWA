package org.mangorage.test.alert;

import java.lang.foreign.Linker;

public interface Alert {
    static Alert create() {
        return create(Linker.nativeLinker());
    }

    static Alert create(Linker linker) {
        return new LinuxAlert(linker);
    }

    void popup(String message, String title) throws Throwable;
}
