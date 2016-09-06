package sonar.logistics.api.connecting;

import java.util.UUID;

import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.IUUIDIdentity;

/**implemented on the Data Emitter*/
public interface IDataEmitter extends ILogicTile, IUUIDIdentity {
	
	/**can the given player UUID connect to this IDataEmitter*/
	public boolean canPlayerConnect(UUID uuid);
	
	public String getEmitterName();

}