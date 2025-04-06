package com.limachi.arss.utils.math;

import java.lang.constant.*;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public record Size2d(double w, double h) implements Constable {
    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.CONSTRUCTOR, ClassDesc.of(Size2d.class.getName()), "<init>", "(DD)V"),
                "Size2d", ClassDesc.of(Size2d.class.getName()),
                Double.valueOf(w).describeConstable().orElseThrow(), Double.valueOf(h).describeConstable().orElseThrow()));
    }
    public String toString() { return "Size2d(" + w + ", " + h + ")"; }
    public boolean equals(Object o) { return this == o || (o instanceof Size2d other && other.w == w && other.h == h); }
    public int hashCode() { return Objects.hash(w, h); }
    public static Size2d from(Rect2d rect) { return new Size2d(rect.w(), rect.h()); }
    public static Size2d splat(double v) { return new Size2d(v, v); }
    public Rect2d rectangle(Pos2d pos) { return Rect2d.from(pos, this); }
    public Rect2d rectangle(double x, double y) { return Rect2d.from(x, y, this); }
    public Rect2d rectangle() { return Rect2d.from(this); }
    public Pos2d asPos() { return new Pos2d(w, h); }
    public Size2d withW(double w) { return new Size2d(w, h); }
    public Size2d withH(double h) { return new Size2d(w, h); }
    public Size2d addX(double w) { return new Size2d(this.w + w, h); }
    public Size2d addY(double h) { return new Size2d(w, this.h + h); }
    public Size2d add(Size2d size) { return new Size2d(w + size.w, h + size.h); }
    public Size2d add(double w, double h) { return new Size2d(this.w + w, this.h + h); }
    public Size2d add(double v) { return new Size2d(w + v, h + v); }
    public Size2d subX(double w) { return new Size2d(this.w - w, h); }
    public Size2d subY(double h) { return new Size2d(w, this.h - h); }
    public Size2d sub(Size2d size) { return new Size2d(w - size.w, h - size.h); }
    public Size2d sub(double w, double h) { return new Size2d(this.w - w, this.h - h); }
    public Size2d sub(double v) { return new Size2d(w - v, h - v); }
    public Size2d neg() { return new Size2d(-w, -h); }
    public Size2d mulW(double w) { return new Size2d(this.w * w, h); }
    public Size2d mulH(double h) { return new Size2d(w, this.h * h); }
    public Size2d mul(double v) { return new Size2d(w * v, h * v); }
    public Size2d mul(double w, double h) { return new Size2d(this.w * w, this.h * h); }
    public Size2d mul(Size2d size) { return new Size2d(w * size.w, h * size.h); }
    public Size2d divW(double w) { return new Size2d(this.w / w, h); }
    public Size2d divH(double h) { return new Size2d(w, this.h / h); }
    public Size2d div(double v) { return new Size2d(w / v, h / v); }
    public Size2d div(double w, double h) { return new Size2d(this.w / w, this.h / h); }
    public Size2d div(Size2d size) { return new Size2d(w / size.w, h / size.h); }
    public Size2d min(Size2d other) { return new Size2d(Double.min(w, other.w), Double.min(h, other.h)); }
    public Size2d max(Size2d other) { return new Size2d(Double.max(w, other.w), Double.max(h, other.h)); }
}
