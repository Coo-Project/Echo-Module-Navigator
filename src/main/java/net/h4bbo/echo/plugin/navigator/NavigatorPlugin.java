package net.h4bbo.echo.plugin.navigator;

import net.h4bbo.echo.api.event.EventHandler;
import net.h4bbo.echo.api.event.types.player.PlayerDisconnectEvent;
import net.h4bbo.echo.api.event.types.player.PlayerLoginEvent;
import net.h4bbo.echo.api.plugin.DependsOn;
import net.h4bbo.echo.api.plugin.JavaPlugin;
import net.h4bbo.echo.api.services.navigator.INavigatorService;
import net.h4bbo.echo.api.services.room.IRoomService;
import net.h4bbo.echo.plugin.navigator.messages.navigator.NavigateMessageEvent;
import net.h4bbo.echo.plugin.navigator.messages.navigator.SearchFlatsMessageEvent;
import net.h4bbo.echo.plugin.navigator.messages.navigator.UserFlatsMessageEvent;
import net.h4bbo.echo.plugin.navigator.messages.user.GetCreditsMessageEvent;
import net.h4bbo.echo.plugin.navigator.messages.user.UserInfoMessageEvent;
import net.h4bbo.echo.plugin.navigator.services.NavigatorService;
import net.h4bbo.echo.storage.models.navigator.NavigatorCategoryData;
import net.h4bbo.echo.storage.models.room.RoomData;
import net.h4bbo.echo.storage.models.user.UserData;
import org.oldskooler.inject4j.ServiceCollection;

import java.util.List;

@DependsOn({"HandshakePlugin", "RoomPlugin"})
public class NavigatorPlugin extends JavaPlugin {
    private NavigatorManager navigatorManager;

    @Override
    public void assignServices(ServiceCollection services) {
        services.addTransient(INavigatorService.class, NavigatorService.class);
    }

    @Override
    public void load() {
        this.navigatorManager = this.getServices().createInstance(NavigatorManager.class);
        this.getEventManager().register(this, this);
    }

    @Override
    public void unload() {

    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        this.getLogger().info("{} has logged in!", event.getPlayer().attr(UserData.DATA_KEY).get().getName());

        var messageHandler = event.getPlayer().getConnection().getMessageHandler();

        messageHandler.register(this, UserInfoMessageEvent.class);
        messageHandler.register(this, GetCreditsMessageEvent.class);
        messageHandler.register(this, NavigateMessageEvent.class);
        messageHandler.register(this, UserFlatsMessageEvent.class);
        messageHandler.register(this, SearchFlatsMessageEvent.class);
    }

    @EventHandler
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        this.getLogger().info("{} has disconnected!", event.getPlayer());
    }

    public NavigatorManager getNavigatorManager() {
        return navigatorManager;
    }
}