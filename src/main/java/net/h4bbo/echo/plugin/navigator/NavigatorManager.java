package net.h4bbo.echo.plugin.navigator;

import net.h4bbo.echo.api.services.navigator.INavigatorService;
import net.h4bbo.echo.api.services.room.IRoomService;
import net.h4bbo.echo.storage.models.navigator.NavigatorCategoryData;
import net.h4bbo.echo.storage.models.room.RoomData;

import java.util.List;

public class NavigatorManager {
    private final IRoomService roomService;
    private final INavigatorService navigatorService;

    public NavigatorManager(IRoomService roomService, INavigatorService navigatorService) {
        this.roomService = roomService;
        this.navigatorService = navigatorService;
    }

    public NavigatorCategoryData getTopParentCategory(int categoryId) {
        NavigatorCategoryData current = this.getNavigatorCategories()
                .stream()
                .filter(c -> c.getId() == categoryId)
                .findFirst()
                .orElse(null);

        while (current != null && current.getParentId() != 0) {
            int parentId = current.getParentId();
            current = this.getNavigatorCategories().stream()
                    .filter(c -> c.getId() == parentId)
                    .findFirst()
                    .orElse(null);
        }
        return current; // This is the top-most parent (or null if not found)
    }

    public List<NavigatorCategoryData> getNavigatorCategories() {
        return this.navigatorService.getCategories();
    }

    public List<RoomData> getRoomsByCategory(int categoryId) {
        return this.roomService.getRoomsByCategory(categoryId);
    }

    public List<RoomData> getRoomsByUserId(int userId) {
        return this.roomService.getRoomsByUserId(userId);
    }

    public List<RoomData> searchRooms(String queryString) {
        return this.roomService.search(queryString);
    }

    public boolean isPublicRoomCategory(int categoryId) {
        var navigatorCategory = this.getNavigatorCategories().stream().filter(x -> x.getId() == categoryId).findFirst().orElse(null);

        if (navigatorCategory == null) {
            throw new NullPointerException("Category " + categoryId + " does not exist");
        }

        return this.getTopParentCategory(navigatorCategory.getId()).getId() == 3;
    }
}