package su.nightexpress.dungeons.nms.nms26dot2.ai.goal;

import java.util.EnumSet;

import javax.annotation.Nullable;

import org.jspecify.annotations.NullMarked;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.type.MobFaction;

@NullMarked
public class LastDamagerTargetGoal extends TargetGoal {

    private static final TargetingConditions CONDITIONS = TargetingConditions.forCombat().ignoreLineOfSight()
        .ignoreInvisibilityTesting();

    private final Dungeon    arena;
    private final MobFaction faction;

    private int timestamp;

    public LastDamagerTargetGoal(Mob mob, Dungeon arena, MobFaction faction) {
        super(mob, true);
        this.setFlags(EnumSet.of(Flag.TARGET));

        this.arena = arena;
        this.faction = faction;
    }

    public void start() {
        this.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;

        super.start();
    }

    public boolean canUse() {
        int lastHurtDate = this.mob.getLastHurtByMobTimestamp();
        LivingEntity lastDamager = this.mob.getLastHurtByMob();
        if (lastHurtDate == this.timestamp || lastDamager == null) return false;

        if (this.arena.getMobFaction((org.bukkit.entity.LivingEntity) lastDamager.getBukkitEntity()) == this.faction) {
            return false;
        }

        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget != null) {
            if (currentTarget == lastDamager) return false;
            if (this.mob.distanceToSqr(lastDamager) > this.mob.distanceToSqr(currentTarget)) return false;
        }

        return this.canAttack(lastDamager, CONDITIONS);
    }

    private void setTarget(@Nullable LivingEntity target) {
        if (target == null) return;

        org.bukkit.entity.Mob bukkitMob = (org.bukkit.entity.Mob) this.mob.getBukkitEntity();
        bukkitMob.setTarget((org.bukkit.entity.LivingEntity) target.getBukkitEntity());
    }
}
