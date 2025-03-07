package com.limachi.arss.utils.annotations;

import com.limachi.arss.utils.commands.CommandManager;

import java.lang.annotation.*;

@Repeatable(CommandManager.RegisterCommands.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterCommand {
    /**
     * command pattern, ex:<br>
     * {@code @RegisterCommand("root cmd <int>")}<br>
     * {@code public static int myCommand(CommandContext<CommandSourceStack> ctx, @CmdArg("int") Integer test){ return test; }}<br>
     * <br>
     * would result in the command: `/root cmd 1` returning 1, `/root cmd 2` returning 2 and so on
     */
    String value();
}
