package net.kunmc.lab.rememberrecipequiz.gui;

import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.entity.Player;

import java.util.List;

public interface IGUIMenu
{
    String getName();
    String getTitle();
    String[] getFormat();
    List<StaticGuiElement> getElements();

    void open(Player player);
}
