package objectdata;

import java.util.Optional;

public class ConvexPolygon extends Polygon {

    private ConvexPolygon(final Polygon polygon) {
        super(polygon);
    }

    public static boolean isConvex(final Polygon polygon) {
        final var segments = polygon.getLineSegments();
        if (segments.size() < 3) {
            return false;
        }
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (int i = 0; i < segments.size(); i++) {
            Vector v1 = segments.get(i).toVector();
            Vector v2 = segments.get((i + 1) % segments.size()).toVector();
            double cross = v1.cross(v2);

            if (cross > 1e-9) {
                hasPositive = true;
            } else if (cross < -1e-9) {
                hasNegative = true;
            }

            if (hasPositive && hasNegative) {
                return false;
            }
        }
        return true;
    }

    public ConvexPolygon toCW() {
        return new ConvexPolygon(super.toCW());
    }

    public ConvexPolygon toCCW() {
        return new ConvexPolygon(super.toCCW());
    }

    public static Optional<ConvexPolygon> of(final Polygon polygon) {
        if (isConvex(polygon)) {
            return Optional.of(new ConvexPolygon(polygon));
        }
        return Optional.empty();
    }


}
