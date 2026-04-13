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

    public Window createWindow(String windowId, String title, int width, int height) {
        WindowConfig config = new WindowConfig(title, width, height);
        final var window = createWindow(windowId, config, () -> unregister(windowId));
        register(windowId, window);
        return window;
    }

    private Window createWindow(String windowId, WindowConfig config, Runnable runnable) {
        final var os = System.getProperty("os.name");
        return new Win32WindowImpl(windowId, config, runnable);
    }

}
