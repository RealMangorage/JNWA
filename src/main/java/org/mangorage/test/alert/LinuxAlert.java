package org.mangorage.test.alert;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.*;

public final class LinuxAlert implements Alert {

    private final MethodHandle notifyInit;
    private final MethodHandle notifyNotificationNew;
    private final MethodHandle notifyNotificationShow;

    LinuxAlert(Linker linker) {
        SymbolLookup lib = SymbolLookup.libraryLookup("libnotify.so.4", Arena.global());

        notifyInit = linker.downcallHandle(
                lib.find("notify_init").orElseThrow(),
                FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS)
        );

        notifyNotificationNew = linker.downcallHandle(
                lib.find("notify_notification_new").orElseThrow(),
                FunctionDescriptor.of(
                        ADDRESS,
                        ADDRESS, // summary
                        ADDRESS, // body
                        ADDRESS  // icon
                )
        );

        notifyNotificationShow = linker.downcallHandle(
                lib.find("notify_notification_show").orElseThrow(),
                FunctionDescriptor.of(
                        JAVA_BOOLEAN,
                        ADDRESS, // notification
                        ADDRESS  // GError**
                )
        );
    }

    @Override
    public void popup(String message, String title) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {

            MemorySegment appName = arena.allocateFrom("JavaApp", StandardCharsets.UTF_8);
            notifyInit.invoke(appName);

            MemorySegment summary = arena.allocateFrom(title, StandardCharsets.UTF_8);
            MemorySegment body = arena.allocateFrom(message, StandardCharsets.UTF_8);
            MemorySegment icon = MemorySegment.NULL;

            MemorySegment notification = (MemorySegment) notifyNotificationNew.invoke(
                    summary, body, icon
            );

            notifyNotificationShow.invoke(notification, MemorySegment.NULL);
        }
    }
}