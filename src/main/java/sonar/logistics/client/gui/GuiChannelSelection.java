package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.FontHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.logistics.Logistics;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoRenderer;

public class GuiChannelSelection extends GuiSelectionList<MonitoredBlockCoords> {

	public EntityPlayer player;
	public IChannelledTile tile;
	public int channelID;

	public GuiChannelSelection(EntityPlayer player, IChannelledTile tile, int channelID) {
		super(new ContainerChannelSelection(tile), tile);
		this.player = player;
		this.tile = tile;
		this.channelID = channelID;
		this.xSize = 182 + 66;
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(FontHelper.translate("Channel Selection"), xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre(String.format("Select the channels you wish to monitor"), xSize, 18, LogisticsColours.grey_text);
	}

	public void selectionPressed(GuiButton button, int infoPos, int buttonID, MonitoredBlockCoords info) {
		if (buttonID == 0) {
			tile.modifyCoords(info, channelID);
		} else {
			RenderBlockSelection.addPosition(info.syncCoords.getCoords(), false);
		}
	}

	public void setInfo() {
		infoList = (ArrayList<MonitoredBlockCoords>) Logistics.getClientManager().coordMap.getOrDefault(tile.getNetworkID(), MonitoredList.<MonitoredBlockCoords>newMonitoredList(tile.getNetworkID())).clone();
	}

	@Override
	public boolean isCategoryHeader(MonitoredBlockCoords info) {
		if (!RenderBlockSelection.positions.isEmpty()) {
			if (RenderBlockSelection.isPositionRenderered(info.syncCoords.getCoords())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(MonitoredBlockCoords info) {
		if (info.isValid() && !info.isHeader() && tile.getChannels().contains(info.syncCoords.getCoords())) {
			return true;
		}
		return false;
	}

	@Override
	public void renderInfo(MonitoredBlockCoords info, int yPos) {
		InfoRenderer.renderMonitorInfoInGUI(info, yPos + 1, LogisticsColours.white_text.getRGB());
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(MonitoredBlockCoords info) {
		return false;
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && tile instanceof IFlexibleGui && SonarCore.instance.guiHandler.lastScreen != null) {
			SonarCore.network.sendToServer(new PacketFlexibleCloseGui(tile.getCoords().getBlockPos()));
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
}
