package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.stream.IntStream;

public class CommandRandom
{
    public static void run(CommandSender sender, String[] args)
    {

        if (Utils.invalidLengthMessage(sender, args, 0, 2))
            return;

        Integer count = 10;
        if (args.length > 1)
            if ((count = Utils.getAsIntegerOrNot(sender, args[0])) == null)
                return;


        Integer thinking = 60;
        if (args.length > 2)
            if ((thinking = Utils.getAsIntegerOrNot(sender, args[1])) == null)
                return;

        Integer finalThinking = thinking;
        IntStream.range(0, count)
                .parallel()
                .forEach(value -> {
                    RememberRecipeQuiz.game.addPhase(new Game.Phase(
                            finalThinking,
                            Utils.getRandomRecipe().getResult().getType()));
                });


        sender.sendMessage(ChatColor.GREEN + "S: ランダムなお題を " + count + " つ追加しました。");

    }
}
