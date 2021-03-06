package sonar.logistics.base.gui.buttons;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.SonarButtons.AnimatedButton;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.PL2Translate;

import java.util.List;

@SideOnly(Side.CLIENT)
public class LogisticsButton extends AnimatedButton {

	public static final ResourceLocation logisticsButtons = new ResourceLocation(PL2Constants.MODID + ":textures/gui/filter_buttons.png");
	public static class CHANNELS extends LogisticsButton{

		public CHANNELS(GuiSonar sonar, int id, int x, int y) {
			super(sonar, id, x, y, 32, 96 + 16, PL2Translate.BUTTON_CHANNELS.t(), "button.Channels");
		}
		
	}
	
	public static class HELP extends LogisticsButton{

		public HELP(GuiSonar sonar, int id, int x, int y) {
			super(sonar, id, x, y, 32, 160 + 32 + (GuiHelpOverlay.enableHelp ? 16 : 0), PL2Translate.BUTTON_HELP_ENABLED.t() + ": " + GuiHelpOverlay.enableHelp, "button.HelpButton");
		}
		
	}
	
	public GuiSonar sonar;
	public String buttonText;
	public int texX, texY;
	public List<String> description;

	public LogisticsButton(GuiSonar sonar, int id, int x, int y, int texX, int texY, String buttonText, String helpKey) {
		this(sonar, id, x, y, texX, texY, 16, 16, buttonText, helpKey);
	}

	public LogisticsButton(GuiSonar sonar, int id, int x, int y, int texX, int texY, int xSize, int ySize, String buttonText, String helpKey) {
		super(id, x, y, logisticsButtons, xSize - 1, ySize - 1);
		this.description = Lists.newArrayList(buttonText);
		List<String> array = SonarHelper.convertArray(FontHelper.translate(helpKey).split("-"));

		for (String des : array) {
			if (!des.isEmpty())
				description.add(TextFormatting.GRAY + des);
		}

		this.sonar = sonar;
		this.buttonText = buttonText;
		this.texX = texX;
		this.texY = texY;
	}

	@Override
	public void drawButtonForegroundLayer(int x, int y) {
		if (hovered) {
			if (GuiHelpOverlay.enableHelp) {
				sonar.drawSpecialToolTip(description, sonar.getGuiLeft() + x, sonar.getGuiTop() + y, null);
			} else {
				sonar.drawSonarCreativeTabHoveringText(buttonText, x, y);
			}
		}
	}

	@Override
	public void onClicked() {
		
	}

	@Override
	public int getTextureX() {
		return texX;
	}

	@Override
	public int getTextureY() {
		return texY;
	}

}