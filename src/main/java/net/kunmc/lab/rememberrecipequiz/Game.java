package net.kunmc.lab.rememberrecipequiz;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game
{
    private final List<UUID> players;
    private final GameTimer game;
    private final List<Phase> phases;
    private boolean start;
    private int currentPhase;

    //=========ここからゲーム用変数
    private final boolean isInPhase;
    private final List<UUID> finishedPlayers;

    public Game()
    {
        this.players = new ArrayList<>();
        this.phases = new ArrayList<>();
        this.start = false;
        this.game = new GameTimer();
        this.currentPhase = -1;

        //初期化
        this.isInPhase = false;
        this.finishedPlayers = new ArrayList<>();
    }

    public void actuallyStart()
    {
        broadcastMessage(ChatColor.GREEN + "ゲームがスタートしました！");
        broadcastTitle(ChatColor.GREEN + "スタート！", "");
        this.start = true;
        this.game.runTaskTimer(RememberRecipeQuiz.getPlugin(), 0L, 20L);
    }

    public void startWithCountdown()
    {
        new BukkitRunnable()
        {
            private int time = 0;

            @Override
            public void run()
            {
                if (++time > 5)
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
    }

    public List<UUID> getPlayers()
    {
        return players;
    }

    public void addPlayer(Player player)
    {
        this.players.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "レシピクイズに参加しました！\n開始をお待ち下さい。");
    }

    //===================================ここから下ゲッター・セッター

    public void removePlayer(Player player)
    {
        this.players.remove(player.getUniqueId());
        this.finishedPlayers.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "レシピクイズから退出しました。");

    }

    public void addPhase(Phase phase)
    {
        broadcastMessage(ChatColor.GREEN + "お題が追加されました！");
        this.phases.add(phase);
    }

    private void broadcastMessage(String message)
    {
        Bukkit.broadcast(Component.text(message));
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
        private Phase phaseStaging;
        private String itemName;

        public GameTimer()
        {

            this.interval = -1;
            this.phaseStaging = null;
        }

        @Override
        public void run()
        {
            if (phaseStaging == null)
            {
                if (--interval > 0)
                {
                    if (interval <= 5)
                        broadcastMessage(ChatColor.YELLOW + "次のお題まで... " +
                                ChatColor.RED + interval);
                    return;
                }

                initPhase(++currentPhase);

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
            int ps = players.size();
            int fps = finishedPlayers.size();
            broadcastTitle(
                    ChatColor.RED + "終了！",
                    Utils.getCP(fps, ps) + ChatColor.GREEN + " 人がクリアしました！(" +
                            ((int) (((double) fps / (double) ps) * 100)) + "%)"
            );

            int count = 0;
            for (UUID uuid : players)
            {
                if (finishedPlayers.contains(uuid))
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

            this.phaseStaging = null;
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
            this.phaseStaging = phases.get(i);
            this.interval = phaseStaging.getTimeWait();
            this.itemName = Utils.getItemName(phaseStaging.targetMaterial);
            broadcastMessage(ChatColor.YELLOW + "お題：" + ChatColor.RED + itemName);
        }
    }

}
