package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandPhaseTime
{
    public static void run(CommandSender sender, String[] args)
    {
        if (!RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームが実行中以外は操作できません。");
            return;
        }

        Integer ag;
        if (Utils.invalidLengthMessage(sender, args, 1, 1) ||
                (ag = Utils.getAsIntegerOrNot(sender, args[0])) == null)
            return;

        if (ag <= 0)
        {
            sender.sendMessage(ChatColor.RED + "E: 時間を0以下にすることは出来ません。");
            return;
        }

        RememberRecipeQuiz.game.phaseTimeChange(ag);
        sender.sendMessage(ChatColor.GREEN + "S: 現在のお題のシンキングタイムを変更しました。");

    }
}
