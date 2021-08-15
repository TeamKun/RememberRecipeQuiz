package net.kunmc.lab.rememberrecipequiz;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class Utils
{
    public static String getItemName(Material mat)
    {
        return ((TextComponent)
                GlobalTranslator.render(Component.translatable(mat.getTranslationKey()), Locale.JAPAN)).content();
    }

    public static String getCP(int now, int max)
    {
        double gm = (double) now / (double) max;
        if (gm < 0.35)
            return ChatColor.RED.toString() + now;
        else if (gm < 0.65)
            return ChatColor.YELLOW.toString() + now;
        return ChatColor.GREEN.toString() + now;
    }

    public static boolean invalidLengthMessage(CommandSender sender, String[] args, int min, int max)
    {
        if (args.length < min || args.length > max)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数の数が不正です。/req helpでヘルプを閲覧してください。");
            return true;
        }

        return false;
    }

    public static Integer getAsIntegerOrNot(CommandSender sender, String value)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException ignored)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不正です。/req helpでヘルプを閲覧してください。");
            return null;
        }
    }

    public static boolean checkSenderPlayer(CommandSender sender)
    {
        if (sender instanceof Player)
            return false;
        sender.sendMessage(ChatColor.RED + "E: このコマンドはプレイヤーから実行する必要があります。");
        return true;
    }

    public static ItemStack getHandItemOrNot(Player sender)
    {
        ItemStack st = sender.getInventory().getItemInMainHand();

        if (st.getType() == Material.AIR)
        {
            sender.sendMessage(ChatColor.RED + "E: このコマンドは手にアイテムを持って実行する必要があります。");
            st = null;
        }

        return st;
    }

    public static boolean ifItemHasRecipe(Material item)
    {
        ItemStack stack = new ItemStack(item);
        return Bukkit.getRecipesFor(stack).size() != 0;
    }
}
