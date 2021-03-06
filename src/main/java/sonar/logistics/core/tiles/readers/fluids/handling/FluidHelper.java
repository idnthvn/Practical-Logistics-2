package sonar.logistics.core.tiles.readers.fluids.handling;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.SonarCore;
import sonar.core.api.fluids.ISonarFluidHandler;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.utils.ActionType;
import sonar.core.handlers.fluids.FluidHelper.ITankFilter;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.utils.CacheType;
import sonar.logistics.core.tiles.connections.data.network.NetworkHelper;

import java.util.List;
import java.util.function.BiPredicate;

public class FluidHelper {

	public static StoredFluidStack transferFluids(ILogisticsNetwork network, StoredFluidStack add, NodeTransferMode mode, ActionType action, ITankFilter filter) {
		if (!validStack(add)) {
			return add;
		}
		return NetworkHelper.forEachTileEntity(network, CacheType.ALL, c -> c.canTransferFluid(c, add, mode), getTileAction(add, mode, action, filter)) ? add : null;
	}

	private static BiPredicate<BlockConnection, TileEntity> getTileAction(StoredFluidStack stack, NodeTransferMode mode, ActionType action, ITankFilter filter) {
		return (c, t) -> stack.setStackSize(transfer(mode, t, stack, c.face, action)).getStackSize() != 0;
	}

	private static StoredFluidStack transfer(NodeTransferMode mode, TileEntity tile, StoredFluidStack stack, EnumFacing dir, ActionType action) {
		List<ISonarFluidHandler> handlers = SonarCore.fluidHandlers;
		for (ISonarFluidHandler handler : handlers) {
			if (handler.canHandleFluids(tile, dir)) {
				StoredFluidStack copy = stack.copy().setStackSize(stack);
				return stack = mode.shouldRemove() ? handler.removeStack(copy, tile, dir, action) : handler.addStack(stack, tile, dir, action);
			}
		}
		return null;
	}

	public static boolean validStack(StoredFluidStack stack) {
		return stack != null && stack.getStackSize() != 0;
	}
	
	/*
	public int fillCapabilityStack(ItemStack container, StoredFluidStack fill, ILogisticsNetwork handling, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
			return container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).fill(fill.getFullStack(), !action.shouldSimulate());
		}
		return 0;
	}

	/** if simulating your expected to pass copies of both the container and stack to fill with 
	public FluidStack drainCapabilityStack(ItemStack container, int toDrain, ILogisticsNetwork handling, ActionType action) {
		if (container.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
			IFluidHandler handler = container.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

			FluidStack stack = handler.getTankProperties()[0].getContents();
			if (stack != null && stack.amount >= 0) {
				StoredFluidStack add = new StoredFluidStack(stack, Math.min(toDrain, stack.amount));
				StoredFluidStack added = SonarAPI.getFluidHelper().getStackToAdd(toDrain, add, transferFluids(handling, add.copy(), NodeTransferMode.ADD, ActionType.SIMULATE, null));
				if (added == null || added.stored >= 0) {
					return handler.drain((int) added.stored, !action.shouldSimulate());
				}
			}
			return null;
		}
		return null;
	}

	public void fillHeldItem(EntityPlayer player, ILogisticsNetwork cache, StoredFluidStack toFill) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem.isEmpty() || toFill == null) {
			return;
		}
		StoredFluidStack remaining = transferFluids(cache, toFill.copy(), NodeTransferMode.REMOVE, ActionType.SIMULATE, null);
		StoredFluidStack removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, remaining);
		if (removed.stored <= 0) {
			return;
		}
		int filled = fillCapabilityStack(heldItem.copy(), removed, cache, ActionType.SIMULATE);
		if (filled != 0) {
			ItemStack toAdd = heldItem.copy();
			removed = SonarAPI.getFluidHelper().getStackToAdd(toFill.getStackSize(), toFill, transferFluids(cache, new StoredFluidStack(toFill.getFullStack(), filled, toFill.capacity), NodeTransferMode.REMOVE, ActionType.PERFORM, null));
			int fill = fillCapabilityStack(heldItem, removed, cache, ActionType.PERFORM);
			/*
			if (player.getHeldItemMainhand().getCount() != 1) {
				player.inventories.decrStackSize(player.inventories.currentItem, 1);
				PL2API.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventories.setInventorySlotContents(player.inventories.currentItem, toAdd);
			}
			
		}
	}

	public void drainHeldItem(EntityPlayer player, ILogisticsNetwork cache, int toDrain) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem.isEmpty() || toDrain <= 0) {
			return;
		}
		FluidStack drained = drainCapabilityStack(heldItem.copy(), toDrain, cache, ActionType.SIMULATE);
		if (drained != null && drained.amount > 0) {
			ItemStack toAdd = heldItem.copy();
			transferFluids(cache, new StoredFluidStack(drainCapabilityStack(toAdd, toDrain, cache, ActionType.PERFORM)), NodeTransferMode.ADD, ActionType.PERFORM, null);
			if (heldItem.getCount() != 1) {
				player.inventories.decrStackSize(player.inventories.currentItem, 1);
				PL2API.getItemHelper().addStackToPlayer(new StoredItemStack(toAdd), player, false, ActionType.PERFORM);
			} else {
				player.inventories.setInventorySlotContents(player.inventories.currentItem, toAdd);
			}
		}
	}
	*/
	/*
	public static void transferFluids(NodeTransferMode mode, BlockConnection filter, BlockConnection connection) {
		TileEntity filterTile = filter.coords.getTileEntity();
		TileEntity netTile = connection.coords.getTileEntity();
		if (filterTile != null && netTile != null) {
			EnumFacing dirFrom = mode.shouldRemove() ? filter.face : connection.face;
			EnumFacing dirTo = !mode.shouldRemove() ? filter.face : connection.face;
			TileEntity from = mode.shouldRemove() ? filterTile : netTile;
			TileEntity to = !mode.shouldRemove() ? filterTile : netTile;
			ConnectionFilters filters = new ConnectionFilters(null, filter, connection);

			SonarAPI.getFluidHelper().transferFluids(from, to, dirFrom.getOpposite(), dirTo.getOpposite(), filters);
		}
	}
	*/
}
