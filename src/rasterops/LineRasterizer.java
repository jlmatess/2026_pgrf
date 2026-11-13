package rasterops;

import rasterdata.Raster;

public interface LineRasterizer {

    void draw(Raster raster, int c1, int r1, int c2, int r2, int color);

    default void draw(Raster raster, double x1, double y1, double x2, double y2, int color) {
        draw(
                raster,
                (int) Math.round(x1),
                (int) Math.round(y1),
                (int) Math.round(x2),
                (int) Math.round(y2),
                color
        );
    }
}