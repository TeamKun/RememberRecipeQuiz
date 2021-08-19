package net.kunmc.lab.rememberrecipequiz.commands;

import net.kunmc.lab.rememberrecipequiz.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHelp
{
    public static void run(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.AQUA + "=====レシピ覚えてるかクイズ=====");
        sender.sendMessage(ChatColor.GREEN + "  /req help    このコマンドです。");
        sender.sendMessage(ChatColor.GREEN + "  /req add <シンキング秒数:int>    手に持っているアイテムをお題に追加します。");
        sender.sendMessage(ChatColor.GREEN + "  /req random [追加数:int]\n    " +
                "ランダムのお題を指定数追加します(デフォルト：10問)。シンキング秒数も指定できます(デフォルト：60秒)。");
        sender.sendMessage(ChatColor.GREEN + "  /req start\n    " +
                "ゲームをスタートします。");
        sender.sendMessage(ChatColor.GREEN + "  /req stop\n    " +
                "ゲームをストップします。");
        sender.sendMessage(ChatColor.GREEN + "  /req thinktime [シンキング秒数:int] \n    " +
                "シンキングタイムをセットします。");

        sender.sendMessage(ChatColor.GREEN + "  /req flag <" + String.join("|", Game.Flag.names()) + ">" +
                " <on|off>\n    " +
                "フラグをセットします。");
        sender.sendMessage(ChatColor.GREEN + "  /req clear \n    お題をすべてクリアします。");
        sender.sendMessage(ChatColor.GREEN + "  /req phasetime [シンキング秒数:int] \n    " +
                "今進行しているお題のシンキングタイムを変更します。");
    }
}
