package com.brandon3055.brandonscore.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 26/08/2016.
 * This is a dynamic inventory that will automatically expand its size to told any number of item stacks.
 */
public class InventoryDynamic implements IInventory {

    private LinkedList<ItemStack> stacks = new LinkedList<ItemStack>();
    public int xp = 0;

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0){
            return;
        }

        if (stack.isEmpty()) {
            if (index < stacks.size()){
                stacks.remove(index);
            }
        }
        else if (index < stacks.size()) {
            stacks.set(index, stack);
        }
        else {
            stacks.add(stack);
        }
    }

    @Override
    public int getSizeInventory() {
        return stacks.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return stacks.size() == 0;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
    }

    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = getStackInSlot(index);

        if (!itemstack.isEmpty()) {
            if (itemstack.getCount() <= count) {
                setInventorySlotContents(index, ItemStack.EMPTY);
            } else {
                itemstack = itemstack.split(count);
                if (itemstack.getCount() == 0) {
                    setInventorySlotContents(index, ItemStack.EMPTY);
                }
            }
        }
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);

        if (!stack.isEmpty()) {
            setInventorySlotContents(index, ItemStack.EMPTY);
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        stacks.removeIf(ItemStack::isEmpty);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }



    @Override
    public void clear() {
        stacks.clear();
    }



    public void writeToNBT(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && stack.getCount() > 0) {
                CompoundNBT tag = new CompoundNBT();
                stack.write(tag);
                list.add(tag);
            }
        }

        compound.put("InvItems", list);
    }

    public void readFromNBT(CompoundNBT compound) {
        ListNBT list = compound.getList("InvItems", 10);
        stacks.clear();

        for (int i = 0; i < list.size(); i++) {
            stacks.add(ItemStack.read(list.getCompound(i)));
        }
    }

    public void removeIf(Predicate<ItemStack> filter) {
        stacks.removeIf(filter);
    }
}
