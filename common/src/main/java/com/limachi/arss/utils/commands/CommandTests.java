package com.limachi.arss.utils.commands;

import com.limachi.arss.utils.ModBase;
import com.limachi.arss.utils.annotations.CmdArg;
import com.limachi.arss.utils.annotations.RegisterCommand;
import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.network.IS2CMsg;

import com.mojang.brigadier.context.CommandContext;

import dev.architectury.networking.NetworkManager;

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

    @RegisterMsg
    public record VerifyConfigs() implements IS2CMsg<VerifyConfigs> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            ctx.getPlayer().displayClientMessage(Component.literal("Client result:\n" + ModBase.configs.dump()), false);
        }
    }

    @RegisterCommand("lim_utils verify_configs")
    public static int configs(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(()->Component.literal("Server result:\n" + ModBase.configs.dump()), false);
        new VerifyConfigs().sendToClient(ctx.getSource().getPlayer());
        return 1;
    }
}
