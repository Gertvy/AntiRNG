package dreamimagination.gertvylib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RNGAntiCheat extends JavaPlugin implements Listener {

    private Map<Player, Integer> bookshelfCount = new HashMap<>(); // 保存每个玩家更换书架的次数
    private Map<Player, List<Long>> itemDropTimeList = new HashMap<>(); // 保存每个玩家丢物品的时间戳列表
    private int maxBookshelfCount = 20; // 更换书架的最大次数
    private int maxItemDropCount = 20; // 丢物品的最大次数
    private long maxItemDropInterval = 500L; // 丢物品的最大间隔时间（毫秒）
    private long banTime = 6000000000000L; // 封禁的时间（秒）

    // 插件启动时注册监听器
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }


    // 监听玩家丢物品的事件
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        List<Long> itemDropTime = itemDropTimeList.getOrDefault(player, new ArrayList<Long>());
        long currentTime = System.currentTimeMillis();
        itemDropTime.add(currentTime);
        itemDropTimeList.put(player, itemDropTime);
        if (itemDropTime.size() > maxItemDropCount) {
            long lastDropTime = itemDropTime.get(itemDropTime.size() - 1);
            if (currentTime - lastDropTime < maxItemDropInterval) {
                banPlayer(player);
            }
            itemDropTimeList.remove(player);
        }
    }

    // 监听玩家交换手中物品的事件
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        List<Long> itemDropTime = itemDropTimeList.getOrDefault(player, new ArrayList<Long>());
        itemDropTime.add(System.currentTimeMillis());
        itemDropTimeList.put(player, itemDropTime);
        if (itemDropTime.size() > maxItemDropCount) {
            banPlayer(player);
            itemDropTimeList.remove(player);
        }
    }

    // 监听玩家退出游戏事件
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        bookshelfCount.remove(player);
        itemDropTimeList.remove(player);
    }

    // 封禁玩家
    private void banPlayer(final Player player) {
        player.kickPlayer("你已被封禁 " + banTime + " 秒，你疑似使用了RNG随机数破解器。如有疑问联系服主。");
        String ip = player.getAddress().getAddress().getHostAddress();
        getServer().banIP(ip);
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().unbanIP(ip);
            }
        }.runTaskLater(this, banTime * 200000000L);
    }

}
