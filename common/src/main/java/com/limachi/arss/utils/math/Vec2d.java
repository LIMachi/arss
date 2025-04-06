package com.limachi.arss.utils.math;

import java.lang.constant.*;
import java.util.Objects;
import java.util.Optional;

public record Vec2d(double x1, double y1, double x2, double y2) implements Constable {
    public static final Vec2d ZERO = new Vec2d(0, 0, 0, 0);
    public static final Vec2d X = new Vec2d(0, 0, 1, 0);
    public static final Vec2d Y = new Vec2d(0, 0, 0, 1);
    @Override
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.CONSTRUCTOR, ClassDesc.of(Vec2d.class.getName()), "<init>", "(DDDD)V"),
                "Vec2d", ClassDesc.of(Vec2d.class.getName()),
                Double.valueOf(x1).describeConstable().orElseThrow(), Double.valueOf(y1).describeConstable().orElseThrow(), Double.valueOf(x2).describeConstable().orElseThrow(), Double.valueOf(y2).describeConstable().orElseThrow()));
    }
    public String toString() { return "Vec2d(" + x1 + ", " + y1 + " -> " + x2 + ", " + y2 + ")"; }
    public boolean equals(Object o) { return this == o || (o instanceof Vec2d other && other.x1 == x1 && other.y1 == y1 && other.x2 == x2 && other.y2 == y2); }
    public int hashCode() { return Objects.hash(x1, y1, x2, y2); }
    public static Vec2d from(Pos2d pos1, Pos2d pos2) { return new Vec2d(pos1.x(), pos1.y(), pos2.x(), pos2.y()); }
    public static Vec2d from(Pos2d pos1, double x2, double y2) { return new Vec2d(pos1.x(), pos1.y(), x2, y2); }
    public static Vec2d from(double x1, double y1, Pos2d pos2) { return new Vec2d(x1, y1, pos2.x(), pos2.y()); }
    public static Vec2d fromLen(double x, double y, double lx, double ly) { return new Vec2d(x, y, x + lx, y + ly); }
    public static Vec2d from(Pos2d pos, Size2d len) { return fromLen(pos.x(), pos.y(), len.w(), len.h()); }
    public static Vec2d from(Pos2d len) { return new Vec2d(0, 0, len.x(), len.y()); }
    public static Vec2d from(double lx, double ly) { return new Vec2d(0, 0, lx, ly); }
    public static Vec2d from(Size2d len) { return new Vec2d(0, 0, len.w(), len.h()); }
    public Pos2d pos() { return new Pos2d(x2 - x1, y2 - y1); }
    public Size2d len2d() { return new Size2d(Math.abs(x2 - x1), Math.abs(y2 - y1)); }
    public double len2() {
        Size2d t = len2d();
        return t.w() * t.w() + t.h() * t.h();
    }
    public double len() { return Math.sqrt(len2()); }
    public Rect2d rect() { return Rect2d.from(Double.min(x1, x2), Double.min(y1, y2), len2d()); }
    public Rect2d rect0() { return Rect2d.from(0, 0, len2d()); }
}
