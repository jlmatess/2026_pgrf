package rasterdata;

import java.util.Optional;

public interface Raster {

    int getWidth();

    int getHeight();

    void setColor(int c, int r, int color);

    Optional<Integer> getColor(int c, int r);

    default boolean isValid(int c, int r) {
        return (c >= 0 && c < getWidth() && r >= 0 && r < getHeight());
    }

}
