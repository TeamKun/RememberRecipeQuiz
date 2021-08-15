package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import net.kunmc.lab.rememberrecipequiz.RememberRecipeQuiz;
import net.kunmc.lab.rememberrecipequiz.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandAdd
{
    public static void run(CommandSender sender, String[] args)
    {
        Integer ag;
        ItemStack st;
        if (Utils.invalidLengthMessage(sender, args, 1, 1) ||
                (ag = Utils.getAsIntegerOrNot(sender, args[0])) == null ||
                Utils.checkSenderPlayer(sender) ||
                (st = Utils.getHandItemOrNot((Player) sender)) == null)
            return;

        if (!Utils.ifItemHasRecipe(st.getType()))
        {
            sender.sendMessage(ChatColor.RED + "E: お題にするアイテムはクラフト可能である必要があります。");
            return;
        }

        RememberRecipeQuiz.game.addPhase(new Game.Phase(ag, st.getType()));
    }
}
