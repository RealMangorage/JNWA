package org.mangorage.nsapi.api;

import org.mangorage.nsapi.internal.platform.windows.Win32WindowImpl;

import java.util.HashMap;
import java.util.Map;

public final class ScreenAPI {
    private static final ScreenAPI API = new ScreenAPI();

    public static ScreenAPI of() {
        return API;
    }

    private final Map<String, Window> windows = new HashMap<>();

    void register(String windowId, Window window) {
        if (windows.containsKey(windowId)) {
            throw new IllegalStateException("""
                    Window with Id %s already registered!
                    
                    Please use a unique id to avoid any conflicts
                    """.formatted(windowId));
        } else {
            windows.put(windowId, window);
        }
    }

    void unregister(String id) {
        windows.remove(id);
    }

    public Window createWindow(String windowId, String title) {
        final var window = createWindow(windowId, () -> unregister(windowId));
        window.setTitle(title);
        register(windowId, window);
        return window;
    }

    private Window createWindow(String windowId, Runnable runnable) {
        return new Win32WindowImpl(windowId, runnable);
    }

}
