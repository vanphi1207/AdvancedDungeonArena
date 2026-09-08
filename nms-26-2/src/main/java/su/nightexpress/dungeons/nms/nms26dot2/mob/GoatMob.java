package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.BrainUtils;
import su.nightexpress.dungeons.nms.nms26dot2.ai.mob.GoatReAi;

@NullMarked
public class GoatMob extends Goat implements DungeonHolder {

    public GoatMob(ServerLevel level) {
        super(EntityTypes.GOAT, level);
    }

    protected Brain.Provider<Goat> brainProvider() {
        return BrainUtils.brainProvider(
            goat -> GoatReAi.initActivities()
        );
    }

    @Override
    protected Brain<Goat> makeBrain(Brain.Packed dynamic) {
        return this.brainProvider().makeBrain(this, dynamic);
    }


    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor,
                                        DifficultyInstance difficulty,
                                        EntitySpawnReason reason,
                                        @Nullable SpawnGroupData groupData) {
        return new AgeableMob.AgeableMobGroupData(false);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller filler = Profiler.get();
        filler.push("goatBrain");
        this.getBrain().tick(level, this);
        filler.pop();
        filler.push("goatActivityUpdate");
        GoatReAi.updateActivity(this);
        filler.pop();
    }

    @Override
    protected void ageBoundaryReached() {

    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return this.level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean dropHorn() {
        return false;
    }
}
