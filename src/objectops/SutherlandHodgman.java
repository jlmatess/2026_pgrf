package objectops;

import objectdata.*;

import java.util.ArrayList;
import java.util.List;

public class SutherlandHodgman {

    public Polygon clip(Polygon polygonToClip, ConvexPolygon clippingPolygon) {
        Polygon result = polygonToClip;
        // Prevedeme orezavaci polygon na CW orientaci, aby vnitrni strana byla vzdy vpravo (side >= 0)
        ConvexPolygon cwClipper = clippingPolygon.toCW();
        for (LineSegment edge : cwClipper.getLineSegments()) {
            result = clipOne(result, edge.toLine());
        }
        return result;
    }

    private Polygon clipOne(Polygon polygonToClip, Line clippingLine) {
        List<Point> points = polygonToClip.getPoints();
        if (points.isEmpty()) {
            return polygonToClip;
        }

        List<Point> resultPoints = new ArrayList<>();
        Point p1 = points.getLast();

        for (Point p2 : points) {
            boolean p1Inside = clippingLine.side(p1) >= 0;
            boolean p2Inside = clippingLine.side(p2) >= 0;

            if (p1Inside && p2Inside) {
                resultPoints.add(p2);
            } else if (p1Inside && !p2Inside) {
                clippingLine.intersection(new LineSegment(p1, p2).toLine())
                        .ifPresent(resultPoints::add);
            } else if (!p1Inside && p2Inside) {
                clippingLine.intersection(new LineSegment(p1, p2).toLine())
                        .ifPresent(resultPoints::add);
                resultPoints.add(p2);
            }
            p1 = p2;
        }

        return new Polygon().withPoints(resultPoints);
    }
}