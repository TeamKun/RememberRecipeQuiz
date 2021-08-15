package net.kunmc.lab.rememberrecipequiz;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Utils
{
    private static List<Recipe> recipes = null;

    public static String getItemName(Material mat)
    {
        return Translator.get(mat.getTranslationKey());
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

    public static boolean validateArg(CommandSender sender, String value, String... need)
    {
        if (Arrays.asList(need).contains(value))
            return false;

        sender.sendMessage(ChatColor.RED + "E: 不正な引数です：" + value +
                "。次のうちのどれかである必要があります：" + String.join(", ", need));
        return true;
    }

    private static void playSound(Sound sound, Player p, float pitch)
    {
        p.playSound(p.getLocation(), sound, SoundCategory.PLAYERS, 1.0f, pitch);
    }

    public static void playPingPongSound(Player player)
    {
        playSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, player, 1.42f);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                playSound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, player, 1.06f);
            }
        }.runTaskLater(RememberRecipeQuiz.getPlugin(), 6L);
    }

    public static void launchFireworks(Player player)
    {
        Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.GREEN)
                .with(FireworkEffect.Type.BALL)
                .withFade(Color.WHITE)
                .build());
        meta.setPower(4);
        fw.setFireworkMeta(meta);
    }

    public static Recipe getRandomRecipe()
    {
        if (recipes == null)
            recipes = Lists.newArrayList(Bukkit.recipeIterator());

        Random random = new Random();

        return recipes.get(random.nextInt(recipes.size()));

    }
}
