package su.nightexpress.dungeons.nms.nms26dot2.ai.mob;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import su.nightexpress.dungeons.nms.nms26dot2.ai.BrainUtils;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.ActivityBuilder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.CoreActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.FightActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.IdleActivities;

public final class GenericAi {

    private GenericAi() {
    }

    public static <E extends PathfinderMob> Brain.Provider<E> brainProvider(Class<E> type) {
        return BrainUtils.brainProvider(pet -> initActivities(type));
    }

    public static void updateActivity(PathfinderMob pet) {
        pet.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        pet.setAggressive(pet.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    public static <E extends PathfinderMob> List<ActivityData<E>> initActivities(Class<E> type) {
        return List.of(initCoreActivity(type), initIdleActivity(type), initFightActivity(type));
    }

    private static <E extends PathfinderMob> ActivityData<E> initCoreActivity(Class<E> type) {
        return ActivityBuilder.init(Activity.CORE, type)
            .goal(CoreActivities.swim())
            .goal(CoreActivities.lookAtLookTarget())
            .goal(CoreActivities.tryReachWalkTargetOrAbandon())
            .goal(CoreActivities.stopAngerIfTargetDead())
            .build();
    }

    private static <E extends PathfinderMob> ActivityData<E> initIdleActivity(Class<E> type) {
        return ActivityBuilder.init(Activity.IDLE, type)
            .priorityOfFirstBehavior(10)
            .goal(IdleActivities.startAttackingNearestValidTarget())
            .goal(IdleActivities.createIdleMovementBehaviors())
            .build();
    }

    private static <E extends PathfinderMob> ActivityData<E> initFightActivity(Class<E> type) {
        return ActivityBuilder.init(Activity.FIGHT, type)
            .priorityOfFirstBehavior(10)
            .memoryThatMustHaveValueAndWillBeErasedAfter(MemoryModuleType.ATTACK_TARGET)
            .goal(FightActivities.walkAttackTargetIfOutOfRange())
            .goal(FightActivities.meleeAttack())
            .goal(FightActivities.stopAttackingIfTargetInvalid())
            .build();
    }
}
