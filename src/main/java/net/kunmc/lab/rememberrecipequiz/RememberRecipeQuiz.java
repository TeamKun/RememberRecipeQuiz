package net.kunmc.lab.rememberrecipequiz;

import net.kunmc.lab.rememberrecipequiz.commands.CommandMain;
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
                .forEach(game::addPlayer);

        getCommand("req").setExecutor(new CommandMain());
        getCommand("req").setTabCompleter(new CommandMain());

    }

    @Override
    public void onDisable()
    {
        game.actuallyStop();
    }
}
