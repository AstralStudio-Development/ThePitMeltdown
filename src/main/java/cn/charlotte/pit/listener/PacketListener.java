package cn.charlotte.pit.listener;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PotionAddEvent;
import cn.charlotte.pit.events.impl.major.RedVSBlueEvent;
import cn.charlotte.pit.util.item.ItemBuilder;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.logging.Level;

/**
 * @Author: EmptyIrony
 * @Date: 2021/2/4 14:56
 */
public class PacketListener extends PacketAdapter {

    // --- 缓存红蓝对抗事件状态 --- 
    private volatile boolean isRedVsBlueActive = false;
    private volatile RedVSBlueEvent currentRedVsBlueEvent = null; // 缓存事件实例
    private ItemStack redWoolNms = null;
    private ItemStack blueWoolNms = null;
    // ---------------------------

    public PacketListener() {
        super(ThePit.getInstance(),
                PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.ENTITY_EFFECT);
        // 初始化时创建 NMS 物品 (在主线程进行)
        createWoolItems();
    }

    // Helper to create NMS items on the main thread
    private void createWoolItems() {
         Bukkit.getScheduler().runTask(ThePit.getInstance(), () -> {
             try {
                 this.redWoolNms = CraftItemStack.asNMSCopy(new ItemBuilder(Material.WOOL).durability(14).build());
                 this.blueWoolNms = CraftItemStack.asNMSCopy(new ItemBuilder(Material.WOOL).durability(11).build());
             } catch (Exception e) {
                 ThePit.getInstance().getLogger().log(Level.SEVERE, "[PacketListener] Failed to create NMS wool items", e);
             }
         });
    }

    /**
     * 由外部事件调用，更新红蓝对抗状态
     * @param isActive 是否激活
     * @param eventInstance 事件实例 (激活时提供，否则为 null)
     */
    public void updateRedVsBlueStatus(boolean isActive, RedVSBlueEvent eventInstance) {
        this.isRedVsBlueActive = isActive;
        this.currentRedVsBlueEvent = isActive ? eventInstance : null;
        ThePit.getInstance().getLogger().info("[PacketListener] RedVsBlue status updated: active = " + isActive);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final Player receivingPlayer = event.getPlayer();
        final PacketContainer container = event.getPacket();
        final PacketType packetType = event.getPacketType();

        try {
            if (packetType == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                if (isRedVsBlueActive && currentRedVsBlueEvent != null && redWoolNms != null && blueWoolNms != null) {
                    final int entityId = container.getIntegers().read(0);
                    final EnumWrappers.ItemSlot slot = container.getItemSlots().read(0);

                    if (slot == EnumWrappers.ItemSlot.HEAD) {
                        // 使用 NMS 获取实体
                        WorldServer world = ((CraftWorld) receivingPlayer.getWorld()).getHandle();
                        Entity targetEntity = world.a(entityId); // 根据 ID 获取 NMS Entity

                        if (targetEntity instanceof EntityPlayer) {
                            // 获取目标玩家的 UUID
                            UUID targetUUID = targetEntity.getUniqueID();
                            ItemStack itemToSendNms = null;

                            if (currentRedVsBlueEvent.getRedTeam().contains(targetUUID)) {
                                itemToSendNms = redWoolNms;
                            } else if (currentRedVsBlueEvent.getBlueTeam().contains(targetUUID)) {
                                itemToSendNms = blueWoolNms;
                            }

                            if (itemToSendNms != null) {
                                // 将 NMS ItemStack 转为 Bukkit ItemStack 再写入
                                org.bukkit.inventory.ItemStack itemToSendBukkit = CraftItemStack.asBukkitCopy(itemToSendNms);
                                container.getItemModifier().write(0, itemToSendBukkit);
                            }
                        }
                    }
                }

            } else if (packetType == PacketType.Play.Server.ENTITY_EFFECT) {
                final int entityId = container.getIntegers().read(0);
                // 只处理发给玩家自己的效果包
                if (receivingPlayer.getEntityId() != entityId) return;

                final PotionEffectType potion = PotionEffectType.getById(container.getBytes().read(0));
                final byte level = container.getBytes().read(1); // 读取为 byte
                final int duration = container.getIntegers().read(1); // 读取为 int
                final boolean hide = container.getBytes().read(2) == 1;

                // PotionEffect 构造函数的 level 是 0-based amplifier
                int amplifier = level & 0xFF; // 将 byte 转为无符号整数 (0-255)

                PotionAddEvent potionEvent = new PotionAddEvent(receivingPlayer, new PotionEffect(potion, duration, amplifier, false, !hide)); // hide 对应 ambient, !hide 对应 particles?
                potionEvent.callEvent();

                if (potionEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
             ThePit.getInstance().getLogger().log(Level.SEVERE, "[PacketListener] Error processing packet " + packetType.name(), e);
        }
    }
}
