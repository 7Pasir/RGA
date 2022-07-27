package rosegoldaddons.features;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import rosegoldaddons.Main;
import rosegoldaddons.events.RenderLivingEntityEvent;
import rosegoldaddons.utils.RenderUtils;
import rosegoldaddons.utils.ScoreboardUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DungeonESP {
    private static HashMap<Entity, Color> highlightedEntities = new HashMap<>();
    private static HashSet<Entity> checkedStarNameTags = new HashSet<>();
    private int ticks = 0;

    private static void highlightEntity(Entity entity, Color color) {
        highlightedEntities.put(entity, color);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!ScoreboardUtils.inDungeon || !Main.configFile.dungeonESP) return;
        if (event.entity instanceof EntityPlayer) {
            String name = event.entity.getName();
            switch (name) {
                case "Shadow Assassin":
                    event.entity.setInvisible(false);
                    highlightEntity(event.entity, Color.MAGENTA);
                    break;

                case "Lost Adventurer":
                    highlightEntity(event.entity, Color.BLUE);
                    break;

                case "Diamond Guy":
                    highlightEntity(event.entity, Color.CYAN);
                    break;
            }
        }
        if (event.entity instanceof EntityBat) {
            highlightEntity(event.entity, Color.RED);
        }
    }

    @SubscribeEvent
    public void onRenderEntityLiving(RenderLivingEntityEvent event) {
        if (!ScoreboardUtils.inDungeon || !Main.configFile.dungeonESP || checkedStarNameTags.contains(event.entity)) return;
        if (event.entity instanceof EntityArmorStand) {
            if (event.entity.hasCustomName() && (event.entity.getCustomNameTag().contains("✯") || event.entity.getCustomNameTag().contains("__rga"))) {
                List<Entity> possibleEntities = event.entity.getEntityWorld().getEntitiesInAABBexcluding(event.entity, event.entity.getEntityBoundingBox().offset(0, -1, 0), entity -> !(entity instanceof EntityArmorStand));
                if (!possibleEntities.isEmpty()) {
                    highlightEntity(possibleEntities.get(0), Color.ORANGE);
                }
                checkedStarNameTags.add(event.entity);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(ScoreboardUtils.inDungeon && Main.configFile.dungeonESP) {
            Main.mc.theWorld.loadedEntityList.forEach(entity -> {
                if(highlightedEntities.containsKey(entity)) {
                    RenderUtils.drawEntityBox(entity, highlightedEntities.get(entity), Main.configFile.lineWidth, event.partialTicks);
                }
            });
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(ticks % 40 == 0) {
            checkedStarNameTags.clear();
            ticks = 0;
        }
        ticks++;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        highlightedEntities.clear();
        checkedStarNameTags.clear();
    }
}
