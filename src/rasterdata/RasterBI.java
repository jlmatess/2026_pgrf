package rasterdata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class RasterBI implements Raster {

    private final BufferedImage image;

    public RasterBI(final int width, final int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void setColor(final int c, final int r, final int color) {
        if (isValid(c, r)) {
            image.setRGB(c, r, color);
        }
    }

    @Override
    public Optional<Integer> getColor(final int c, final int r) {
        if (isValid(c, r)) {
            return Optional.of(image.getRGB(c, r));
        }
        return Optional.empty();
    }

    public void present(final Graphics g) {
        if (g != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    public void clear(final int color) {
        Graphics g = image.getGraphics();
        if (g != null) {
            g.setColor(new Color(color));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
