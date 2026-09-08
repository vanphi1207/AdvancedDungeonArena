package su.nightexpress.dungeons.nms.nms26dot2.ai.goal;

import java.util.EnumSet;
import java.util.UUID;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jspecify.annotations.NullMarked;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.api.type.GameState;

@NullMarked
public class FollowPlayersGoal extends Goal {

    private static final float SPEED_MODIFIER = 1F;
    private static final float STOP_DISTANCE  = 2F;

    private static final int TIME_TO_RECALC_PATH = 20 * 20;

    private final Mob            mob;
    private final Dungeon        dungeon;
    private final PathNavigation navigation;

    private UUID  target;
    private int   timeToRecalcPath;
    private float oldWaterCost;

    public FollowPlayersGoal(Mob mob, Dungeon dungeon) {
        this.mob = mob;
        this.dungeon = dungeon;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    private Player setTarget() {
        DungeonPlayer dungeonPlayer = this.dungeon.getRandomAlivePlayer();
        Player player = ((CraftPlayer) dungeonPlayer.getPlayer()).getHandle();

        this.target = player.getUUID();
        return player;
    }

    private boolean unableToMove() {
        return this.mob.isPassenger() || this.mob.isLeashed();
    }

    @Override
    public boolean canUse() {
        if (this.dungeon.getState() != GameState.INGAME) return false;
        if (!this.dungeon.hasAlivePlayers()) return false;

        return !this.unableToMove();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = TIME_TO_RECALC_PATH;
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0F);
        this.setTarget();
    }

    @Override
    public void tick() {
        Player player = this.mob.level().getPlayerByUUID(this.target);

        if (--this.timeToRecalcPath <= 0 || player == null || !this.dungeon.hasPlayer(this.target)) {
            player = this.setTarget();

            this.timeToRecalcPath = TIME_TO_RECALC_PATH;

            if (this.mob.distanceToSqr(player) >= 256D) {
                this.timeToRecalcPath = 20;
                return;
            }
        }

        if (this.mob.closerThan(player, STOP_DISTANCE)) {
            return;
        }

        if (!this.mob.getNavigation().moveTo(player, SPEED_MODIFIER)) {
            this.timeToRecalcPath = 20;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.navigation.isDone() && !this.unableToMove();

        //        Player player = this.mob.level().getPlayerByUUID(this.target);
        //        if (player == null || !this.dungeon.hasPlayer(this.target)) return false;
        //
        //        return !this.mob.closerThan(player, this.stopDistance);
    }

    @Override
    public void stop() {
        this.target = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }
}
