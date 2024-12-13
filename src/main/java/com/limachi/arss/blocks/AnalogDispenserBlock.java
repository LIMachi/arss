package com.limachi.arss.blocks;

import com.limachi.arss.Arss;
import com.limachi.arss.items.BlockItemWithCustomRenderer;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.limachi.arss.blocks.block_state_properties.ArssBlockStateProperties.HIDE_DOT;

public class AnalogDispenserBlock extends DispenserBlock {

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;

    public static class AnalogDispenserItem extends BlockItemWithCustomRenderer {

        @RegisterItem
        public static RegistryObject<BlockItem> R_ITEM;

        public AnalogDispenserItem() { super(R_BLOCK.get(), new Item.Properties(), Blocks.DISPENSER); }
    }

    public AnalogDispenserBlock() {
        super(Properties.copy(Blocks.DISPENSER).isRedstoneConductor((s, l, p) -> false));
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false).setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HIDE_DOT);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("analog_dispenser_block", components);
    }

    @RegisterMsg
    public record ResyncDeltaMovement(int entityId, Level level, Vec3 velocity) implements IRecordMsg {
        @Override
        public void clientWork(Player player) {
            Entity entity = level.getEntity(entityId);
            if (entity != null)
                entity.setDeltaMovement(velocity);
        }
    }

    protected Vec3 projectionVelocity(@Nonnull ServerLevel level, @Nonnull BlockPos pos, Direction facing) {
        double power = level.getSignal(pos.relative(facing.getOpposite()), facing.getOpposite()) * (facing == Direction.UP || facing == Direction.DOWN ? 0.08 : 0.17);
        Vec3 out = new Vec3(facing.step());
        for (Direction test : Direction.values()) {
            if (test.getAxis() == facing.getAxis())
                continue;
            double pow = level.getSignal(pos.relative(test.getOpposite()), test);
            if (test == Direction.UP && pow > 0)
                power *= 0.90 - (pow / 20.);
            out = out.add(new Vec3(test.step()).scale(pow * 0.07));
        }
        return out.normalize().scale(power);
    }

    @Override
    protected void dispenseFrom(@Nonnull ServerLevel level, @Nonnull BlockPos pos) {
        Direction dir = level.getBlockState(pos).getValue(FACING);
        BlockPos lookPos = pos.relative(dir);
        AABB detectionArea = new AABB(lookPos.offset(-1, -1, -1), lookPos.offset(1, 1, 1));
        List<Entity> before = level.getEntities(null, detectionArea);
        super.dispenseFrom(level, pos);
        List<Entity> after = level.getEntities(null, detectionArea);
        for (Entity test : after)
            if (!before.contains(test)) {
                test.setDeltaMovement(projectionVelocity(level, pos, dir));
                NetworkManager.toClients(Arss.MOD_ID, new ResyncDeltaMovement(test.getId(), level, test.getDeltaMovement()));
            }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos from, boolean piston) {
        Direction facing = state.getValue(FACING);
        boolean backPowered = level.getSignal(pos.relative(facing.getOpposite()), facing.getOpposite()) > 0;
        boolean triggered = state.getValue(TRIGGERED);
        if (backPowered && !triggered) {
            level.scheduleTick(pos, this, 2);
            if (level instanceof ServerLevel sl)
                dispenseFrom(sl, pos);
            level.setBlock(pos, state.setValue(TRIGGERED, true), 4);
        } else if (!backPowered && triggered && !level.getBlockTicks().hasScheduledTick(pos, this))
            level.setBlock(pos, state.setValue(TRIGGERED, false), 4);
    }

    @Override
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource rng) {}

    @Override
    @Nonnull
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item held = player.getItemInHand(hand).getItem();
        if ((held == Items.REDSTONE_TORCH || held == AnalogRedstoneTorchBlock.AnalogRedstoneTorchItem.R_ITEM.get()) && !KeyMapController.SNEAK.getState(player)) {
            level.setBlock(pos, state.setValue(HIDE_DOT, !state.getValue(HIDE_DOT)), 3);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
