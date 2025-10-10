package net.pneumono.gravestones.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.pneumonocore.util.MultiVersionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

//? if >=1.21.8 {
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
//?} else if >=1.20.6 {
/*import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.multiversion.VersionUtil;
*///?} else {
/*import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.multiversion.VersionUtil;
*///?}

//? if >=1.21.5 {
import net.minecraft.component.ComponentsAccess;
import net.minecraft.util.ItemScatterer;
//?}

//? if >=1.20.6 {
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
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

    //? if >=1.21.8 {
    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        if (!this.headStack.isEmpty()) {
            view.put("head", ItemStack.CODEC, this.headStack);
        }
        view.put("text", SignText.CODEC, this.text);
        view.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        this.headStack = view.read("head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = view.read("text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = view.getBoolean("is_waxed", false);
    }
    //?} else if >=1.20.6 {
    /*@Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);

        if (!this.headStack.isEmpty()) {
            VersionUtil.put(ops, nbt, "head", ItemStack.CODEC, this.headStack);
        }
        VersionUtil.put(ops, nbt, "text", SignText.CODEC, this.text);
        nbt.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        RegistryOps<NbtElement> ops = registries.getOps(NbtOps.INSTANCE);

        this.headStack = VersionUtil.get(ops, nbt, "head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = VersionUtil.get(ops, nbt, "text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = nbt.getBoolean("is_waxed"/^? if >=1.21.5 {^//^, false^//^?}^/);
    }
    *///?} else {
    /*@Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (!this.headStack.isEmpty()) {
            VersionUtil.put(nbt, "head", ItemStack.CODEC, this.headStack);
        }
        VersionUtil.put(nbt, "text", SignText.CODEC, this.text);
        nbt.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.headStack = VersionUtil.get(nbt, "head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = VersionUtil.get(nbt, "text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = nbt.getBoolean("is_waxed"/^? if >=1.21.5 {^/, false/^?}^/);
    }
    *///?}

    //? if >=1.20.6 {
    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(this.headStack)));
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.headStack = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyFirstStack();
    }
    //?}

    //? if >=1.21.8 {
    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("head");
    }
    //?} else if >=1.20.6 {
    /*@SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove("head");
    }
    *///?}

    private SignText parseLines(SignText signText) {
        for (int i = 0; i < 4; ++i) {
            Text text = this.parseLine(signText.getMessage(i, false));
            Text text2 = this.parseLine(signText.getMessage(i, true));
            signText = signText.withMessage(i, text, text2);
        }
        return signText;
    }

    private Text parseLine(Text text) {
        World world = this.world;
        if (world instanceof ServerWorld serverWorld) {
            try {
                return Texts.parse(AestheticGravestoneBlockEntity.createCommandSource(null, serverWorld, this.pos), text, null, 0);
            } catch (CommandSyntaxException ignored) {}
        }
        return text;
    }

    public void tryChangeText(PlayerEntity player, List<FilteredMessage> messages) {
        if (!this.isWaxed() && player.getUuid().equals(this.getEditor()) && this.world != null) {
            this.changeText(text -> this.getTextWithMessages(player, messages, text));
            this.setEditor(null);
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        } else {
            Gravestones.LOGGER.warn("Player {} just tried to change non-editable gravestone", player.getName().getString());
        }
    }

    public boolean changeText(UnaryOperator<SignText> textChanger) {
        return this.setText(textChanger.apply(this.getText()));
    }

    private SignText getTextWithMessages(PlayerEntity player, List<FilteredMessage> messages, SignText text) {
        for (int i = 0; i < messages.size(); i++) {
            FilteredMessage filteredMessage = messages.get(i);
            Style style = text.getMessage(i, player.shouldFilterText()).getStyle();
            if (player.shouldFilterText()) {
                text = text.withMessage(i, Text.literal(filteredMessage.getString()).setStyle(style));
            } else {
                text = text.withMessage(i, Text.literal(filteredMessage.raw()).setStyle(style), Text.literal(filteredMessage.getString()).setStyle(style));
            }
        }

        return text;
    }

    public boolean runCommandClickEvent(PlayerEntity player, World world, BlockPos pos) {
        boolean hasRunCommand = false;

        for (Text text : this.getText().getMessages(player.shouldFilterText())) {
            Style style = text.getStyle();
            //? if >=1.21.5 {
            if (style.getClickEvent() instanceof ClickEvent.RunCommand(String value)) {
            //?} else {
            /*ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                String value = clickEvent.getValue();
            *///?}
                CommandManager manager = Objects.requireNonNull(MultiVersionUtil.getWorld(player).getServer()).getCommandManager();
                ServerCommandSource commandSource = createCommandSource(player, world, pos);
                String command = value.startsWith("/") ? value.substring(1) : value;
                manager.execute(manager.getDispatcher().parse(command, commandSource), command);
                hasRunCommand = true;
            }
        }

        return hasRunCommand;
    }

    private static ServerCommandSource createCommandSource(@Nullable PlayerEntity player, World world, BlockPos pos) {
        String string = player == null ? "Gravestone" : player.getName().getString();
        Text text = player == null ? Text.literal("Gravestone") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(pos), Vec2f.ZERO, (ServerWorld)world, 2, string, text, world.getServer(), player);
    }

    public boolean isPlayerTooFarToEdit(UUID uuid) {
        if (this.world == null) {
            return true;
        }
        PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
        return playerEntity == null || /*? if >=1.20.6 {*/!playerEntity.canInteractWithBlockAt(this.getPos(), 4.0)/*?} else {*//*playerEntity.squaredDistanceTo(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) > 64.0*//*?}*/;
    }

    @Override
    public Direction getGravestoneDirection() {
        World world = getWorld();
        if (world != null) {
            BlockState state = world.getBlockState(getPos());
            if (state.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                return state.get(Properties.HORIZONTAL_FACING);
            }
        }
        return Direction.NORTH;
    }

    public void setHeadStack(@Nullable LivingEntity entity, ItemStack headStack) {
        //? if >=1.21.1 {
        this.headStack = headStack.splitUnlessCreative(1, entity);
        //?} else {
        /*this.headStack = headStack.copyWithCount(1);
        if (!(entity instanceof PlayerEntity player) || !player.isCreative()) {
            headStack.decrement(1);
        }
        *///?}
        this.updateListeners();
        Objects.requireNonNull(this.getWorld()).emitGameEvent(
                GameEvent.BLOCK_CHANGE, getPos(), GameEvent.Emitter.of(entity, getCachedState())
        );
    }

    public ItemStack getHeadStack() {
        return headStack;
    }

    //? if >=1.21.5 {
    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (this.world != null) {
            ItemScatterer.spawn(this.world, pos.getX(), pos.getY(), pos.getZ(), this.headStack);
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
        this.markDirty();
        Objects.requireNonNull(this.world).updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }
}
