package extracells.api.definitions;

import appeng.api.definitions.IItemDefinition;

public interface IMaterials {
    // Fluid Storage Components
    IItemDefinition cell1kPartFluid();

    IItemDefinition cell4kPartFluid();

    IItemDefinition cell16kPartFluid();

    IItemDefinition cell64kPartFluid();

    IItemDefinition cell256kPartFluid();

    IItemDefinition cell1024kPartFluid();

    IItemDefinition cell4096kPartFluid();

    // Physical Storage Components
    IItemDefinition cell256kPart();

    IItemDefinition cell1024kPart();

    IItemDefinition cell4096kPart();

    IItemDefinition cell16384kPart();

    // Fluid Storage Casing
    IItemDefinition fluidCasing();

    // Physical Storage Casing
    IItemDefinition physCasing();
}
