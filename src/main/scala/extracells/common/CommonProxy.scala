package extracells.common

import java.io._

import appeng.api.{IAppEngApi, AEApi}
import appeng.api.recipes.{IRecipeHandler, IRecipeLoader}
import cpw.mods.fml.common.registry.GameRegistry
import extracells.common.registries.{ItemEnum, BlockEnum}

class CommonProxy {
  private val externalRecipeLoader : IRecipeLoader = new IRecipeLoader {
    override def getFile(path: String): BufferedReader = {
      return new BufferedReader(new FileReader(new File(path)))
    }
  }

  private val internalRecipeLoader: IRecipeLoader = new IRecipeLoader {
    override def getFile(path: String): BufferedReader = {
      val resourceStream: InputStream = getClass().getResourceAsStream(
        "/assets/extracells/recipes/".concat(path))
      val streamReader = new InputStreamReader(resourceStream, "UTF-8")
      return new BufferedReader(streamReader)
    }
  }

  def addRecipes(configFolder: File) {
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
