package net.pneumono.gravestones;

/*
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
 */

public class TrinketsSupport {
    // Most of this code is provided courtesy of wouter173, thank you!
    // Commented out for the time being until Trinkets updates to 1.21

    /*
    protected static void register() {
        GravestonesApi.registerModSupport(new ModSupport() {
            @Override
            public void insertData(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {
                TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
                if (trinketComponent == null) {
                    GravestoneCreation.logger("Player does not have trinkets, so no trinkets were inserted");
                    return;
                }

                List<Pair<SlotReferencePrimitive, ItemStack>> filteredTrinkets = trinketComponent
                        .getAllEquipped()
                        .stream()
                        .filter(pair -> EnchantmentHelper.getLevel(Enchantments.VANISHING_CURSE, pair.getRight()) == 0)
                        .map(pair -> {
                            SlotType slotType = pair.getLeft().inventory().getSlotType();
                            SlotReferencePrimitive slotReferencePrimitive = new SlotReferencePrimitive(slotType.getGroup(), slotType.getName());
                            return new Pair<>(slotReferencePrimitive, pair.getRight());
                        })
                        .toList();

                entity.addOrReplaceModData("trinkets", serializeSlotData(filteredTrinkets));
                trinketComponent.getInventory().clear();
            }

            @Override
            public void onBreak(TechnicalGravestoneBlockEntity entity) {
                World world = entity.getWorld();
                if (world != null && !world.isClient()) {
                    List<Pair<SlotReferencePrimitive, ItemStack>> gravestoneTrinkets = deserializeSlotData(entity.getModData("trinkets"));
                    BlockPos pos = entity.getPos();

                    for (Pair<SlotReferencePrimitive, ItemStack> pair : gravestoneTrinkets) {
                        ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), pair.getRight());
                        world.spawnEntity(item);
                    }
                }
            }

            @Override
            public void onCollect(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {
                GravestoneCreation.logger("Returning trinkets...");

                World world = player.getWorld();
                BlockPos pos = entity.getPos();
                List<Pair<SlotReferencePrimitive, ItemStack>> gravestoneTrinkets = deserializeSlotData(entity.getModData("trinkets"));
                TrinketComponent playerTrinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);

                for (Pair<SlotReferencePrimitive, ItemStack> pair : gravestoneTrinkets) {
                    ItemStack stack = pair.getRight();
                    if (playerTrinketComponent != null) {
                        SlotReferencePrimitive slot = pair.getLeft();

                        boolean moved = moveTrinketToPlayer(player, slot, stack);
                        if (moved) {
                            continue;
                        }
                    }
                    ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    world.spawnEntity(item);
                }
                entity.addOrReplaceModData("trinkets", new NbtCompound());
            }

            private boolean moveTrinketToPlayer(PlayerEntity player, SlotReferencePrimitive slot, ItemStack stack) {
                TrinketInventory playerTrinketInventory = getTrinketInventory(player, slot.groupName(), slot.slotName());
                if (playerTrinketInventory != null) {
                    for (int i = 0; i < playerTrinketInventory.size(); i++) {
                        if (playerTrinketInventory.getStack(i).isEmpty()) {
                            playerTrinketInventory.setStack(i, stack);
                            return true;
                        }
                    }
                }

                return false;
            }

            private TrinketInventory getTrinketInventory(PlayerEntity player, String groupId, String slotId) {
                Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
                if (optional.isPresent()) {
                    Map<String, TrinketInventory> group = optional.get().getInventory().get(groupId);

                    if (group != null) {
                        return group.get(slotId);
                    }
                }
                return null;
            }
        });
    }

    public static NbtCompound serializeSlotData(List<Pair<SlotReferencePrimitive, ItemStack>> slotData) {
        NbtCompound compoundTag = new NbtCompound();
        NbtList listTag = new NbtList();

        for (Pair<SlotReferencePrimitive, ItemStack> pair : slotData) {
            NbtCompound slotTag = new NbtCompound();
            slotTag.putString("slotName", pair.getLeft().slotName());
            slotTag.putString("groupName", pair.getLeft().groupName());

            NbtCompound itemStackTag = new NbtCompound();
            pair.getRight().writeNbt(itemStackTag);
            slotTag.put("itemStack", itemStackTag);

            listTag.add(slotTag);
        }

        compoundTag.put("slotData", listTag);
        return compoundTag;
    }

    public static List<Pair<SlotReferencePrimitive, ItemStack>> deserializeSlotData(NbtCompound compoundTag) {
        List<Pair<SlotReferencePrimitive, ItemStack>> slotData = new ArrayList<>();
        NbtList listTag = compoundTag.getList("slotData", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < listTag.size(); i++) {
            NbtCompound slotTag = listTag.getCompound(i);

            String slotName = slotTag.getString("slotName");
            String groupName = slotTag.getString("groupName");
            SlotReferencePrimitive slotReference = new SlotReferencePrimitive(groupName, slotName);

            NbtCompound itemStackTag = slotTag.getCompound("itemStack");
            ItemStack itemStack = ItemStack.fromNbt(itemStackTag);

            slotData.add(new Pair<>(slotReference, itemStack));
        }

        return slotData;
    }

    private record SlotReferencePrimitive(String groupName, String slotName) { }
     */
}
