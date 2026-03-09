package net.pneumono.gravestones.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

//? if >=1.21.11
import net.minecraft.server.permissions.LevelBasedPermissionSet;

//? if >=1.21.6 {
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
*///?}

//? if >=1.21.5 {
import net.minecraft.world.Containers;
//?}

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
//?}

public class AestheticGravestoneBlockEntity extends AbstractGravestoneBlockEntity {
    private ItemStack headStack = ItemStack.EMPTY;
    @Nullable
    private UUID editor;
    private SignText text = createText();
    private boolean waxed;

    public AestheticGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.AESTHETIC_GRAVESTONE_ENTITY, pos, state);
    }

    protected SignText createText() {
        return new SignText();
    }

    //? if >=1.21.6 {
    @Override
    public void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (!this.headStack.isEmpty()) {
            view.store("head", ItemStack.CODEC, this.headStack);
        }
        view.store("text", SignText.DIRECT_CODEC, this.text);
        view.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.headStack = view.read("head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = view.read("text", SignText.DIRECT_CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = view.getBooleanOr("is_waxed", false);
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);

        if (!this.headStack.isEmpty()) {
            VersionUtil.put(ops, tag, "head", ItemStack.CODEC, this.headStack);
        }
        VersionUtil.put(ops, tag, "text", SignText.DIRECT_CODEC, this.text);
        tag.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);

        this.headStack = VersionUtil.get(ops, tag, "head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = VersionUtil.get(ops, tag, "text", SignText.DIRECT_CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = tag.getBoolean("is_waxed")/^? if >=1.21.5 {^//^.orElse(false)^//^?}^/;
    }
    *///?} else {
    /*@Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!this.headStack.isEmpty()) {
            VersionUtil.put(tag, "head", ItemStack.CODEC, this.headStack);
        }
        VersionUtil.put(tag, "text", SignText.DIRECT_CODEC, this.text);
        tag.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.headStack = VersionUtil.get(tag, "head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = VersionUtil.get(tag, "text", SignText.DIRECT_CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = tag.getBoolean("is_waxed"/^? if >=1.21.5 {^/, false/^?}^/);
    }
    *///?}

    //? if >=1.20.5 {
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.headStack)));
    }

    @Override
    protected void applyImplicitComponents(/*? if >=1.21.6 {*//*DataComponentInput*//*?} else {*/DataComponentGetter/*?}*/ components) {
        super.applyImplicitComponents(components);
        this.headStack = components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }
    //?}

    //? if >=1.21.6 {
    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("head");
    }
    //?} else if >=1.20.5 {
    /*@SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("head");
    }
    *///?}

    private SignText parseLines(SignText signText) {
        for (int i = 0; i < 4; ++i) {
            Component text = this.parseLine(signText.getMessage(i, false));
            Component text2 = this.parseLine(signText.getMessage(i, true));
            signText = signText.setMessage(i, text, text2);
        }
        return signText;
    }

    private Component parseLine(Component text) {
        Level level = this.level;
        if (level instanceof ServerLevel serverLevel) {
            try {
                return ComponentUtils.updateForEntity(AestheticGravestoneBlockEntity.createCommandSource(null, serverLevel, this.worldPosition), text, null, 0);
            } catch (CommandSyntaxException ignored) {}
        }
        return text;
    }

    public void tryChangeText(Player player, List<FilteredText> messages) {
        if (!this.isWaxed() && player.getUUID().equals(this.getEditor()) && this.level != null) {
            this.changeText(text -> this.getTextWithMessages(player, messages, text));
            this.setEditor(null);
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        } else {
            Gravestones.LOGGER.warn("Player {} just tried to change non-editable gravestone", player.getName().getString());
        }
    }

    public boolean changeText(UnaryOperator<SignText> textChanger) {
        return this.setText(textChanger.apply(this.getText()));
    }

    private SignText getTextWithMessages(Player player, List<FilteredText> messages, SignText text) {
        for (int i = 0; i < messages.size(); i++) {
            FilteredText filteredMessage = messages.get(i);
            Style style = text.getMessage(i, player.isTextFilteringEnabled()).getStyle();
            if (player.isTextFilteringEnabled()) {
                text = text.setMessage(i, Component.literal(filteredMessage.filteredOrEmpty()).setStyle(style));
            } else {
                text = text.setMessage(i, Component.literal(filteredMessage.raw()).setStyle(style), Component.literal(filteredMessage.filteredOrEmpty()).setStyle(style));
            }
        }

        return text;
    }

    public boolean runCommandClickEvent(Player player, Level level, BlockPos pos) {
        boolean hasRunCommand = false;

        for (Component text : this.getText().getMessages(player.isTextFilteringEnabled())) {
            Style style = text.getStyle();
            //? if >=1.21.5 {
            if (style.getClickEvent() instanceof ClickEvent.RunCommand clickEvent) {
                String value = clickEvent.command();
            //?} else {
            /*ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                String value = clickEvent.getValue();
            *///?}
                Commands manager = Objects.requireNonNull(player.level().getServer()).getCommands();
                CommandSourceStack commandSource = createCommandSource(player, level, pos);
                String command = value.startsWith("/") ? value.substring(1) : value;
                manager.performCommand(manager.getDispatcher().parse(command, commandSource), command);
                hasRunCommand = true;
            }
        }

        return hasRunCommand;
    }

    private static CommandSourceStack createCommandSource(@Nullable Player player, Level level, BlockPos pos) {
        String string = player == null ? "Gravestone" : player.getName().getString();
        Component text = player == null ? Component.literal("Gravestone") : player.getDisplayName();
        return new CommandSourceStack(
                CommandSource.NULL, Vec3.atCenterOf(pos), Vec2.ZERO, (ServerLevel) level,
                /*? if >=1.21.11 {*/LevelBasedPermissionSet.GAMEMASTER/*?} else {*//*2*//*?}*/,
                string, text, level.getServer(), player
        );
    }

    public boolean isPlayerTooFarToEdit(UUID uuid) {
        if (this.level == null) {
            return true;
        }
        Player playerEntity = this.level.getPlayerByUUID(uuid);
        if (playerEntity != null) {
            //? if >=1.21.11 {
            return !playerEntity.isWithinBlockInteractionRange(this.getBlockPos(), 4.0);
            //?} else if >=1.20.5 {
            /*return !playerEntity.canInteractWithBlock(this.getBlockPos(), 4.0);
            *///?} else {
            /*return playerEntity.distanceToSqr(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ()) > 64.0;
            *///?}
        }
        return true;
    }

    @Override
    public Direction getGravestoneDirection() {
        Level level = getLevel();
        if (level != null) {
            BlockState state = level.getBlockState(getBlockPos());
            if (state.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
                return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        }
        return Direction.NORTH;
    }

    public void setHeadStack(@Nullable LivingEntity entity, ItemStack headStack) {
        //? if >=1.21 {
        this.headStack = headStack.consumeAndReturn(1, entity);
        //?} else {
        /*this.headStack = headStack.copyWithCount(1);
        if (!(entity instanceof Player player) || !player.isCreative()) {
            headStack.shrink(1);
        }
        *///?}
        this.updateListeners();
        Objects.requireNonNull(this.getLevel()).gameEvent(
                GameEvent.BLOCK_CHANGE, getBlockPos(), GameEvent.Context.of(entity, getBlockState())
        );
    }

    public ItemStack getHeadStack() {
        return headStack;
    }

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (this.level != null) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.headStack);
        }
    }
    //?}

    public void setEditor(@Nullable UUID editor) {
        this.editor = editor;
    }

    @Nullable
    public UUID getEditor() {
        return this.editor;
    }

    public boolean setText(SignText text) {
        if (this.text != text) {
            this.text = text;
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }

    public SignText getText() {
        return this.text;
    }

    public boolean setWaxed(boolean waxed) {
        if (this.waxed != waxed) {
            this.waxed = waxed;
            this.updateListeners();
            return true;
        } else {
            return false;
        }
    }

    public boolean isWaxed() {
        return this.waxed;
    }

    private void updateListeners() {
        this.setChanged();
        Objects.requireNonNull(this.level).sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }
}
