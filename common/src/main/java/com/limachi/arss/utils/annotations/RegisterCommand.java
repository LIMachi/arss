package com.limachi.arss.utils.annotations;

import com.limachi.arss.utils.commands.CommandManager;

import java.lang.annotation.*;

@Repeatable(CommandManager.RegisterCommands.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterCommand {
    String value();
}
