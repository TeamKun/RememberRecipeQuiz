package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandStop
{
    public static void run(CommandSender sender, String[] args)
    {
        if (!RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームがスタートしていません。");
            return;
        }
        RememberRecipeQuiz.game.actuallyStop();
    }
}
