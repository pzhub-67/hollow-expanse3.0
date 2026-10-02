package com.hollowexpanse;

import com.hollowexpanse.registry.ModBlocks;
import com.hollowexpanse.registry.ModCreativeTabs;
import com.hollowexpanse.registry.ModEffects;
import com.hollowexpanse.registry.ModEntities;
import com.hollowexpanse.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HollowExpanse.MOD_ID)
public class HollowExpanse {
    public static final String MOD_ID = "hollow_expanse";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HollowExpanse() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModEffects.EFFECTS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
