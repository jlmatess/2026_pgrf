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
        // 1. Get color at seed (current color)
        final Optional<Integer> maybeCurrentColor = raster.getColor(c, r);
        if (maybeCurrentColor.isPresent()) { // false if seed address is invalid
            final Integer currentColor = maybeCurrentColor.get();
            // 2. Compare current color with bgColor
            if (isInArea.test(currentColor)) {
                // Set color of the seed using fillColor
                raster.setColor(c, r, fillColor);
                // Repeat for neighbors
                fill(raster, c, r + 1, isInArea, fillColor);
                fill(raster, c, r - 1, isInArea, fillColor);
                fill(raster, c + 1, r, isInArea, fillColor);
                fill(raster, c - 1, r, isInArea, fillColor);
            }

        }

    }
}
