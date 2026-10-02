package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HollowExpanse.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("hollow_expanse",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hollow_expanse"))
                    .icon(() -> new ItemStack(ModItems.VOIDSTEEL_INGOT.get()))
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());
}
