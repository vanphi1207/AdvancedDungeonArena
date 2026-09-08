package su.nightexpress.dungeons.nms.nms26dot2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.nms.DungeonNMS;
import su.nightexpress.dungeons.nms.nms26dot2.ai.goal.FollowPlayersGoal;
import su.nightexpress.dungeons.nms.nms26dot2.ai.goal.LastDamagerTargetGoal;
import su.nightexpress.dungeons.nms.nms26dot2.ai.goal.NearestFactionTargetGoal;
import su.nightexpress.dungeons.nms.nms26dot2.util.AttributeInjector;

@NullMarked
public class NMS26dot2 implements DungeonNMS {

    private final AttributeInjector attributeInjector;

    public NMS26dot2() {
        this.attributeInjector = new AttributeInjector();
    }

    @Override
    public boolean isSupportedMob(EntityType type) {
        return EntityCreator.isSupported(type);
    }

    @Override
    @Nullable
    public EntityType getSpawnEggType(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.world.entity.EntityType<?> type = SpawnEggItem.getType(nmsStack);
        return type == null ? null : CraftEntityType.minecraftToBukkit(type);
    }

    @Override
    public LivingEntity spawnMob(Dungeon dungeon, EntityType type, MobFaction faction,
                                 Location location, Consumer<LivingEntity> function) {
        ServerLevel level = ((CraftWorld) dungeon.getWorld()).getHandle();

        net.minecraft.world.entity.Mob mob = EntityCreator.createEntity(/*dungeon, faction, */type, level);
        if (mob == null) throw new IllegalStateException("Failed to create mob of type " + type);

        LivingEntity bukkitEntity = (LivingEntity) mob.getBukkitEntity();

        this.attributeInjector.injectFor(mob);

        this.setAttribute(mob, Attributes.FOLLOW_RANGE, 256D);

        if (mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 0) {
            this.setAttribute(mob, Attributes.ATTACK_DAMAGE, 1);
        }

        boolean isAlly = faction == MobFaction.ALLY;

        if (mob instanceof PathfinderMob pathfinderMob) {
            if (isAlly && !EntityCreator.isCustom(type)) {
                mob.getGoalSelector().addGoal(6, new FollowPlayersGoal(mob, dungeon));
            }

            if (bukkitEntity instanceof Animals || bukkitEntity instanceof org.bukkit.entity.IronGolem) {
                mob.getGoalSelector().getAvailableGoals().clear();
                mob.getGoalSelector().addGoal(0, new FloatGoal(mob));
                mob.getGoalSelector().addGoal(2, new su.nightexpress.dungeons.nms.nms26dot2.ai.goal.MeleeAttackGoal(
                    pathfinderMob, dungeon, faction));
                mob.getGoalSelector().addGoal(8, new LookAtPlayerGoal(pathfinderMob,
                    net.minecraft.world.entity.player.Player.class, 8.0F));
            }
            else {
                if (mob instanceof Drowned drowned) {
                    drowned.getGoalSelector().getAvailableGoals().removeIf(goal -> goal
                        .getGoal() instanceof ZombieAttackGoal);
                    drowned.getGoalSelector().addGoal(3, new ZombieAttackGoal(drowned, 1D, false));
                }
                else if (mob instanceof EnderMan ender) {
                    ender.getGoalSelector().getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getPriority() == 1); // EndermanFreezeWhenLookedAt
                }
                pathfinderMob.getGoalSelector().getAvailableGoals().removeIf(wrappedGoal -> {
                    var goal = wrappedGoal.getGoal();
                    return goal instanceof FleeSunGoal || goal instanceof AvoidEntityGoal<?> || goal instanceof RestrictSunGoal;
                });
            }
        }

        if (!EntityCreator.isCustom(type)) {
            mob.targetSelector.getAvailableGoals().clear();
            mob.targetSelector.addGoal(1, new LastDamagerTargetGoal(mob, dungeon, faction));
            mob.targetSelector.addGoal(2, new NearestFactionTargetGoal(mob, dungeon, faction));
            mob.setAggressive(true);
        }

        function.accept(bukkitEntity);

        level.addFreshEntity(mob, null);
        mob.snapTo(location.getX(), location.getY(), location.getZ());

        return bukkitEntity;
    }

    @Override
    public void setSchemaBlock(World world, SchemaBlock schemaBlock) {
        ServerLevel level = ((CraftWorld) world).getHandle();

        CraftBlock craftBlock = (CraftBlock) schemaBlock.getBlockPos().toLocation(world).getBlock();
        craftBlock.setBlockData(schemaBlock.getBlockData());

        if (schemaBlock.getNbt() instanceof CompoundTag tag) {
            BlockPos blockPos = craftBlock.getPosition();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity == null) return;

            //entityState.loadData(tag);
            //entityState.update(true, false);

            // Load NBT data directly to NMS block.
            // CraftBlockEntityState#load wipes out some data, for example CraftSign overrides #load() and wipes out sign text.
            blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(),
                tag));
            blockEntity.setChanged();
        }
    }

    private void setAttribute(net.minecraft.world.entity.LivingEntity handle, Holder<Attribute> attribute,
                              double value) {
        AttributeInstance instance = handle.getAttribute(attribute);
        if (instance == null) return;

        instance.setBaseValue(value);
    }


    @Override
    public List<SchemaBlock> loadSchema(File file, boolean compressed) {
        List<SchemaBlock> schemaBlocks = new ArrayList<>();
        CompoundTag schemTag;

        try {
            schemTag = compressed ? NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()) : NbtIo.read(file
                .toPath());
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return schemaBlocks;
        }
        if (schemTag == null) {
            return schemaBlocks;
        }

        ListTag blocksTag = schemTag.getListOrEmpty("blocks");

        blocksTag.forEach(tag -> {
            CompoundTag blockTag = (CompoundTag) tag;

            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, blockTag.getCompound("state")
                .orElseThrow());

            ListTag posTag = blockTag.getListOrEmpty("pos");
            BlockPos pos = new BlockPos(posTag.getIntOr(0, 0), posTag.getIntOr(1, 0), posTag.getIntOr(2, 0));
            CompoundTag nbt = blockTag.getCompound("nbt").orElse(null);

            CraftBlockData craftBlockData = CraftBlockData.createData(state);
            su.nightexpress.nightcore.util.geodata.pos.BlockPos blockPos = new su.nightexpress.nightcore.util.geodata.pos.BlockPos(
                pos.getX(), pos.getY(), pos.getZ());

            schemaBlocks.add(new SchemaBlock(blockPos, craftBlockData, nbt));
        });

        return schemaBlocks;
    }

    @Override
    public void saveSchema(World world, List<Block> blocks, File file) {
        ServerLevel level = ((CraftWorld) world).getHandle();

        CompoundTag root = new CompoundTag();
        ListTag blocksTag = new ListTag();

        for (Block block : blocks) {
            CraftBlockState craftState = (CraftBlockState) block.getState();
            BlockPos blockPos = craftState.getPosition();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            CompoundTag blockTag = new CompoundTag();
            blockTag.put("state", NbtUtils.writeBlockState(craftState.getHandle()));
            blockTag.put("pos", this.newIntegerList(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            if (blockEntity != null) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level
                    .registryAccess());//TagValueOutput.createWithContext(reporter, level.registryAccess());
                blockEntity.saveWithId(output);
                blockTag.put("nbt", output.buildResult());
            }
            blocksTag.add(blockTag);
        }

        root.put("blocks", blocksTag);

        try {
            NbtIo.writeCompressed(root, file.toPath());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    private ListTag newIntegerList(int... arr) {
        ListTag tag = new ListTag();

        for (int value : arr) {
            tag.add(IntTag.valueOf(value));
        }

        return tag;
    }
}
