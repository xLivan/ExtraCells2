package extracells.core.common.registries;

import net.minecraft.util.ResourceLocation;

/**
 * Central location for storing GUI texture locations and unlocalized names.
 * Reason: To allow for easy modification.
 */
public enum GuiEnum {
    FluidTerm("extracells.part.fluid.terminal.name", "textures/gui/terminalfluid.png"),
    PortableCell("extracells.item.storage.fluid.portable.name", FluidTerm.guiTexture);

    private String unlocalizedName;
    private ResourceLocation guiTexture;

    GuiEnum(String unlocalizedName, String guiTexturePath) {
        this(unlocalizedName, new ResourceLocation("extracells",guiTexturePath));
    }

    GuiEnum(String unlocalizedName, ResourceLocation guiTexture) {
        this.unlocalizedName = unlocalizedName;
        this.guiTexture = guiTexture;
    }

    static public GuiEnum getByID(int id) {
        return GuiEnum.values()[id - 6];
    }
    static public boolean isValidID(int id) {
        return id >= 6 && id < (GuiEnum.values().length + 6);
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }

    public int getID() {
        return this.ordinal() + 6;
    }

    public ResourceLocation getTexture() {
        return guiTexture;
    }
}
