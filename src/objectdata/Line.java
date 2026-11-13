package objectdata;

import java.util.Optional;


public record Line(Point p, Vector v) {


    public Optional<Point> intersection(final Line other) {
        double denom = this.v.cross(other.v);
        if (Math.abs(denom) < 1e-9) {
            return Optional.empty();
        }
        Vector diff = new Vector(other.p.x() - this.p.x(), other.p.y() - this.p.y());
        double t = diff.cross(other.v) / denom;
        return Optional.of(new Point(
                this.p.x() + t * this.v.x(),
                this.p.y() + t * this.v.y()
        ));
    }

    public double side(final Point q) {
        Vector u = new Vector(q.x() - p.x(), q.y() - p.y());
        return v.cross(u);
    }
}