package sonar.logistics.info.providers.tile;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import sonar.logistics.api.Info;
import sonar.logistics.api.StandardInfo;
import sonar.logistics.api.data.TileProvider;
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;

public class RFEnergyProvider extends TileProvider {

	public static String name = "Redstone-Flux";
	public String[] categories = new String[] { "ENERGY RF" };
	public String[] subcategories = new String[] { "Connects: ","Current","Max Energy","Stored","Max Stored" };

	@Override
	public String helperName() {
		return "Redstone-Flux";
	}

	@Override
	public boolean canProvideInfo(World world, int x, int y, int z, ForgeDirection dir) {
		TileEntity target = world.getTileEntity(x, y, z);
		return target != null && target instanceof IEnergyConnection;
	}

	@Override
	public void getHelperInfo(List<Info> infoList, World world, int x, int y, int z, ForgeDirection dir) {
		byte id = this.getID();

		TileEntity handler = world.getTileEntity(x, y, z);
		if (handler instanceof IEnergyConnection) {
			IEnergyConnection info = (IEnergyConnection) handler;
			boolean canConnect = info.canConnectEnergy(dir);
			infoList.add(new StandardInfo(id, 0, 0, "" + canConnect));
		}

		if (handler instanceof IEnergyStorage) {

			IEnergyStorage info = (IEnergyStorage) handler;
			infoList.add(new StandardInfo(id, 0, 1, info.getEnergyStored(), "RF"));
			infoList.add(new StandardInfo(id, 0, 2, info.getMaxEnergyStored(), "RF"));

		} else if (handler instanceof IEnergyHandler) {
			IEnergyHandler info = (IEnergyHandler) handler;
			int energyStored = info.getEnergyStored(dir);
			int maxStored = info.getMaxEnergyStored(dir);
			if (maxStored != 0) {
				infoList.add(new StandardInfo(id, 0, 3, energyStored, "RF"));
				infoList.add(new StandardInfo(id, 0, 4, maxStored, "RF"));
			}
		}

	}

	@Override
	public String getCategory(byte id) {
		return categories[id];
	}

	@Override
	public String getSubCategory(byte id) {
		return subcategories[id];
	}
}