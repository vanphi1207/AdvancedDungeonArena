package su.nightexpress.dungeons.nms.nms26dot2.ai.mob;

import java.util.List;

import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.BreezeAi;
import net.minecraft.world.entity.monster.breeze.LongJump;
import net.minecraft.world.entity.monster.breeze.Shoot;
import net.minecraft.world.entity.monster.breeze.ShootWhenStuck;
import net.minecraft.world.entity.monster.breeze.Slide;
import net.minecraft.world.entity.schedule.Activity;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.ActivityBuilder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.CoreActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.FightActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.IdleActivities;

public final class BreezeReAi {

    private BreezeReAi() {
    }

    public static List<ActivityData<Breeze>> initActivities() {
        return List.of(initCoreActivity(), initIdleActivity(), initFightActivity());
    }

    private static ActivityData<Breeze> initCoreActivity() {
        return ActivityBuilder.init(Activity.CORE, Breeze.class)
            .goal(CoreActivities.swim())
            .goal(CoreActivities.lookAtLookTarget())
            //.goal(CoreActivities.tryReachWalkTargetOrAbandon())
            .goal(CoreActivities.stopAngerIfTargetDead())
            .build();
    }

    private static ActivityData<Breeze> initIdleActivity() {
        return ActivityBuilder.init(Activity.IDLE, Breeze.class)
            .goalWithPriority(0, IdleActivities.startAttackingNearestValidTarget())
            .goalWithPriority(1, new BreezeAi.SlideToTargetSink(20, 40))
            .goalWithPriority(4, IdleActivities.createIdleMovementBehaviors())
            .build();
    }

    private static ActivityData<Breeze> initFightActivity() {
        return ActivityBuilder.init(Activity.FIGHT, Breeze.class)
            .goalWithPriority(0, FightActivities.stopAttackingIfTargetInvalid())
            .goalWithPriority(0, new Shoot())
            .goalWithPriority(1, new LongJump())
            .goalWithPriority(2, new ShootWhenStuck())
            .goalWithPriority(3, new Slide())
            .condition(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
            .condition(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)
            .build();
    }
}
