package objectdata;

public record Vector(double x, double y) {

    public Vector add(final Vector other) {
        return new Vector(x + other.x(), y + other.y());
    }

    public Vector mul(final double k) {
        return new Vector(k * x, k * y);
    }

    /**
     * Dot product between this vector and the other vector
     * v.dot(u) = cos(\alpha) * ||v|| * ||u||
     * where \alpha is the angle between v and u.
     *
     * @param other Vector to compute dot product with
     * @return dot product of the two vectors
     */
    public double dot(final Vector other) {
        return x * other.x() + y * other.y();
    }

    /**
     * Cross product between this vector and the other vector
     * v.cross(u) = signed area of the parallelogram defined by v and u
     *
     * @param other Vector to compute cross product with
     * @return cross product of the two vectors
     */
    public double cross(final Vector other) {
        return x * other.y() - y * other.x();
    }
}
