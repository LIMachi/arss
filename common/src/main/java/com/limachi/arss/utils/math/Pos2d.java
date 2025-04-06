package com.limachi.arss.utils.math;

import java.lang.constant.*;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public record Pos2d(double x, double y) implements Constable {
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.CONSTRUCTOR, ClassDesc.of(Pos2d.class.getName()), "<init>", "(DD)V"),
                "Pos2d", ClassDesc.of(Pos2d.class.getName()),
                Double.valueOf(x).describeConstable().orElseThrow(), Double.valueOf(y).describeConstable().orElseThrow()));
    }
    public String toString() { return "Pos2d(" + x + ", " + y + ")"; }
    public boolean equals(Object o) { return this == o || (o instanceof Pos2d other && other.x == x && other.y == y); }
    public int hashCode() { return Objects.hash(x, y); }
    public static Pos2d from(Rect2d rect) { return new Pos2d(rect.x(), rect.y()); }
    public static Pos2d splat(double v) { return new Pos2d(v, v); }
    public Rect2d rectangle(Size2d size) { return Rect2d.from(this, size); }
    public Rect2d rectangle(double w, double h) { return Rect2d.from(this, w, h); }
    public Size2d asSize() { return new Size2d(x, y); }
    public Pos2d withX(double x) { return new Pos2d(x, y); }
    public Pos2d withY(double y) { return new Pos2d(x, y); }
    public Pos2d addX(double x) { return new Pos2d(this.x + x, y); }
    public Pos2d addY(double y) { return new Pos2d(x, this.y + y); }
    public Pos2d add(Pos2d pos) { return new Pos2d(x + pos.x, y + pos.y); }
    public Pos2d add(double x, double y) { return new Pos2d(this.x + x, this.y + y); }
    public Pos2d add(double v) { return new Pos2d(x + v, y + v); }
    public Pos2d subX(double x) { return new Pos2d(this.x - x, y); }
    public Pos2d subY(double y) { return new Pos2d(x, this.y - y); }
    public Pos2d sub(Pos2d pos) { return new Pos2d(x - pos.x, y - pos.y); }
    public Pos2d sub(double x, double y) { return new Pos2d(this.x - x, this.y - y); }
    public Pos2d sub(double v) { return new Pos2d(x - v, y - v); }
    public Pos2d neg() { return new Pos2d(-x, -y); }
    public Pos2d mulX(double x) { return new Pos2d(this.x * x, y); }
    public Pos2d mulY(double y) { return new Pos2d(x, this.y * y); }
    public Pos2d mul(double v) { return new Pos2d(x * v, y * v); }
    public Pos2d mul(double x, double y) { return new Pos2d(this.x * x, this.y * y); }
    public Pos2d mul(Pos2d size) { return new Pos2d(x * size.x, y * size.y); }
    public Pos2d divX(double x) { return new Pos2d(this.x / x, y); }
    public Pos2d divY(double y) { return new Pos2d(x, this.y / y); }
    public Pos2d div(double v) { return new Pos2d(x / v, y / v); }
    public Pos2d div(double x, double y) { return new Pos2d(this.x / x, this.y / y); }
    public Pos2d div(Pos2d size) { return new Pos2d(x / size.x, y / size.y); }
    public Pos2d min(Pos2d other) { return new Pos2d(Double.min(x, other.x), Double.min(y, other.y)); }
    public Pos2d max(Pos2d other) { return new Pos2d(Double.max(x, other.x), Double.max(y, other.y)); }
}
