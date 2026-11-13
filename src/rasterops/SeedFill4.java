package rasterops;

import rasterdata.Raster;

import java.util.Optional;
import java.util.function.Predicate;

public class SeedFill4 implements SeedFill {

    @Override
    public void fill(Raster raster, int c, int r, Predicate<Integer> isInArea, Raster pattern) {
        java.util.Queue<int[]> queue = new java.util.ArrayDeque<>();
        queue.add(new int[]{c, r});

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0];
            int y = p[1];

            Optional<Integer> maybeCurrentColor = raster.getColor(x, y);
            if (maybeCurrentColor.isPresent()) {
                int currentColor = maybeCurrentColor.get();
                int fillColor = pattern.getColor(
                        Math.floorMod(x, pattern.getWidth()),
                        Math.floorMod(y, pattern.getHeight())
                ).orElse(0);

                if (isInArea.test(currentColor) && currentColor != fillColor) {
                    raster.setColor(x, y, fillColor);
                    queue.add(new int[]{x, y + 1});
                    queue.add(new int[]{x, y - 1});
                    queue.add(new int[]{x + 1, y});
                    queue.add(new int[]{x - 1, y});
                }
            }
        }
    }
    @Override
    public void fill(final Raster raster, final int c, final int r, final Predicate<Integer> isInArea, final int fillColor) {
        rasterdata.RasterBI singlePixel = new rasterdata.RasterBI(1, 1);
        singlePixel.setColor(0, 0, fillColor);
        fill(raster, c, r, isInArea, singlePixel);
    }
}