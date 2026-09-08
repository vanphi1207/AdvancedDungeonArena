package su.nightexpress.dungeons.nms.nms26dot2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.AllayMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.CreakingMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.GoatMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.HoglinMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.IronGolemMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.MagmaCubeMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.PiglinBruteMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.PiglinMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.SlimeMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.WardenMob;
import su.nightexpress.dungeons.nms.nms26dot2.mob.ZoglinMob;

@NullMarked
public final class EntityCreator {

    private static final Set<EntityType>             VANILLA_SUPPORTED = new HashSet<>();
    private static final Map<EntityType, Creator<?>> CUSTOMS           = new HashMap<>();

    private EntityCreator() {
    }

    static {
        VANILLA_SUPPORTED.add(EntityType.BEE);
        VANILLA_SUPPORTED.add(EntityType.BLAZE);
        VANILLA_SUPPORTED.add(EntityType.BOGGED);
        VANILLA_SUPPORTED.add(EntityType.BREEZE);
        VANILLA_SUPPORTED.add(EntityType.CAT);
        VANILLA_SUPPORTED.add(EntityType.CAVE_SPIDER);
        VANILLA_SUPPORTED.add(EntityType.COW);
        VANILLA_SUPPORTED.add(EntityType.CHICKEN);
        VANILLA_SUPPORTED.add(EntityType.CREEPER);
        VANILLA_SUPPORTED.add(EntityType.DONKEY);
        VANILLA_SUPPORTED.add(EntityType.DROWNED);
        VANILLA_SUPPORTED.add(EntityType.ENDERMAN);
        VANILLA_SUPPORTED.add(EntityType.ENDERMITE);
        VANILLA_SUPPORTED.add(EntityType.EVOKER);
        VANILLA_SUPPORTED.add(EntityType.FOX);
        VANILLA_SUPPORTED.add(EntityType.GHAST);
        VANILLA_SUPPORTED.add(EntityType.GUARDIAN);
        VANILLA_SUPPORTED.add(EntityType.HORSE);
        VANILLA_SUPPORTED.add(EntityType.HUSK);
        VANILLA_SUPPORTED.add(EntityType.ILLUSIONER);
        VANILLA_SUPPORTED.add(EntityType.LLAMA);
        VANILLA_SUPPORTED.add(EntityType.MULE);
        VANILLA_SUPPORTED.add(EntityType.MOOSHROOM);
        VANILLA_SUPPORTED.add(EntityType.OCELOT);
        VANILLA_SUPPORTED.add(EntityType.PANDA);
        VANILLA_SUPPORTED.add(EntityType.PIG);
        VANILLA_SUPPORTED.add(EntityType.PILLAGER);
        VANILLA_SUPPORTED.add(EntityType.PHANTOM);
        VANILLA_SUPPORTED.add(EntityType.POLAR_BEAR);
        VANILLA_SUPPORTED.add(EntityType.RABBIT);
        VANILLA_SUPPORTED.add(EntityType.RAVAGER);
        VANILLA_SUPPORTED.add(EntityType.SHEEP);
        VANILLA_SUPPORTED.add(EntityType.SHULKER);
        VANILLA_SUPPORTED.add(EntityType.SILVERFISH);
        VANILLA_SUPPORTED.add(EntityType.STRAY);
        VANILLA_SUPPORTED.add(EntityType.SKELETON);
        VANILLA_SUPPORTED.add(EntityType.SKELETON_HORSE);
        VANILLA_SUPPORTED.add(EntityType.SPIDER);
        VANILLA_SUPPORTED.add(EntityType.SNOW_GOLEM);
        VANILLA_SUPPORTED.add(EntityType.TURTLE);
        VANILLA_SUPPORTED.add(EntityType.TRADER_LLAMA);
        VANILLA_SUPPORTED.add(EntityType.VEX);
        VANILLA_SUPPORTED.add(EntityType.VILLAGER);
        VANILLA_SUPPORTED.add(EntityType.VINDICATOR);
        VANILLA_SUPPORTED.add(EntityType.WANDERING_TRADER);
        VANILLA_SUPPORTED.add(EntityType.WITCH);
        VANILLA_SUPPORTED.add(EntityType.WITHER_SKELETON);
        VANILLA_SUPPORTED.add(EntityType.WITHER);
        VANILLA_SUPPORTED.add(EntityType.WOLF);
        VANILLA_SUPPORTED.add(EntityType.ZOMBIE);
        VANILLA_SUPPORTED.add(EntityType.ZOMBIE_HORSE);
        VANILLA_SUPPORTED.add(EntityType.ZOMBIE_VILLAGER);
        VANILLA_SUPPORTED.add(EntityType.ZOMBIFIED_PIGLIN);

        addCreator(EntityType.CREAKING, CreakingMob::new);
        addCreator(EntityType.PIGLIN, PiglinMob::new);
        addCreator(EntityType.PIGLIN_BRUTE, PiglinBruteMob::new);
        addCreator(EntityType.HOGLIN, HoglinMob::new);
        addCreator(EntityType.ZOGLIN, ZoglinMob::new);
        addCreator(EntityType.GOAT, GoatMob::new);
        addCreator(EntityType.ALLAY, AllayMob::new);
        addCreator(EntityType.WARDEN, WardenMob::new);
        addCreator(EntityType.SLIME, SlimeMob::new);
        addCreator(EntityType.MAGMA_CUBE, MagmaCubeMob::new);
        addCreator(EntityType.IRON_GOLEM, IronGolemMob::new);
    }

    public interface Creator<T extends Mob> {


        T create(ServerLevel level/*,  Dungeon dungeon,  MobFaction faction*/);
    }

    private static <T extends Mob> void addCreator(EntityType type, Creator<T> creator) {
        CUSTOMS.put(type, creator);
    }

    public static boolean isCustom(EntityType type) {
        return CUSTOMS.containsKey(type);
    }

    public static boolean isVanilla(EntityType type) {
        return VANILLA_SUPPORTED.contains(type);
    }

    public static boolean isSupported(EntityType type) {
        return isCustom(type) || isVanilla(type);
    }

    public static @Nullable Mob createEntity(EntityType type, ServerLevel level) {
        if (VANILLA_SUPPORTED.contains(type)) {
            var nmsType = CraftEntityType.bukkitToMinecraft(type);
            return (Mob) nmsType.create(level, EntitySpawnReason.EVENT);
        }

        Creator<?> creator = CUSTOMS.get(type);
        if (creator == null) return null;

        return creator.create(level);
    }
}
