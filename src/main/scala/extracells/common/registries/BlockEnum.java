package extracells.common.registries;

import extracells.common.block.BlockWalrus;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;

public enum BlockEnum {
    WALRUS("walrus",new BlockWalrus());
    /**
        CERTUSTANK("certustank", new BlockCertusTank(), ItemBlockCertusTank.class),
        FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter()),
        ECBASEBLOCK("ecbaseblock", new ECBaseBlock(), ItemBlockECBase.class),
        BLASTRESISTANTMEDRIVE("hardmedrive", BlockHardMEDrive.instance()),
        VIBRANTCHAMBERFLUID("vibrantchamberfluid", new BlockVibrationChamberFluid());
     */

    private final String internalName;
    private Block block;
    private Class<? extends ItemBlock> itemBlockClass;

    BlockEnum(String _internalName, Block _block) {
        this(_internalName, _block, ItemBlock.class);
    }

    BlockEnum(String _internalName, Block _block,
              Class<? extends ItemBlock> _itemBlockClass) {
        this.internalName = _internalName;
        this.block = _block;
        this.block.setUnlocalizedName("extracells.block." + this.internalName);
        this.itemBlockClass = _itemBlockClass;
    }

    public Block getBlock() {
        return this.block;
    }

    public String getInternalName() {
        return this.internalName;
    }

    public Class<? extends ItemBlock> getItemBlockClass() {
        return this.itemBlockClass;
    }

    public String getStatName() {
        return StatCollector.translateToLocal(this.block.getUnlocalizedName() + ".name");
    }
}
