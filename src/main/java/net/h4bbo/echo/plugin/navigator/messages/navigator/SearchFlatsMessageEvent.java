package net.h4bbo.echo.plugin.navigator.messages.navigator;

import net.h4bbo.echo.api.game.player.IPlayer;
import net.h4bbo.echo.api.messages.MessageEvent;
import net.h4bbo.echo.api.network.codecs.DataCodec;
import net.h4bbo.echo.api.network.codecs.IClientCodec;
import net.h4bbo.echo.api.services.room.IRoomService;
import net.h4bbo.echo.codecs.PacketCodec;
import net.h4bbo.echo.plugin.navigator.NavigatorPlugin;
import net.h4bbo.echo.storage.models.room.RoomData;
import net.h4bbo.echo.storage.models.user.UserData;

import java.util.List;

public class SearchFlatsMessageEvent extends MessageEvent<NavigatorPlugin> {
    private final IRoomService roomService;

    public SearchFlatsMessageEvent(IRoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void handle(IPlayer player, IClientCodec msg) {
        var playerData = player.attr(UserData.DATA_KEY).get();
        String stringQuery = msg.get(DataCodec.STRING);

        List<RoomData> roomList = this.roomService.search(stringQuery);

        if (roomList.isEmpty()) {
            PacketCodec.create(58)
                    .send(player);
            return;
        }

        var codec = PacketCodec.create(55);

        for (var room : roomList) {
                codec = codec
                        .append(DataCodec.BYTES, room.getId())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, room.getName())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, room.getOwnerName())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, "open")
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, "x")
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, room.getVisitorsNow())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, room.getVisitorsMax())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, "null")
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, room.getDescription())
                        .append(DataCodec.BYTES, (char) 9)
                        .append(DataCodec.BYTES, (char) 13);

        }

        codec.send(player);
    }

    @Override
    public int getHeaderId() {
        return 17;
    }
}