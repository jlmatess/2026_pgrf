package rasterops;

import rasterdata.Raster;

public interface PatternFill {
    default int getPatternColor(Raster pattern, int x, int y) {
        return pattern.getColor(
                Math.floorMod(x, pattern.getWidth()),
                Math.floorMod(y, pattern.getHeight())
        ).orElse(0);
    }
}