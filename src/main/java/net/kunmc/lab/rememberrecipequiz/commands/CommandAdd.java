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
        if (RememberRecipeQuiz.game.isStarted())
        {
            sender.sendMessage(ChatColor.RED + "E: ゲームが実行中はこの操作は出来ません。");
            return;
        }

        ItemStack st;
        if (Utils.invalidLengthMessage(sender, args, 0, 0) ||
                Utils.checkSenderPlayer(sender) ||
                (st = Utils.getHandItemOrNot((Player) sender)) == null)
            return;

        if (!Utils.ifItemHasRecipe(st.getType()))
        {
            sender.sendMessage(ChatColor.RED + "E: お題にするアイテムはクラフト可能である必要があります。");
            return;
        }

        RememberRecipeQuiz.game.addPhase(new Game.Phase(st.getType()), false);
        sender.sendMessage(ChatColor.GREEN + "S: お題を追加しました。");
    }
}
