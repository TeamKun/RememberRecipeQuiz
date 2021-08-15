package net.kunmc.lab.rememberrecipequiz;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Game
{
    private final List<UUID> players;
    private final GameTimer game;
    private final List<Phase> phases;
    private boolean start;
    private final ProtocolManager protocol;
    private final GameLogic logic;
    private int currentPhase;

    //=========ここからゲーム用変数
    private final BossBar indicator;
    private final List<UUID> finishedPlayers;
    private final List<UUID> eliminatedPlayers;
    private Phase phaseStaging;
    private final List<Flag> flags;

    public Game()
    {
        this.players = new ArrayList<>();
        this.phases = new ArrayList<>();
        this.protocol = ProtocolLibrary.getProtocolManager();
        this.start = false;
        this.game = new GameTimer();
        this.currentPhase = -1;
        this.logic = new GameLogic();

        //初期化
        this.indicator = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.indicator.setVisible(false);
        this.finishedPlayers = new ArrayList<>();
        this.eliminatedPlayers = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.phaseStaging = null;

        this.protocol.addPacketListener(new CraftPacketListener());
    }

    public boolean isStarted()
    {
        return start;
    }

    public void actuallyStart()
    {
        broadcastMessage(ChatColor.GREEN + "ゲームがスタートしました！");
        broadcastTitle(ChatColor.GREEN + "スタート！", "");
        this.indicator.setVisible(true);
        this.start = true;
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
    }

    public void removePlayer(Player player)
    {
        this.players.remove(player.getUniqueId());
        this.finishedPlayers.remove(player.getUniqueId());
        this.indicator.removePlayer(player);
        player.sendMessage(ChatColor.RED + "レシピクイズから退出しました。");

    }

    public void addPhase(Phase phase)
    {
        broadcastMessage(ChatColor.GREEN + "お題が追加されました！");
        this.phases.add(phase);
    }

    private void broadcastMessage(String message)
    {
        Bukkit.broadcast(Component.text(message), "req.play");
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
        public void onCraftComplete(CraftItemEvent e)
        {
            Player player = (Player) e.getWhoClicked();
            if(e.getRecipe().getResult().getType() != phaseStaging.getTargetMaterial())
                if (flags.contains(Flag.ONLY_ONCE_SUBMIT))
                {
                    player.setHealth(0.0);
                    broadcastMessage(player.getName() + " はクラフトを間違えて失格になった。");
                    eliminatedPlayers.add(player.getUniqueId());
                    return;
                }
                else
                    return;

            Utils.playPingPongSound(player);
            Utils.launchFireworks(player);
            broadcastMessage(ChatColor.GREEN + player.getName() + " はお題のクラフトに成功しました！");
            finishedPlayers.add(player.getUniqueId());
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
            UUID id = e.getPlayer().getUniqueId();
            if (players.contains(id) && !finishedPlayers.contains(id))
            {
                e.getPlayer().setHealth(0.0);
                broadcastMessage(e.getPlayer().getName() + " はカンニングをしようとしたため失格になった。");
                eliminatedPlayers.add(id);
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
        REVIVE_IN_NEXT_PHASE("revive_next_phase");
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
                    eliminatedPlayers.stream().parallel()
                            .forEach(uuid -> {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player == null)
                                    return;

                                if (player.isDead())
                                    player.spigot().respawn();
                                player.setGameMode(GameMode.CREATIVE);
                                player.sendMessage(ChatColor.GREEN + "復活モードが有効のため、復活しました！");
                            });
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

        private void processPhaseEnd()
        {
            int ps = players.size() - eliminatedPlayers.size();
            int fps = finishedPlayers.size();
            broadcastTitle(
                    ChatColor.RED + "終了！",
                    Utils.getCP(fps, ps) + ChatColor.GREEN + " 人がクリアしました！(" +
                            ((int) (((double) fps / (double) ps) * 100)) + "%)"
            );

            int count = 0;
            for (UUID uuid : players)
            {
                if (finishedPlayers.contains(uuid) || eliminatedPlayers.contains(uuid))
                    continue;
                Player player = Bukkit.getPlayer(uuid);
                if (player == null)
                    continue;
                count++;
                player.setHealth(0);
                broadcastMessage(player.getName() + " はレシピを覚えていられなかった。");
            }

            String msg = "==========\n" +
                    "お題：" + itemName + "\n" +
                    "失格人数：" + count + "\n";


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
                        int ps = players.size();
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
            broadcastMessage(ChatColor.YELLOW + "お題：" + ChatColor.RED + itemName);

            indicator.setProgress((currentPhase + 1) / (double) phases.size());
        }
    }
}
