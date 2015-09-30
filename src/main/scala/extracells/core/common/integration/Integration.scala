package extracells.core.common.integration

import net.minecraftforge.common.config.Configuration

class Integration {
  def loadConfig(config: Configuration): Unit = {
    for (mod <- Mods.values)
      mod.loadConfig(config)
  }

  def preInit(): Unit = {
    if (Mods.IGW.correctSide() && Mods.IGW.shouldLoad){}
      //TODO: IGW
  }

  def init(): Unit = {
    //TODO: Integration
  }

  def postInit(): Unit = {
    //TODO: Integration
  }
}
