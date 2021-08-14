package net.kunmc.lab.rememberrecipequiz.gui;

import de.themoep.inventorygui.InventoryGui;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class GUIManager
{
    public static GUIManager instance;


    private final HashMap<String, InventoryGui> guis;
    private final JavaPlugin plugin;

    public GUIManager(JavaPlugin plugin)
    {
        instance = this;
        this.plugin = plugin;
        this.guis = new HashMap<>();
    }

    public <T extends IGUIMenu> void addGui(T gui)
    {
        InventoryGui g = new InventoryGui(plugin, gui.getTitle(), gui.getFormat());
        ItemStack st = new ItemStack(Material.GLASS_PANE);
        Colorable cl = (Colorable) st.getData();
        cl.setColor(DyeColor.LIGHT_GRAY);

        st.setData((MaterialData) cl);

        g.setFiller(st);
        this.guis.put(gui.getName(), g);
    }

    public void open(String name, Player player)
    {
        if (!guis.containsKey(name))
            return;
        guis.get(name).show(player);
    }
}
