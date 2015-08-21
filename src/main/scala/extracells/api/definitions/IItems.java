package extracells.api.definitions;

import appeng.api.definitions.IItemDefinition;

public interface IItems {
    // Fluid Cells
    IItemDefinition fluidCell1k();

    IItemDefinition fluidCell4k();

    IItemDefinition fluidCell16k();

    IItemDefinition fluidCell64k();

    IItemDefinition fluidCell256k();

    IItemDefinition fluidCell1024k();

    IItemDefinition fluidCell4096k();

    IItemDefinition fluidCellPortable();

    // Physical Cells
    IItemDefinition physCell256k();

    IItemDefinition physCell1024k();

    IItemDefinition physCell4096k();

    IItemDefinition physCell16384k();

    IItemDefinition physCellContainer();

    // MISC
    IItemDefinition wirelessFluidTerminal();
}
