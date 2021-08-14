package net.kunmc.lab.rememberrecipequiz;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

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
}
