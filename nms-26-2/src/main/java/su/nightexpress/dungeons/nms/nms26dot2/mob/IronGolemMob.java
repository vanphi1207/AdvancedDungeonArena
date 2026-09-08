package su.nightexpress.dungeons.nms.nms26dot2.mob;

import org.jspecify.annotations.NullMarked;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import su.nightexpress.dungeons.api.dungeon.DungeonHolder;

@NullMarked
public class IronGolemMob extends IronGolem implements DungeonHolder {

    public IronGolemMob(ServerLevel level) {
        super(EntityTypes.IRON_GOLEM, level);
    }

    @Override
    protected void doPush(Entity entity) {
        entity.push(this);
    }
}
