package rasterops;

import objectdata.LineSegment;
import objectdata.Polygon;
import rasterdata.Raster;
import java.util.ArrayList;
import java.util.List;
public class ScanLine implements PatternFill {

    public void fill(
    final Raster raster,
    final Polygon polygon,
    final int fillColor,
    final int outlineColor,
    final PolygonRasterizer polygonRasterizer,
    final LineRasterizer lineRasterizer) {
        final Raster pattern = new rasterdata.RasterBI(1, 1);
        pattern.setColor(0, 0, fillColor);
        fill(raster, polygon, pattern, outlineColor, polygonRasterizer, lineRasterizer);
    }

    public void fill(
    final Raster raster,
    final Polygon polygon,
    final Raster pattern,
    final int outlineColor,
    final PolygonRasterizer polygonRasterizer,
    final LineRasterizer lineRasterizer) {

        List<LineSegment> filteredOrientedLines = new ArrayList<>();
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (LineSegment segment : polygon.getLineSegments()) {
            if (segment.p1().y() != segment.p2().y()) {
                LineSegment oriented = segment.p1().y() < segment.p2().y()
                        ? segment
                        : new LineSegment(segment.p2(), segment.p1());
                filteredOrientedLines.add(oriented);

                if (oriented.p1().y() < minY) minY = oriented.p1().y();
                if (oriented.p2().y() > maxY) maxY = oriented.p2().y();
            }
        }

        int yStart = Math.max(0, (int) Math.ceil(minY));
        int yEnd = Math.min(raster.getHeight() - 1, (int) Math.floor(maxY));

        for (int y = yStart; y <= yEnd; y++) {
            List<Double> intersections = new ArrayList<>();
            for (LineSegment segment : filteredOrientedLines) {
                if (intersects(segment, y)) {
                    intersections.add(xAt(segment, y));
                }
            }
            intersections.sort(Double::compareTo);

            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                int xStart = (int) Math.ceil(intersections.get(i));
                int xEnd = (int) Math.floor(intersections.get(i + 1));
                for (int x = xStart; x <= xEnd; x++) {
                    if (raster.isValid(x, y)) {
                        raster.setColor(x, y, getPatternColor(pattern, x, y));
                    }
                }
            }
        }

        polygonRasterizer.draw(raster, polygon, lineRasterizer, outlineColor);
    }

    private boolean intersects(final LineSegment lineSegment, final int y) {
        return y >= lineSegment.p1().y() && y < lineSegment.p2().y();
    }

    private double xAt(final LineSegment lineSegment, final int y) {
        double dx = lineSegment.p2().x() - lineSegment.p1().x();
        double dy = lineSegment.p2().y() - lineSegment.p1().y();
        return lineSegment.p1().x() + dx / dy * (y - lineSegment.p1().y());
    }
}