package sonar.logistics.core.tiles.displays.info.types;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.IComparableInfo;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.INameableInfo;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;

import java.util.List;

@ASMInfo(id = ClockInfo.id, modid = PL2Constants.MODID)
public class ClockInfo extends BaseInfo<ClockInfo> implements IInfo<ClockInfo>, INBTSyncable, INameableInfo<ClockInfo>, IComparableInfo<ClockInfo> {

	public static final String id = "clock";
	public int compare;
	public SyncTagType.DOUBLE firstNum = new SyncTagType.DOUBLE(1), secondNum = new SyncTagType.DOUBLE(2);
	public SyncTagType.STRING clockString = new SyncTagType.STRING(3);

	{
		syncList.addParts(firstNum, secondNum, clockString);
	}

	public ClockInfo() {}

	public ClockInfo(double firstNum, double secondNum, String clockString) {
		this.firstNum.setObject(firstNum);
		this.secondNum.setObject(secondNum);
		this.clockString.setObject(clockString);
	}

	@Override
	public String getClientIdentifier() {
		return "Time";
	}

	@Override
	public String getClientObject() {
		if (!isValid()) {
			return "ERROR";
		}
		return clockString.getObject();
	}

	@Override
	public String getClientType() {
		return "time";
	}

	@Override
	public boolean isIdenticalInfo(ClockInfo info) {
		return info.firstNum.getObject().equals(firstNum.getObject()) && info.secondNum.getObject().equals(secondNum.getObject());
	}

	@Override
	public boolean isMatchingInfo(ClockInfo info) {
		return true;
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof ClockInfo;
	}

	@Override
	public boolean isValid() {
		return firstNum.getObject() != null && firstNum.getObject() != null && clockString.getObject() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public ClockInfo copy() {
		return new ClockInfo(firstNum.getObject(), secondNum.getObject(), clockString.getObject());
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "isEmitting", firstNum.getObject().equals(secondNum.getObject())));
		return objects;
	}
}