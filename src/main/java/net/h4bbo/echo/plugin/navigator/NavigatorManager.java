package net.h4bbo.echo.plugin.navigator;

import net.h4bbo.echo.api.services.navigator.INavigatorService;
import net.h4bbo.echo.api.services.room.IRoomService;
import net.h4bbo.echo.storage.models.navigator.NavigatorCategoryData;
import net.h4bbo.echo.storage.models.room.RoomData;

import java.util.List;

public class NavigatorManager {
    private final List<NavigatorCategoryData> navigatorCategories;

    public NavigatorManager(INavigatorService navigatorService) {
        this.navigatorCategories = navigatorService.getCategories();
    }

    public NavigatorCategoryData getTopParentCategory(int categoryId) {
        NavigatorCategoryData current = this.navigatorCategories
                .stream()
                .filter(c -> c.getId() == categoryId)
                .findFirst()
                .orElse(null);

        while (current != null && current.getParentId() != 0) {
            int parentId = current.getParentId();
            current = this.navigatorCategories.stream()
                    .filter(c -> c.getId() == parentId)
                    .findFirst()
                    .orElse(null);
        }
        return current; // This is the top-most parent (or null if not found)
    }

    public boolean isPublicRoomCategory(int categoryId) {
        var navigatorCategory = this.navigatorCategories
                .stream()
                .filter(x -> x.getId() == categoryId)
                .findFirst()
                .orElse(null);

        if (navigatorCategory == null) {
            throw new NullPointerException("Category " + categoryId + " does not exist");
        }

        return this.getTopParentCategory(navigatorCategory.getId()).getId() == 3;
    }
}