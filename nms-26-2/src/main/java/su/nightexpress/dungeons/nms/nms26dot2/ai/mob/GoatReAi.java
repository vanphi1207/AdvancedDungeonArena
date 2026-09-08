package su.nightexpress.dungeons.nms.nms26dot2.ai.mob;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import com.google.common.collect.ImmutableList;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.schedule.Activity;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.ActivityBuilder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.CoreActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.FightActivities;
import su.nightexpress.dungeons.nms.nms26dot2.ai.activity.IdleActivities;

public final class GoatReAi {

    private static final UniformInt          TIME_BETWEEN_RAMS          = UniformInt.of(100, 300);
    private static final UniformInt          TIME_BETWEEN_RAMS_SCREAMER = UniformInt.of(100, 300);
    private static final TargetingConditions RAM_TARGET_CONDITIONS      = TargetingConditions.forCombat();

    private GoatReAi() {
    }

    public static List<ActivityData<Goat>> initActivities() {
        return List.of(initCoreActivity(), initIdleActivity(), initFightActivity(), initRamActivity());
    }

    public static void updateActivity(Goat goat) {
        goat.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.RAM, Activity.FIGHT, Activity.IDLE));
    }

    public static BehaviorControl<Goat> ram() {
        Function<Goat, UniformInt> getTimeBetweenRams = goat -> goat
            .isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER : TIME_BETWEEN_RAMS;

        float speed = 3F;

        ToDoubleFunction<Goat> getKnockbackForce = goat -> goat.isBaby() ? 1.0 : 2.5;

        Function<Goat, SoundEvent> getImpactSound = goat -> goat
            .isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_RAM_IMPACT : SoundEvents.GOAT_RAM_IMPACT;

        Function<Goat, SoundEvent> getHornBreakSound = goat -> SoundEvents.GOAT_HORN_BREAK;

        return new RamTarget(getTimeBetweenRams, RAM_TARGET_CONDITIONS, speed, getKnockbackForce, getImpactSound,
            getHornBreakSound);
    }

    public static BehaviorControl<Goat> prepareRam() {
        ToIntFunction<Goat> failCooldown = mob -> mob.isScreamingGoat() ? TIME_BETWEEN_RAMS_SCREAMER
            .minInclusive() : TIME_BETWEEN_RAMS.minInclusive();
        int minDist = 4;
        int maxDist = 7;
        float walkSpeed = 1.25F;
        int prepareTime = 20;
        Function<Goat, SoundEvent> sound = goat -> goat
            .isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM;

        return new PrepareRamNearestTarget<>(failCooldown, minDist, maxDist, walkSpeed, RAM_TARGET_CONDITIONS,
            prepareTime, sound);
    }

    private static ActivityData<Goat> initCoreActivity() {
        return ActivityBuilder.init(Activity.CORE, Goat.class)
            .goal(CoreActivities.swim())
            .goal(CoreActivities.lookAtLookTarget())
            .goal(CoreActivities.tryReachWalkTargetOrAbandon())
            .goal(CoreActivities.stopAngerIfTargetDead())
            .goal(new CountDownCooldownTicks(MemoryModuleType.RAM_COOLDOWN_TICKS))
            .build();
    }

    private static ActivityData<Goat> initIdleActivity() {
        return ActivityBuilder.init(Activity.IDLE, Goat.class)
            .goalWithPriority(0, IdleActivities.startAttackingNearestValidTarget())
            .goalWithPriority(3, IdleActivities.createIdleMovementBehaviors())
            .condition(MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT)
            .build();
    }

    private static ActivityData<Goat> initFightActivity() {
        return ActivityBuilder.init(Activity.FIGHT, Goat.class)
            .goalWithPriority(0, FightActivities.walkAttackTargetIfOutOfRange())
            .goalWithPriority(1, FightActivities.meleeAttack())
            .goalWithPriority(2, FightActivities.stopAttackingIfTargetInvalid())
            .condition(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
            .build();
    }

    private static ActivityData<Goat> initRamActivity() {
        return ActivityBuilder.init(Activity.RAM, Goat.class)
            .goalWithPriority(0, ram())
            .goalWithPriority(1, prepareRam())
            .condition(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)
            .build();
    }
}
