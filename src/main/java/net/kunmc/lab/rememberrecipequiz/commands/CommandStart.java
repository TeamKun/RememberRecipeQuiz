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

        if (RememberRecipeQuiz.game.getPhases().size() == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: 先にお題をセットしてください。。");
            return;
        }

        if (RememberRecipeQuiz.game.isStarting)
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームを開始し中です。");
            return;
        }
        RememberRecipeQuiz.game.startWithCountdown();
    }
}
