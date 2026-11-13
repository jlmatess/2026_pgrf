package objectops;

import objectdata.LineSegment;
import objectdata.Point;

import java.util.Optional;

public class CohenSutherland {

    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001
    private static final int RIGHT = 2;  // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8;    // 1000

    private final double xMin;
    private final double yMin;
    private final double xMax;
    private final double yMax;

    public CohenSutherland(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = Math.min(xMin, xMax);
        this.yMin = Math.min(yMin, yMax);
        this.xMax = Math.max(xMin, xMax);
        this.yMax = Math.max(yMin, yMax);
    }

    private int computeOutCode(double x, double y) {
        int code = INSIDE;
        if (x < xMin) {
            code |= LEFT;
        } else if (x > xMax) {
            code |= RIGHT;
        }
        if (y < yMin) {
            code |= TOP;
        } else if (y > yMax) {
            code |= BOTTOM;
        }
        return code;
    }

    public Optional<LineSegment> clip(LineSegment segment) {
        double x1 = segment.p1().x();
        double y1 = segment.p1().y();
        double x2 = segment.p2().x();
        double y2 = segment.p2().y();

        int outcode1 = computeOutCode(x1, y1);
        int outcode2 = computeOutCode(x2, y2);

        while (true) {
            if ((outcode1 | outcode2) == 0) {
                // Trivialni prijeti (oba body uvnitr okna)
                return Optional.of(new LineSegment(new Point(x1, y1), new Point(x2, y2)));
            } else if ((outcode1 & outcode2) != 0) {
                // Trivialni zamitnuti (oba body mimo okno na stejne strane)
                return Optional.empty();
            } else {
                // Castecne uvnitr - vybereme bod mimo okno a spocitame prusecik s hranou
                double x = 0;
                double y = 0;
                int outcodeOut = (outcode1 != 0) ? outcode1 : outcode2;

                if ((outcodeOut & TOP) != 0) {
                    x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
                    y = yMin;
                } else if ((outcodeOut & BOTTOM) != 0) {
                    x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
                    y = yMax;
                } else if ((outcodeOut & RIGHT) != 0) {
                    y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
                    x = xMax;
                } else if ((outcodeOut & LEFT) != 0) {
                    y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
                    x = xMin;
                }

                if (outcodeOut == outcode1) {
                    x1 = x;
                    y1 = y;
                    outcode1 = computeOutCode(x1, y1);
                } else {
                    x2 = x;
                    y2 = y;
                    outcode2 = computeOutCode(x2, y2);
                }
            }
        }
    }
}