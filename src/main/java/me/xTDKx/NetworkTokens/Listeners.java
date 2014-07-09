package me.xTDKx.NetworkTokens;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

public class Listeners implements Listener{
    private Main plugin;

    public Listeners(Main p){
        plugin = p;
    }


    public static ScoreboardManager manager;
    public static Scoreboard board;
    public static Objective objective;
    public static Score score;

    /*public void giveScoreboard(Player player) {
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        objective = board.registerNewObjective("board", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Scoreboard Title"))"Jim");

        for (String score : plugin.getConfig().getStringList("Scoreboard Layout")) {
            score1 = objective.getScore(ChatColor.translateAlternateColorCodes('&', score).replace("%Tokens%", Integer.toString(sql.getTokens(player))));
            score1.setScore(0);

            if (score.equalsIgnoreCase("%TokensFullLine%")) {
                Score score2 = objective.getScore(ChatColor.translateAlternateColorCodes('&', score).replace("%TokensFullLine%", "Your tokens"));
                score2.setScore(sql.getTokens(player));
            }
        }
    }*/

    public void giveBoard(Player player, int balance){
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        objective = board.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Scoreboard Title")));

        int i = 16;

        for(String entry : plugin.getConfig().getStringList("Scoreboard Layout")) {

                i--;

                if (entry.length() > 15) {
                    entry = entry.substring(0, 15);
                    score = objective.getScore(ChatColor.translateAlternateColorCodes('&', entry).replace("%Tokens%", Integer.toString(balance)).replace("%NewLine%", " "));
                    score.setScore(i);
                } else {
                    Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', entry).replace("%Tokens%", Integer.toString(balance)).replace("%NewLine%", " "));
                    score.setScore(i);
                }
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        if(!(plugin.getConfig().getInt("Initial Amount") == 0)){
           Player player = event.getPlayer();
           if(!plugin.sql.tableContains(player)){
                plugin.sql.setTokens(player, plugin.getConfig().getInt("Initial Amount"));
            }
        }


        //giveScoreboard(event.getPlayer());

        /*if(plugin.getConfig().getBoolean("Enable Scoreboard")){
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    giveScoreboard(p);
                }
            }, 10, plugin.getConfig().getInt("Sync Interval"));

        }*/
    }



}
