package objectdata;

public record Vector(double x, double y) {

    public Vector add(final Vector other) {
        return new Vector(x + other.x(), y + other.y());
    }

    public Vector mul(final double k) {
        return new Vector(k * x, k * y);
    }

    public double dot(final Vector other) {
        return x * other.x() + y * other.y();
    }

    public double cross(final Vector other) {
        return x * other.y() - y * other.x();
    }
}
