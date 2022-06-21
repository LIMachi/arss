package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.ArssBlockStateProperties;
import com.limachi.arss.utils.StaticInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.system.NonnullDefault;

import static com.limachi.arss.Registries.BLOCK_REGISTER;
import static com.limachi.arss.Registries.ITEM_REGISTER;

@SuppressWarnings("unused")
@StaticInitializer.Static
@NonnullDefault
public class AnalogNoteBlock extends NoteBlock {

    public static final Properties PROPS = Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.8F);
    public static final RegistryObject<Block> R_BLOCK = BLOCK_REGISTER.register("analog_note_block", AnalogNoteBlock::new);
    public static final RegistryObject<Item> R_ITEM = ITEM_REGISTER.register("analog_note_block", ()->new BlockItem(R_BLOCK.get(), new Item.Properties().tab(Arss.ITEM_GROUP)));

    public static final BooleanProperty HIGH = ArssBlockStateProperties.HIGH;

    public AnalogNoteBlock() {
        super(PROPS);
        registerDefaultState(stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, 0).setValue(POWERED, false).setValue(HIGH, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE, HIGH);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_55045_, boolean p_55046_) {
        int power = level.getBestNeighborSignal(pos);
        int _new = getNote(state, level, pos);
        if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
            _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
            if (_new == -1) return;
        }
        if (_new != state.getValue(NOTE)) {
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(POWERED, power > 0), 3);
            if (power > 0)
                playNote(level, pos);
        }
    }

    private void playNote(Level level, BlockPos pos) {
        if (level.getBlockState(pos.above()).isAir())
            level.blockEvent(pos, this, 0, 0);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        else {
            int power = level.getBestNeighborSignal(pos);
            int _new = getNote(state, level, pos);
            if (!((_new == -1 && power == 0) || _new == state.getValue(NOTE))) {
                _new = net.minecraftforge.common.ForgeHooks.onNoteChange(level, pos, state, state.getValue(NOTE), _new);
                if (_new == -1) return InteractionResult.FAIL;
            }
            level.setBlock(pos, state.setValue(NOTE, _new != -1 ? _new : state.getValue(NOTE)).setValue(HIGH, !state.getValue(HIGH)), 3);
            playNote(level, pos);
            player.awardStat(Stats.TUNE_NOTEBLOCK);
            return InteractionResult.CONSUME;
        }
    }

    protected int getNote(BlockState state, Level level, BlockPos pos) {
        int power = level.getBestNeighborSignal(pos);
        return power > 0 ? power + (state.getValue(HIGH) ? 9 : -1) : -1;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {}

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int p_55026_, int p_55027_) {
        int note = getNote(state, level, pos);
        if (note != -1) {
            net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(level, pos, state, note, state.getValue(INSTRUMENT));
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) return false;
            float f = (float)Math.pow(2.f, (double)(e.getVanillaNoteId() - 12) / 12.0D);
            level.playSound(null, pos, e.getInstrument().getSoundEvent(), SoundSource.RECORDS, 3.0F, f);
            level.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)e.getVanillaNoteId() / 24.0D, 0.0D, 0.0D);
            return true;
        }
        return false;
    }
}
