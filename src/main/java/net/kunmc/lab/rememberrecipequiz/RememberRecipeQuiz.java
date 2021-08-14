package net.kunmc.lab.rememberrecipequiz;

import net.kunmc.lab.rememberrecipequiz.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RememberRecipeQuiz extends JavaPlugin
{
    public static Game game;
    private static RememberRecipeQuiz plugin;
    private static GUIManager gui;

    public static RememberRecipeQuiz getPlugin()
    {
        return plugin;
    }

    @Override
    public void onEnable()
    {
        plugin = this;
        Bukkit.getOnlinePlayers().stream().parallel()
                .filter(player -> player.hasPermission("req.play"))
                .forEach(game::addPlayer);

        gui = new GUIManager(this);



    }

    @Override
    public void onDisable()
    {
        game.actuallyStop();
    }
}
