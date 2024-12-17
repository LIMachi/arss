package com.limachi.arss.utils.commands;

import com.limachi.arss.utils.annotations.CmdArg;
import com.limachi.arss.utils.annotations.RegisterCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandTests {
    @RegisterCommand("lim_utils test <int>")
    @RegisterCommand("lim_utils test <int> <opt_bool>")
    @RegisterCommand("lim_utils test2 <opt_bool>")
    public static int test(CommandContext<CommandSourceStack> ctx, @CmdArg("int") Integer test, @CmdArg("opt_bool") Boolean opt) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("got int value: " + test + " and bool: " + opt), false);
        return test;
    }
}
