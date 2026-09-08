package su.nightexpress.dungeons.nms.nms26dot2.ai.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

@NullMarked
public class ActivityBuilder<E extends LivingEntity> {

    private final Activity                                        activity;
    private final List<BehaviorControl<? super E>>                goals;
    private final List<Pair<Integer, BehaviorControl<? super E>>> goalsByPriority;
    private final Set<Pair<MemoryModuleType<?>, MemoryStatus>>    conditions;

    private int                 priorityOfFirstBehavior;
    @Nullable
    private MemoryModuleType<?> memoryThatMustHaveValueAndWillBeErasedAfter;

    public ActivityBuilder(Activity activity) {
        this.activity = activity;
        this.goals = new ArrayList<>();
        this.goalsByPriority = new ArrayList<>();
        this.conditions = new HashSet<>();

        this.priorityOfFirstBehavior = 0;
    }

    public static <E extends LivingEntity> ActivityBuilder<E> init(Activity activity, Class<E> type) {
        return new ActivityBuilder<>(activity);
    }

    public ActivityData<E> build() {
        MemoryModuleType<?> memoryCondition = this.memoryThatMustHaveValueAndWillBeErasedAfter;
        ImmutableList<? extends BehaviorControl<? super E>> goals = ImmutableList.copyOf(this.goals);

        if (memoryCondition != null) {
            return ActivityData.create(this.activity, this.priorityOfFirstBehavior, goals, memoryCondition);
        }

        if (this.goals.isEmpty()) {
            return ActivityData.create(this.activity, ImmutableList.copyOf(this.goalsByPriority), this.conditions);
        }
        else {
            return ActivityData.create(this.activity, this.priorityOfFirstBehavior, goals, this.conditions);
        }
    }

    public ActivityBuilder<E> priorityOfFirstBehavior(int priority) {
        this.priorityOfFirstBehavior = priority;
        return this;
    }

    public ActivityBuilder<E> memoryThatMustHaveValueAndWillBeErasedAfter(MemoryModuleType<?> type) {
        this.memoryThatMustHaveValueAndWillBeErasedAfter = type;
        return this;
    }

    public ActivityBuilder<E> goal(BehaviorControl<? super E> goal) {
        this.goals.add(goal);
        return this;
    }

    public ActivityBuilder<E> goalWithPriority(int priority, BehaviorControl<? super E> goal) {
        this.goalsByPriority.add(Pair.of(priority, goal));
        return this;
    }

    public ActivityBuilder<E> condition(MemoryModuleType<?> type, MemoryStatus status) {
        this.conditions.add(Pair.of(type, status));
        return this;
    }

}
