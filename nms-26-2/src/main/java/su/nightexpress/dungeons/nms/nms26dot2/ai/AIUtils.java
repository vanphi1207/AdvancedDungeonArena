package su.nightexpress.dungeons.nms.nms26dot2.ai;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.api.type.MobFaction;

@NullMarked
public final class AIUtils {

    private static final int    MAX_TICKS_TO_AUTO_ATTACK = 50;
    private static final double SEE_DISTANCE             = 32D;
    private static final double ZERO_VISIBILITY          = 0D;

    private static final TargetingConditions IGNORE_LOS = TargetingConditions
        .forCombat()
        .ignoreLineOfSight();

    public static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private AIUtils() {
    }

    public static boolean isDamagedBySomeone(LivingEntity entity) {
        return entity.tickCount - entity.getLastHurtByMobTimestamp() <= MAX_TICKS_TO_AUTO_ATTACK && entity
            .getLastHurtByMob() != entity;
    }

    public static boolean isAttackedSomeone(LivingEntity entity) {
        return entity.tickCount - entity.getLastHurtMobTimestamp() <= MAX_TICKS_TO_AUTO_ATTACK && entity
            .getLastHurtMob() != entity;
    }

    public static Optional<? extends LivingEntity> findAngerOrAttackerTarget(ServerLevel level, Mob mob) {
        if (!(mob instanceof DungeonHolder dungeonHolder)) return Optional.empty();

        LivingEntity target = getNearestTarget(mob, dungeonHolder.getFaction(), dungeonHolder.getDungeon());

        if (target == null) {
            return Optional.empty();
        }

        return Optional.of(target);
    }

    public static Set<LivingEntity> getTargetList(LivingEntity mob, MobFaction faction, Dungeon dungeon) {
        Set<org.bukkit.entity.LivingEntity> bukkitEntities = new HashSet<>();
        if (faction == MobFaction.ENEMY) {
            dungeon.getAlivePlayers().forEach(arenaPlayer -> bukkitEntities.add(arenaPlayer.getPlayer()));
            dungeon.getAllyMobs().forEach(dungeonEntity -> bukkitEntities.add(dungeonEntity.getBukkitEntity()));
        }
        else if (faction == MobFaction.ALLY) {
            dungeon.getEnemyMobs().forEach(dungeonEntity -> bukkitEntities.add(dungeonEntity.getBukkitEntity()));
        }

        Set<LivingEntity> targetList = new HashSet<>();
        for (org.bukkit.entity.LivingEntity bukkitEntity : bukkitEntities) {
            LivingEntity entity = ((CraftLivingEntity) bukkitEntity).getHandle();
            targetList.add(entity);
        }

        return targetList;
    }

    public static @Nullable LivingEntity getNearestTarget(LivingEntity mob, MobFaction faction, Dungeon dungeon) {
        return getNearestTarget(mob, getTargetList(mob, faction, dungeon));
    }

    public static @Nullable LivingEntity getNearestTarget(LivingEntity mob, Set<LivingEntity> entities) {
        //double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        //AABB searchArea = mob.getBoundingBox().inflate(followRange, 4D, followRange);

        double bestDistance = -1D;
        LivingEntity target = null;

        for (LivingEntity entity : entities) {
            if (!IGNORE_LOS.test((ServerLevel) mob.level(), mob, entity)) continue;
            //if (!searchArea.contains(entity.getX(), entity.getY(), entity.getZ())) continue;

            double distance = entity.distanceToSqr(mob);

            // Custom invisibility test, bc otherwise TargetConditions will fail for low range value.
            double visibilityPercent = entity.getVisibilityPercent(mob);
            if (visibilityPercent < 1D && entity.isInvisible()) {
                double seeDistance = SEE_DISTANCE * SEE_DISTANCE;
                double threshold = distance / seeDistance;
                double visibility = visibilityPercent - threshold;
                if (visibility <= ZERO_VISIBILITY) continue;
            }

            if (bestDistance == -1D || distance < bestDistance) {
                bestDistance = distance;
                target = entity;
            }
        }

        return target;
    }

    public static Optional<LivingEntity> getAttackTarget(Mob pet) {
        return pet.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
    }

    public static boolean hasAttackTarget(Mob mob) {
        return getAttackTarget(mob).isPresent();
    }

    // Reimplemention of the Entity#tunderHit method to cancel lightning effects and apply damage for certain mobs like
    // Mooshroms, Pigs.
    public static void thunderHit(LivingEntity entity, ServerLevel level, LightningBolt bolt) {
        entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        org.bukkit.entity.Entity bukkitStorm = bolt.getBukkitEntity();
        if (entity.getRemainingFireTicks() == 0) {
            EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(bukkitStorm, bukkitEntity,
                8.0F);
            Bukkit.getPluginManager().callEvent(entityCombustEvent);
            if (!entityCombustEvent.isCancelled()) {
                entity.igniteForSeconds(entityCombustEvent.getDuration(), false);
            }
        }

        if (!entity.fireImmune()) {
            entity.hurtServer(level, entity.damageSources().lightningBolt().eventEntityDamager(bolt), 5.0F);
        }
    }

    // Simulate natural mob damage, but without the actual damager to prevent certain AI goals from triggering.
    public static boolean hurt(LivingEntity pet,
                               DamageSource original,
                               Function<DamageSource, Boolean> hurtServer) {
        DamageSource fixed = new DamageSource(original.typeHolder(), original.getDirectEntity(), null, original
            .sourcePositionRaw());

        boolean flag = hurtServer.apply(fixed);

        if (original.getEntity() instanceof LivingEntity mob) {
            pet.setLastHurtByMob(mob);
        }
        if (original.getEntity() instanceof ServerPlayer player) {
            pet.setLastHurtByPlayer(player, LivingEntity.PLAYER_HURT_EXPERIENCE_TIME);
        }
        pet.handleDamageEvent(original);

        return flag;
    }
}
