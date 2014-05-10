package me.xTDKx.NetworkTokens;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {

    private static FileConfiguration messages;
    private static File messagesFile;

    public static void setupMessages(File file) {
        reloadMessages(file);
    }

    public static void reloadMessages(File file) {
        messagesFile = new File(file, "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static void saveMessages() {
        try {
            getMessages().save(getMessagesFile());
        } catch (Exception e) {
            Bukkit.getLogger().severe("Couldn't save messages, because: " + e.getMessage());
        }
    }

    public static FileConfiguration getMessages() {
        return messages;
    }

    public static File getMessagesFile() {
        return messagesFile;
    }

    public static void addID(String message, String id) {
        if (!getMessages().contains(id)) {
            getMessages().set(id, message);
            saveMessages();
        }
    }

    public static String getMessageFromID(String id) {
        String message;
        if(getMessages().getString(id) !=null) {
            message = getMessages().getString(id);
        }else{
            message = "Hi";
        }
        return message;



    }
    
}
