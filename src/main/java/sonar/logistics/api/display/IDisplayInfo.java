package sonar.logistics.api.display;

import java.util.ArrayList;

import javax.annotation.Nullable;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.RenderInfoProperties;
import sonar.logistics.api.info.monitor.IMonitorInfo;

/** used within a IInfoContainer */
public interface IDisplayInfo extends INBTSyncable {

	/** sets the info UUID to get cached info from */
	public void setUUID(InfoUUID infoUUID);

	/** the currently cached info, obtained via the Client Cache, this can be null */
	@Nullable
	public IMonitorInfo getCachedInfo();

	/** the currently cached info UUID, obtained via the Client Cache, this can be null */
	@Nullable
	public InfoUUID getInfoUUID();

	/** the current text colour */
	public CustomColour getTextColour();

	/** the current background colour */
	public CustomColour getBackgroundColour();

	/** the info current render properties */
	public RenderInfoProperties getRenderProperties();
	
	public void setFormatStrings(ArrayList<String> strings);
	
	public ArrayList<String> getUnformattedStrings();
	
	public ArrayList<String> getFormattedStrings();
}
