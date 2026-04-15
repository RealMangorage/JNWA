package org.mangorage.nsapi.internal.platform.windows;

import org.mangorage.nsapi.api.Graphics;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.lang.foreign.ValueLayout.*;
import static org.mangorage.nsapi.internal.InternalUtil.downcall;

public final class Win32GraphicsImpl implements Graphics {

    // =========================
    // Native Libraries & Methods
    // =========================

    private static final SymbolLookup USER32 = SymbolLookup.libraryLookup("user32", Arena.global());
    private static final SymbolLookup GDI32 = SymbolLookup.libraryLookup("gdi32", Arena.global());
    private static final SymbolLookup MSIMG32 = SymbolLookup.libraryLookup("msimg32", Arena.global());

    // USER32
    private static final MethodHandle GetDC = downcall(USER32, "GetDC", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle ReleaseDC = downcall(USER32, "ReleaseDC", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle GetClientRect = downcall(USER32, "GetClientRect", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle FillRect = downcall(USER32, "FillRect", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));

    // GDI32
    private static final MethodHandle CreateCompatibleDC = downcall(GDI32, "CreateCompatibleDC", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle CreateCompatibleBitmap = downcall(GDI32, "CreateCompatibleBitmap", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));
    private static final MethodHandle SelectObject = downcall(GDI32, "SelectObject", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle BitBlt = downcall(GDI32, "BitBlt", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle DeleteObject = downcall(GDI32, "DeleteObject", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle DeleteDC = downcall(GDI32, "DeleteDC", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    private static final MethodHandle Rectangle = downcall(GDI32, "Rectangle", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle Ellipse = downcall(GDI32, "Ellipse", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle CreateSolidBrush = downcall(GDI32, "CreateSolidBrush", FunctionDescriptor.of(ADDRESS, JAVA_INT));
    private static final MethodHandle CreatePen = downcall(GDI32, "CreatePen", FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle GetStockObject = downcall(GDI32, "GetStockObject", FunctionDescriptor.of(ADDRESS, JAVA_INT));
    private static final MethodHandle CreateDIBSection = downcall(GDI32, "CreateDIBSection", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));

    // TEXT GDI32
    private static final MethodHandle CreateFontW = downcall(GDI32, "CreateFontW", FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS));
    private static final MethodHandle TextOutW = downcall(GDI32, "TextOutW", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle SetTextColor = downcall(GDI32, "SetTextColor", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle SetBkMode = downcall(GDI32, "SetBkMode", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle GetTextExtentPoint32W = downcall(GDI32, "GetTextExtentPoint32W", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, ADDRESS));

    // MSIMG32
    private static final MethodHandle AlphaBlend = downcall(MSIMG32, "AlphaBlend", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));

    // =========================
    // Fields & State
    // =========================

    private final MemorySegment hwnd;
    private final MemorySegment hdcScreen;

    private MemorySegment hdcMem;
    private MemorySegment hBitmap;
    private MemorySegment defaultBitmap;

    private int currentColorRef = 0;
    private int lastW, lastH;

    private record NativeImage(MemorySegment hBitmap, int width, int height) {}
    private record FontKey(int size, boolean bold) {}

    private final Map<Image, NativeImage> imageCache = new IdentityHashMap<>();
    private final Map<FontKey, MemorySegment> fontCache = new HashMap<>();

    public Win32GraphicsImpl(MemorySegment hwnd) {
        this.hwnd = hwnd;
        try {
            this.hdcScreen = (MemorySegment) GetDC.invoke(hwnd);
            this.hdcMem = (MemorySegment) CreateCompatibleDC.invoke(hdcScreen);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // =========================
    // Text Rendering
    // =========================

    @Override
    public void renderText(String text, int x, int y, int fontSize, boolean bold) {
        syncBuffer();
        try (Arena arena = Arena.ofConfined()) {
            FontKey key = new FontKey(fontSize, bold);

            // Retrieve or create the native font handle
            MemorySegment hFont = fontCache.computeIfAbsent(key, k -> {
                try {
                    return (MemorySegment) CreateFontW.invoke(
                            -fontSize,                         // cHeight: font height (negative = pixel height)
                            0,                                 // cWidth: average char width (0 = default)
                            0,                                 // cEscapement: text rotation (0 = none)
                            0,                                 // cOrientation: glyph orientation (0 = normal)
                            bold ? 700 : 400,                  // cWeight: FW_BOLD (700) / FW_NORMAL (400)
                            0,                                 // bItalic: italic (0 = false)
                            0,                                 // bUnderline: underline (0 = false)
                            0,                                 // bStrikeOut: strikeout (0 = false)
                            1,                                 // iCharSet: DEFAULT_CHARSET
                            0,                                 // iOutPrecision: default output precision
                            0,                                 // iClipPrecision: default clipping precision
                            0,                                 // iQuality: default rendering quality
                            0,                                 // iPitchAndFamily: default pitch/family
                            arena.allocateFrom("Arial", StandardCharsets.UTF_16LE) // pszFaceName: font name (UTF-16)
                    );
                } catch (Throwable t) { throw new RuntimeException(t); }
            });

            MemorySegment oldFont = (MemorySegment) SelectObject.invoke(hdcMem, hFont);
            SetTextColor.invoke(hdcMem, currentColorRef);
            SetBkMode.invoke(hdcMem, 1); // TRANSPARENT

            MemorySegment textSegment = arena.allocateFrom(text, StandardCharsets.UTF_16LE);
            TextOutW.invoke(hdcMem, x, y, textSegment, text.length());

            SelectObject.invoke(hdcMem, oldFont);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // Add this method to Win32GraphicsImpl
    public int getTextWidth(String text, int fontSize, boolean bold) {
        try (Arena arena = Arena.ofConfined()) {
            FontKey key = new FontKey(fontSize, bold);
            MemorySegment hFont = fontCache.get(key); // Assumes font was used/cached already

            MemorySegment oldFont = (MemorySegment) SelectObject.invoke(hdcMem, hFont);
            MemorySegment textSegment = arena.allocateFrom(text, StandardCharsets.UTF_16LE);
            MemorySegment sizeStruct = arena.allocate(8); // SIZE struct has two LONGs (4 bytes each)

            GetTextExtentPoint32W.invoke(hdcMem, textSegment, text.length(), sizeStruct);
            int width = sizeStruct.get(JAVA_INT, 0);

            SelectObject.invoke(hdcMem, oldFont);
            return width;
        } catch (Throwable t) {
            return text.length() * (fontSize / 2); // Fallback estimate
        }
    }

    // =========================
    // Buffer Management
    // =========================

    private void syncBuffer() {
        int w = width();
        int h = height();
        if (w <= 0 || h <= 0) return;

        if (w != lastW || h != lastH) {
            try {
                if (hBitmap != null && !hBitmap.equals(MemorySegment.NULL)) {
                    if (defaultBitmap != null)
                        SelectObject.invoke(hdcMem, defaultBitmap);

                    DeleteObject.invoke(hBitmap);
                }
                hBitmap = (MemorySegment) CreateCompatibleBitmap.invoke(hdcScreen, w, h);
                MemorySegment oldBmp = (MemorySegment) SelectObject.invoke(hdcMem, hBitmap);
                if (defaultBitmap == null)
                    defaultBitmap = oldBmp;

                lastW = w;
                lastH = h;
            } catch (Throwable t) { throw new RuntimeException(t); }
        }
    }

    // =========================
    // Basic Drawing
    // =========================

    @Override
    public void setColor(int argb) {
        // GDI uses COLORREF: 0x00BBGGRR
        this.currentColorRef = ((argb & 0xFF) << 16) | (argb & 0xFF00) | ((argb >> 16) & 0xFF);
    }

    @Override
    public int width() {
        return getBounds()[0];
    }
    @Override
    public int height() {
        return getBounds()[1];
    }

    private int[] getBounds() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment rect = arena.allocate(16);

            GetClientRect.invoke(hwnd, rect);

            int left   = rect.get(JAVA_INT, 0);
            int top    = rect.get(JAVA_INT, 4);
            int right  = rect.get(JAVA_INT, 8);
            int bottom = rect.get(JAVA_INT, 12);

            return new int[] {
                    right - left,
                    bottom - top
            };
        } catch (Throwable t) {
            return new int[] {0, 0};
        }
    }

    @Override
    public void clear(int argb) {
        syncBuffer();
        if (lastW <= 0 || lastH <= 0) return;
        try (Arena arena = Arena.ofConfined()) {
            int winColor = ((argb & 0xFF) << 16) | (argb & 0xFF00) | ((argb >> 16) & 0xFF);

            MemorySegment brush = (MemorySegment) CreateSolidBrush.invoke(winColor);
            MemorySegment rect = arena.allocate(16);

            rect.set(JAVA_INT, 0, 0);
            rect.set(JAVA_INT, 4, 0);
            rect.set(JAVA_INT, 8, lastW);
            rect.set(JAVA_INT, 12, lastH);

            FillRect.invoke(hdcMem, rect, brush);
            DeleteObject.invoke(brush);
        } catch (Throwable ignored) {}
    }

    public void present() {
        if (lastW <= 0 || lastH <= 0)
            return;

        try {
            BitBlt.invoke(hdcScreen, 0, 0, lastW, lastH, hdcMem, 0, 0, 0x00CC0020);
        } catch (Throwable ignored) {}
    }

    @Override
    public void drawRect(int x, int y, int w, int h) {
        renderRect(x, y, w, h, true);
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        renderRect(x, y, w, h, false);
    }

    // =========================
    // Circles & Ellipses
    // =========================

    @Override
    public void drawCircle(int x, int y, int r) {
        renderEllipse(x, y, r * 2, r * 2, true);
    }

    @Override
    public void fillCircle(int x, int y, int r) {
        renderEllipse(x, y, r * 2, r * 2, false);
    }

    private void renderEllipse(int x, int y, int w, int h, boolean outline) {
        try {
            // Select Pen (Outline) or Null Pen (Filled)
            MemorySegment pen = outline
                    ? (MemorySegment) CreatePen.invoke(0, 1, currentColorRef)
                    : (MemorySegment) GetStockObject.invoke(8); // NULL_PEN

            // Select Null Brush (Outline) or Solid Brush (Filled)
            MemorySegment brush = outline
                    ? (MemorySegment) GetStockObject.invoke(5) // NULL_BRUSH
                    : (MemorySegment) CreateSolidBrush.invoke(currentColorRef);

            MemorySegment oldP = (MemorySegment) SelectObject.invoke(hdcMem, pen);
            MemorySegment oldB = (MemorySegment) SelectObject.invoke(hdcMem, brush);

            // GDI Ellipse uses bounding box coordinates
            Ellipse.invoke(hdcMem, x, y, x + w, y + h);

            // Restore and Cleanup
            SelectObject.invoke(hdcMem, oldP);
            SelectObject.invoke(hdcMem, oldB);

            if (outline)
                DeleteObject.invoke(pen);
            else
                DeleteObject.invoke(brush);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void renderRect(int x, int y, int w, int h, boolean outline) {
        try {
            MemorySegment pen = outline ? (MemorySegment) CreatePen.invoke(0, 1, currentColorRef) : (MemorySegment) GetStockObject.invoke(8);
            MemorySegment brush = outline ? (MemorySegment) GetStockObject.invoke(5) : (MemorySegment) CreateSolidBrush.invoke(currentColorRef);
            MemorySegment oldP = (MemorySegment) SelectObject.invoke(hdcMem, pen);
            MemorySegment oldB = (MemorySegment) SelectObject.invoke(hdcMem, brush);

            Rectangle.invoke(hdcMem, x, y, x + w, y + h);
            SelectObject.invoke(hdcMem, oldP);
            SelectObject.invoke(hdcMem, oldB);

            if (outline)
                DeleteObject.invoke(pen);
            else
                DeleteObject.invoke(brush);

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // =========================
    // Image Rendering
    // =========================

    @Override
    public void drawImage(Image image, int x, int y, int w, int h) {
        try {
            NativeImage natImg = imageCache.computeIfAbsent(image, this::toNativeImage);
            MemorySegment memDC = (MemorySegment) CreateCompatibleDC.invoke(hdcMem);
            MemorySegment old = (MemorySegment) SelectObject.invoke(memDC, natImg.hBitmap());

            // AC_SRC_ALPHA blend
            AlphaBlend.invoke(hdcMem, x, y, w, h, memDC, 0, 0, natImg.width(), natImg.height(), 0x01FF0000);

            SelectObject.invoke(memDC, old);
            DeleteDC.invoke(memDC);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private NativeImage toNativeImage(Image image) {
        BufferedImage bi = (image instanceof BufferedImage b) ? b : toBufferedImage(image);
        int w = bi.getWidth();
        int h = bi.getHeight();

        int[] pixels = bi.getRGB(0, 0, w, h, null, 0, w);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment bmi = arena.allocate(40);

            bmi.set(JAVA_INT, 0, 40);
            bmi.set(JAVA_INT, 4, w);
            bmi.set(JAVA_INT, 8, -h);
            bmi.set(JAVA_SHORT, 12, (short) 1);
            bmi.set(JAVA_SHORT, 14, (short) 32);

            MemorySegment ppvBits = arena.allocate(ADDRESS);
            MemorySegment hLocalBitmap = (MemorySegment) CreateDIBSection.invoke(hdcScreen, bmi, 0, ppvBits, MemorySegment.NULL, 0);
            MemorySegment pixelView = ppvBits.get(ADDRESS, 0).reinterpret((long) w * h * 4);

            for (int i = 0; i < pixels.length; i++) {
                int argb = pixels[i];
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;

                if (a < 255) { // Pre-multiply Alpha for GDI
                    r = (r * a) / 255; g = (g * a) / 255; b = (b * a) / 255;
                }
                pixelView.setAtIndex(JAVA_INT, i, (b) | (g << 8) | (r << 16) | (a << 24));
            }
            return new NativeImage(hLocalBitmap, w, h);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bi;
    }

    // =========================
    // Cleanup
    // =========================

    public void dispose() {
        try {
            if (defaultBitmap != null && hdcMem != null)
                SelectObject.invoke(hdcMem, defaultBitmap);

            if (hdcMem != null)
                DeleteDC.invoke(hdcMem);

            if (hBitmap != null && !hBitmap.equals(MemorySegment.NULL))
                DeleteObject.invoke(hBitmap);

            ReleaseDC.invoke(hwnd, hdcScreen);

            for (NativeImage cached : imageCache.values())
                DeleteObject.invoke(cached.hBitmap());

            imageCache.clear();

            for (MemorySegment hFont : fontCache.values())
                DeleteObject.invoke(hFont);

            fontCache.clear();
        } catch (Throwable ignored) {}
    }
}