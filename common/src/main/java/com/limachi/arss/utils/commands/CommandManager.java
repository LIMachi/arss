package com.limachi.arss.utils.commands;

import com.limachi.arss.utils.ClassExtractor;
import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.CmdArg;
import com.limachi.arss.utils.annotations.RegisterCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.annotation.*;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

public class CommandManager {
    public static LiteralArgumentBuilder<CommandSourceStack> cmd(String cmd, Command<CommandSourceStack> run, HashMap<String, ArgumentType<?>> mappedTypes) {
        String[] c = cmd.startsWith("/") ? cmd.substring(1).split(" ") : cmd.split(" ");
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(c[0]);
        ArrayList<ArgumentBuilder<CommandSourceStack, ?>> rev = new ArrayList<>(c.length - 1);
        for (int i = 1; i < c.length; ++i) {
            if (c[i].startsWith("<") && c[i].endsWith(">")) {
                String label = c[i].substring(1, c[i].length() - 1);
                rev.add(Commands.argument(label, mappedTypes.get(label)));
            } else
                rev.add(Commands.literal(c[i]));
        }
        rev.get(c.length - 2).executes(run);
        for (int i = c.length - 2; i > 0; --i)
            rev.get(i - 1).then(rev.get(i));
        root.then(rev.get(0));
        return root;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> cmd(String cmd, Command<CommandSourceStack> run, ArgumentType<?> ... types) {
        String[] c = cmd.startsWith("/") ? cmd.substring(1).split(" ") : cmd.split(" ");
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(c[0]);
        int arg = 0;
        ArrayList<ArgumentBuilder<CommandSourceStack, ?>> rev = new ArrayList<>(c.length - 1);
        for (int i = 1; i < c.length; ++i) {
            if (c[i].startsWith("<") && c[i].endsWith(">")) {
                String label = c[i].substring(1, c[i].length() - 1);
                rev.add(Commands.argument(label, types[arg++]));
            } else
                rev.add(Commands.literal(c[i]));
        }
        rev.get(c.length - 2).executes(run);
        for (int i = c.length - 2; i > 0; --i)
            rev.get(i - 1).then(rev.get(i));
        root.then(rev.get(0));
        return root;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RegisterCommands {
        RegisterCommand[] value();
    }

    @FunctionalInterface
    public interface CommandGetter<R> {
        R apply(CommandContext<CommandSourceStack> ctx, String label) throws CommandSyntaxException;
    }

    private static final HashMap<Class<?>, CommandGetter<?>> GETTER_OVERRIDE = Util.make(new HashMap<>(), m->{
        m.put(BlockPos.class, BlockPosArgument::getBlockPos);
        m.put(Entity.class, EntityArgument::getEntity);
        m.put(ServerPlayer.class, EntityArgument::getPlayer);
        m.put(Player.class, EntityArgument::getPlayer);
        m.put(BlockInput.class, BlockStateArgument::getBlock);
        m.put(BlockState.class, (ctx, label)->BlockStateArgument.getBlock(ctx, label).getState());
        m.put(ColumnPos.class, ColumnPosArgument::getColumnPos);
        m.put(ChunkPos.class, (ctx, label)->ColumnPosArgument.getColumnPos(ctx, label).toChunkPos());
        m.put(ServerLevel.class, DimensionArgument::getDimension);
        m.put(Level.class, DimensionArgument::getDimension);
        m.put(ItemInput.class, ItemArgument::getItem);
        m.put(ItemStack.class, (ctx, label)->ItemArgument.getItem(ctx, label).createItemStack(1, false));
    });

    private static final HashMap<Class<?>, BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>>> ARG_TYPES = Util.make(new HashMap<>(), m -> {
        BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>> intArg = (b, a)->{
            if (a.min().isBlank() && a.max().isBlank())
                return IntegerArgumentType.integer();
            if (a.max().isBlank())
                return IntegerArgumentType.integer(Integer.parseInt(a.min()));
            if (a.min().isBlank())
                return IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.parseInt(a.max()));
            return IntegerArgumentType.integer(Integer.parseInt(a.min()), Integer.parseInt(a.max()));
        };
        BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>> longArg = (b, a)->{
            if (a.min().isBlank() && a.max().isBlank())
                return LongArgumentType.longArg();
            if (a.max().isBlank())
                return LongArgumentType.longArg(Long.parseLong(a.min()));
            if (a.min().isBlank())
                return LongArgumentType.longArg(Long.MIN_VALUE, Long.parseLong(a.max()));
            return LongArgumentType.longArg(Long.parseLong(a.min()), Long.parseLong(a.max()));
        };
        BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>> floatArg = (b, a)->{
            if (a.min().isBlank() && a.max().isBlank())
                return FloatArgumentType.floatArg();
            if (a.max().isBlank())
                return FloatArgumentType.floatArg(Float.parseFloat(a.min()));
            if (a.min().isBlank())
                return FloatArgumentType.floatArg(Float.MIN_VALUE, Float.parseFloat(a.max()));
            return FloatArgumentType.floatArg(Float.parseFloat(a.min()), Float.parseFloat(a.max()));
        };
        BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>> doubleArg = (b, a)->{
            if (a.min().isBlank() && a.max().isBlank())
                return DoubleArgumentType.doubleArg();
            if (a.max().isBlank())
                return DoubleArgumentType.doubleArg(Double.parseDouble(a.min()));
            if (a.min().isBlank())
                return DoubleArgumentType.doubleArg(Double.MIN_VALUE, Double.parseDouble(a.max()));
            return DoubleArgumentType.doubleArg(Double.parseDouble(a.min()), Double.parseDouble(a.max()));
        };
        m.put(String.class, (b, a)->a.matcher() != null ? switch (a.matcher()) {
            case SINGLE_WORD -> StringArgumentType.word();
            case QUOTABLE_PHRASE -> StringArgumentType.string();
            case GREEDY_PHRASE -> StringArgumentType.greedyString();
        } : StringArgumentType.string());
        m.put(boolean.class, (b, a)->BoolArgumentType.bool());
        m.put(Boolean.class, (b, a)->BoolArgumentType.bool());
        m.put(int.class, intArg);
        m.put(Integer.class, intArg);
        m.put(long.class, longArg);
        m.put(Long.class, longArg);
        m.put(float.class, floatArg);
        m.put(Float.class, floatArg);
        m.put(double.class, doubleArg);
        m.put(Double.class, doubleArg);
        m.put(BlockPos.class, (b, a)->BlockPosArgument.blockPos());
        m.put(Entity.class, (b, a)->EntityArgument.entity());
        m.put(ServerPlayer.class, (b, a)->EntityArgument.player());
        m.put(Player.class, (b, a)->EntityArgument.player());
        m.put(BlockPredicateArgument.Result.class, (b, a)->BlockPredicateArgument.blockPredicate(b));
        m.put(BlockInput.class, (b, a)->BlockStateArgument.block(b));
        m.put(BlockState.class, (b, a)->BlockStateArgument.block(b));
        m.put(ColumnPos.class, (b, a)->ColumnPosArgument.columnPos());
        m.put(ChunkPos.class, (b, a)->ColumnPosArgument.columnPos());
        m.put(ServerLevel.class, (b, a)-> DimensionArgument.dimension());
        m.put(Level.class, (b, a)-> DimensionArgument.dimension());
        m.put(ItemInput.class, (b, a)-> ItemArgument.item(b));
        m.put(ItemStack.class, (b, a)->ItemArgument.item(b));
    });

    public static void registerArg(Class<?> forClass, BiFunction<CommandBuildContext, CmdArg, ArgumentType<?>> argProvider) {
        ARG_TYPES.put(forClass, argProvider);
    }

    public static void registerGetter(Class<?> forClass, CommandGetter<?> getter) {
        GETTER_OVERRIDE.put(forClass, getter);
    }

    private static <T> Optional<LiteralArgumentBuilder<CommandSourceStack>> cmdAnnotation(CommandBuildContext builder, ClassExtractor.MethodAccess<?> m, RegisterCommand a) {
        Parameter[] parameters = m.parameters();
        if (parameters.length == 0) {
            //error: missing ctx as first arg
            ModBase.logger.error("missing first arg (ctx): " + m.name() + " # " + a.value());
            return Optional.empty();
        }
        ArgumentType<?>[] at = new ArgumentType[parameters.length - 1];
        String[] labels = new String[parameters.length - 1];
        HashMap<String, ArgumentType<?>> mapping = new HashMap<>();
        if (!m.returnType().isAssignableFrom(int.class)) {
            //error: annotation is on a method that does not return an int
            ModBase.logger.error("should return int: " + m.name() + " # " + a.value());
            return Optional.empty();
        }
        String command = a.value();
        for (int p = 1; p < parameters.length; ++p) {
            boolean found = false;
            for (Annotation pa : parameters[p].getAnnotations()) {
                if (pa instanceof CmdArg arg) {
                    labels[p - 1] = arg.value();
                    at[p - 1] = ARG_TYPES.get(parameters[p].getType()).apply(builder, arg);
                    mapping.put(labels[p - 1], at[p - 1]);
                    found = true;
                    break;
                }
            }
            if (!found) {
                //error, parameter without Arg annotation!
                ModBase.logger.error("unexpected arg without annotation: " + m.name() + " # " + a.value() + " @ " + p);
                return Optional.empty();
            }
        }
        return Optional.of(cmd(command, ctx -> {
            Object[] args = new Object[parameters.length];
            args[0] = ctx;
            for (int i = 0; i < at.length; ++i)
                try {
                    Class<?> clazz = parameters[i + 1].getType();
                    if (GETTER_OVERRIDE.containsKey(clazz))
                        args[i + 1] = GETTER_OVERRIDE.get(clazz).apply(ctx, labels[i]);
                    else
                        args[i + 1] = ctx.getArgument(labels[i], clazz);
                } catch (Exception ignored) {
                    ModBase.logger.warn("command arg exception: " + ignored);
                } //probably an optional arg
            try {
                return (int) m.invokeStatic(args);
            } catch (Exception e) {
                ModBase.logger.warn("command exception: " + e);
                return 0;
            }
        }, mapping));
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register(((dispatcher, builder, selection) -> ModBase.extractor.runOnMethods(RegisterCommand.class, (m, a)->cmdAnnotation(builder, m, a).ifPresent(dispatcher::register))));
    }
}
