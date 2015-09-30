package extracells.core

import java.io.File
import java.util.function.Consumer

import appeng.api.AEApi
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLInterModComms.{IMCEvent, IMCMessage}
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{FMLCommonHandler, Loader, Mod, SidedProxy}
import extracells.core.client.render.RenderHandler
import extracells.core.common.grid.helper.FluidCellHandler
import extracells.core.common.integration.Integration
import extracells.core.common.network.{GuiHandler, NetworkWrapper}
import extracells.core.common.registries.ItemEnum
import extracells.core.common.util.IMCHandler
import extracells.core.common.{CommonProxy, ECEventHandler}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.LogManager

@Mod(modid = "extracells", modLanguage = "scala", useMetadata = true)
object ExtraCells {


	@SidedProxy(clientSide = "extracells.core.client.ClientProxy",
		serverSide = "extracells.core.common.CommonProxy")
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

	val logger = LogManager.getLogger("extracells")

	@EventHandler
	def preInit(event: FMLPreInitializationEvent) : Unit = {
		VERSION = Loader.instance.activeModContainer.getVersion
		configFolder = event.getModConfigurationDirectory

		NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)


		// Config
		val config = new Configuration(new File(
			configFolder.getPath + File.separator + "AppliedEnergistics2"
				+ File.separator + "extracells.cfg"))
		config.load()
		shortenedBuckets = config.get("Tooltips", "shortenedBuckets", true, "Should large mb values be shortened?")
			.getBoolean(true)
		dynamicTypes = config.get("Storage Cells", "dynamicTypes", true,
			"Should the amount of bytes needed for a new type depend on the cell size?").getBoolean(true)
		integration.loadConfig(config)


		config.save()

    proxy.preInit()
		integration.preInit()
	}

	@EventHandler
	def init(event: FMLInitializationEvent) : Unit = {
		//AEApi.instance.registries.recipes.addNewSubItemResolver(new NameHandler)
		AEApi.instance.registries.cell.addCellHandler(new FluidCellHandler)
		val handler = new ECEventHandler
		FMLCommonHandler.instance.bus.register(handler)
		MinecraftForge.EVENT_BUS.register(handler)
    proxy.init()
		proxy.addRecipes(configFolder)
		NetworkWrapper.registerMessages()
		RenderingRegistry.registerBlockHandler(new RenderHandler(RenderingRegistry.getNextAvailableRenderId))
		integration.init()
	}

	@EventHandler
	def postInit(event: FMLPostInitializationEvent) : Unit = {
		integration.postInit()
	}

	@EventHandler
	def imcHandler(event: IMCEvent): Unit = {
		val handler = new IMCHandler
		event.getMessages.forEach(new Consumer[IMCMessage] {
      override def accept(msg: IMCMessage): Unit = handler.handle(msg)
    })
	}
}
