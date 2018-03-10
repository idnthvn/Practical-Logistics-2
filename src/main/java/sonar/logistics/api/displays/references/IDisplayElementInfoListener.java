package sonar.logistics.api.displays.references;

import java.util.List;

import sonar.logistics.api.info.InfoUUID;

public interface IDisplayElementInfoListener {

	public boolean isDisplaying(InfoUUID uuid);
	
	public List<InfoUUID> getDisplayingUUIDs();
}