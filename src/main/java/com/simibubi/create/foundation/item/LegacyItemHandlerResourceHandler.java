package com.simibubi.create.foundation.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Transactional transfer view of a Create-owned legacy item handler.
 *
 * <p>The old {@code IItemHandler} API has no transaction support. Keeping the
 * pending contents in the transfer handler until the root transaction commits
 * is therefore essential: a simulated or aborted transfer must never leak a
 * change into the block entity inventory.</p>
 */
public class LegacyItemHandlerResourceHandler extends ItemStacksResourceHandler {
	private final IItemHandler handler;

	public LegacyItemHandlerResourceHandler(IItemHandler handler) {
		super(copy(handler));
		this.handler = handler;
	}

	private static NonNullList<ItemStack> copy(IItemHandler handler) {
		NonNullList<ItemStack> contents = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
		for (int slot = 0; slot < contents.size(); slot++)
			contents.set(slot, handler.getStackInSlot(slot).copy());
		return contents;
	}

	private void refresh() {
		if (isInTransaction())
			return;
		if (handler.getSlots() != stacks.size()) {
			setStacks(copy(handler));
			return;
		}
		for (int slot = 0; slot < stacks.size(); slot++)
			stacks.set(slot, handler.getStackInSlot(slot).copy());
	}

	@Override
	public ItemResource getResource(int index) {
		refresh();
		return super.getResource(index);
	}

	@Override
	public long getAmountAsLong(int index) {
		refresh();
		return super.getAmountAsLong(index);
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		refresh();
		return super.getCapacityAsLong(index, resource);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		refresh();
		return handler.isItemValid(index, resource.toStack());
	}

	@Override
	protected int getCapacity(int index, ItemResource resource) {
		return handler.getSlotLimit(index);
	}

	@Override
	protected void onContentsChanged(int index, ItemStack previousContents) {
		ItemStack contents = stacks.get(index);
		if (handler instanceof IItemHandlerModifiable modifiable) {
			modifiable.setStackInSlot(index, contents);
			return;
		}

		// A few Create inventories deliberately expose only the read/insert/extract
		// part of the old API. Apply the transaction only after it committed.
		// This path is not entered for simulations or aborted transactions.
		ItemStack extracted;
		do {
			extracted = handler.extractItem(index, Integer.MAX_VALUE, false);
		} while (!extracted.isEmpty());

		ItemStack remainder = handler.insertItem(index, contents.copy(), false);
		if (!remainder.isEmpty())
			throw new IllegalStateException("Legacy item handler rejected a committed transfer at slot " + index);
	}
}
