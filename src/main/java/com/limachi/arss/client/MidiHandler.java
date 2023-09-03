package com.limachi.arss.client;

import com.limachi.arss.Arss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber(modid = Arss.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MidiHandler {

    public interface ICatchMIDI {
        boolean keyState(int channel, int key, boolean state);
    }

    public static final int MSG_TYPE_MASK = 0b11110000;
    public static final int CHANNEL_MASK =  0b00001111;
    public static final int NOTE_OFF =      0b10000000;
    public static final int NOTE_ON =       0b10010000;
    public static final int NOTE_OFFSET =   48; //the default for most keyboard midi is to have the first note at 48, so we can go down up to 4 octaves

    @Nullable
    private static MidiDevice inputDevice = null;

    private static final TestReceiver RECEIVER_INSTANCE = new TestReceiver();

    private static final Path CONFIG_PATH = new File(FMLPaths.CONFIGDIR.get().toFile(), Arss.MOD_ID + "_midi_device").toPath();

    static {
        try {
            boolean ignore = CONFIG_PATH.toFile().createNewFile();
            bindDevice(Files.readString(CONFIG_PATH));
        } catch (IOException ignore) {}
    }

    private static final short[] states = new short[128];

    public static ICatchMIDI KEY_CATCHER = null;

    public static boolean keyState(int channel, int key) {
        if (key >= 128 || channel >= 16) return false;
        return (states[key] & (1 << channel)) != 0;
    }

    public static void setKeyState(int channel, int key, boolean state) {
        if (KEY_CATCHER != null && KEY_CATCHER.keyState(channel, key, state))
            return;
        if (state)
            states[key] |= (short)(1 << channel);
        else
            states[key] &= (short)~(1 << channel);
    }

    /**
     * list of all valid midi inputs (store the result, do not call every frame)
     */
    public static List<String> getDevices() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        ArrayList<String> out = new ArrayList<>();
        try {
            for (MidiDevice.Info info : infos) {
                if (MidiSystem.getMidiDevice(info).getMaxTransmitters() != 0)
                    out.add(info.getName());
            }
        } catch (MidiUnavailableException ignore) {}
        return out;
    }

    public static String currentDevice() {
        if (inputDevice == null || !inputDevice.isOpen()) return "";
        return inputDevice.getDeviceInfo().getName();
    }

    /**
     * try to bind a device by (partial) name or info
     */
    public static void bindDevice(String device) {
        if (inputDevice != null && inputDevice.isOpen())
            inputDevice.close();
        inputDevice = null;
        if (device == null || device.isBlank()) return;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        try {
            for (MidiDevice.Info info : infos) {
                if (MidiSystem.getMidiDevice(info).getMaxTransmitters() != 0)
                    if (info.getName().contains(device)) {
                        inputDevice = MidiSystem.getMidiDevice(info);
                        inputDevice.open();
                        inputDevice.getTransmitter().setReceiver(RECEIVER_INSTANCE);
                        break;
                    }
            }
        } catch (MidiUnavailableException ignore) {
            if (inputDevice != null && inputDevice.isOpen())
                inputDevice.close();
            inputDevice = null;
        }
        try {
            Files.write(CONFIG_PATH, currentDevice().getBytes());
        } catch (IOException ignore) {}
    }

    public static class TestReceiver implements Receiver {

        @Override
        public void send(MidiMessage midiMessage, long l) {
            byte[] msg = midiMessage.getMessage();
            if (msg.length == 3) {
                int type = msg[0] & MSG_TYPE_MASK;
                int channel = msg[0] & CHANNEL_MASK;
                if (type == NOTE_ON)
                    setKeyState(channel, msg[1], true);
                if (type == NOTE_OFF)
                    setKeyState(channel, msg[1], false);
            }
        }

        @Override
        public void close() {
            for (int i = 0; i < 128; ++i)
                states[i] = 0;
        }
    }
}
