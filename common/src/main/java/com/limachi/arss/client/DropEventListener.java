package com.limachi.arss.client;

import com.limachi.arss.Arss;
import com.limachi.arss.utils.client.annotations.StaticInitClient;

import dev.architectury.event.events.client.ClientTickEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

import java.nio.file.Path;
import java.nio.file.Paths;

@Environment(EnvType.CLIENT)
public class DropEventListener {
    private static boolean initialized = false;

    protected static void fileDrop(Path path) {
        Arss.logger.error("file drop: " + path);
    }

    protected static void setFileDropListener() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        final GLFWDropCallback chainDropCallback = GLFW.glfwSetDropCallback(window, null);
        GLFW.glfwSetDropCallback(window, chainDropCallback != null ? (windowId, count, paths)->{
            if (windowId == window) {
                for (int i = 0; i < count; ++i)
                    fileDrop(Paths.get(GLFWDropCallback.getName(paths, i)));
            }
            chainDropCallback.invoke(windowId, count, paths);
        } : (windowId, count, paths)->{
            if (windowId == window) {
                for (int i = 0; i < count; ++i)
                    fileDrop(Paths.get(GLFWDropCallback.getName(paths, i)));
            }
        });
        initialized = true;
    }

    @StaticInitClient
    public static void init() {
        //should try to find a better event
        ClientTickEvent.CLIENT_PRE.register(client->{
            if (initialized)
                return;
            setFileDropListener();
        });
    }
}