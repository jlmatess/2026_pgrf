package rasterops;

import rasterdata.Raster;

public class LineRasterizerTrivial implements LineRasterizer {

    @Override
    public void draw(Raster raster, int c1, int r1, int c2, int r2, int color) {
        int dc = c2 - c1;
        int dr = r2 - r1;

        if (Math.abs(dc) >= Math.abs(dr)) {
            if (c1 > c2) {
                int tempC = c1; c1 = c2; c2 = tempC;
                int tempR = r1; r1 = r2; r2 = tempR;
            }
            double k = (double) (r2 - r1) / (c2 - c1 == 0 ? 1 : c2 - c1);
            double q = r1 - k * c1;

            for (int c = c1; c <= c2; c++) {
                int r = (int) Math.round(k * c + q);
                if (raster.isValid(c, r)) {
                    raster.setColor(c, r, color);
                }
            }
        } else {

            if (r1 > r2) {
                int tempC = c1; c1 = c2; c2 = tempC;
                int tempR = r1; r1 = r2; r2 = tempR;
            }
            double k = (double) (c2 - c1) / (r2 - r1 == 0 ? 1 : r2 - r1);
            double q = c1 - k * r1;

            for (int r = r1; r <= r2; r++) {
                int c = (int) Math.round(k * r + q);
                if (raster.isValid(c, r)) {
                    raster.setColor(c, r, color);
                }
            }
        }
    }
}