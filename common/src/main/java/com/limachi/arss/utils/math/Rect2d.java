package com.limachi.arss.utils.math;

import java.lang.constant.*;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public record Rect2d(double x, double y, double w, double h) implements Constable {
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.CONSTRUCTOR, ClassDesc.of(Rect2d.class.getName()), "<init>", "(DDDD)V"),
                "Rect2d", ClassDesc.of(Rect2d.class.getName()),
                Double.valueOf(x).describeConstable().orElseThrow(), Double.valueOf(y).describeConstable().orElseThrow(), Double.valueOf(w).describeConstable().orElseThrow(), Double.valueOf(h).describeConstable().orElseThrow()));
    }
    public String toString() { return "Rec2d(" + x + ", " + x + ", " + w + ", " + h + ")"; }
    public boolean equals(Object o) { return this == o || (o instanceof Rect2d other && other.x == x && other.y == y && other.w == w && other.h == h); }
    public int hashCode() { return Objects.hash(x, y, w, h); }
    public static Rect2d from(Pos2d pos, Size2d size) { return new Rect2d(pos.x(), pos.y(), size.w(), size.h()); }
    public static Rect2d from(double x, double y, Size2d size) { return new Rect2d(x, y, size.w(), size.h()); }
    public static Rect2d from(Pos2d pos, double w, double h) { return new Rect2d(pos.x(), pos.y(), w, h); }
    public static Rect2d from(Size2d size) { return new Rect2d(0., 0., size.w(), size.h()); }
    public static Rect2d from(double w, double h) { return new Rect2d(0., 0., w, h); }
    public static Rect2d from(Pos2d topLeft, Pos2d bottomRight) { return from(topLeft, bottomRight.x() - topLeft.x(), bottomRight.y() - topLeft.x()); }
    public static Rect2d splat(double p, double s) { return new Rect2d(p, p, s, s); }
    public static Rect2d splat(double v) { return new Rect2d(v, v, v, v); }
    public Pos2d pos() { return Pos2d.from(this); }
    public Size2d size() { return Size2d.from(this); }
    public Rect2d withX(double x) { return new Rect2d(x, y, w, h); }
    public Rect2d withY(double y) { return new Rect2d(x, y, w, h); }
    public Rect2d withW(double w) { return new Rect2d(x, y, w, h); }
    public Rect2d withH(double h) { return new Rect2d(x, y, w, h); }
    public Rect2d withPos(double x, double y) { return new Rect2d(x, y, w, h); }
    public Rect2d with(Pos2d pos) { return from(pos, w, h); }
    public Rect2d withSize(double w, double h) { return new Rect2d(x, y, w, h); }
    public Rect2d with(Size2d size) { return from(x, y, size); }
    public double x0() { return x; }
    public double x1() { return x + w; }
    public double y0() { return y; }
    public double y1() { return y + h; }
    public Pos2d topLeft() { return pos(); }
    public Pos2d topRight() { return new Pos2d(x1(), y); }
    public Pos2d bottomLeft() { return new Pos2d(x, y1()); }
    public Pos2d bottomRight() { return new Pos2d(x1(), y1()); }
    public Rect2d addX(double x) { return new Rect2d(this.x + x, y, w, h); }
    public Rect2d addY(double y) { return new Rect2d(x, this.y + y, w, h); }
    public Rect2d addW(double w) { return new Rect2d(x, y, this.w + w, h); }
    public Rect2d addH(double h) { return new Rect2d(x, y, w, this.h + h); }
    public Rect2d addPos(double x, double y) { return new Rect2d(this.x + x, this.y + y, w, h); }
    public Rect2d add(Pos2d pos) { return from(pos.add(x, y), w, h); }
    public Rect2d addSize(double w, double h) { return new Rect2d(x, y, this.w + w, this.h + h); }
    public Rect2d add(Size2d size) { return from(x, y, size.add(w, h)); }
    public Rect2d subX(double x) { return new Rect2d(this.x - x, y, w, h); }
    public Rect2d subY(double y) { return new Rect2d(x, this.y - y, w, h); }
    public Rect2d subW(double w) { return new Rect2d(x, y, this.w - w, h); }
    public Rect2d subH(double h) { return new Rect2d(x, y, w, this.h - h); }
    public Rect2d subPos(double x, double y) { return new Rect2d(this.x - x, this.y - y, w, h); }
    public Rect2d sub(Pos2d pos) { return from(pos.sub(x, y), w, h); }
    public Rect2d subSize(double w, double h) { return new Rect2d(x, y, this.w - w, this.h - h); }
    public Rect2d sub(Size2d size) { return from(x, y, size.sub(w, h)); }
    public boolean inside(Pos2d pos) { return pos.x() >= x && pos.x() <= x1() && pos.y() >= x && pos.x() <= y1(); }
}
