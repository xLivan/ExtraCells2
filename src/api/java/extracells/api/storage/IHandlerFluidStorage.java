package extracells.api.storage;

public interface IHandlerFluidStorage {

    boolean isFormatted();

    int totalBytes();

    int totalTypes();

    int usedBytes();

    int usedTypes();

}
