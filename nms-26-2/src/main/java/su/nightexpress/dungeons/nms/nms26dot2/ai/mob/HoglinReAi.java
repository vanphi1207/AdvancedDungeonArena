package su.nightexpress.dungeons.nms.nms26dot2.ai.mob;

import java.util.List;

import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.schedule.Activity;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.ActivityBuilder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.CoreActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.FightActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.IdleActivities;

public final class HoglinReAi {

    private HoglinReAi() {
    }

    public static List<ActivityData<Hoglin>> initActivities() {
        return List.of(initCoreActivity(), initIdleActivity(), initFightActivity());
    }

    private static ActivityData<Hoglin> initCoreActivity() {
        return ActivityBuilder.init(Activity.CORE, Hoglin.class)
            .goal(CoreActivities.swim())
            .goal(CoreActivities.lookAtLookTarget())
            .goal(CoreActivities.tryReachWalkTargetOrAbandon())
            .goal(CoreActivities.stopAngerIfTargetDead())
            .build();
    }

    private static ActivityData<Hoglin> initIdleActivity() {
        return ActivityBuilder.init(Activity.IDLE, Hoglin.class)
            .priorityOfFirstBehavior(10)
            .goal(IdleActivities.startAttackingNearestValidTarget())
            .goal(IdleActivities.createIdleMovementBehaviors())
            .build();
    }

    private static ActivityData<Hoglin> initFightActivity() {
        return ActivityBuilder.init(Activity.FIGHT, Hoglin.class)
            .priorityOfFirstBehavior(10)
            .memoryThatMustHaveValueAndWillBeErasedAfter(MemoryModuleType.ATTACK_TARGET)
            .goal(FightActivities.walkAttackTargetIfOutOfRange())
            .goal(FightActivities.meleeAttack())
            .goal(FightActivities.stopAttackingIfTargetInvalid())
            .build();
    }
}
