package extracells.core.common

import java.io._

import appeng.api.{IAppEngApi, AEApi}
import appeng.api.recipes.{IRecipeHandler, IRecipeLoader}
import cpw.mods.fml.common.registry.GameRegistry
import extracells.core.common.registries.{ItemEnum, BlockEnum}
import extracells.core.ExtraCells

class CommonProxy {
  lazy private val externalRecipeLoader : IRecipeLoader = new IRecipeLoader {
    override def getFile(path: String): BufferedReader = {
      new BufferedReader(new FileReader(new File(path)))
    }
  }

  lazy private val internalRecipeLoader: IRecipeLoader = new IRecipeLoader {
    override def getFile(path: String): BufferedReader = {
      val resourceStream: InputStream = getClass.getResourceAsStream(
        "/assets/extracells/recipes/".concat(path))
      val streamReader = new InputStreamReader(resourceStream, "UTF-8")
      new BufferedReader(streamReader)
    }
  }

  def preInit(): Unit = {
    registerBlocks()
    registerItems()
  }

  def init(): Unit = {
    registerTileEntities()
    registerMovables()
    registerRenderers()
    registerFluidBurnTimes()
  }

  def addRecipes(configFolder: File) {
    try {
      val recipeHandler: IRecipeHandler = AEApi.instance()
        .registries()
        .recipes()
        .createNewRecipehandler()
      val externalRecipes = new File(configFolder.getPath
        + File.separator
        + "extracells.recipe")
      if(externalRecipes.exists())
        recipeHandler.parseRecipes(externalRecipeLoader,externalRecipes.getPath)
      else
        recipeHandler.parseRecipes(internalRecipeLoader,"main.recipe")
      recipeHandler.injectRecipes()
    }
    catch {
      case e: IllegalStateException => ExtraCells.logger.info("Recipe Error: ".concat(e.getMessage))
    }
  }

  def registerBlocks() {
    BlockEnum.values.foreach(block => GameRegistry.registerBlock(block.getBlock, block.getItemBlockClass, block.getInternalName))
  }

  def registerItems() {
    ItemEnum.values.foreach(item => GameRegistry.registerItem(item.getItem, item.getInternalName))
  }

  //AE Spatial IO
  def registerMovables() {
    val api: IAppEngApi = AEApi.instance()
  }

  def registerTileEntities() {

  }

  def registerFluidBurnTimes() {
    //Probably for fluid vibration chamber.
  }

  //Only required client-side
  def registerRenderers() {}

  def isClient = false
  def isServer = true
}
