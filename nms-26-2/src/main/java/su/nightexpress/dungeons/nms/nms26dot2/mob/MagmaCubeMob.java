package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.player.Player;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.api.type.MobFaction;

@NullMarked
public class MagmaCubeMob extends MagmaCube implements DungeonHolder {

    public MagmaCubeMob(ServerLevel world) {
        super(EntityTypes.MAGMA_CUBE, world);
    }

    @Override
    protected int getJumpDelay() {
        return 8;
    }

    //    public void jumpFromGround() {
    //        Vec3 vec3d = this.getDeltaMovement();
    //        this.setDeltaMovement(vec3d.x, this.getJumpPower(), vec3d.z);
    //        this.hasImpulse = true;
    //    }

    @Override
    public void push(Entity pusher) {
        if (pusher.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity bukkitMob && this.getFaction() != this
            .getDungeon().getMobFaction(bukkitMob)) {
            this.dealDamage((LivingEntity) pusher);
        }

        //super.push(pusher);
    }

    @Override
    public void playerTouch(Player entity) {
        if (this.getFaction() == MobFaction.ALLY) return;

        super.playerTouch(entity);
    }
}
