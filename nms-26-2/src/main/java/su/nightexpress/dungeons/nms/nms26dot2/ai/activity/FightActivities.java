package su.nightexpress.dungeons.nms.nms26dot2.ai.activity;

import org.jspecify.annotations.NullMarked;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;

@NullMarked
public final class FightActivities {

    private FightActivities() {
    }

    public static BehaviorControl<Mob> meleeAttack() {
        return BehaviorBuilder.create(instance -> {
            return instance.group(
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(instance, (lookTarget, attackTarget, attackCooldown, nearestMobs) -> {
                return (world, pet, timestamp) -> {
                    if (!(pet instanceof DungeonEntity)) return false;

                    LivingEntity target = instance.get(attackTarget);
                    if (isHoldingUsableProjectileWeapon(pet)) return false;
                    if (!pet.isWithinMeleeAttackRange(target)) return false;
                    if (!instance.get(nearestMobs).contains(target)) return false;

                    lookTarget.set(new EntityTracker(target, true));
                    pet.swing(InteractionHand.MAIN_HAND);
                    pet.doHurtTarget(world, target);
                    attackCooldown.setWithExpiry(true, getAttackCooldownTicks(pet));
                    return true;
                };
            });
        });
    }

    private static int getAttackCooldownTicks(Mob pet) {
        AttributeInstance attribute = pet.getAttribute(Attributes.ATTACK_SPEED);
        double atkSpeed = attribute == null ? 0.5 : attribute.getValue();
        return (int) Math.max(1, 20D / atkSpeed);
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob pet) {
        return pet.isHolding(itemStack -> {
            Item item = itemStack.getItem();
            return item instanceof ProjectileWeaponItem && pet.canUseNonMeleeWeapon(itemStack);
        });
    }

    public static BehaviorControl<Mob> stopAttackingIfTargetInvalid() {
        return StopAttackingIfTargetInvalid.create();
    }

    public static BehaviorControl<Mob> walkAttackTargetIfOutOfRange() {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1F);
    }
}
