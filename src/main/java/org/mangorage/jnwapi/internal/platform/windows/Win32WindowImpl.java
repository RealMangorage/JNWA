package org.mangorage.jnwapi.internal.platform.windows;

import org.mangorage.jnwapi.api.Screen;
import org.mangorage.jnwapi.api.Window;
import org.mangorage.jnwapi.api.event.WindowKeyEvent;
import org.mangorage.jnwapi.api.event.MouseButtonEvent;
import org.mangorage.jnwapi.api.event.MouseMoveEvent;
import org.mangorage.jnwapi.api.event.MouseScrollEvent;
import org.mangorage.jnwapi.internal.platform.EmptyScreen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.foreign.ValueLayout.*;
import static org.mangorage.jnwapi.internal.InternalUtil.downcall;

public final class Win32WindowImpl implements Window {

    // ---------------- WIN32 CONSTANTS ----------------
    private static final int WM_DESTROY    = 0x0002;
    private static final int WM_SIZE       = 0x0005;
    private static final int SWP_NOZORDER     = 0x0004;
    private static final int SWP_NOACTIVATE   = 0x0010;
    public static final int GWL_STYLE = -16;
    public static final int GWL_EXSTYLE = -20;

    // ---------------- WINDOW STYLE FLAGS ----------------
    public static final long WS_THICKFRAME  = 0x00040000L;
    public static final long WS_MAXIMIZEBOX = 0x00010000L;

    // ---------------- SetWindowPos FLAGS ----------------
    public static final int SWP_NOSIZE       = 0x0001;
    public static final int SWP_NOMOVE       = 0x0002;
    public static final int SWP_FRAMECHANGED = 0x0020;

    private static final int WM_KEYDOWN    = 0x0100;
    private static final int WM_KEYUP      = 0x0101;
    private static final int WM_MOUSEMOVE  = 0x0200;
    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP   = 0x0202;
    private static final int WM_RBUTTONDOWN = 0x0204;
    private static final int WM_RBUTTONUP   = 0x0205;
    private static final int WM_MBUTTONDOWN = 0x0207;
    private static final int WM_MBUTTONUP   = 0x0208;
    private static final int WM_MOUSEWHEEL  = 0x020A;

    private static final int IDC_ARROW  = 32512;
    private static final int WS_OVERLAPPEDWINDOW = 0x00CF0000;
    private static final int WS_VISIBLE = 0x10000000;

    // ---------------- WIN32 LIBS ----------------
    private static final SymbolLookup USER32 = SymbolLookup.libraryLookup("user32", Arena.global());
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());
    private static final SymbolLookup GDI32 = SymbolLookup.libraryLookup("gdi32", Arena.global());

    private static final MethodHandle GetModuleHandleW = downcall(KERNEL32, "GetModuleHandleW", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle RegisterClassExW = downcall(USER32, "RegisterClassExW", FunctionDescriptor.of(JAVA_SHORT, ADDRESS));
    private static final MethodHandle LoadCursorW = downcall(USER32, "LoadCursorW", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle CreateWindowExW = downcall(USER32, "CreateWindowExW", FunctionDescriptor.of(ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle PeekMessageW = downcall(USER32, "PeekMessageW", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle TranslateMessage = downcall(USER32, "TranslateMessage", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle DispatchMessageW = downcall(USER32, "DispatchMessageW", FunctionDescriptor.of(JAVA_LONG, ADDRESS));
    private static final MethodHandle ShowWindow = downcall(USER32, "ShowWindow", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle DefWindowProcW = downcall(USER32, "DefWindowProcW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_LONG, JAVA_LONG));
    private static final MethodHandle GetDC = downcall(USER32, "GetDC", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle ReleaseDC = downcall(USER32, "ReleaseDC", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle SendMessageW = downcall(USER32, "SendMessageW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_LONG, ADDRESS));
    private static final MethodHandle SetWindowTextW = downcall(USER32, "SetWindowTextW", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle CreateIconIndirect = downcall(USER32, "CreateIconIndirect", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle SetWindowPos = downcall(USER32, "SetWindowPos", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle SetWindowLongPtr = downcall(USER32, "SetWindowLongPtrW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_LONG));
    private static final MethodHandle GetWindowLongPtr = downcall(USER32, "GetWindowLongPtrW", FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT));

    private static final MethodHandle CreateDIBSection = downcall(GDI32, "CreateDIBSection", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));
    private static final MethodHandle CreateBitmap = downcall(GDI32, "CreateBitmap", FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS));
    private static final MethodHandle DeleteObject = downcall(GDI32, "DeleteObject", FunctionDescriptor.of(JAVA_INT, ADDRESS));


    private static final StructLayout WNDCLASSEXW = MemoryLayout.structLayout(
            JAVA_INT.withName("cbSize"),
            JAVA_INT.withName("style"),
            ADDRESS.withName("lpfnWndProc"),
            JAVA_INT.withName("cbClsExtra"),
            JAVA_INT.withName("cbWndExtra"),
            ADDRESS.withName("hInstance"),
            ADDRESS.withName("hIcon"),
            ADDRESS.withName("hCursor"),
            ADDRESS.withName("hbrBackground"),
            ADDRESS.withName("lpszMenuName"),
            ADDRESS.withName("lpszClassName"),
            ADDRESS.withName("hIconSm")
    );

    // ---------------- STATE ----------------
    private final String id;
    private final Runnable unregisterHook;

    private volatile boolean running = true;
    private volatile boolean visible = false;

    private volatile int width;
    private volatile int height;

    private volatile int posX = 0;
    private volatile int posY = 0;

    private volatile String title = "Untitled";

    private MemorySegment hwnd;
    private Screen currentScreen = new EmptyScreen();
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private Thread thread;

    public Win32WindowImpl(String id, Runnable unregisterHook) {
        this.id = id;
        this.unregisterHook = unregisterHook;
    }


    // ---------------- WINDOW PROCEDURE ----------------

    private long windowProc(MemorySegment hWnd, int msg, long wParam, long lParam) {
        switch (msg) {

            // -------- Mouse Movement --------
            case WM_MOUSEMOVE -> {
                short x = (short) (lParam & 0xFFFF);
                short y = (short) ((lParam >> 16) & 0xFFFF);

                currentScreen.onEvent(new MouseMoveEvent(x, y));
                return 0;
            }

            // -------- Mouse Buttons --------
            case WM_LBUTTONDOWN -> {
                currentScreen.onEvent(new MouseButtonEvent(0, true));
                return 0;
            }
            case WM_LBUTTONUP -> {
                currentScreen.onEvent(new MouseButtonEvent(0, false));
                return 0;
            }

            case WM_RBUTTONDOWN -> {
                currentScreen.onEvent(new MouseButtonEvent(1, true));
                return 0;
            }
            case WM_RBUTTONUP -> {
                currentScreen.onEvent(new MouseButtonEvent(1, false));
                return 0;
            }

            case WM_MBUTTONDOWN -> {
                currentScreen.onEvent(new MouseButtonEvent(2, true));
                return 0;
            }
            case WM_MBUTTONUP -> {
                currentScreen.onEvent(new MouseButtonEvent(2, false));
                return 0;
            }

            // -------- Keyboard --------
            case WM_KEYDOWN -> {
                currentScreen.onEvent(new WindowKeyEvent((int) wParam, true));
                return 0;
            }
            case WM_KEYUP -> {
                currentScreen.onEvent(new WindowKeyEvent((int) wParam, false));
                return 0;
            }

            // -------- Mouse Wheel --------
            case WM_MOUSEWHEEL -> {
                short delta = (short) ((wParam >> 16) & 0xFFFF);
                double scroll = delta / 120.0;

                currentScreen.onEvent(new MouseScrollEvent(scroll));
                return 0;
            }

            // -------- Resize --------
            case WM_SIZE -> {
                width = (int) (lParam & 0xFFFF);
                height = (int) ((lParam >> 16) & 0xFFFF);
                return 0;
            }

            // -------- Destroy --------
            case WM_DESTROY -> {
                running = false;
                return 0;
            }

            // -------- Default --------
            default -> {
                try {
                    return (long) DefWindowProcW.invokeExact(hWnd, msg, wParam, lParam);
                } catch (Throwable t) {
                    return 0;
                }
            }
        }
    }

    // ---------------- MAIN LOOP ----------------

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long handle() {
        return hwnd.address();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Screen getScreen() {
        return currentScreen;
    }

    @Override
    public void start() {
        if (thread != null)
            return;

        thread = new Thread(this::run, "Window-" + id);
        thread.start();
    }

    private void run() {
        createWindow();
        Win32GraphicsImpl graphics = new Win32GraphicsImpl(hwnd);
        ShowWindowSafe(true);
        currentScreen.init(this);

        while (running) {
            drainTasks();
            pollEvents();
            currentScreen.update();
            if (visible) {
                currentScreen.render(graphics, this);
                graphics.present();
            }
        }

        currentScreen.dispose();
        graphics.dispose();
        unregisterHook.run();
    }

    private void createWindow() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment hInstance = (MemorySegment) GetModuleHandleW.invokeExact(MemorySegment.NULL);
            MemorySegment className = arena.allocateFrom("WinClass_" + id, StandardCharsets.UTF_16LE);
            // Why does it work if MemorySegment is NULL?
            MemorySegment hCursor = (MemorySegment) LoadCursorW.invokeExact(MemorySegment.NULL, MemorySegment.ofAddress(IDC_ARROW));

            // Setup Upcall Stub for WndProc

            Method method = Win32WindowImpl.class.getDeclaredMethod(
                    "windowProc",
                    MemorySegment.class,
                    int.class,
                    long.class,
                    long.class
            );

            MethodHandle jWndProc = MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(this);

            MemorySegment wndProcStub = Linker.nativeLinker()
                    .upcallStub(
                            jWndProc,
                            FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_INT, JAVA_LONG, JAVA_LONG),
                            Arena.global()
                    );

            MemorySegment wndClass = arena.allocate(WNDCLASSEXW);

            wndClass.set(JAVA_INT, 0, (int) WNDCLASSEXW.byteSize());
            wndClass.set(JAVA_INT, 4, 0);
            wndClass.set(ADDRESS, 8, wndProcStub); // Point to our Java method
            wndClass.set(JAVA_INT, 16, 0);
            wndClass.set(JAVA_INT, 20, 0);
            wndClass.set(ADDRESS, 24, hInstance);
            wndClass.set(ADDRESS, 32, MemorySegment.NULL);
            wndClass.set(ADDRESS, 40, hCursor);
            wndClass.set(ADDRESS, 48, MemorySegment.NULL);
            wndClass.set(ADDRESS, 56, MemorySegment.NULL);
            wndClass.set(ADDRESS, 64, className);
            wndClass.set(ADDRESS, 72, MemorySegment.NULL);

            RegisterClassExW.invoke(wndClass);

            this.hwnd = (MemorySegment) CreateWindowExW.invoke(
                    0,
                    className,
                    arena.allocateFrom(title, StandardCharsets.UTF_16LE),
                    WS_OVERLAPPEDWINDOW | WS_VISIBLE,
                    100,
                    100,
                    width,
                    height,
                    MemorySegment.NULL,
                    MemorySegment.NULL,
                    hInstance,
                    MemorySegment.NULL
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void pollEvents() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment msg = arena.allocate(48);
            // PeekMessage calls our windowProc stub internally via DispatchMessage
            while ((int) PeekMessageW.invoke(msg, hwnd, 0, 0, 1) != 0) {
                TranslateMessage.invoke(msg);
                DispatchMessageW.invoke(msg);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // ---------------- IMPLEMENTATIONS ----------------

    @Override
    public void setIcon(byte[] data) {
        tasks.offer(() -> {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                if (img == null)
                    return;

                int w = img.getWidth(), h = img.getHeight();

                int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);

                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment hdc = (MemorySegment) GetDC.invoke(hwnd);
                    MemorySegment bmi = arena.allocate(40);

                    bmi.set(JAVA_INT, 0, 40);
                    bmi.set(JAVA_INT, 4, w);
                    bmi.set(JAVA_INT, 8, -h);
                    bmi.set(JAVA_SHORT, 12, (short) 1);
                    bmi.set(JAVA_SHORT, 14, (short) 32);

                    MemorySegment ppvBits = arena.allocate(ADDRESS);
                    MemorySegment hbmColor = (MemorySegment) CreateDIBSection.invoke(hdc, bmi, 0, ppvBits, MemorySegment.NULL, 0);
                    MemorySegment bits = ppvBits.get(ADDRESS, 0).reinterpret((long) w * h * 4);

                    for (int i = 0; i < pixels.length; i++) {
                        bits.set(JAVA_INT, i * 4L, pixels[i]);
                    }

                    MemorySegment hbmMask = (MemorySegment) CreateBitmap.invoke(w, h, 1, 1, MemorySegment.NULL);
                    MemorySegment iconStruct = arena.allocate(32);
                    iconStruct.set(JAVA_INT, 0, 1);
                    iconStruct.set(ADDRESS, 16, hbmMask);
                    iconStruct.set(ADDRESS, 24, hbmColor);

                    MemorySegment hIcon = (MemorySegment) CreateIconIndirect.invoke(iconStruct);
                    if (hIcon != null && !hIcon.equals(MemorySegment.NULL)) {
                        SendMessageW.invoke(hwnd, 0x0080, 1L, hIcon);
                    }

                    DeleteObject.invoke(hbmColor);
                    DeleteObject.invoke(hbmMask);
                    ReleaseDC.invoke(hwnd, hdc);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void setVisible(boolean v) {
        visible = v;
        tasks.offer(() -> ShowWindowSafe(v));
    }

    private void ShowWindowSafe(boolean v) {
        try {
            ShowWindow.invoke(hwnd, v ? 5 : 0);
        } catch (Throwable ignored) {}
    }

    @Override
    public void setTitle(String title) {
        tasks.offer(() -> {
            this.title = title;
            try (Arena arena = Arena.ofConfined()) {
                SetWindowTextW.invoke(hwnd, arena.allocateFrom(title, StandardCharsets.UTF_16LE));
            } catch (Throwable ignored) {}
        });
    }

    @Override
    public void setPosition(int x, int y) {
        tasks.offer(() -> {
            this.posX = x;
            this.posY = y;

            try {
                SetWindowPos.invoke(
                        hwnd,
                        MemorySegment.NULL,
                        this.posX,
                        this.posY,
                        this.width,
                        this.height,
                        0x0001 | 0x0004 | 0x0010
                );
            } catch (Throwable ignored) {}
        });
    }

    @Override
    public void setSize(int width, int height) {
        tasks.offer(() -> {
            this.width = width;
            this.height = height;

            try {
                SetWindowPos.invoke(
                        hwnd,
                        MemorySegment.NULL, // HWND_TOP = NULL is fine
                        this.posX,
                        this.posY,
                        this.width,
                        this.height,
                        SWP_NOZORDER | SWP_NOACTIVATE
                );
            } catch (Throwable ignored) {}
        });
    }

    private long originalStyle = -1;

    @Override
    public void setSizeLock(boolean lock) {
        tasks.offer(() -> {
            try {
                long style = (long) GetWindowLongPtr.invoke(hwnd, GWL_STYLE);

                // cache original style once
                if (originalStyle == -1) {
                    originalStyle = style;
                }

                if (lock) {
                    // remove resize + maximize
                    style &= ~WS_THICKFRAME;
                    style &= ~WS_MAXIMIZEBOX;
                } else {
                    // restore original style
                    style = originalStyle;
                }

                SetWindowLongPtr.invoke(hwnd, GWL_STYLE, style);

                SetWindowPos.invoke(
                        hwnd,
                        MemorySegment.NULL,
                        0, 0, 0, 0,
                        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED
                );
            } catch (Throwable ignored) {}
        });
    }
    @Override
    public void setScreen(Screen screen) {
        tasks.offer(() -> {
            if (currentScreen != null)
                currentScreen.dispose();

            currentScreen = (screen != null) ? screen : new EmptyScreen();
            currentScreen.init(this);
        });
    }

    @Override
    public void close() {
        running = false;
    }

    private void drainTasks() {
        Runnable r;
        while ((r = tasks.poll()) != null) {
            try {
                r.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}