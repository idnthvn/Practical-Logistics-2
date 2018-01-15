package sonar.logistics;

import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.networking.connections.CableConnectionHandler;
import sonar.logistics.networking.connections.WirelessDataHandler;
import sonar.logistics.worlddata.ConnectedDisplayData;
import sonar.logistics.worlddata.IdentityCountData;

public class PL2Events {

	public static final int saveDimension = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			CableConnectionHandler.instance().tick();			
			PL2.getNetworkManager().tick();
			PL2.getServerManager().onServerTick();
			WirelessDataHandler.tick();
			PL2.getDisplayManager().tick();
			TileArray.entityChanged = false;
			
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			storage.getOrLoadData(ConnectedDisplayData.class, ConnectedDisplayData.tag);
			storage.getOrLoadData(IdentityCountData.class, IdentityCountData.tag);

		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == saveDimension) {
			MapStorage storage = event.getWorld().getPerWorldStorage();
			ConnectedDisplayData displayData = (ConnectedDisplayData) storage.getOrLoadData(ConnectedDisplayData.class, ConnectedDisplayData.tag);
			if (displayData == null) {
				storage.setData(ConnectedDisplayData.tag, new ConnectedDisplayData(ConnectedDisplayData.tag));
			}

			IdentityCountData countData = (IdentityCountData) storage.getOrLoadData(IdentityCountData.class, IdentityCountData.tag);
			if (countData == null) {
				storage.setData(IdentityCountData.tag, new IdentityCountData(IdentityCountData.tag));
			}

		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		TileArray.entityChanged = true;
	}
}