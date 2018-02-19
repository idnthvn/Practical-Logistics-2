package sonar.logistics.info.types;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.register.LogicPath;

@LogicInfoType(id = InfoError.id, modid = PL2Constants.MODID)
public class InfoError implements IInfo<InfoError> {

	public static final InfoError noData = new InfoError("NO DATA");
	public static final InfoError noMonitor = new InfoError("NO MONITOR");
	public static final InfoError noItem = new InfoError("NO ITEMSTACK");
	public static final InfoError incompleteDisplay = new InfoError("INCOMPLETE");

	public static final String id = "error";
	public String error;

	public InfoError() {}

	public InfoError(String error) {
		this.error = FontHelper.translate(error == null ? "" : error);
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(InfoError info) {
		return info.error.equals(error);
	}

	@Override
	public boolean isMatchingInfo(InfoError info) {
		return true;
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof InfoError;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public InfoError copy() {
		return new InfoError(error);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		return nbt;
	}

	@Override
	public void identifyChanges(InfoError newInfo) {}

	@Override
	public LogicPath getPath() {
		return null;
	}

	@Override
	public InfoError setPath(LogicPath path) {
		return this;
	}
	
	@Override
	public void onInfoStored() {}

}
