package org.mangorage.test.alert;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class WindowsAlert implements Alert {

    private final MethodHandle messageBoxHandle;

    WindowsAlert(Linker linker) {
        SymbolLookup user32 = SymbolLookup.libraryLookup("user32", Arena.global());

        FunctionDescriptor descriptor = FunctionDescriptor.of(
                JAVA_INT,    // Return value
                ADDRESS,     // HWND hWnd (Handle to owner window)
                ADDRESS,     // LPCWSTR lpText (The message)
                ADDRESS,     // LPCWSTR lpCaption (The title)
                JAVA_INT     // UINT uType (Buttons/Icons)
        );

        messageBoxHandle = linker.downcallHandle(
                user32.find("MessageBoxW").orElseThrow(),
                descriptor
        );
    }

    @Override
    public void popup(String message, String title) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment messageSegment = arena.allocateFrom(message, StandardCharsets.UTF_16LE);
            MemorySegment titleSegment = arena.allocateFrom(title, StandardCharsets.UTF_16LE);
            messageBoxHandle.invoke(
                    MemorySegment.NULL,
                    messageSegment,
                    titleSegment,
                    0x00000040
            );
        }
    }
}
