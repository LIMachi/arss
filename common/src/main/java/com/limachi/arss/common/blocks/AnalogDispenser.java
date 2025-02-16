package com.limachi.arss.common.blocks;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.ArssBlockBehaviors;
import com.limachi.arss.utils.annotations.RegisterBlock;
import com.limachi.arss.utils.annotations.RegisterBlockItem;
import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.client.annotations.FabricLayer;
import com.limachi.arss.utils.network.IS2CMsg;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.limachi.arss.common.ArssBlockStateProperties.HIDE_DOT;

public class AnalogDispenser extends DispenserBlock {

    @FabricLayer("cutout")
    @RegisterBlock
    public static RegistrySupplier<Block> R_BLOCK;

    @RegisterBlockItem
    public static RegistrySupplier<BlockItem> R_ITEM;

    public AnalogDispenser() {
        super(Properties.ofFullCopy(Blocks.DISPENSER).isRedstoneConductor((s, l, p) -> false));
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false).setValue(HIDE_DOT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HIDE_DOT);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("analog_dispenser_block", components);
    }

    @RegisterMsg
    public record ResyncDeltaMovement(int entityId, Vec3 velocity) implements IS2CMsg<ResyncDeltaMovement> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            Entity entity = ctx.getPlayer().level().getEntity(entityId);
            if (entity != null)
                entity.setDeltaMovement(velocity);
        }
    }

    protected Vec3 projectionVelocity(ServerLevel level, BlockPos pos, Direction facing) {
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
    protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
        Direction dir = level.getBlockState(pos).getValue(FACING);
        BlockPos lookPos = pos.relative(dir);
        AABB detectionArea = new AABB(lookPos.offset(-1, -1, -1).getCenter(), lookPos.offset(1, 1, 1).getCenter());
        List<Entity> before = level.getEntities(null, detectionArea);
        super.dispenseFrom(level, state, pos);
        List<Entity> after = level.getEntities(null, detectionArea);
        for (Entity test : after)
            if (!before.contains(test)) {
                test.setDeltaMovement(projectionVelocity(level, pos, dir));
                new ResyncDeltaMovement(test.getId(), test.getDeltaMovement()).sendToClients(level, pos);
            }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean piston) {
        Direction facing = state.getValue(FACING);
        boolean backPowered = level.getSignal(pos.relative(facing.getOpposite()), facing.getOpposite()) > 0;
        boolean triggered = state.getValue(TRIGGERED);
        if (backPowered && !triggered) {
            level.scheduleTick(pos, this, 2);
            if (level instanceof ServerLevel sl)
                dispenseFrom(sl, state, pos);
            level.setBlock(pos, state.setValue(TRIGGERED, true), 4);
        } else if (!backPowered && triggered && !level.getBlockTicks().hasScheduledTick(pos, this))
            level.setBlock(pos, state.setValue(TRIGGERED, false), 4);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rng) {}

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ArssBlockBehaviors.useItemOn(stack, state, level, pos, player, hand, hit, ArssBlockBehaviors::useItemOnRedstoneDotBlock, super::useItemOn);
    }
}
