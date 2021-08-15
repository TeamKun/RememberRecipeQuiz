package net.kunmc.lab.rememberrecipequiz;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RememberRecipeQuiz extends JavaPlugin
{
    public static Game game;
    private static RememberRecipeQuiz plugin;

    public static RememberRecipeQuiz getPlugin()
    {
        return plugin;
    }

    @Override
    public void onEnable()
    {
        plugin = this;

        game = new Game();
        game.registerLogics();
        Bukkit.getOnlinePlayers().stream().parallel()
                .filter(player -> player.hasPermission("req.play"))
                .forEach(game::addPlayer);
    }

    @Override
    public void onDisable()
    {
        game.actuallyStop();
    }
}
