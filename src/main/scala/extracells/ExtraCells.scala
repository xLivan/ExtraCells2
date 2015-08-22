package extracells

import java.io.File

import appeng.api.AEApi
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.{FMLCommonHandler, Loader, Mod, SidedProxy}
import extracells.common.{CommonProxy, ECEventHandler}
import extracells.common.grid.helper.FluidCellHandler
import extracells.common.integration.Integration
import extracells.common.network.NetworkWrapper
import extracells.common.registries.ItemEnum
import extracells.common.util.{BasicFluidFilter, IMCHandler}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration

@Mod(modid = "extracells", name = "Extra Cells", modLanguage = "scala", dependencies = "after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2")
object ExtraCells {


	@SidedProxy(clientSide = "extracells.client.ClientProxy", serverSide = "extracells.common.CommonProxy")
	var proxy: CommonProxy = null

	var VERSION = ""

	var configFolder: File = null
	var shortenedBuckets = true
	var dynamicTypes = true
	val integration = new Integration
	
	val ModTab = new  CreativeTabs("Extra_Cells") {

		override def  getIconItemStack = new ItemStack(ItemEnum.FLUIDSTORAGE.getItem)

		override def getTabIconItem = ItemEnum.FLUIDSTORAGE.getItem
	}

	@EventHandler
	def preInit(event: FMLPreInitializationEvent) : Unit = {
		VERSION = Loader.instance.activeModContainer.getVersion
		configFolder = event.getModConfigurationDirectory

		//NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)



		// Config
		val config = new Configuration(new File(
			configFolder.getPath + File.separator + "AppliedEnergistics2"
				+ File.separator + "extracells.cfg"))
		config.load
		shortenedBuckets = config.get("Tooltips", "shortenedBuckets", true, "Shall the guis shorten large mB values?")
			.getBoolean(true)
		dynamicTypes = config.get("Storage Cells", "dynamicTypes", true,
			"Should the mount of bytes needed for a new type depend on the cellsize?").getBoolean(true)
		integration.loadConfig(config)


		config.save

    proxy.preInit()
		integration.preInit
	}

	@EventHandler
	def init(event: FMLInitializationEvent) : Unit = {
		AEApi.instance.registries.recipes.addNewSubItemResolver(new NameHandler)
		AEApi.instance.registries.cell.addCellHandler(new FluidCellHandler)
		val handler = new ECEventHandler
		FMLCommonHandler.instance.bus.register(handler)
		MinecraftForge.EVENT_BUS.register(handler)
    proxy.init()
		proxy.addRecipes(configFolder)
		NetworkWrapper.registerMessages()
		RenderingRegistry.registerBlockHandler(new RenderHandler(RenderingRegistry.getNextAvailableRenderId))
		integration.init
	}

	@EventHandler
	def postInit(event: FMLPostInitializationEvent) : Unit = {
		integration.postInit
	}

	@EventHandler
	def imcHandler(event: IMCEvent): Unit = {
		val handler = new IMCHandler
		for(message <- event.getMessages)
			handler.handle(message)
	}
}
