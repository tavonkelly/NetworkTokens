package me.xTDKx.NetworkTokens;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.logging.Logger;

public class Main extends JavaPlugin{
    public static Main plugin;
    Logger logger = Logger.getLogger("Minecraft");
    HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    HashMap<String, Integer> cach = new HashMap<String, Integer>();


    @Override
    public void onEnable() {
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    listeners.giveBoard(player, sql.getTokens(player));
                }
            }
        }, 40L, 100L);
        //f(sql.canConnect()) {
        try {
            sql.tableCheck();
        } catch (Exception e) {
            logger.warning("Table creation failed");
        }
    }
        /*}else{
            logger.warning("[Network Tokens] Unable to connect to database, disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }*/

    public void disablePlugin(){
        Bukkit.getPluginManager().disablePlugin(this);
    }

    TokenSQL sql = new TokenSQL(this);
    Listeners listeners = new Listeners(this);

    @Override
    public void onDisable(){

    }


    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Messages.setupMessages(getDataFolder());
    }

    public int getCachedData(Player player){
        if(cach.containsKey(player.getUniqueId().toString())){
            return cach.get(player.getUniqueId().toString());
        }else{
            return 0;
        }
    }

    /*public void setCachedData(){

    }*/

    public float getTimeLeft(String cooldown) {
        String format = cooldown;
        try {
            long l = cooldowns.get(format);
            long diff = l - System.currentTimeMillis();
            if (diff < 0)
                diff = 0;
            float left = diff / 1000f;
            return left;
        } catch (NullPointerException e) {
            return 0f;
        }
    }

    public boolean isOnCooldown(String cooldown) {
        return getTimeLeft(cooldown) != 0;
    }

    public void setCooldown(String cooldown, float time) {
        String format = cooldown;
        long l = System.currentTimeMillis() + (long) (time * 1000);
        cooldowns.put(format, l);
    }

    public void sendHelp(CommandSender sender){
        String helpHead = (ChatColor.AQUA +""+ ChatColor.STRIKETHROUGH+"----------"+ChatColor.RESET+ChatColor.AQUA+"[ " + ChatColor.DARK_AQUA + "Network Tokens " + ChatColor.AQUA + "]"+ChatColor.STRIKETHROUGH+"----------");
        sender.sendMessage(helpHead);
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt help,? " + ChatColor.GRAY + "- Display the help page.");
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt balance [player] " + ChatColor.GRAY + "- Display the balance of a player.");
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt add <amount> [player] " + ChatColor.GRAY + "- Add tokens to a players balance.");
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt remove <amount> [player] " + ChatColor.GRAY + "- Remove tokens from a players balance.");
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt set <amount> [player] " + ChatColor.GRAY + "- Set the amount of tokens a player has.");
        sender.sendMessage(ChatColor.DARK_AQUA + "/nt reload " + ChatColor.GRAY + "- Reload the config file.");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        String prefix = ChatColor.AQUA+"["+ChatColor.DARK_AQUA+"NT"+ChatColor.AQUA+"] ";

        if(commandLabel.equalsIgnoreCase("networktokens") || commandLabel.equalsIgnoreCase("nt")){

            if(args.length == 0) {
                sendHelp(sender);
            }

            if(args.length == 1){

                if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
                    sendHelp(sender);
                }

                if(args[0].equalsIgnoreCase("balance")){
                    if(sender instanceof Player){
                        Player player = (Player) sender;
                        if(!isOnCooldown(player.getUniqueId().toString())){
                           int tokens = sql.getTokens(player);
                           String tokensS = Integer.toString(tokens);
                           if(tokensS !=null && Messages.getMessageFromID("tokensBalance") !=null) {
                               player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("tokensBalance")).replace("%Tokens%", tokensS));
                               setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                               cach.put(player.getUniqueId().toString(), tokens);
                           }


                        }else{
                            int tokens = getCachedData(player);
                            String tokensS = Integer.toString(tokens);
                            player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("tokensBalance")).replace("%Tokens%", tokensS));
                        }
                    }else{
                        sender.sendMessage(prefix+"Incorrect usage. /nt balance <player>");
                    }
                }

                if(args[0].equalsIgnoreCase("add")){
                    sender.sendMessage(prefix+"Incorrect usage. /nt add <amount> [player]");
                }

                if(args[0].equalsIgnoreCase("remove")){
                    sender.sendMessage(prefix+"Incorrect usage. /nt remove <amount> [player]");
                }

                if(args[0].equalsIgnoreCase("set")){
                    sender.sendMessage(prefix+"Incorrect usage. /nt set <amount> [player]");
                }

                if(args[0].equalsIgnoreCase("reload")){
                    reloadConfig();
                    sender.sendMessage(prefix+"Configuration reloaded.");
                }


            }

            if(args.length == 2){

                if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
                    sendHelp(sender);
                }

                if(args[0].equalsIgnoreCase("balance")){
                    if(Bukkit.getPlayer(args[1]) !=null){
                        Player targetPlayer = Bukkit.getPlayer(args[1]);
                        if(!isOnCooldown(targetPlayer.getUniqueId().toString())){
                            int tokens = sql.getTokens(targetPlayer);
                            String tokensS = Integer.toString(tokens);
                            sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("tokensBalanceOther")).replace("%Tokens%", tokensS).replace("%Player%", targetPlayer.getDisplayName()));
                            setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                            cach.put(targetPlayer.getUniqueId().toString(), tokens);
                        }else{
                            int tokens = getCachedData(targetPlayer);
                            String tokensS = Integer.toString(tokens);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+Messages.getMessageFromID("tokensBalanceOther")).replace("%Tokens%", tokensS).replace("%Player%", targetPlayer.getDisplayName()));
                        }
                    }else{
                        sender.sendMessage(prefix+"Player not found.");
                    }
                }

                if(args[0].equalsIgnoreCase("add")){
                    if(sender instanceof Player){
                        final Player player = (Player) sender;
                        try {
                           final int i = Integer.parseInt(args[1]);
                           if(!isOnCooldown(player.getUniqueId().toString())){
                               int beforeTokens = sql.getTokens(player);
                               int afterTokens = beforeTokens + i;
                               sql.addTokens(player, i);
                               player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("addedTokens")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%AddedTokens%", Integer.toString(i)));
                               setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                               cach.put(player.getUniqueId().toString(), afterTokens);
                           }else{
                               int beforeTokens = getCachedData(player);
                               int afterTokens = beforeTokens + i;
                               float timeLeft = getTimeLeft(player.getUniqueId().toString());
                               long timeLeftLong = (long) timeLeft;
                               player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&',Messages.getMessageFromID("addedTokens")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%AddedTokens%", Integer.toString(i)));
                               cach.put(player.getUniqueId().toString(), afterTokens);
                               Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){

                                   @Override
                                    public void run(){
                                        sql.addTokens(player, i);
                                        setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                   }

                               },timeLeftLong);

                           }
                        } catch(NumberFormatException e) {
                            player.sendMessage(prefix+"Incorrect usage. That's not a number!");
                        }

                    }else{
                        sender.sendMessage(prefix+"You're the console, so do /nt add <amount> [player]");
                    }
                }

                if(args[0].equalsIgnoreCase("remove")){
                    if(sender instanceof Player){
                        final Player player = (Player) sender;
                        try {
                           int i = Integer.parseInt(args[1]);
                            if (!isOnCooldown(player.getUniqueId().toString())) {
                                int beforeTokens = sql.getTokens(player);
                                if (i > beforeTokens) {
                                    sql.setTokens(player, 0);
                                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokens")).replace("%Tokens%", Integer.toString(0)).replace("%RemovedTokens%", Integer.toString(i)));
                                    cach.put(player.getUniqueId().toString(), 0);
                                    setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                } else {
                                    int afterTokens = beforeTokens - i;
                                    sql.removeTokens(player, i);
                                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokens")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%RemovedTokens%", Integer.toString(i)));
                                    cach.put(player.getUniqueId().toString(), afterTokens);
                                    setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));

                                }
                            }else{
                                int beforeTokens = getCachedData(player);
                                float timeLeft = getTimeLeft(player.getUniqueId().toString());
                                long timeLeftLong = (long) timeLeft;
                                if(i > beforeTokens){
                                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokens")).replace("%Tokens%", Integer.toString(0)).replace("%RemovedTokens%", Integer.toString(i)));
                                    cach.put(player.getUniqueId().toString(), 0);
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                        @Override
                                        public void run() {
                                            sql.setTokens(player, 0);
                                            setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                        }
                                    }, timeLeftLong);
                                }else{
                                    int afterTokens = beforeTokens - i;
                                    player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokens")).replace("%Tokens", Integer.toString(afterTokens)).replace("%RemovedTokens%", Integer.toString(i)));
                                    cach.put(player.getUniqueId().toString(), afterTokens);
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                        @Override
                                        public void run() {
                                            sql.removeTokens(player, 0);
                                            setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                        }
                                    }, timeLeftLong);
                                }
                            }

                        }catch (NumberFormatException e){
                            player.sendMessage(prefix+"Incorrect usage. That's not a number!");
                        }
                    }else{
                        sender.sendMessage(prefix+"You're the console, so do /nt remove <amount> [player]");
                    }
                }

                if(args[0].equalsIgnoreCase("set")){
                    if(sender instanceof Player){
                        final Player player = (Player) sender;
                        try{
                            final int i = Integer.parseInt(args[1]);
                            if(!isOnCooldown(player.getUniqueId().toString())) {
                                sql.setTokens(player, i);
                                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("setTokens")).replace("%Tokens%", Integer.toString(i)));
                                cach.put(player.getUniqueId().toString(), i);
                                setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                            }else{
                                float timeLeft = getTimeLeft(player.getUniqueId().toString());
                                long timeLeftLong = (long) timeLeft;
                                player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("setTokens")).replace("%Tokens%", Integer.toString(i)));
                                cach.put(player.getUniqueId().toString(), i);
                                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
                                    @Override
                                    public void run(){
                                       sql.setTokens(player, i);
                                       setCooldown(player.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                    }
                                }, timeLeftLong);
                            }
                        }catch (NumberFormatException e){
                            player.sendMessage(prefix+"Incorrect usage. That's not a number!");
                        }
                    }else{
                        sender.sendMessage(prefix+"You're the console, so do /nt set <amount> [player]");
                    }
                }

                if(args[0].equalsIgnoreCase("reload")){
                       reloadConfig();
                       sender.sendMessage(prefix+"Configuration and Messages files reloaded!");
                }


            }

            if(args.length == 3){

                if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
                    sendHelp(sender);
                }

                if(args[0].equalsIgnoreCase("balance")){
                    sender.sendMessage(prefix+"Incorrect usage. /nt balance [player]");
                }

                if(args[0].equalsIgnoreCase("add")){
                        if (Bukkit.getPlayer(args[2]) != null){
                            final Player targetPlayer = Bukkit.getPlayer(args[2]);
                        try {
                            final int i = Integer.parseInt(args[1]);
                            if (!isOnCooldown(targetPlayer.getUniqueId().toString())) {
                                int beforeTokens = sql.getTokens(targetPlayer);
                                int afterTokens = beforeTokens + i;
                                sql.addTokens(targetPlayer, i);
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("addedTokensOther")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%AddedTokens%", Integer.toString(i)).replace("%AddedPlayer%", targetPlayer.getDisplayName()));
                                setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                cach.put(targetPlayer.getUniqueId().toString(), afterTokens);
                            } else {
                                int beforeTokens = getCachedData(targetPlayer);
                                int afterTokens = beforeTokens + i;
                                float timeLeft = getTimeLeft(targetPlayer.getUniqueId().toString());
                                long timeLeftLong = (long) timeLeft;
                                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("addedTokensOther")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%AddedTokens%", Integer.toString(i)).replace("%AddedPlayer%", targetPlayer.getDisplayName()));
                                cach.put(targetPlayer.getUniqueId().toString(), afterTokens);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                                    @Override
                                    public void run() {
                                        sql.addTokens(targetPlayer, i);
                                        setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                    }

                                }, timeLeftLong);

                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix + "Incorrect usage. That's not a number!");
                        }
                    }else{
                            sender.sendMessage(prefix+ ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("playerNotFound")));
                        }
                }

                if(args[0].equalsIgnoreCase("remove")){
                    if(Bukkit.getPlayer(args[2]) !=null){

                        final Player targetPlayer = Bukkit.getPlayer(args[2]);
                        try {
                            int i = Integer.parseInt(args[1]);
                            if (!isOnCooldown(targetPlayer.getUniqueId().toString())) {
                                int beforeTokens = sql.getTokens(targetPlayer);
                                if (i > beforeTokens) {
                                    sql.setTokens(targetPlayer, 0);
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokensOther")).replace("%Tokens%", Integer.toString(0)).replace("%RemovedTokens%", Integer.toString(i)).replace("%RemovedPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), 0);
                                    setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                } else {
                                    int afterTokens = beforeTokens - i;
                                    sql.removeTokens(targetPlayer, i);
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokensOther")).replace("%Tokens%", Integer.toString(afterTokens)).replace("%RemovedTokens%", Integer.toString(i)).replace("%RemovedPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), afterTokens);
                                    setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));

                                }
                            } else {
                                int beforeTokens = getCachedData(targetPlayer);
                                float timeLeft = getTimeLeft(targetPlayer.getUniqueId().toString());
                                long timeLeftLong = (long) timeLeft;
                                if (i > beforeTokens) {
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokensOther")).replace("%Tokens%", Integer.toString(0)).replace("%RemovedTokens%", Integer.toString(i)).replace("%RemovedPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), 0);
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                        @Override
                                        public void run() {
                                            sql.setTokens(targetPlayer, 0);
                                            setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                        }
                                    }, timeLeftLong);
                                } else {
                                    int afterTokens = beforeTokens - i;
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("removedTokensOther")).replace("%Tokens", Integer.toString(afterTokens)).replace("%RemovedTokens%", Integer.toString(i)).replace("%RemovedPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), afterTokens);
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                        @Override
                                        public void run() {
                                            sql.removeTokens(targetPlayer, 0);
                                            setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                        }
                                    }, timeLeftLong);
                                }
                            }

                        } catch (NumberFormatException e) {
                            sender.sendMessage(prefix + "Incorrect usage. That's not a number!");
                        }
                    }else{
                        sender.sendMessage(prefix+ ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("playerNotFound")));
                    }


                }

                if(args[0].equalsIgnoreCase("set")){
                    if(Bukkit.getPlayer(args[2]) !=null){
                        final Player targetPlayer = Bukkit.getPlayer(args[2]);
                            try{
                                final int i = Integer.parseInt(args[1]);
                                if(!isOnCooldown(targetPlayer.getUniqueId().toString())) {
                                    sql.setTokens(targetPlayer, i);
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("setTokensOther")).replace("%Tokens%", Integer.toString(i)).replace("%SetTokens%", i+"").replace("%SetPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), i);
                                    setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                }else{
                                    float timeLeft = getTimeLeft(targetPlayer.getUniqueId().toString());
                                    long timeLeftLong = (long) timeLeft;
                                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("setTokensOther")).replace("%Tokens%", Integer.toString(i)).replace("%SetTokens%", i + "").replace("%SetPlayer%", targetPlayer.getDisplayName()));
                                    cach.put(targetPlayer.getUniqueId().toString(), i);
                                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
                                        @Override
                                        public void run(){
                                            sql.setTokens(targetPlayer, i);
                                            setCooldown(targetPlayer.getUniqueId().toString(), getConfig().getInt("Sync Interval"));
                                        }
                                    }, timeLeftLong);
                                }
                            }catch (NumberFormatException e){
                                sender.sendMessage(prefix+"Incorrect usage. That's not a number!");
                            }

                    }else{
                        sender.sendMessage(prefix+ ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("playerNotFound")));
                    }
                }

                if(args[0].equalsIgnoreCase("reload")){
                    reloadConfig();
                    sender.sendMessage(prefix+"Configuration and Messages files reloaded!");
                }

            }

            if(args.length > 3) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String cach = getCachedData(player) + "";
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("unknownCommand")).replace("%Tokens%", cach));
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Messages.getMessageFromID("unknownCommand")).replace("%Tokens%", ""));
                }
            }

        }




        return false;
    }



}
