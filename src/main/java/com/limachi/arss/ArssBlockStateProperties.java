package com.limachi.arss;

import com.limachi.arss.blocks.EpuratedRedstone;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ArssBlockStateProperties {

    public enum MemoryMode implements StringRepresentable {
        SET("set"),
        RESET("reset");

        private final String name;
        MemoryMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }

        public boolean set() { return this == SET; }
        public boolean reset() { return this == RESET; }
    }

    public enum AdderMode implements StringRepresentable {
        COMPARE("compare"), // >=
        ADD("add"); // +

        private final String name;
        AdderMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }

        public boolean compare() { return this == COMPARE; }
        public boolean add() { return this == ADD; }
    }

    public enum CheckerMode implements StringRepresentable {
        EQUAL("equal"), // ==
        DIFFERENT("different"); // !=

        private final String name;
        CheckerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }

        public boolean equal() { return this == EQUAL; }
        public boolean different() { return this == DIFFERENT; }
    }

    public enum EdgeMode implements StringRepresentable {
        RISING("rising"),
        LOWERING("lowering");

        private final String name;
        EdgeMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }

        public boolean rising() { return this == RISING; }
        public boolean lowering() { return this == LOWERING; }
    }

    public enum DemuxerMode implements StringRepresentable {
        ONE("1"),
        TWO("2"),
        FOUR("4"),
        EIGHT("8"),
        SIDE_CONTROL("side_control");

        private final String name;
        DemuxerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }
    }

    public enum DelayerMode implements StringRepresentable {
        ONE("1"),
        TWO("2"),
        FOUR("4"),
        EIGHT("8"),
        SIDE_CONTROL("side_control");

        private final String name;
        DelayerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }
    }

    public enum ShifterMode implements StringRepresentable {
        UP("up"),
        DOWN("down");

        private final String name;
        ShifterMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @NotNull String getSerializedName() { return this.name; }

        public boolean up() { return this == UP; }
        public boolean down() { return this == DOWN; }
    }

    public static final IntegerProperty RANGE = IntegerProperty.create("range", 0, EpuratedRedstone.TOTAL_RANGE);
    public static final IntegerProperty PREVIOUS_READ_POWER = IntegerProperty.create("previous_read_power", 0, 15);
    public static final EnumProperty<MemoryMode> MEMORY_MODE = EnumProperty.create("mode", MemoryMode.class);
    public static final EnumProperty<AdderMode> ADDER_MODE = EnumProperty.create("mode", AdderMode.class);
    public static final EnumProperty<CheckerMode> CHECKER_MODE = EnumProperty.create("mode", CheckerMode.class);
    public static final EnumProperty<EdgeMode> EDGE_MODE = EnumProperty.create("mode", EdgeMode.class);
    public static final EnumProperty<DemuxerMode> DEMUXER_MODE = EnumProperty.create("mode", DemuxerMode.class);
    public static final EnumProperty<DelayerMode> DELAYER_MODE = EnumProperty.create("mode", DelayerMode.class);
    public static final EnumProperty<ShifterMode> SHIFTER_MODE = EnumProperty.create("mode", ShifterMode.class);
}
