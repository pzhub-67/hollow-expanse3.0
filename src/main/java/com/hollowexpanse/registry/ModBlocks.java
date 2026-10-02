package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.block.AnchorStoneBlock;
import com.hollowexpanse.block.HollowPortalBlock;
import com.hollowexpanse.block.LumenBloomBlock;
import com.hollowexpanse.block.ReinforcedObsidianBlock;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HollowExpanse.MOD_ID);

    // The "aura": every block of the dimension gives off a little light, brightest where it matters.
    public static final RegistryObject<Block> HOLLOW_STONE = register("hollow_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(3.0F, 9.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)
                    .lightLevel(state -> 2)));

    public static final RegistryObject<Block> HOLLOW_SOIL = register("hollow_soil",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(0.6F)
                    .sound(SoundType.SOUL_SOIL)
                    .lightLevel(state -> 1)));

    public static final RegistryObject<Block> VOIDMOSS = register("voidmoss",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WARPED_NYLIUM).strength(0.7F)
                    .sound(SoundType.MOSS)
                    .lightLevel(state -> 5)));

    public static final RegistryObject<Block> VOIDSTEEL_ORE = register("voidsteel_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(4.5F, 12.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)
                    .lightLevel(state -> 6)));

    public static final RegistryObject<Block> REINFORCED_OBSIDIAN = register("reinforced_obsidian",
            () -> new ReinforcedObsidianBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)
                    .lightLevel(state -> 4)));

    public static final RegistryObject<Block> ANCHOR_STONE = register("anchor_stone",
            () -> new AnchorStoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(4.0F, 10.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)
                    .lightLevel(state -> 12)));

    public static final RegistryObject<Block> LUMEN_BLOOM = register("lumen_bloom",
            () -> new LumenBloomBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN).noCollission().instabreak()
                    .sound(SoundType.SPORE_BLOSSOM).pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 12)));

    /** The portal has no item form. */
    public static final RegistryObject<Block> HOLLOW_PORTAL = BLOCKS.register("hollow_portal",
            () -> new HollowPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).noCollission().noOcclusion()
                    .strength(-1.0F, 3600000.0F).noLootTable()
                    .sound(SoundType.GLASS).lightLevel(state -> 11)));

    private static RegistryObject<Block> register(String name, Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, factory);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
