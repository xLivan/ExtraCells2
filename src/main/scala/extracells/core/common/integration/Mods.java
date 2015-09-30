package extracells.core.common.integration;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.relauncher.Side;
import extracells.core.ExtraCells;
import net.minecraftforge.common.config.Configuration;

public enum Mods {
    WAILA("Waila"),
    OPENCOMPUTERS("OpenComputers"),
    BCFUEL("BuildCraftAPI|fuels", "BuildCraftFuel"),
    NEI("NotEnoughItems"),
    MEKANISMGAS("MekanismAPI|gas", "MekanismGas"),
    IGW("IGWMod", "IngameWikiMod", Side.CLIENT);

    private final String modID;

    private boolean shouldLoad = true;

    private final String name;

    private final Side side;

    Mods(String modid){
        this(modid, modid);
    }

    Mods(String modid, String modName, Side side) {
        this.modID = modid;
        this.name = modName;
        this.side = side;
    }

    Mods(String modid, String modName){
        this(modid, modName, null);
    }

    Mods(String modid, Side side){
        this(modid, modid, side);
    }

    public String getModID(){
        return modID;
    }

    public String getModName() {
        return name;
    }

    public boolean isOnClient(){
        return side != Side.SERVER;
    }

    public boolean isOnServer(){
        return side != Side.CLIENT;
    }

    public void loadConfig(Configuration config){
        shouldLoad = config.get("Integration", "enable" + getModName(), true, "Enable " + getModName() + " Integration.").getBoolean(true);
    }

    public boolean isEnabled(){
        return (Loader.isModLoaded(getModID()) && shouldLoad && correctSide()) || (ModAPIManager.INSTANCE.hasAPI(getModID()) && shouldLoad && correctSide());
    }

    protected boolean correctSide(){
        return ExtraCells.proxy().isClient() ? isOnClient() : isOnServer();
    }
    protected boolean shouldLoad() {return shouldLoad;}

}
