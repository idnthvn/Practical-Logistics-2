package sonar.logistics.parts;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import sonar.core.utils.Pair;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.LogicInfo;
import sonar.logistics.api.info.ProgressInfo;
import sonar.logistics.api.info.monitor.IMonitorInfo;
import sonar.logistics.connections.LogicMonitorCache;
import sonar.logistics.connections.MonitoredList;
import sonar.logistics.network.SyncMonitoredType;

public abstract class LogicReaderPart<T extends IMonitorInfo> extends ReaderMultipart<T> {

	private ArrayList<SyncMonitoredType<T>> selected = Lists.newArrayListWithCapacity(getMaxInfo()), paired = Lists.newArrayListWithCapacity(getMaxInfo());
	{
		for (int i = 0; i < getMaxInfo(); i++) {
			selected.add(i, new SyncMonitoredType<T>(i + 10));
			paired.add(i, new SyncMonitoredType<T>(i + 10 + 100));
		}
		selected.forEach(part -> syncParts.add(part));
		paired.forEach(part -> syncParts.add(part));
	}

	public LogicReaderPart(String handlerID) {
		super(handlerID);
	}

	public LogicReaderPart(String handlerID, EnumFacing face) {
		super(handlerID, face);
	}

	public ArrayList<IMonitorInfo> getSelectedInfo() {
		ArrayList<IMonitorInfo> cachedSelected = Lists.<IMonitorInfo>newArrayList();
		selected.forEach(info -> cachedSelected.add(info.getMonitoredInfo()));
		return cachedSelected;
	}

	public ArrayList<IMonitorInfo> getPairedInfo() {
		ArrayList<IMonitorInfo> cachedPaired = Lists.<IMonitorInfo>newArrayList();
		paired.forEach(info -> cachedPaired.add(info.getMonitoredInfo()));
		return cachedPaired;
	}

	@Override
	public void setMonitoredInfo(MonitoredList<T> updateInfo) {
		ArrayList<IMonitorInfo> cachedSelected = this.getSelectedInfo();
		ArrayList<IMonitorInfo> cachedPaired = this.getPairedInfo();
		for (int i = 0; i < this.getMaxInfo(); i++) {
			InfoUUID id = new InfoUUID(getMonitorUUID().hashCode(), i);
			IMonitorInfo selectedInfo = cachedSelected.get(i);
			IMonitorInfo lastInfo = LogicMonitorCache.info.get(id);
			if (selectedInfo != null) {
				IMonitorInfo latestInfo = selectedInfo;
				Pair<Boolean, IMonitorInfo> newInfo = updateInfo.getLatestInfo(selectedInfo);
				if (cachedPaired != null) {
					IMonitorInfo paired = cachedPaired.get(i);
					if (paired != null) {
						Pair<Boolean, IMonitorInfo> newPaired = updateInfo.getLatestInfo(paired);
						if (newInfo.b instanceof LogicInfo && newPaired.b instanceof LogicInfo)
							latestInfo = new ProgressInfo((LogicInfo) newInfo.b, (LogicInfo) newPaired.b);
					}
				}
				if (!newInfo.a && lastInfo != null && lastInfo.isMatchingType(newInfo.b) && !lastInfo.isIdenticalInfo(newInfo.b)) {
					continue;
				} else {
					//latestInfo = newInfo.b;
				}

				LogicMonitorCache.changeInfo(id, latestInfo);
			}else if(lastInfo!=null){
				//set to empty info type
				LogicMonitorCache.changeInfo(id, null);
			}
		}
	}

	// this is kind of messy, could be made better for sure
	public void addInfo(T info, int type, int newPos) {
		ArrayList<SyncMonitoredType<T>> syncInfo = type == 0 ? selected : paired;
		if (newPos == -1) {
			int pos = 0;
			for (SyncMonitoredType<T> sync : syncInfo) {
				if (sync.getMonitoredInfo() != null) {
					if (sync.getMonitoredInfo().isMatchingType(info) && sync.getMonitoredInfo().isMatchingInfo(info)) {
						sync.setInfo(null);
						(type != 0 ? selected : paired).get(pos).setInfo(null);
						sendByteBufPacket(100);
						lastPos = -1;
						return;
					}
				} else if (newPos == -1) {
					newPos = pos;
				}
				pos++;
			}
		}
		if (newPos != -1) {
			lastPos = newPos;
		}
		syncInfo.get(newPos == -1 ? 0 : newPos).setInfo(info);
		sendSyncPacket();
		//sendByteBufPacket(100);
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		if (id == 100) {
			for (SyncMonitoredType<T> sync : selected) {
				sync.writeToBuf(buf);
			}
			for (SyncMonitoredType<T> sync : paired) {
				sync.writeToBuf(buf);
			}
		}
		switch (id) {
		case ADD:
		case PAIRED:
			selectedInfo.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		if (id == 100) {
			for (SyncMonitoredType<T> sync : selected) {
				sync.readFromBuf(buf);
			}
			for (SyncMonitoredType<T> sync : paired) {
				sync.readFromBuf(buf);
			}
		}
		switch (id) {
		case PAIRED:
			selectedInfo.readFromBuf(buf);
			addInfo((T) selectedInfo.info, 1, lastPos);
			break;
		case ADD:
			selectedInfo.readFromBuf(buf);
			addInfo((T) selectedInfo.info, 0, -1);
			break;
		}

	}
}
