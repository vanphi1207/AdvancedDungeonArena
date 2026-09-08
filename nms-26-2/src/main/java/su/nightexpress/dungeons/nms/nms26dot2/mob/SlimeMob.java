package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;
import su.nightexpress.dungeons.api.type.MobFaction;

@NullMarked
public class SlimeMob extends Slime implements DungeonHolder {

    public SlimeMob(ServerLevel world) {
        super(EntityTypes.SLIME, world);
    }

    @Override
    protected int getJumpDelay() {
        return 8;
    }

    @Override
    protected boolean isDealsDamage() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        super.doPush(entity);
    }

    @Override
    public void push(Entity pusher) {
        if (!this.isValidDungeon()) return;
        if (pusher.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity bukkitMob && this.getFaction() != this
            .getDungeon().getMobFaction(bukkitMob)) {
            this.dealDamage((LivingEntity) pusher);
        }

        //super.push(pusher);
    }

    @Override
    public void playerTouch(Player player) {
        if (this.isValidDungeon() && this.getFaction() == MobFaction.ALLY) return;

        super.playerTouch(player);
    }
}
