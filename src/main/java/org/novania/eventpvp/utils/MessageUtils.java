// ===== MessageUtils.java =====
package org.novania.eventpvp.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtils {
    
    public static String colorize(String message) {
        return message.replace("&", "§");
    }
    
    public static List<String> colorize(List<String> messages) {
        return messages.stream()
                .map(MessageUtils::colorize)
                .toList();
    }
    
    public static String replaceVariables(String message, String... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                result = result.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return result;
    }
    
    public static void sendMessage(Player player, String message) {
        if (message != null && !message.isEmpty()) {
            player.sendMessage(colorize(message));
        }
    }
    
    public static void sendMessages(Player player, String... messages) {
        for (String message : messages) {
            sendMessage(player, message);
        }
    }
    
    public static void sendMessages(Player player, List<String> messages) {
        for (String message : messages) {
            sendMessage(player, message);
        }
    }
    
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(colorize(message));
    }
    
    public static void broadcast(String message, String... replacements) {
        broadcast(replaceVariables(message, replacements));
    }
    
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    public static String formatTimeAgo(long timestamp) {
        long timeAgo = System.currentTimeMillis() - timestamp;
        
        if (timeAgo < 60000) { // moins d'1 minute
            return (timeAgo / 1000) + "s";
        } else if (timeAgo < 3600000) { // moins d'1 heure
            return (timeAgo / 60000) + "min";
        } else if (timeAgo < 86400000) { // moins d'1 jour
            return (timeAgo / 3600000) + "h";
        } else {
            return (timeAgo / 86400000) + "j";
        }
    }
    
    public static String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    public static String formatPercentage(double value, double max) {
        if (max == 0) return "0%";
        double percentage = (value / max) * 100;
        return String.format("%.1f%%", percentage);
    }
    
    public static String getProgressBar(int current, int max, int length, char symbol, String completeColor, String incompleteColor) {
        if (max == 0) {
            return incompleteColor + String.valueOf(symbol).repeat(length);
        }
        
        int completed = (int) ((double) current / max * length);
        StringBuilder bar = new StringBuilder();
        
        bar.append(completeColor);
        bar.append(String.valueOf(symbol).repeat(Math.max(0, completed)));
        
        bar.append(incompleteColor);
        bar.append(String.valueOf(symbol).repeat(Math.max(0, length - completed)));
        
        return bar.toString();
    }
    
    public static String centerMessage(String message, int lineLength) {
        if (message.length() >= lineLength) {
            return message;
        }
        
        int padding = (lineLength - message.length()) / 2;
        return " ".repeat(padding) + message;
    }
    
    public static String createSeparator(int length, char character) {
        return colorize("&7" + String.valueOf(character).repeat(length));
    }
    
    public static String createTitle(String title) {
        int titleLength = title.length();
        String separator = createSeparator(titleLength + 4, '=');
        return separator + "\n  " + colorize(title) + "\n" + separator;
    }
}