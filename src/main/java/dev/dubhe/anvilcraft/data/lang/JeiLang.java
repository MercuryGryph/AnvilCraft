package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class JeiLang {
    public static void init(RegistrateLangProvider provider) {
        provider.add("gui.anvilcraft.category.mesh", "Mesh");
        provider.add("gui.anvilcraft.category.mesh.chance", "Chance: %s%%");
        provider.add("gui.anvilcraft.category.mesh.average_output", "Average: %s");
        provider.add("gui.anvilcraft.category.mesh.min_output", "Min: %s");
        provider.add("gui.anvilcraft.category.mesh.max_output", "Max: %s");

        provider.add("gui.anvilcraft.category.block_compress", "Block Compress");
        provider.add("gui.anvilcraft.category.block_crush", "Block Crush");

        provider.add("gui.anvilcraft.category.item_compress", "Item Compress");
        provider.add("gui.anvilcraft.category.item_crush", "Item Crush");

        provider.add("gui.anvilcraft.category.cooking", "Cooking");
        provider.add("gui.anvilcraft.category.boiling", "Boiling");

        provider.add("gui.anvilcraft.category.stamping", "Stamping");

        provider.add("gui.anvilcraft.category.super_heating", "Super Heating");
        provider.add("gui.anvilcraft.category.super_heating.convert_to", "Convert to %s");

        provider.add("gui.anvilcraft.category.squeezing", "Squeezing");

        provider.add("gui.anvilcraft.category.item_inject", "Item Inject");

        provider.add("gui.anvilcraft.category.cement_staining", "Cement Staining");

        provider.add("gui.anvilcraft.category.concrete", "Concrete");
    }
}