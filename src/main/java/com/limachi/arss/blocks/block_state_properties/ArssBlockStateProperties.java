package com.limachi.arss.blocks.block_state_properties;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ArssBlockStateProperties {

    public enum MemoryMode implements StringRepresentable {
        SET("set"),
        RESET("reset");

        private final String name;
        MemoryMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean set() { return this == SET; }
        public boolean reset() { return this == RESET; }
    }

    public enum AdderMode implements StringRepresentable {
        COMPARE("compare"), // >=
        ADD("add"); // +

        private final String name;
        AdderMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean compare() { return this == COMPARE; }
        public boolean add() { return this == ADD; }
    }

    public enum CheckerMode implements StringRepresentable {
        EQUAL("equal"), // ==
        DIFFERENT("different"); // !=

        private final String name;
        CheckerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean equal() { return this == EQUAL; }
        public boolean different() { return this == DIFFERENT; }
    }

    public enum EdgeMode implements StringRepresentable {
        RISING("rising"),
        FALLING("falling");

        private final String name;
        EdgeMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean rising() { return this == RISING; }
        public boolean falling() { return this == FALLING; }
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
        public @Nonnull String getSerializedName() { return this.name; }
    }

    public enum DelayerMode implements StringRepresentable {
        ONE("1"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        SIDE_CONTROL("side_control");

        private final String name;
        DelayerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }
    }

    public enum ShifterMode implements StringRepresentable {
        UP("up"),
        DOWN("down");

        private final String name;
        ShifterMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean up() { return this == UP; }
        public boolean down() { return this == DOWN; }
    }

    public enum SignalGeneratorMode implements StringRepresentable {
        INVERSE_SAW("inverse_saw"),
        RANDOM("random"),
        SAW("saw"),
        SINE("sine"),
        SQUARE("square"),
        TRIANGLE("triangle");

        private final String name;
        SignalGeneratorMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }
    }

    public enum SequencerMode implements StringRepresentable {
        PLAY_ONCE("play_once"),
        PLAY_LOOP("play_loop"),
        RECORD("record");

        private final String name;
        SequencerMode(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public boolean isPlaying() { return this == PLAY_ONCE || this == PLAY_LOOP; }
        public boolean isRecording() { return this == RECORD; }
    }

    public enum SideToggling implements StringRepresentable {
        ALL_ACTIVE("all_active"),
        RIGHT_DISABLED("right_disabled"),
        LEFT_DISABLED("left_disabled"),
        BOTH_SIDE_DISABLED("both_side_disabled"), //only a few selected gates can use this property
        INPUT_DISABLED("input_disabled"); //only combinator gates with agnostic side can use this property

        private final String name;
        SideToggling(String name) { this.name = name; }
        public String toString() { return this.getSerializedName(); }
        public @Nonnull String getSerializedName() { return this.name; }

        public SideToggling cycle(boolean up, boolean includeBothSides, boolean includeInput) {
            if (up)
                return switch (this) {
                    case ALL_ACTIVE -> RIGHT_DISABLED;
                    case RIGHT_DISABLED -> LEFT_DISABLED;
                    case LEFT_DISABLED -> includeBothSides ? BOTH_SIDE_DISABLED : includeInput ? INPUT_DISABLED : ALL_ACTIVE;
                    case BOTH_SIDE_DISABLED -> includeInput ? INPUT_DISABLED : ALL_ACTIVE;
                    case INPUT_DISABLED -> ALL_ACTIVE;
                };
            else
                return switch (this) {
                    case ALL_ACTIVE -> includeInput ? INPUT_DISABLED : includeBothSides ? BOTH_SIDE_DISABLED : LEFT_DISABLED;
                    case RIGHT_DISABLED -> ALL_ACTIVE;
                    case LEFT_DISABLED -> RIGHT_DISABLED;
                    case BOTH_SIDE_DISABLED -> LEFT_DISABLED;
                    case INPUT_DISABLED -> includeBothSides ? BOTH_SIDE_DISABLED : LEFT_DISABLED;
                };
        }

        public boolean acceptRight() { return this != RIGHT_DISABLED && this != BOTH_SIDE_DISABLED; }

        public boolean acceptLeft() { return this != LEFT_DISABLED && this != BOTH_SIDE_DISABLED; }

        public boolean acceptBelow() { return true; } //FIXME
    }

    public static final IntegerProperty ENRICHED_RS_RANGE = IntegerProperty.create("range", 0, 4);
    public static final IntegerProperty PERFECTED_RS_RANGE = IntegerProperty.create("range", 0, 32);
    public static final EnumProperty<MemoryMode> MEMORY_MODE = EnumProperty.create("mode", MemoryMode.class);
    public static final EnumProperty<AdderMode> ADDER_MODE = EnumProperty.create("mode", AdderMode.class);
    public static final EnumProperty<CheckerMode> CHECKER_MODE = EnumProperty.create("mode", CheckerMode.class);
    public static final EnumProperty<EdgeMode> EDGE_MODE = EnumProperty.create("mode", EdgeMode.class);
    public static final EnumProperty<DemuxerMode> DEMUXER_MODE = EnumProperty.create("mode", DemuxerMode.class);
    public static final EnumProperty<DelayerMode> DELAYER_MODE = EnumProperty.create("mode", DelayerMode.class);
    public static final EnumProperty<ShifterMode> SHIFTER_MODE = EnumProperty.create("mode", ShifterMode.class);
    public static final BooleanProperty HIGH = BooleanProperty.create("high");
    public static final EnumProperty<SignalGeneratorMode> GENERATOR_MODE = EnumProperty.create("mode", SignalGeneratorMode.class);
    public static final EnumProperty<SequencerMode> SEQUENCER_MODE = EnumProperty.create("mode", SequencerMode.class);
    public static final EnumProperty<SideToggling> SIDES = EnumProperty.create("sides", SideToggling.class);
    public static final BooleanProperty CAN_SCROLL = BooleanProperty.create("can_scroll");
    public static final BooleanProperty HIDE_DOT = BooleanProperty.create("hide_dot");
    public static final BooleanProperty BOOSTED = BooleanProperty.create("boosted");
}
