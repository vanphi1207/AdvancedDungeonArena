package su.nightexpress.dungeons.nms;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.api.type.MobFaction;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

@NullMarked
public interface DungeonNMS {

    boolean isSupportedMob(EntityType type);

    @Nullable
    LivingEntity spawnMob(Dungeon dungeon, EntityType type, MobFaction faction, Location location,
                          Consumer<LivingEntity> function);

    @Nullable
    EntityType getSpawnEggType(ItemStack itemStack);

    //void setBlockStateFromTag( Block block,  Object compoundTag);

    void setSchemaBlock(World world, SchemaBlock schemaBlock);

    List<SchemaBlock> loadSchema(File file, boolean compressed);

    void saveSchema(World world, List<Block> blocks, File file);
}
