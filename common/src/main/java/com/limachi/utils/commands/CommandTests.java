package com.limachi.utils.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandTests {
    @CommandManager.Cmd("lim_utils test <int>")
    @CommandManager.Cmd("lim_utils test <int> <opt_bool>")
    @CommandManager.Cmd("lim_utils test2 <opt_bool>")
    public static int test(CommandContext<CommandSourceStack> ctx, @CommandManager.Arg("int") Integer test, @CommandManager.Arg("opt_bool") Boolean opt) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("got int value: " + test + " and bool: " + opt), false);
        return test;
    }
}
