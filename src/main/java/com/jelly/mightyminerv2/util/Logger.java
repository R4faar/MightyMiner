package com.jelly.mightyminerv2.util;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class Logger {

    protected static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<String, String> lastMessages = new HashMap<>();

    public static void addMessage(String text) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            System.out.println("MightyMiner " + StringUtils.stripControlCodes(text));
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(text));
        }
    }

    public static void sendMessage(final String message) {
        addMessage(formatPrefix("§bMighty Miner", message));
    }

    public static void sendWarning(final String message) {
        addMessage("§c§l[WARNING] §8» §e" + message);
    }

    public static void sendError(final String message) {
        addMessage("§l§4§kZ§r§l§4[Mighty Miner]§kH§r §8» §c" + message);
    }

    public static void sendNote(final String message) {
        sendMessage(message);
    }

    public static void sendLog(final String message) {
        if (isDuplicate("debug", message)) return;

        if (MightyMinerConfig.debugMode && mc.thePlayer != null) {
            addMessage("§l§2[Mighty Miner] §8» §7" + message);
        } else {
            System.out.println("[Mighty Miner] " + message);
        }
    }

    public static void sendNotification(String title, String message, Long duration) {
        if (isDuplicate("notification", message)) return;
        Notifications.INSTANCE.send(title, message, duration);
    }

    private static boolean isDuplicate(String type, String message) {
        if (lastMessages.containsKey(type) && lastMessages.get(type).equals(message)) {
            return true;
        }
        lastMessages.put(type, message);
        return false;
    }

    private static String formatPrefix(String prefix, String message) {
        return EnumChatFormatting.RED + "[" + EnumChatFormatting.BLUE + prefix + EnumChatFormatting.RED + "] §8» §e" + message;
    }

    public abstract String getName();

    protected void log(String message) {
        sendLog(formatMessage(message));
    }

    protected void send(String message) {
        sendMessage(formatMessage(message));
    }

    protected void error(String message) {
        sendError(formatMessage(message));
    }

    protected void warn(String message) {
        sendWarning(formatMessage(message));
    }

    protected void note(String message) {
        sendNote(formatMessage(message));
    }

    protected String formatMessage(String message) {
        return "[" + getName() + "] " + message;
    }
}
