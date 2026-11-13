package objectdata;

import java.util.Optional;

/**
 * Infinite straight oriented line
 */
public record Line(Point p, Vector v) {

    /**
     * Compute the intersection of this line and the other line.
     *
     * @param other Line to compute the intersection with
     * @return Optional of an intersection; empty Optional if two lines are parallel
     */
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

    /**
     * Returns positive values for points lying to the right of this line.
     * Returns negative values for points lying to the left of this line.
     * Returns 0 for points lying on this line.
     * Assumes viewport coordinate system with its origin in the top left corner
     * and the y-axis pointing down.
     *
     * @param q Point to test side for
     * @return numerical value indicating side
     */
    public double side(final Point q) {
        Vector u = new Vector(q.x() - p.x(), q.y() - p.y());
        return v.cross(u);
    }
}