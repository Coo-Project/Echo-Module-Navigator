package net.h4bbo.echo.plugin.navigator.messages.navigator;

import net.h4bbo.echo.api.game.player.IPlayer;
import net.h4bbo.echo.api.game.room.RoomConsts;
import net.h4bbo.echo.api.messages.MessageEvent;
import net.h4bbo.echo.api.network.codecs.DataCodec;
import net.h4bbo.echo.api.network.codecs.IClientCodec;
import net.h4bbo.echo.api.services.navigator.INavigatorService;
import net.h4bbo.echo.api.services.room.IRoomService;
import net.h4bbo.echo.codecs.PacketCodec;
import net.h4bbo.echo.plugin.navigator.NavigatorPlugin;
import net.h4bbo.echo.storage.models.room.RoomData;
import net.h4bbo.echo.storage.models.user.UserData;

import java.util.List;

public class NavigateMessageEvent extends MessageEvent<NavigatorPlugin> {
    private final INavigatorService navigatorService;
    private final IRoomService roomService;

    public NavigateMessageEvent(INavigatorService navigatorService, IRoomService roomService) {
        this.navigatorService = navigatorService;
        this.roomService = roomService;
    }

    @Override
    public void handle(IPlayer player, IClientCodec msg) {
        var playerData = player.attr(UserData.DATA_KEY).get();

        boolean hideFulLRooms = msg.get(DataCodec.BOOL);
        int categoryId = msg.get(DataCodec.VL64_INT);

        var navigatorCategoryOpt = this.navigatorService.getCategories().stream()
                .filter(x -> x.getId() == categoryId)
                .findFirst();

        if (navigatorCategoryOpt.isEmpty()) {
            return;
        }

        var navigatorCategory = navigatorCategoryOpt.get();

        if (navigatorCategory.getRankId() > playerData.getRank()) {
            return;
        }

        List<RoomData> roomList = this.roomService.getRoomsByCategory(navigatorCategory.getId());
        var isPublicRoomCategory = this.getPlugin().getNavigatorManager().isPublicRoomCategory(navigatorCategory.getId());
        
        var codec = PacketCodec.create(220)
                .append(DataCodec.BOOL, hideFulLRooms)
                .append(DataCodec.VL64_INT, navigatorCategory.getId())
                .append(DataCodec.VL64_INT, isPublicRoomCategory ? 0 : 2)
                .append(DataCodec.STRING, navigatorCategory.getName())
                .append(DataCodec.VL64_INT, 0) // TODO: Current visitors
                .append(DataCodec.VL64_INT, 25) // TODO: Max visitors
                .append(DataCodec.VL64_INT, navigatorCategory.getParentId());

        if (!isPublicRoomCategory) {
            codec = codec.append(DataCodec.VL64_INT, roomList.size());
        }

        for (var room : roomList) {
            if (isPublicRoomCategory) {
                int door = 0;
                String description = room.getDescription();

                if (room.getDescription().contains("/")) {
                    String[] data = description.split("/");
                    description = data[0];
                    door = Integer.parseInt(data[1]);
                }

                codec = codec
                        .append(DataCodec.VL64_INT, room.getId() + RoomConsts.PUBLIC_ROOM_OFFSET)
                        .append(DataCodec.VL64_INT, 1)
                        .append(DataCodec.STRING, room.getName())
                        .append(DataCodec.VL64_INT, room.getVisitorsNow())
                        .append(DataCodec.VL64_INT, room.getVisitorsMax())
                        .append(DataCodec.VL64_INT, room.getCategoryId())
                        .append(DataCodec.STRING, description)
                        .append(DataCodec.VL64_INT, room.getId())
                        .append(DataCodec.VL64_INT, door)
                        .append(DataCodec.STRING, room.getCcts() == null ? "" : room.getCcts())
                        .append(DataCodec.VL64_INT, 0)
                        .append(DataCodec.VL64_INT, 1);
            } else {
                codec = codec
                        .append(DataCodec.VL64_INT, room.getId())
                        .append(DataCodec.STRING, room.getName())
                        .append(DataCodec.STRING, room.getOwnerName())
                        .append(DataCodec.STRING, "open")
                        .append(DataCodec.VL64_INT, room.getVisitorsNow())
                        .append(DataCodec.VL64_INT, room.getVisitorsMax())
                        .append(DataCodec.STRING, room.getDescription());
            }
        }

        var subCategories = this.navigatorService.getCategories().stream()
                .filter(x -> x.getParentId() == navigatorCategory.getId() &&
                        playerData.getRank() >= x.getRankId())
                .toList();

        for (var category : subCategories) {
            codec = codec
                    .append(DataCodec.VL64_INT, category.getId())
                    .append(DataCodec.VL64_INT, 0)
                    .append(DataCodec.STRING, category.getName())
                    .append(DataCodec.VL64_INT, 0) // TODO: Current visitors
                    .append(DataCodec.VL64_INT, 25) // TODO: Max visitors
                    .append(DataCodec.VL64_INT, navigatorCategory.getId());
        }

        codec.send(player);
    }

    @Override
    public int getHeaderId() {
        return 150;
    }
}