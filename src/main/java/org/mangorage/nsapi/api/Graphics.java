package org.mangorage.nsapi.api;

import java.awt.Image;

public interface Graphics {
    void drawImage(Image image, int x, int y, int w, int h);
    void renderText(String text, int x, int y, int fontSize, boolean bold);

    int getTextWidth(String text, int fontSize, boolean bold);

    void drawRect(int x, int y, int w, int h);
    void fillRect(int x, int y, int w, int h);

    void drawCircle(int x, int y, int r);
    void fillCircle(int x, int y, int r);

    void drawOval(int x, int y, int w, int h);
    void fillOval(int x, int y, int w, int h);
    void drawRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);
    void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);

    void setColor(int argb);
    void clear(int argb);

    int width();
    int height();
}