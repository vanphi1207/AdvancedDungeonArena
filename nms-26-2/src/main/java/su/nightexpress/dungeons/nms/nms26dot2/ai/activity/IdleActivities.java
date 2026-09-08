package su.nightexpress.dungeons.nms.nms26dot2.ai.activity;

import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.NullMarked;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import su.nightexpress.dungeons.nms.nms26dot2.ai.AIUtils;

@NullMarked
public final class IdleActivities {

    private IdleActivities() {
    }

    public static RunOne<PathfinderMob> createIdleMovementBehaviors() {
        return new RunOne<>(
            List.of(
                Pair.of(RandomStroll.stroll(0.4F, 1, 1), 2),
                Pair.of(setLookTargetFromNearestEntities(entity -> true), 3),
                Pair.of(new DoNothing(30, 60), 6)
            )
        );
    }

    public static BehaviorControl<LivingEntity> setLookTargetFromNearestEntities(Predicate<LivingEntity> predicate) {
        return BehaviorBuilder.create(
            instance -> instance.group(
                instance.absent(MemoryModuleType.LOOK_TARGET),
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                .apply(instance, (lookTarget, nearestEntities) -> (level, body, timestamp) -> {
                    float maxDist = (float) body.getAttributeValue(Attributes.FOLLOW_RANGE) / 2F;
                    float maxDistSqr = maxDist * maxDist;

                    LivingEntity target = instance.get(nearestEntities)
                        .find(predicate.and(mob -> mob.distanceToSqr(body) <= maxDistSqr))
                        .findFirst()
                        .orElse(null);
                    if (target == null) return false;

                    lookTarget.set(new EntityTracker(target, true));
                    return true;
                })
        );
    }

    /**
     * Sets pet's MemoryModuleType.ATTACK_TARGET value when pet has ANGRY_AT memory value.
     * 
     * @return
     */
    public static BehaviorControl<Mob> startAttackingNearestValidTarget() {
        return StartAttacking.create(AIUtils::findAngerOrAttackerTarget);
    }
}
