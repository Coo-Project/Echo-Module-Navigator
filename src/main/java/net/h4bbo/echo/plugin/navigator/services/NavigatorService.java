package net.h4bbo.echo.plugin.navigator.services;

import net.h4bbo.echo.api.services.navigator.INavigatorService;
import net.h4bbo.echo.plugin.navigator.NavigatorPlugin;
import net.h4bbo.echo.storage.StorageContextFactory;
import net.h4bbo.echo.storage.models.navigator.NavigatorCategoryData;
import net.h4bbo.echo.storage.models.room.RoomData;

import java.sql.SQLException;
import java.util.List;

public class NavigatorService implements INavigatorService {
    private final NavigatorPlugin plugin;
    private List<NavigatorCategoryData> navigatorCategories;

    public NavigatorService(NavigatorPlugin plugin) {
        this.plugin = plugin;

        try (var ctx = StorageContextFactory.getStorage()) {
            this.navigatorCategories = ctx.from(NavigatorCategoryData.class).toList();
        } catch (SQLException e) {
            plugin.getLogger().error("Error loading navigator categories: ", e);
        }
    }

    @Override
    public List<NavigatorCategoryData> getCategories() {
        return navigatorCategories;
    }
}
