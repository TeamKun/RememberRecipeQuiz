package net.kunmc.lab.rememberrecipequiz.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHelp
{
    public static void run(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.AQUA + "=====レシピ覚えてるかクイズ=====");
        sender.sendMessage(ChatColor.GREEN + "  /req help     このコマンドです。");
        sender.sendMessage(ChatColor.GREEN + "  /req (gui)    GUIを開きます。");
    }
}
