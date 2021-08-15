package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMain implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("req.admin"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return true;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 不明なコマンドです！");
            sender.sendMessage(ChatColor.AQUA + "/req help コマンドでヘルプを閲覧することが出来ます。");
            return true;
        }

        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));

        argsList.remove(0);

        switch (args[0].toLowerCase())
        {
            case "help":
                CommandHelp.run(sender, argsList.toArray(new String[0]));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "E: 不明なコマンドです！");
                sender.sendMessage(ChatColor.AQUA + "/req help コマンドでヘルプを閲覧することが出来ます。");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ArrayList<String> completes = new ArrayList<>();

        if (!sender.hasPermission("req.use"))
            return new ArrayList<>();

        switch (args.length)
        {
            case 1:
                completes.addAll(Arrays.asList("help", "add", "flag", "random"));
                break;
            case 2:
                switch (args[0])
                {
                    case "flag":
                        completes.addAll(Arrays.asList(Game.Flag.names()));
                        break;
                }
            case 3:
                switch (args[0])
                {
                    case "flag":
                        completes.add("on");
                        completes.add("off");
                        break;
                }
        }

        ArrayList<String> asCopy = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], completes, asCopy);
        Collections.sort(asCopy);
        return asCopy;
    }
}
