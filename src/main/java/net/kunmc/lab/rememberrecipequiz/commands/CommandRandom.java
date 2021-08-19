package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandRandom
{
    public static void run(CommandSender sender, String[] args)
    {

        if (Utils.invalidLengthMessage(sender, args, 0, 2))
            return;

        if (RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームが実行中はこの操作は出来ません。");
            return;
        }

        Integer count = 10;
        if (args.length >= 1)
            if ((count = Utils.getAsIntegerOrNot(sender, args[0])) == null)
                return;

        int total = 0;


        RememberRecipeQuiz.game.clearRandomPhases();

        for (int i = 0; i < count; i++)
            total = RememberRecipeQuiz.game.addPhase(new Game.RandomPhase());

        sender.sendMessage(ChatColor.GREEN + "S: ランダムなお題を " + count + " つに設定しました。");

        sender.sendMessage(ChatColor.BLUE + "I: 現在のお題数は " + total + " つです。");
    }
}
