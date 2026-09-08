package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.monster.warden.Warden;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.nms.nms26dot2.ai.AIUtils;
import su.nightexpress.dungeons.nms.nms26dot2.ai.mob.GenericAi;

@NullMarked
public class WardenMob extends Warden implements DungeonHolder {

    // TODO Better AI

    public WardenMob(ServerLevel level) {
        super(EntityTypes.WARDEN, level);
    }

    protected Brain.Provider<Warden> brainProvider() {
        return GenericAi.brainProvider(Warden.class);
    }

    @Override
    protected Brain<Warden> makeBrain(Brain.Packed dynamic) {
        return this.brainProvider().makeBrain(this, dynamic);
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
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller filler = Profiler.get();
        filler.push("wardenBrain");
        this.getBrain().tick(level, this);
        filler.pop();
        GenericAi.updateActivity(this);
    }
}
