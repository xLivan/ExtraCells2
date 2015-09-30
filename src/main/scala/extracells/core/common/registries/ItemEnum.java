package extracells.core.common.registries;

import extracells.core.common.item.*;
import extracells.core.ExtraCells;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public enum ItemEnum {
	FLUIDPLACEHOLDER("item.fluid", ItemFluidPlaceholder$.MODULE$), // Internal EC Item
	FLUIDSTORAGE("storage.fluid", ItemFluidCell$.MODULE$),
    STORAGECASING( "storage.casing", ItemCellCasing$.MODULE$),
	STORAGECOMPONENT( "storage.component", ItemStorageComponent$.MODULE$),
	PARTITEM("part.base", ItemPart$.MODULE$),
	/*PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	GASSTORAGE("storage.gas", new ItemStorageGas()),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL( "terminal.fluid.wireless", new ItemWirelessTerminalFluid()),
	FLUIDSTORAGEPORTABLE("storage.fluid.portable", new ItemStoragePortableCell()),
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern());// Internal EC Item
    */;
	private final String internalName;
	private Item item;

	ItemEnum(String _internalName, Item _item) {
		this.internalName = _internalName;
		this.item = _item;
		this.item.setUnlocalizedName("extracells." + this.internalName);
		if (!(this.internalName.equals("item.fluid") || this.internalName.equals("pattern.crafting")))
			this.item.setCreativeTab(ExtraCells.ModTab());
	}



	public ItemStack getDamagedStack(int damage) {
		return new ItemStack(this.item, 1, damage);
	}

	public String getInternalName() {
		return this.internalName;
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getSizedStack(int size) {
		return new ItemStack(this.item, size);
	}

	public String getStatName() {
		return StatCollector.translateToLocal(this.item.getUnlocalizedName());
	}
}
