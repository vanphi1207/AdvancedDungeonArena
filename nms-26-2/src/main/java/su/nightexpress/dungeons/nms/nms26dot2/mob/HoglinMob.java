package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.AIUtils;
import su.nightexpress.dungeons.nms.nms26dot2.ai.BrainUtils;
import su.nightexpress.dungeons.nms.nms26dot2.ai.mob.GenericAi;
import su.nightexpress.dungeons.nms.nms26dot2.ai.mob.HoglinReAi;

@NullMarked
public class HoglinMob extends Hoglin implements DungeonHolder {

    public HoglinMob(ServerLevel level) {
        super(EntityTypes.HOGLIN, level);
        this.setImmuneToZombification(true);
    }

    protected Brain.Provider<Hoglin> brainProvider() {
        return BrainUtils.brainProvider(
            /* Lists.newList(
                SensorType.NEAREST_LIVING_ENTITIES,
                SensorType.HURT_BY
            ), */
            pet -> HoglinReAi.initActivities()
        );
    }

    @Override
    protected Brain<Hoglin> makeBrain(Brain.Packed dynamic) {
        return this.brainProvider().makeBrain(this, dynamic);
    }


    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor,
                                                  DifficultyInstance difficulty,
                                                  EntitySpawnReason reason,
                                                  @Nullable SpawnGroupData groupData) {
        return groupData;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller filler = Profiler.get();
        filler.push("hoglinBrain");
        this.getBrain().tick(level, this);
        filler.pop();
        GenericAi.updateActivity(this);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        if (entity instanceof LivingEntity target) {
            this.handleEntityEvent((byte) 4);
            this.level().broadcastEntityEvent(this, (byte) 4);
            this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
            return HoglinBase.hurtAndThrowTarget(level, this, target);
        }
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(Blocks.CRIMSON_NYLIUM) ? 10.0F : 0.0F;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return AIUtils.hurt(this, damageSource, fixed -> super.hurtServer(level, fixed, amount));
    }

    @Override
    protected void hurtArmor(DamageSource source, float amount) {
        this.doHurtEquipment(source, amount, AIUtils.ARMOR_SLOTS);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return this.level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean isImmuneToZombification() {
        return true;
    }

    @Override
    protected void ageBoundaryReached() {
    }

    @Override
    public boolean canBeHunted() {
        return false;
    }

    @Override
    public boolean isConverting() {
        return false;
    }
}
