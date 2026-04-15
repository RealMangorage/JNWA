package org.mangorage.jnwa.internal;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

public final class InternalUtil {
    private static final Linker LINKER = Linker.nativeLinker();

    public static MethodHandle downcall(SymbolLookup lib, String name, FunctionDescriptor fd) {
        return LINKER.downcallHandle(lib.find(name).orElseThrow(), fd);
    }
}
