package me.vitalframework.inventories;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Used to easily create an interactive paged Inventory Menu.
 * This class extends VitalInventoryMenu for creating paginated menus.
 */
@Getter
public abstract class VitalPagedInventory extends VitalInventory {
    private long page = 1;

    @Setter
    private long maxPage = 1;

    @Setter
    private int fromSlot = 0;

    @Setter
    private int toSlot = 0;

    /**
     * Constructs a new paged inventory with the specified previous inventory to open after clicking out of inventory menu bounds.
     */
    public VitalPagedInventory(VitalInventory previousInventory) {
        super(previousInventory);

        final var info = getClass().getAnnotation(Info.class);

        fromSlot = info.fromSlot();
        toSlot = info.toSlot();
    }

    /**
     * Gets the amount of items required to fill a page from {@link VitalPagedInventory#fromSlot} to {@link VitalPagedInventory#toSlot}.
     */
    public int getPageContent() {
        return (toSlot + 1/* since content is INCLUSIVE to the SLOT itself */) - fromSlot;
    }

    /**
     * Updates the maxPage indicator with the given total content amount.
     */
    public void updateMaxPage(int totalContent) {
        maxPage = (int) Math.ceil((double) totalContent / getPageContent());
    }

    /**
     * Called when the page of this paged inventory menu changes.
     */
    protected void onPageChange(long page, @NonNull Player player) {

    }

    /**
     * Sets the current page of this paged inventory menu.
     */
    public final void setPage(long page, @NonNull Player player) {
        if (page <= 0) {
            page = 1;
        }

        if (page >= maxPage) {
            page = maxPage;
        }

        this.page = page;
        onPageChange(page, player);
        super.update(player);
    }

    /**
     * Slices the given list of items to fit the inventory for the current page
     */
    @NonNull
    protected <T> List<T> sliceForPage(@NonNull List<T> list) {
        final var startIndex = (int) (getPageContent() * (page - 1));

        if (startIndex >= list.size() || startIndex < 0) {
            return List.of();
        }

        final var endIndex = startIndex + getPageContent();

        if (endIndex >= list.size()) {
            return list.subList(startIndex, list.size());
        }

        return list.subList(startIndex, endIndex);
    }

    @Override
    public void open(@NonNull Player player) {
        super.open(player);
        setPage(1, player);
    }

    @Override
    public void update(@NonNull Player player) {
        super.update(player);
        setPage(page, player);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * Defines the starting slot for each page item.
         */
        @Range(from = 0, to = 9)
        int fromSlot() default 0;

        /**
         * Defines the ending slot for each page item.
         */
        @Range(from = 0, to = 9)
        int toSlot() default 0;
    }
}