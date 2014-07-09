package me.xTDKx.NetworkTokens;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class TokenSQL {
    private Main plugin;

    public TokenSQL(Main p){
        plugin = p;
    }

    private static Connection connection;
    Logger logger = Logger.getLogger("Minecraft");


   public boolean canConnect(){
      try {
          connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("Database Address") + ":" + plugin.getConfig().getString("Database Port") + "/" + plugin.getConfig().getString("Database Name"), plugin.getConfig().getString("Database User"), plugin.getConfig().getString("Database Password"));
          //connection = DriverManager.getConnection("jdbc:mysql://mysql-east.getnodecraft.net:3306/mcs_5437", "mcs_5437", "39abedaf30");
          return true;
      }catch (Exception e){
          return false;
      }

    }


    public synchronized void openConnection(){
        try{
                connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("Database Address") + ":" + plugin.getConfig().getString("Database Port") + "/" + plugin.getConfig().getString("Database Name"), plugin.getConfig().getString("Database User"), plugin.getConfig().getString("Database Password"));
            //connection = DriverManager.getConnection("jdbc:mysql://mysql-east.getnodecraft.net:3306/mcs_5437", "mcs_5437", "39abedaf30");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void closeConnection(){
        try {
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized boolean playerDataContainsPlayer(Player player){
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
            sql.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = sql.executeQuery();
            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean tableContains(Player player){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
            sql.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = sql.executeQuery();
            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            closeConnection();
        }
    }

    public synchronized void createTable(){
        try{
            PreparedStatement sql = connection.prepareStatement("CREATE TABLE "+plugin.getConfig().getString("Table Prefix")+"Tokens (Player VARCHAR(40), Tokens int, Doubled int);");
            sql.executeUpdate();
            sql.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void tableCheck(){
        openConnection();
        try {
            PreparedStatement sql = connection.prepareStatement("SELECT 1 FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` LIMIT 1;");
            ResultSet set = sql.executeQuery();
            set.next();

            sql.close();
            set.close();
        }catch (Exception e){
            logger.info("Tokens table doesn't exist, creating one.");
            createTable();
        }finally {
            closeConnection();
        }
    }

    public synchronized void addTokens(Player player, int amount){
        openConnection();
        if(playerDataContainsPlayer(player)){
            int beforeTokens;
            try {
                PreparedStatement sql = connection.prepareStatement("SELECT Tokens FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = sql.executeQuery();
                resultSet.next();
                beforeTokens = resultSet.getInt("Tokens");


                PreparedStatement sql2 = connection.prepareStatement("UPDATE `"+plugin.getConfig().getString("Table Prefix")+"Tokens` SET Tokens=? WHERE Player=?;");
                sql2.setInt(1, beforeTokens + amount);
                sql2.setString(2, player.getUniqueId().toString());
                sql2.executeUpdate();

                sql.close();
                resultSet.close();
                sql2.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }else{
            try{
                PreparedStatement sql3 = connection.prepareStatement("INSERT INTO `"+plugin.getConfig().getString("Table Prefix")+"Tokens` values(?,?,?);");
                sql3.setString(1, player.getUniqueId().toString());
                sql3.setInt(2, amount);
                sql3.setInt(3, 0);
                sql3.execute();
                sql3.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }
    }

    public synchronized void setTokens(Player player, int amount){
        openConnection();
        if(playerDataContainsPlayer(player)){
            try{
                PreparedStatement sql = connection.prepareStatement("UPDATE `"+plugin.getConfig().getString("Table Prefix")+"Tokens` SET Tokens=? WHERE Player=?;");
                sql.setInt(1, amount);
                sql.setString(2, player.getUniqueId().toString());
                sql.executeUpdate();

                sql.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }else{
            try{
                PreparedStatement sql2 = connection.prepareStatement("INSERT INTO `"+plugin.getConfig().getString("Table Prefix")+"Tokens` values(?,?,?);");
                sql2.setString(1, player.getUniqueId().toString());
                sql2.setInt(2, amount);
                sql2.setInt(3, 0);
                sql2.execute();

                sql2.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }
    }

    public synchronized int getTokens(Player player){
        openConnection();
        if(playerDataContainsPlayer(player)){
            try{
                int currentTokens;
                PreparedStatement sql = connection.prepareStatement("SELECT Tokens FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = sql.executeQuery();
                resultSet.next();
                currentTokens = resultSet.getInt("Tokens");

                return currentTokens;
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }finally {
                closeConnection();
            }
        }else{
            return 0;
        }
    }

    public synchronized void removeTokens(Player player, int amount){
        openConnection();
        if(playerDataContainsPlayer(player)){
            int beforeTokens;
            try{
                PreparedStatement sql = connection.prepareStatement("SELECT Tokens FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = sql.executeQuery();
                resultSet.next();

                beforeTokens = resultSet.getInt("Tokens");

                PreparedStatement sql2 = connection.prepareStatement("UPDATE `"+plugin.getConfig().getString("Table Prefix")+"Tokens` SET Tokens=? WHERE Player=?;");
                if(amount <= beforeTokens){
                    sql2.setInt(1, beforeTokens - amount);
                    sql2.setString(2, player.getUniqueId().toString());
                    sql2.executeUpdate();

                    sql2.close();
                }else{
                    sql2.setInt(1, 0);
                    sql2.setString(2, player.getUniqueId().toString());
                    sql2.executeUpdate();

                    sql2.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally{
                closeConnection();
            }
        }
    }

    public synchronized boolean isDoub(Player player) {
        boolean isDub = false;
        if (playerDataContainsPlayer(player)){

            try {
                PreparedStatement sql = connection.prepareStatement("SELECT Double FROM `"+plugin.getConfig().getString("Table Prefix")+"Tokens` WHERE Player=?;");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = sql.executeQuery();
                resultSet.next();

                if(resultSet.getInt("Double") == 0){
                    isDub = false;
                }else{
                    isDub = true;
                }
                return isDub;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            isDub = false;
        }
        return isDub;
    }

    public synchronized boolean isDoubled(Player player){
        openConnection();
        boolean doub = false;
        try {
            isDoub(player);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeConnection();
        }
        if(isDoub(player)){
            doub = true;
        }else{
            doub = false;
        }
        return doub;
    }

    public synchronized void setDoubled(Player player){

        if(playerDataContainsPlayer(player)){
            openConnection();
            try {
                PreparedStatement sql = connection.prepareStatement("UPDATE `"+plugin.getConfig().getString("Table Prefix")+"Tokens` SET Double=? WHERE Player=?;");
                sql.setInt(1, 1);
                sql.setString(2, player.getUniqueId().toString());
                sql.executeUpdate();

                sql.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }
    }

    public synchronized void setUnDoubled(Player player){
        if(playerDataContainsPlayer(player)){
            openConnection();
            try {
                PreparedStatement sql = connection.prepareStatement("UPDATE `"+plugin.getConfig().getString("Table Prefix")+"Tokens` SET Double=? WHERE Player=?;");
                sql.setInt(1, 0);
                sql.setString(2, player.getUniqueId().toString());
                sql.executeUpdate();

                sql.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
