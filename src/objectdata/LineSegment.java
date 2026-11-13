package objectdata;

public record LineSegment(Point p1, Point p2) {

    public Vector toVector() {
        return new Vector(p2.x() - p1.x(), p2.y() - p1.y());
    }

    public Line toLine() {
        return new Line(p1, toVector());
    }
}