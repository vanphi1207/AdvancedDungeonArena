package su.nightexpress.dungeons.nms.nms26dot2.ai.activity;

import java.util.Optional;

import org.jspecify.annotations.NullMarked;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

@NullMarked
public final class CoreActivities {

    private CoreActivities() {
    }

    public static Behavior<Mob> lookAtLookTarget() {
        return new LookAtTargetSink(45, 90);
    }

    public static Behavior<Mob> tryReachWalkTargetOrAbandon() {
        return new MoveToTargetSink();
    }

    public static Behavior<Mob> swim() {
        return swim(0.8F);
    }

    public static Behavior<Mob> swim(float chance) {
        return new Swim<>(chance);
    }

    public static BehaviorControl<LivingEntity> stopAngerIfTargetDead() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ANGRY_AT))
            .apply(instance, angryAt -> (level, body, timestamp) -> {
                Optional.ofNullable(level.getEntity(instance.get(angryAt)))
                    .map(entity -> entity instanceof LivingEntity livingEntity ? livingEntity : null)
                    .filter(LivingEntity::isDeadOrDying)
                    .ifPresent(angerTarget -> angryAt.erase());
                return true;
            })
        );
    }
}
