package rasterops;

import rasterdata.Raster;
import rasterdata.RasterBI;

import java.util.function.Predicate;

public interface SeedFill {

    void fill(Raster raster, int c, int r, Predicate<Integer> isInArea, Raster pattern);

    default void fill(Raster raster, int c, int r, Predicate<Integer> isInArea, int fillColor) {
        final Raster pattern = new RasterBI(1, 1);
        pattern.setColor(0, 0, fillColor);
        fill(raster, c, r, isInArea, pattern);
    }

    static Predicate<Integer> checkBackground(int bgColor) {
        return color -> color == bgColor;
    }

    static Predicate<Integer> checkBorder(int borderColor) {
        return color -> color != borderColor;
    }
}
