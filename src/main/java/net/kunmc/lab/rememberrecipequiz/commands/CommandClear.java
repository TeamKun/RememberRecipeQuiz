package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandClear
{
    public static void run(CommandSender sender, String[] args)
    {
        if (RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームが実行中はこの操作は出来ません。");
            return;
        }

        RememberRecipeQuiz.game.clearPhase();

        sender.sendMessage(ChatColor.GREEN + "S: お題をクリアしました。");
    }
}
