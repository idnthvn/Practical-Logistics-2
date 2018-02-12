package sonar.logistics.common.multiparts.displays;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.SonarHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.DisplayElementList;
import sonar.logistics.api.displays.elements.TextDisplayElement;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.client.gui.GuiDisplayScreen.GuiState;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.displays.ConnectedDisplayHandler;
import sonar.logistics.networking.displays.ConnectedDisplayHandler.ConnectedDisplayChange;
import sonar.logistics.packets.PacketConnectedDisplayUpdate;

public class TileLargeDisplayScreen extends TileAbstractDisplay implements ILargeDisplay {

	// public int registryID = -1;
	public ConnectedDisplay overrideDisplay = null;
	public SyncTagType.BOOLEAN shouldRender = (BOOLEAN) new SyncTagType.BOOLEAN(3); // if this is the top left screen
	public SyncTagType.INT connected_display_ID = (INT) new SyncTagType.INT(4).setDefault(-1); // id of the connected display.
	public SyncTagType.BOOLEAN isLocked = (BOOLEAN) new SyncTagType.BOOLEAN(5); // if this screen was locked, must also be stored in screens as sometimes they connect before the connected display is loaded

	{
		syncList.addParts(shouldRender, connected_display_ID, isLocked);
	}

	public void updateDefaultInfo() {
		if (this.getConnectedDisplay() != null && shouldRender()) {
			super.updateDefaultInfo();
		}
	}

	//// IInfoDisplay \\\\

	@Override
	public int getInfoContainerID() {
		return getConnectedDisplay().getInfoContainerID();
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		// overrideDisplay = PL2.getInfoManager(world.isRemote).getOrCreateDisplayScreen(getWorld(), this, getRegistryID());
	}

	@Override
	public DisplayGSI getGSI() {
		return getConnectedDisplay().getGSI();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.SCREEN;
	}

	//// ILargeDisplay \\\\

	@Override
	public int getRegistryID() {
		return connected_display_ID.getObject();
	}

	@Override
	public void setRegistryID(int id) {
		connected_display_ID.setObject(id);
		// SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
	}

	@Override
	public ConnectedDisplay getConnectedDisplay() {
		if (isClient() && overrideDisplay != null) {
			return overrideDisplay;
		}
		return PL2.getInfoManager(world.isRemote).getOrCreateDisplayScreen(getWorld(), this, getRegistryID());
	}

	@Override
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay) {
		overrideDisplay = connectedDisplay;
	}

	@Override
	public boolean shouldRender() {
		return shouldRender.getObject() && getRegistryID() != -1 && getConnectedDisplay() != null;
	}

	@Override
	public void setShouldRender(boolean shouldRender) {
		if (shouldRender != this.shouldRender.getObject()) {
			this.shouldRender.setObject(shouldRender);
		}
	}

	//// ILogicViewable \\\\

	//// NETWORK \\\\
	public void onNetworkConnect(ILogisticsNetwork network) {
		super.onNetworkConnect(network);
		ConnectedDisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.SUB_NETWORK_CHANGED);
	}

	public void onNetworkDisconnect(ILogisticsNetwork network) {
		super.onNetworkDisconnect(network);
		ConnectedDisplayHandler.instance().markConnectedDisplayChanged(getRegistryID(), ConnectedDisplayChange.SUB_NETWORK_CHANGED);
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		boolean cableFace = (dir == getCableFace() || dir == getCableFace().getOpposite());
		boolean cableConnection = (internal && cableFace && type == ConnectableType.CONNECTABLE);

		if (cableConnection || (!cableFace && type == this.getConnectableType() && (getRegistryID() == registryID || !(isLocked())))) {
			return CableConnectionType.NETWORK;
		}
		return CableConnectionType.NONE;
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Large Display ID: " + connected_display_ID);
		info.add("Should Render " + this.shouldRender.getObject());
	}

	//// EVENTS \\\\
	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			ConnectedDisplayHandler.instance().queueDisplayAddition(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			ConnectedDisplayHandler.instance().queueDisplayRemoval(this);
		}
	}

	public boolean isLocked() {
		return this.isLocked.getObject() || getConnectedDisplay() != null && getConnectedDisplay().isLocked.getObject();
	}
	//// PACKETS \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		if (type.isType(SyncType.SPECIAL)) {
			getGSI().writeData(tag, SyncType.SAVE);
		}
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		if (type.isType(SyncType.SPECIAL)) {
			getGSI().readData(tag, SyncType.SAVE);			
		}
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		switch (id) {
		case 5:
			shouldRender.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		switch (id) {
		case 5:
			this.shouldRender.readFromBuf(buf);
			break;
		case 6:
			ConnectedDisplayHandler.setDisplayLocking(getRegistryID(), !getConnectedDisplay().isLocked.getObject());
			break;
		}
	}

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		// TileAbstractDisplay part = (TileAbstractDisplay) getConnectedDisplay().getTopLeftScreen();
		// return new ContainerMultipartSync(part);
		return null;
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		// TileAbstractDisplay part = (TileAbstractDisplay) getConnectedDisplay().getTopLeftScreen();
		// return new GuiDisplayScreen(part, part.getGSI(), GuiState.values()[id], tag.getInteger("infopos"));
		return null;
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		if (!world.isRemote) {
			DisplayElementContainer cont = getGSI().addElementContainer(new double[] { 0, 0, 0 }, getGSI().getDisplayScaling(), 0.5);
			//cont.getElements().addElement(new TextDisplayElement(cont, "HELLO SAVE ME!"));
			
			List<String> strings = SonarHelper.convertArray(FontHelper.translate(TextFormatting.BOLD + ""+ TextFormatting.UNDERLINE + "Videotape by Radiohead"+ "-" + "When I'm at the pearly gates-This will be on my videotape, my videotape-Mephistopheles is just beneath-And he's reaching up to grab me- + -This is one for the good days-And I have it all here-In red, blue, green-Red, blue, green- + -You are my center-When I spin away-Out of control on videotape-On videotape-On videotape-On videotape-On videotape-On videotape- + -This is my way of saying goodbye-Because I can't do it face to face-I'm talking to you after it's too late-No matter what happens now-You shouldn't be afraid-Because I know today has been-the most perfect day I've ever seen").split("-"));
			DisplayElementList list = new DisplayElementList();
			for(String t : strings){
				list.getElements().addElement(new TextDisplayElement(t));
			}
			cont.getElements().addElement(list);
		}
		GuiState state = GuiState.values()[id];
		TileAbstractDisplay part = (TileAbstractDisplay) getConnectedDisplay().getTopLeftScreen();
		SonarMultipartHelper.sendMultipartSyncToPlayer(part, (EntityPlayerMP) player);
		PL2.network.sendTo(new PacketConnectedDisplayUpdate(getConnectedDisplay(), getRegistryID()), (EntityPlayerMP) player);
		if (state.needsSources())
			PacketHelper.sendLocalProvidersFromScreen(part, world, pos, player);

	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}

	@Override
	public boolean isBlocked(EnumFacing dir) {
		return false;
	}

	@Override
	public void updateCableRenders() {}

	@Override
	public void setLocked(boolean locked) {
		this.isLocked.setObject(locked);
	}
}
