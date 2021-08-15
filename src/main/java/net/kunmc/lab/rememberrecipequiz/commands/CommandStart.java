package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandStart
{
    public static void run(CommandSender sender, String[] args)
    {
        if (RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームはすでにスタートしています。");
            return;
        }
        RememberRecipeQuiz.game.startWithCountdown();
    }
}
