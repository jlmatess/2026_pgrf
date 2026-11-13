package objectdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Polygon {

    private final List<Point> points;

    public Polygon() {
        points = List.of();
    }

    private Polygon(final List<Point> points) {
        this.points = points;
    }

    protected Polygon(final Polygon polygon) {
        points = polygon.points;
    }

    public Polygon withPoint(final Point point) {
        final List<Point> newPoints = new ArrayList<>(this.points);
        newPoints.add(point);
        return new Polygon(newPoints);
    }

    public Polygon withPoints(final Collection<Point> points) {
        final List<Point> newPoints = new ArrayList<>(this.points);
        newPoints.addAll(points);
        return new Polygon(newPoints);
    }

    public List<LineSegment> getLineSegments() {
        final List<LineSegment> lineSegments = new ArrayList<>();
        if (points.size() < 2) {
            return lineSegments;
        }
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            lineSegments.add(new LineSegment(p1, p2));
        }
        return lineSegments;
    }

    public List<Point> getPoints() {
        return List.copyOf(points);
    }

    public int getNearestPointIndex(final double x, final double y) {
        int nearestIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            double dist = Math.hypot(p.x() - x, p.y() - y);
            if (dist < minDistance) {
                minDistance = dist;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    public Polygon withUpdatedPoint(final int index, final Point newPoint) {
        if (index < 0 || index >= points.size()) {
            return this;
        }
        final List<Point> newPoints = new ArrayList<>(this.points);
        newPoints.set(index, newPoint);
        return new Polygon(newPoints);
    }

    public boolean isCW() {
        if (points.size() < 3) {
            return false;
        }
        double signedArea = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            signedArea += (p2.x() - p1.x()) * (p2.y() + p1.y());
        }
        return signedArea < 0;
    }

    public Polygon toCW() {
        if (isCW()) return this;
        return new Polygon(points.reversed());
    }

    public Polygon toCCW() {
        if (!isCW()) return this;
        return new Polygon(points.reversed());
    }
}