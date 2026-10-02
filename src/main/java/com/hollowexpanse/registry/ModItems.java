package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.item.HollowSigilItem;
import com.hollowexpanse.item.TetherHookItem;
import com.hollowexpanse.item.VoidMaceItem;
import com.hollowexpanse.item.VoidsteelArmorItem;
import com.hollowexpanse.item.VoidsteelTier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Block items are registered by ModBlocks. */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HollowExpanse.MOD_ID);

    // ---- materials ----
    public static final RegistryObject<Item> RAW_VOIDSTEEL = ITEMS.register("raw_voidsteel",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOIDSTEEL_INGOT = ITEMS.register("voidsteel_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_IGNITION_CORE = ITEMS.register("void_ignition_core",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> VOID_SHARD = ITEMS.register("void_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HOLLOW_SIGIL = ITEMS.register("hollow_sigil",
            () -> new HollowSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> WARDENS_CORE = ITEMS.register("wardens_core",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> VOID_HEART = ITEMS.register("void_heart",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    // ---- tether hooks ----
    public static final RegistryObject<Item> TETHER_HOOK = ITEMS.register("tether_hook",
            () -> new TetherHookItem(32.0D, 1.5D, new Item.Properties().stacksTo(1).durability(384)));
    public static final RegistryObject<Item> VOIDSTEEL_TETHER_HOOK = ITEMS.register("voidsteel_tether_hook",
            () -> new TetherHookItem(48.0D, 2.2D,
                    new Item.Properties().stacksTo(1).durability(2048).rarity(Rarity.RARE).fireResistant()));

    // ---- voidsteel tools ----
    public static final RegistryObject<Item> VOIDSTEEL_SWORD = ITEMS.register("voidsteel_sword",
            () -> new SwordItem(VoidsteelTier.VOIDSTEEL, new Item.Properties()
                    .attributes(SwordItem.createAttributes(VoidsteelTier.VOIDSTEEL, 3, -2.4F))));
    public static final RegistryObject<Item> VOIDSTEEL_PICKAXE = ITEMS.register("voidsteel_pickaxe",
            () -> new PickaxeItem(VoidsteelTier.VOIDSTEEL, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(VoidsteelTier.VOIDSTEEL, 1.0F, -2.8F))));
    public static final RegistryObject<Item> VOIDSTEEL_AXE = ITEMS.register("voidsteel_axe",
            () -> new AxeItem(VoidsteelTier.VOIDSTEEL, new Item.Properties()
                    .attributes(AxeItem.createAttributes(VoidsteelTier.VOIDSTEEL, 5.0F, -3.0F))));
    public static final RegistryObject<Item> VOIDSTEEL_SHOVEL = ITEMS.register("voidsteel_shovel",
            () -> new ShovelItem(VoidsteelTier.VOIDSTEEL, new Item.Properties()
                    .attributes(ShovelItem.createAttributes(VoidsteelTier.VOIDSTEEL, 1.5F, -3.0F))));
    public static final RegistryObject<Item> VOIDSTEEL_HOE = ITEMS.register("voidsteel_hoe",
            () -> new HoeItem(VoidsteelTier.VOIDSTEEL, new Item.Properties()
                    .attributes(HoeItem.createAttributes(VoidsteelTier.VOIDSTEEL, -4.0F, 0.0F))));

    // ---- hollow-forged (netherite + warden's core) ----
    public static final RegistryObject<Item> HOLLOWFORGED_SWORD = ITEMS.register("hollowforged_sword",
            () -> new SwordItem(VoidsteelTier.HOLLOWFORGED, new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                    .attributes(SwordItem.createAttributes(VoidsteelTier.HOLLOWFORGED, 3, -2.4F))));
    public static final RegistryObject<Item> HOLLOWFORGED_PICKAXE = ITEMS.register("hollowforged_pickaxe",
            () -> new PickaxeItem(VoidsteelTier.HOLLOWFORGED, new Item.Properties().fireResistant().rarity(Rarity.EPIC)
                    .attributes(PickaxeItem.createAttributes(VoidsteelTier.HOLLOWFORGED, 1.0F, -2.8F))));

    public static final RegistryObject<Item> VOID_MACE = ITEMS.register("void_mace",
            () -> new VoidMaceItem(new Item.Properties().stacksTo(1).durability(1500)
                    .rarity(Rarity.EPIC).fireResistant().attributes(VoidMaceItem.createAttributes())));

    // ---- voidsteel armor ----
    public static final RegistryObject<Item> VOIDSTEEL_HELMET = ITEMS.register("voidsteel_helmet",
            () -> new VoidsteelArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> VOIDSTEEL_CHESTPLATE = ITEMS.register("voidsteel_chestplate",
            () -> new VoidsteelArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> VOIDSTEEL_LEGGINGS = ITEMS.register("voidsteel_leggings",
            () -> new VoidsteelArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> VOIDSTEEL_BOOTS = ITEMS.register("voidsteel_boots",
            () -> new VoidsteelArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    // ---- spawn eggs (handy for testing) ----
    public static final RegistryObject<Item> HOLLOW_WARDEN_SPAWN_EGG = ITEMS.register("hollow_warden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOLLOW_WARDEN, 0x0A0A0F, 0x4ACCCC, new Item.Properties()));
    public static final RegistryObject<Item> DRIFTER_SPAWN_EGG = ITEMS.register("drifter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DRIFTER, 0x1A6B6B, 0xB88AE0, new Item.Properties()));
    public static final RegistryObject<Item> HUSK_STALKER_SPAWN_EGG = ITEMS.register("husk_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HUSK_STALKER, 0x15151D, 0x9A5AB8, new Item.Properties()));
}
