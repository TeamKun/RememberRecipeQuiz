package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandThingTime
{
    public static void run(CommandSender sender, String[] args)
    {
        if (RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームが実行中はこの操作は出来ません。");
            return;
        }

        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GREEN + "現在の値は " + Game.Phase.timeWaitDefault + " 秒です。");
            return;
        }

        Integer ag;
        if (Utils.invalidLengthMessage(sender, args, 1, 1) ||
                (ag = Utils.getAsIntegerOrNot(sender, args[0])) == null)
            return;

        Game.Phase.timeWaitDefault = ag;

        RememberRecipeQuiz.game.getPhases().forEach(phase -> {
            phase.setTimeWait(ag);
        });

        sender.sendMessage(ChatColor.GREEN + "S: シンキングタイムを変更しました。");
    }
}
