package com.limachi.arss.blockEntities;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties;
import com.limachi.arss.blocks.diodes.DiodeBlockFactory;
import com.limachi.arss.client.screen.SequencerScreen;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.Registries;
import com.limachi.lim_lib.registries.Stage;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//new idea for playing: play while at least 1 of the 2 side is powered
//if both sides are powered, play a range delimited by bookmarks
//modes: recording, playing once (reset on signal change), playing loop (reset on signal change)

@StaticInit(Stage.BLOCK_ENTITY)
public class SequencerBlockEntity extends GenericDiodeBlockEntity {


    public static RegistryObject<BlockEntityType<SequencerBlockEntity>> TYPE = Registries.blockEntity(Arss.MOD_ID, "sequencer", SequencerBlockEntity::new, DiodeBlockFactory.getBlockRegister("sequencer"));

    @Configs.Config(min = "60", max = "30000", cmt = "Number of redstone ticks that can be stored in sequencer memory (the default is 600, aka 1 minute).")
    public static int MAXIMUM_TICK_COUNT = 600;
    public static final int MAXIMUM_SUB_SECTION_COUNT = 14; //1-15
    private final ArrayList<Integer> ticks = Util.make(new ArrayList<>(MAXIMUM_TICK_COUNT), l->{for (int i = 0; i < MAXIMUM_TICK_COUNT; ++i) l.add(0); });
    private int head = 0;
    private boolean preview = false;
    private int length = 0;
    private final ArrayList<Integer> limits = new ArrayList<>(); //power 1 -> play from start (limit[-1]) to limit[0], ... power 15 -> play from limit[13] to end (limit[14]). if a limit is invalid (aka 0) skip it.
    private final HashSet<Player> editing = new HashSet<>();
    private boolean playing = false;
    private int startLimit = 0;
    private int finishLimit = 0;
    private int prevStart = 0;
    private int prevFinish = 0;
    private final HashMap<String, String> mappings = Util.make(new HashMap<>(), m->{
        m.put("record", "back");
        m.put("start", "left");
        m.put("finish", "right");
    });

    public final HashMap<String, String> getMappings() { return mappings; }

    public Pair<Integer, Integer> getRangeLimit(int startPower, int finishPower) {
        int start = finishPower > 0 && startPower == 0 ? 1 : startPower;
        int finish = startPower > 0 && finishPower < startPower ? 15 : finishPower;
        start = start > 1 && start - 2 < limits.size() ? limits.get(start - 2) : 0;
        finish = finish > 1 && finish - 2 < limits.size() ? limits.get(finishLimit - 2) : Mth.clamp(length, 0, MAXIMUM_TICK_COUNT);
        return new Pair<>(start, finish);
    }

    public boolean isPlaying() { return playing && ((startLimit > 0 || finishLimit > 0) && getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE).isPlaying()); }
    public boolean isRecording() { return  playing && (startLimit > 0 || finishLimit > 0) && getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE).isRecording(); }

    public ArrayList<Integer> getLimits() { return limits; }

    @RegisterMsg
    public record TogglePlay(BlockPos pos) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be) {
                be.preview = !be.preview;
                if (!be.preview && be.playing)
                    be.playing = false;
                Pair<Integer, Integer> range = be.getRangeLimit(be.prevStart, be.prevFinish);
                if (be.preview && be.getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE) != ArssBlockStateProperties.SequencerMode.PLAY_LOOP && be.head + 1 >= range.getSecond()) {
                    be.head = range.getFirst();
                }
                be.setChanged();
            }
        }
    }

    public void toggleTestPlay() {
        NetworkManager.toServer(new TogglePlay(worldPosition));
    }

    @RegisterMsg
    public record ToggleLimit(BlockPos pos, int tick, int approximation) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be)
                be.toggleLimit(tick, approximation);
        }
    }

    public void toggleLimit(int tick, int approximation) {
        if (level != null && level.isClientSide())
            NetworkManager.toServer(new ToggleLimit(worldPosition, tick, approximation));
        int f = limits.indexOf(tick);
        if (f == -1 && approximation > 0)
            for (int a = 1; a <= approximation; ++a) {
                if (tick - a >= 0) {
                    f = limits.indexOf(tick - a);
                    if (f != -1) break;
                }
                if (tick + a < length) {
                    f = limits.indexOf(tick + a);
                    if (f != -1) break;
                }
            }
        if (f == -1) {
            if (tick < 1 + approximation || tick >= length - 1 - approximation) return;
            if (limits.size() < MAXIMUM_SUB_SECTION_COUNT) {
                for (int i = 0; i < limits.size(); ++i)
                    if (limits.get(i) == 0) {
                        limits.set(i, tick);
                        setChanged();
                        return;
                    } else if (limits.get(i) > tick) {
                        limits.add(i, tick);
                        setChanged();
                        return;
                    }
                limits.add(tick);
                setChanged();
            }
        } else {
            limits.remove(f);
            setChanged();
        }
    }

    public ArrayList<Integer> getTicks() { return ticks; }
    public int getHead() { return head; }
    public void setTick(int tick, int power) {
        power = Mth.clamp(power, 0, 15);
        if (tick >= 0 && tick < ticks.size() && ticks.get(tick) != power) {
            ticks.set(tick, power);
            setChanged();
        }
    }

    public void setHead(int at) {
        if (at != head && at >= 0 && at < ticks.size() && at < length) {
            head = at;
            setChanged();
        }
    }

    public int getLength() { return length; }

    public void setLength(int len) {
        if (len >= 0 && len <= MAXIMUM_TICK_COUNT && len != length) {
            length = len;
            if (head >= length)
                head = length > 0 ? length - 1 : 0;
            setChanged();
        }
    }

    protected boolean isChanged = false;

    @Override
    public void setChanged() {
        super.setChanged();
        isChanged = true;
    }

    public void startEditing(Player player) {
        editing.add(player);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->{
            SequencerScreen.client_open(this);
            return null;
        });
    }

    public int readPower() { return head >= 0 && head < length ? ticks.get(head) : 0; }

    /**
     * start playing:
     *   when the start/finish are updated and at least 1 side is powered or when using playback
     * keep playing:
     *   any side is powered or playback is active
     * stop playing:
     *   end reached or no side powered nor playback active
     */
    public int update(int back, int left, int right) {
        int input = switch (mappings.get("record")) {
            case "back" -> back;
            case "left" -> left;
            case "right" -> right;
            default -> 0;
        };
        int output = input;
        startLimit = switch (mappings.get("start")) {
            case "back" -> back;
            case "left" -> left;
            case "right" -> right;
            default -> 0;
        };
        finishLimit = switch (mappings.get("finish")) {
            case "back" -> back;
            case "left" -> left;
            case "right" -> right;
            default -> 0;
        };

        Pair<Integer, Integer> range = getRangeLimit(startLimit, finishLimit);
        int sl = range.getFirst();
        int fl = range.getSecond();
        boolean prevPlaying = playing;
        boolean edge = ((startLimit != prevStart && startLimit > 0) || (finishLimit != prevFinish && finishLimit > 0));
        if (startLimit == 0 && finishLimit == 0) {
            playing = false;
            if (startLimit != prevStart || finishLimit != prevFinish)
                preview = false;
            isChanged = true;
        }
        playing |= preview || edge;
        boolean startedPlaying = !preview && ((!prevPlaying && playing) || edge);
        prevStart = startLimit;
        prevFinish = finishLimit;
        if (playing) {
            switch (getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE)) {
                case PLAY_ONCE -> {
                    if (startedPlaying) {
                        head = sl;
                    }
                    if (output == 0 && head >= 0 && head < ticks.size())
                        output = ticks.get(head);
                    if (!startedPlaying) {
                        ++head;
                        if (head >= fl || head >= ticks.size()) {
                            head = fl;
                            playing = false;
                            preview = false;
                        }
                    }
                }
                case PLAY_LOOP -> {
                    if (startedPlaying) {
                        head = sl;
                    }
                    if (output == 0 && head >= 0 && head < ticks.size())
                        output = ticks.get(head);
                    ++head;
                    if (head >= fl || head >= ticks.size())
                        head = sl;
                }
                case RECORD -> {
                    if (startLimit == 0 && finishLimit == 0 && !preview) {
                        playing = false;
                    } else {
                        if (startedPlaying && startLimit <= limits.size() + 1) {
                            head = sl;
                        }
                        if (head >= 0 && head < ticks.size()) {
                            if (!preview)
                                setTick(head, input);
                            output = ticks.get(head);
                        }
                        if (!startedPlaying) {
                            ++head;
                            if (head >= ticks.size() || (preview && head >= length)) {
                                --head;
                                playing = false;
                                preview = false;
                            }
                            if (head >= length) {
                                length = head;
                            }
                        }
                    }
                }
            }
            setChanged();
        }
        if (isChanged) {
            if (level instanceof ServerLevel serverLevel) {
                if (getBlockState().getValue(BlockStateProperties.POWER) != output)
                    serverLevel.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWER, output));
                NetworkManager.toClients(Arss.MOD_ID, new SyncManually(worldPosition, saveSyncData(new CompoundTag()))); //should sync only to interested clients
            }
            isChanged = false;
        }
        return output;
    }

    protected ArrayList<Byte> toRLE() {
        ArrayList<Byte> tmp = new ArrayList<>();
        if (!ticks.isEmpty()) {
            int tl = 0;
            for (int i = ticks.size() - 1; i >= 0; --i)
                if (ticks.get(i) != 0) {
                    tl = i + 1;
                    break;
                }
            int power = ticks.get(0);
            int prev = 0;
            for (int i = 1; i < tl; ++i)
                if (ticks.get(i) != power) {
                    tmp.add((byte) ((power & 0xF) | (((i - prev - 1) << 4) & 0xF0)));
                    prev = i;
                    power = ticks.get(i);
                } else if (i - prev == 16) {
                    tmp.add((byte) ((power & 0xF) | 0xF0));
                    prev = i;
                }
            int length = tl - prev;
            if (length > 0)
                tmp.add((byte) ((power & 0xF) | (((length - 1) << 4) & 0xF0)));
        }
        return tmp;
    }

    protected void fromRLE(byte[] rle) {
        int h = 0;
        for (byte b : rle) {
            int power = b & 0xF;
            int length = ((b & 0xF0) >> 4) + 1;
            for (int l = 0; l < length; ++l)
                ticks.set(h++, power);
        }
        for (; h < MAXIMUM_TICK_COUNT; ++h)
            ticks.set(h, 0);
    }

    public SequencerBlockEntity(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    @RegisterMsg
    public record SyncManually(BlockPos pos, CompoundTag serialized) implements IRecordMsg {
        @Override
        public void clientWork(Player player) {
            if (player.level().getBlockEntity(pos) instanceof SequencerBlockEntity be)
                be.loadSyncData(serialized);
        }
    }

    public CompoundTag memoryItemData(@Nonnull CompoundTag tag) {
        tag.putInt("Head", head);
        tag.putBoolean("Preview", preview);
        tag.putByteArray("Ticks", toRLE());
        tag.putInt("Length", length);
        tag.putIntArray("Limits", limits);
        return tag;
    }

    public boolean isDefault() {
        return limits.isEmpty() && ticks.stream().noneMatch(i->i != 0);
    }

    public CompoundTag saveSyncData(@Nonnull CompoundTag tag) {
        tag.putInt("Start", startLimit);
        tag.putInt("Finish", finishLimit);
        tag.putBoolean("Playing", playing);
        CompoundTag b = new CompoundTag();
        for (Map.Entry<String, String> p : mappings.entrySet())
            b.putString(p.getKey(), p.getValue());
        tag.put("Bindings", b);
        tag.putInt("PrevStart", prevStart);
        tag.putInt("PrevFinish", prevFinish);
        return memoryItemData(tag);
    }

    public void loadMemoryItem(@Nonnull CompoundTag tag) {
        head = tag.getInt("Head");
        preview = tag.getBoolean("Preview");
        fromRLE(tag.getByteArray("Ticks"));
        length = tag.getInt("Length");
        limits.clear();
        int[] l = tag.getIntArray("Limits");
        int i = 0;
        for (; i < 14 && i < l.length; ++i)
            limits.add(l[i]);
        if (head >= length)
            head = length > 0 ? length - 1 : 0;
    }

    public void loadSyncData(@Nonnull CompoundTag tag) {
        loadMemoryItem(tag);
        startLimit = tag.getInt("Start");
        finishLimit = tag.getInt("Finish");
        playing = tag.getBoolean("Playing");
        CompoundTag b = tag.getCompound("Bindings");
        if (b.contains("record", Tag.TAG_STRING))
            mappings.put("record", b.getString("record"));
        else
            mappings.put("record", "back");
        if (b.contains("start", Tag.TAG_STRING))
            mappings.put("start", b.getString("start"));
        else
            mappings.put("start", "left");
        if (b.contains("finish", Tag.TAG_STRING))
            mappings.put("finish", b.getString("finish"));
        else
            mappings.put("finish", "right");
        prevStart = tag.getInt("PrevStart");
        prevFinish = tag.getInt("PrevFinish");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        saveSyncData(tag);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        loadSyncData(tag);
    }

    protected CompoundTag dropWithMode() {
        CompoundTag out = saveSyncData(new CompoundTag());
        out.putInt("Mode", getBlockState().getValue(ArssBlockStateProperties.SEQUENCER_MODE).ordinal());
        return out;
    }

    @Override
    public List<ItemStack> getDrops(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        boolean empty = isDefault();
        if (empty && player.isCreative())
            return Collections.emptyList();
        ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
        if (!isDefault())
            stack.getOrCreateTag().put("BlockEntityTag", dropWithMode());
        return Collections.singletonList(stack);
    }
}
