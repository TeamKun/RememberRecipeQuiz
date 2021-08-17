package net.kunmc.lab.rememberrecipequiz;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Furnace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Game
{
    private final List<UUID> players;
    private GameTimer game;
    private List<Phase> phases;
    private boolean start;
    private final ProtocolManager protocol;
    private final GameLogic logic;
    private int currentPhase;

    //=========ここからゲーム用変数
    private final BossBar indicator;
    private final List<UUID> finishedPlayers;
    private List<UUID> eliminatedPlayers;
    private Phase phaseStaging;
    private final List<Flag> flags;
    private int eliminatedThisPhase;

    public Game()
    {
        this.players = new ArrayList<>();
        this.phases = new ArrayList<>();
        this.protocol = ProtocolLibrary.getProtocolManager();
        this.start = false;
        this.currentPhase = -1;
        this.logic = new GameLogic();

        //初期化
        this.indicator = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.indicator.setVisible(false);
        this.finishedPlayers = new ArrayList<>();
        this.eliminatedPlayers = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.phaseStaging = null;
        this.eliminatedThisPhase = 0;

        this.protocol.addPacketListener(new CraftPacketListener());
    }

    public boolean isStarted()
    {
        return start;
    }

    public void actuallyStart()
    {
        ArrayList<World> worlds = new ArrayList<>();
        players.forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null)
                        return;
                    if (!worlds.contains(player.getWorld()))
                        worlds.add(player.getWorld());
                });
        worlds.forEach(world -> {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        });

        broadcastMessage(ChatColor.GREEN + "ゲームがスタートしました！");
        broadcastTitle(ChatColor.GREEN + "スタート！", "");
        this.indicator.setVisible(true);
        this.start = true;
        this.game = new GameTimer();
        this.game.runTaskTimer(RememberRecipeQuiz.getPlugin(), 0L, 20L);
    }

    public void startWithCountdown()
    {
        new BukkitRunnable()
        {
            private int time = 6;

            @Override
            public void run()
            {
                if (--time <= 0)
                {
                    this.cancel();
                    actuallyStart();
                    return;
                }

                broadcastMessage(ChatColor.YELLOW + "スタートまで " +
                        ChatColor.RED + time + " 秒!");
                broadcastTitle(
                        ChatColor.RED + String.valueOf(time),
                        ChatColor.GREEN + "まもなくゲームがスタートします！"
                );
            }
        }.runTaskTimer(RememberRecipeQuiz.getPlugin(), 0L, 20L);
    }

    public void actuallyStop()
    {
        broadcastPlayer(ChatColor.RED + "ゲームが終了しました！");
        this.game.cancel();
        this.game = null;
        this.start = false;
        this.indicator.setVisible(false);
    }

    public List<UUID> getPlayers()
    {
        return players;
    }

    public void addPlayer(Player player)
    {
        this.players.add(player.getUniqueId());
        this.indicator.addPlayer(player);
        player.sendMessage(ChatColor.GREEN + "レシピクイズに参加しました！\n開始をお待ち下さい。");
        player.setGameMode(GameMode.CREATIVE);
    }

    public void removePlayer(Player player)
    {
        this.players.remove(player.getUniqueId());
        this.finishedPlayers.remove(player.getUniqueId());
        this.eliminatedPlayers.remove(player.getUniqueId());
        this.indicator.removePlayer(player);
        player.sendMessage(ChatColor.RED + "レシピクイズから退出しました。");
        player.setGameMode(GameMode.SPECTATOR);

    }

    public List<Phase> getPhases()
    {
        return phases;
    }

    public int addPhase(Phase phase, boolean silent)
    {
        if (!silent)
            broadcastMessage(ChatColor.GREEN + "お題が追加されました！");
        this.phases.add(phase);
        return this.phases.size();
    }

    public int addPhase(Phase phase)
    {
        return addPhase(phase, true);
    }

    public void clearPhase()
    {
        this.phases.clear();
    }

    public void clearRandomPhases()
    {
        this.phases = this.phases.stream()
                .filter(phase -> !(phase instanceof RandomPhase)).collect(Collectors.toList());
    }

    private void broadcastMessage(String message)
    {
        Bukkit.getOnlinePlayers().stream().parallel()
                .forEach(player -> player.sendMessage(message));
    }

    private void broadcastABMessage(String message)
    {
        Bukkit.getOnlinePlayers().stream().parallel()
                .forEach(player -> player.sendActionBar(Component.text(message)));
    }

    private void broadcastTitle(String title, String subtitle)
    {
        Bukkit.getOnlinePlayers().stream().parallel().forEach(player -> {
            player.sendTitle(title, subtitle, 5, 10, 5);
        });
    }

    private void broadcastTitle(String title, String subtitle, int i, int s, int o)
    {
        Bukkit.getOnlinePlayers().stream().parallel().forEach(player -> {
            player.sendTitle(title, subtitle, i, s, o);
        });
    }

    private void broadcastPlayer(String message)
    {
        this.players.stream().parallel()
                .forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null)
                        player.sendMessage(message);
                });
    }

    public class GameLogic implements Listener
    {
        @EventHandler
        public void onGameModeChange(PlayerGameModeChangeEvent e)
        {
            Player p = e.getPlayer();

            switch (e.getNewGameMode())
            {
                case CREATIVE:
                    p.sendMessage(ChatColor.GREEN + "あなたのゲームモードが クリエイティブ モードに変更されたため状態を更新しています...");
                    addPlayer(p);
                    break;
                case SPECTATOR:
                    p.sendMessage(ChatColor.GREEN + "あなたのゲームモードが スペクテイター モードに変更されたため状態を更新しています...");
                    removePlayer(p);
                    break;
            }
        }

        @EventHandler
        public void onInvClick(InventoryClickEvent e)
        {
            if (!start)
                return;

            if (flags.contains(Flag.NO_EXAMPLE))
                return;

            if (e.getSlot() != 8)
                return;
            e.setCancelled(true);
            e.getView().setCursor(null);
            ((Player) e.getWhoClicked()).updateInventory();
        }

        @EventHandler
        public void onRemove(PlayerQuitEvent e)
        {
            removePlayer(e.getPlayer());
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent e)
        {
            addPlayer(e.getPlayer());
        }

        @EventHandler
        public void onRespawn(PlayerRespawnEvent e)
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    e.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
            }.runTaskLater(RememberRecipeQuiz.getPlugin(), 1L);
        }


        @EventHandler
        public void onCraftComplete(CraftItemEvent e)
        {
            onComplete((Player) e.getWhoClicked(), e.getRecipe().getResult().getType());
        }

        @EventHandler
        public void onFurnaceBurn(FurnaceBurnEvent e)
        {
            Furnace furnace = (Furnace) e.getBlock().getState();
            if (furnace.getCookTime() < 2)
            {
                furnace.setCookTime((short) 9);
                furnace.setCookTimeTotal((short) 10);
            }
            furnace.update(true);
        }

        @EventHandler
        public void onFurnaceSmelt(FurnaceSmeltEvent e) {

            Furnace furnace = (Furnace) e.getBlock().getState();
            if (furnace.getCookTime() < 2)
            {
                furnace.setCookTime((short) 9);
                furnace.setCookTimeTotal((short) 10);
            }
            furnace.update(true);
        }

        @EventHandler
        public void onFurnaceExtract(FurnaceExtractEvent e)
        {
            onComplete(e.getPlayer(), e.getItemType());
        }

        private void onComplete(Player player, Material type)
        {
            if (phaseStaging == null)
                return;

            if(type != phaseStaging.getTargetMaterial())
                if (flags.contains(Flag.ONLY_ONCE_SUBMIT))
                {
                    player.setHealth(0.0);
                    broadcastMessage(player.getName() + " はクラフトを間違えて失格になった。");
                    player.playSound(Sound.sound(Key.key("minecraft:block.anvil.land"), Sound.Source.BLOCK, 1.0f, 1.0f));
                    eliminatedPlayers.add(player.getUniqueId());
                    return;
                }
                else
                    return;

            Utils.playPingPongSound(player);
            Utils.launchFireworks(player);
            broadcastMessage(ChatColor.GREEN + player.getName() + " はお題のクラフトに成功しました！");
            finishedPlayers.add(player.getUniqueId());

            if (players.stream().parallel()
                    .noneMatch(uuid -> !finishedPlayers.contains(uuid) && !eliminatedPlayers.contains(uuid)))
            {
                broadcastPlayer(ChatColor.GREEN + "全員がこのお題を処理しました！時間を進めています...");
                game.skip();
            }
        }
    }

    public class CraftPacketListener extends PacketAdapter
    {
        public CraftPacketListener()
        {
            super(RememberRecipeQuiz.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.AUTO_RECIPE);
        }

        @Override
        public void onPacketSending(PacketEvent e)
        {
            if (e.getPacketType() != PacketType.Play.Server.AUTO_RECIPE)
                return;

            if (!start)
                return;

            UUID id = e.getPlayer().getUniqueId();
            if (players.contains(id) && !finishedPlayers.contains(id))
            {
                e.getPlayer().setHealth(0.0);
                broadcastMessage(e.getPlayer().getName() + " はカンニングをしようとしたため失格になった。");
                e.getPlayer().playSound(Sound.sound(Key.key("minecraft:block.anvil.land"), Sound.Source.BLOCK, 1.0f, 1.0f));
                eliminatedPlayers.add(id);
                eliminatedThisPhase++;
                e.setCancelled(true);
            }
        }
    }

    public List<Flag> getFlags()
    {
        return flags;
    }

    public void addFlag(Flag f)
    {
        if (!flags.contains(f))
            flags.add(f);
    }

    public void removeFlag(Flag f)
    {
        flags.remove(f);
    }

    public void registerLogics()
    {
        Bukkit.getPluginManager().registerEvents(logic, RememberRecipeQuiz.getPlugin());
    }

    public enum Flag
    {
        ONLY_ONCE_SUBMIT("once_craft"),
        REVIVE_IN_NEXT_PHASE("revive_next_phase"),
        NO_EXAMPLE("no_example");
        //ENDLESS("endless");

        private final String id;

        Flag(String id)
        {
            this.id = id;
        }

        public String getId()
        {
            return id;
        }

        public static String[] names()
        {
            return Arrays.stream(values()).map(Flag::getId).toArray(String[]::new);
        }

        public static Flag fromName(String name)
        {
            for (Flag value : values())
                if (value.id.equals(name))
                    return value;
            return null;
        }
    }

    public static class Phase
    {
        private final int timeWait;
        private final Material targetMaterial;

        public static int timeWaitDefault;

        public Phase(int timeWait, Material targetMaterial)
        {
            this.timeWait = timeWait;
            this.targetMaterial = targetMaterial;
        }

        public int getTimeWait()
        {
            return timeWait;
        }

        public Material getTargetMaterial()
        {
            return targetMaterial;
        }
    }

    public static class RandomPhase extends Phase
    {
        public RandomPhase(int timeWait)
        {
            super(timeWait, Utils.getRandomRecipe().getResult().getType());
        }
    }

    public void randomizePhases()
    {
        Collections.shuffle(this.phases);
    }

    private class GameTimer extends BukkitRunnable
    {

        private int interval;
        private String itemName;

        public GameTimer()
        {
            this.interval = -1;
        }

        @Override
        public void run()
        {
            if (phaseStaging == null)
            {
                if (--interval >= 0)
                {
                    if (interval <= 5)
                        broadcastMessage(ChatColor.YELLOW + "次のお題まで... " +
                                ChatColor.RED + interval);
                    return;
                }

                initPhase(++currentPhase);

                if (flags.contains(Flag.REVIVE_IN_NEXT_PHASE))
                {
                    eliminatedPlayers = eliminatedPlayers.stream()
                            .filter(uuid -> {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player == null)
                                    return false;

                                if (player.isDead())
                                    player.spigot().respawn();
                                player.setGameMode(GameMode.CREATIVE);
                                player.sendMessage(ChatColor.GREEN + "復活モードが有効のため、復活しました！");
                                return false;
                            }).collect(Collectors.toList());
                }

                sendInfo();
                return;
            }

            if (--interval <= 0)
            {
                processPhaseEnd();
                return;
            }

            sendInfo();
        }

        public void skip()
        {
            if (this.interval > 3)
                this.interval = 3;
        }

        private void processPhaseEnd()
        {
            int ps = players.size() - eliminatedPlayers.size();
            int fps = finishedPlayers.size();
            broadcastTitle(
                    ChatColor.RED + "終了！",
                    Utils.getCP(fps, ps) + ChatColor.GREEN + " 人がクリアしました！(" +
                            ((int) (((double) fps / (double) ps) * 100)) + "%)"
            );

            for (UUID uuid : players)
            {
                if (finishedPlayers.contains(uuid) || eliminatedPlayers.contains(uuid))
                    continue;
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    continue;
                eliminatedThisPhase++;
                player.setHealth(0);
                broadcastMessage(player.getName() + " はレシピを覚えていられなかった。");
                eliminatedPlayers.add(player.getUniqueId());
            }

            finishedPlayers.clear();

            String msg = "==========\n" +
                    "お題：" + itemName + "\n" +
                    "失格人数：" + eliminatedThisPhase + "\n";

            eliminatedThisPhase = 0;

            if (currentPhase + 1 >= phases.size())
            {
                msg += "==========";
                broadcastMessage(msg);
                actuallyStop();
                return;
            }
            msg += ChatColor.GREEN + "次のお題がまもなく出題されます！\n";
            msg += ChatColor.WHITE + "==========";
            broadcastMessage(msg);

            phaseStaging = null;
            this.interval = 7;
        }

        private void sendInfo()
        {
            players.stream().parallel()
                    .forEach(uuid -> {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null)
                            return;

                        player.sendTitle(ChatColor.GREEN + "お題：" + this.itemName,
                                ChatColor.YELLOW + "残り " + Utils.getCP(interval, phaseStaging.timeWait) +
                                        ChatColor.YELLOW + " 秒",
                                0, 22, 0
                        );
                        int ps = players.size() - eliminatedPlayers.size();
                        int fps = finishedPlayers.size();
                        player.sendActionBar(Component.text(ChatColor.GREEN.toString() + ps + " 人中 " + Utils.getCP(fps, ps) +
                                ChatColor.GREEN + " 人がクリアしました！(" + ((int) (((double) fps / (double) ps) * 100)) + "%)"));
                    });
        }

        private void initPhase(int i)
        {
            phaseStaging = phases.get(i);
            this.interval = phaseStaging.getTimeWait();
            this.itemName = Utils.getItemName(phaseStaging.targetMaterial);

            ItemStack example = new ItemStack(phaseStaging.targetMaterial);
            ItemMeta meta = example.getItemMeta();
            meta.displayName(Component.text("お題"));
            example.setItemMeta(meta);

            players.stream().parallel()
                    .forEach(uuid -> {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null)
                            return;
                        player.sendMessage(Component.text(ChatColor.YELLOW + "お題：" + ChatColor.RED + itemName));

                        if (!flags.contains(Flag.NO_EXAMPLE))
                            player.getInventory().setItem(8, example);
                    });

            indicator.setProgress((currentPhase + 1) / (double) phases.size());
            indicator.setTitle(ChatColor.GREEN.toString() + (currentPhase + 1) + " 問目( " + phases.size() + " )問中");
        }
    }
}
