package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandFlag
{
    public static void run(CommandSender sender, String[] args)
    {

        if (Utils.invalidLengthMessage(sender, args, 2, 2) ||
                Utils.validateArg(sender, args[1], "on", "off"))
            return;

        Game.Flag flag = Game.Flag.fromName(args[0]);

        if (flag == null)
        {
            sender.sendMessage(ChatColor.RED + "E: フラグが存在しません。");
            return;
        }

        if (args[1].equals("on"))
            RememberRecipeQuiz.game.addFlag(flag);
        else
            RememberRecipeQuiz.game.removeFlag(flag);


        sender.sendMessage(ChatColor.GREEN + "S: フラグ '" + flag + "' を " + args[1] + " にセットしました。");
    }
}
