package cn.charlotte.pit.events;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.EventQueue;
import cn.charlotte.pit.util.random.RandomUtil;
import cn.charlotte.pit.util.thread.ThreadHelper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;

// Import IEpicEvent and INormalEvent if they are in the same package
import cn.charlotte.pit.events.IEpicEvent; 
import cn.charlotte.pit.events.INormalEvent;
// Assuming IEvent exists and is accessible
import cn.charlotte.pit.events.IEvent;

/**
 * 处理事件队列管理、加载和检索。
 * 从 Kotlin 对象 EventsHandler 转换而来。
 */
public final class EventsHandler {

    @Getter
    private static final Queue<String> epicQueue = new LinkedList<>();
    @Getter
    private static final Queue<String> normalQueue = new LinkedList<>();
    private static final Random random = new Random();

    /**
     * 如果事件队列低于某个阈值，则刷新它们。
     * 从工厂添加新事件并将更新后的队列保存到数据库。
     */
    public static void refreshEvents() {
        EventFactory factory = ThePit.getInstance().getEventFactory();
        if (factory == null) {
            throw new RuntimeException("EventFactory未初始化 事件加载失败");
        }
        // 根据 EventFactory 定义更正类型
        List<IEpicEvent> epicEventList = factory.getEpicEvents();
        List<INormalEvent> normalEventList = factory.getNormalEvents();

        int count = epicQueue.size();
        if (count < 50 && epicEventList != null && !epicEventList.isEmpty()) {
            int need = 50 - count;
            for (int index = 0; index <= need; index++) {
                IEvent event = (IEvent) epicEventList.get(random.nextInt(epicEventList.size()));
                if (event != null && event.getEventInternalName() != null) {
                     epicQueue.add(event.getEventInternalName());
                }
            }
        }

        count = normalQueue.size();

        if (count < 100 && normalEventList != null && !normalEventList.isEmpty()) {
            int need = 100 - count;
            for (int index = 0; index <= need; index++) {
                 // 强制转换为 IEvent 以访问 getEventInternalName()
                 IEvent event = (IEvent) normalEventList.get(random.nextInt(normalEventList.size()));
                 if (event == null || event.getEventInternalName() == null) continue; // 如果事件或名称为 null 则跳过

                // 使用 equals 进行字符串比较
                if ("auction".equals(event.getEventInternalName()) && RandomUtil.hasSuccessfullyByChance(0.75)) {
                    // 在获取另一个事件之前，确保列表仍然有效且不为空
                    if (!normalEventList.isEmpty()) {
                        // 强制转换为 IEvent
                        IEvent anotherEvent = (IEvent) normalEventList.get(random.nextInt(normalEventList.size()));
                        if (anotherEvent != null && anotherEvent.getEventInternalName() != null) {
                           normalQueue.add(anotherEvent.getEventInternalName());
                        } else {
                           // 如果另一个事件无效，则可选择添加原始事件
                           normalQueue.add(event.getEventInternalName());
                        }
                    } else {
                        // 如果列表变空，则添加原始事件
                        normalQueue.add(event.getEventInternalName());
                    }
                } else {
                    normalQueue.add(event.getEventInternalName());
                }
            }
        }

        // 翻译 Kotlin 的 apply 块
        EventQueue eventQueue = new EventQueue();
        // 直接访问公共字段
        if (eventQueue.normalEvents != null) {
           eventQueue.normalEvents.addAll(normalQueue);
        }
         if (eventQueue.epicEvents != null) {
            eventQueue.epicEvents.addAll(epicQueue);
         }

        // 将 Kotlin lambda 翻译为 Java lambda 以执行异步任务
        ThreadHelper.Async(() -> {
             // 假设 getMongoDB().getEventQueueCollection() 返回正确的 MongoCollection 类型
             // 并适当处理潜在的 null 值。
            try {
                 ThePit.getInstance().getMongoDB().getEventQueueCollection().replaceOne(
                        Filters.eq("id", "1"), // 假设 "id" 是正确的字段名称
                        eventQueue,
                        new ReplaceOptions().upsert(true)
                );
            } catch (Exception e) {
                 // 在异步任务中处理异常至关重要
                 ThePit.getInstance().getLogger().log(Level.SEVERE, "event数据储存失败");
                 e.printStackTrace();
            }
        });
    }

    /**
     * 从 MongoDB 数据库加载事件队列。
     * 如果未找到数据或队列太少，则触发刷新。
     */
    public static void loadFromDatabase() {
        epicQueue.clear();
        normalQueue.clear();

        EventQueue queue;
        try {
           // 假设 findOne() 返回 EventQueue 对象或 null
           queue = ThePit.getInstance().getMongoDB().getEventQueueCollection().findOne();
        } catch (Exception e) {
            System.err.println("Failed to load event queue from MongoDB:");
            e.printStackTrace();
            refreshEvents(); // Attempt to refresh if loading fails
            return;
        }


        if (queue == null) {
            refreshEvents(); // Refresh if no queue data found in DB
            return;
        }

        if (queue.normalEvents != null) {
            normalQueue.addAll(queue.normalEvents);
        }
         if (queue.epicEvents != null) {
            epicQueue.addAll(queue.epicEvents);
         }


        // 翻译 Kotlin 的 try-catch 空块
        // 保留了原始逻辑但添加了注释
        try {
            if (epicQueue.size() < 45 || normalQueue.size() < 90) {
                refreshEvents();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 从指定的队列中检索下一个事件（major 为 true 表示史诗队列，false 表示普通队列）。
     * 在轮询事件后触发刷新。
     *
     * @param major 如果为 true，则从史诗队列轮询；否则，从普通队列轮询。
     * @return 下一个事件的内部名称，如果队列为空，则返回 "No event available"。
     */
    public static String nextEvent(boolean major) {
        // 使用 poll()，它检索并移除头部元素，如果队列为空则返回 null
        String event = major ? epicQueue.poll() : normalQueue.poll();

        // 轮询事件后刷新事件，无论是否检索到事件
        refreshEvents();

        return event != null ? event : "No event available";
    }

    // Note: The definition for the IEvent interface/class is required for this code to compile.
    // It should have a method like:
    // String getEventInternalName();
    // Example:
    /*
    public interface IEvent {
        String getEventInternalName();
        // Other event methods...
    }
    */

    // Similarly, the EventFactory class returned by ThePit.getInstance().getEventFactory()
    // needs methods like:
    // List<IEpicEvent> getEpicEvents();
    // List<INormalEvent> getNormalEvents();

    // And the EventQueue class needs public fields like:
    // List<String> normalEvents;
    // List<String> epicEvents;
} 