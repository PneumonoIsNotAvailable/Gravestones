package net.pneumono.gravestones.block;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignText;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

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

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.headStack.isEmpty()) {
            view.put("head", ItemStack.CODEC, this.headStack);
        }
        view.put("text", SignText.CODEC, this.text);
        view.putBoolean("is_waxed", this.waxed);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.headStack = view.read("head", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.text = view.read("text", SignText.CODEC).map(this::parseLines).orElseGet(SignText::new);
        this.waxed = view.getBoolean("is_waxed", false);
    }

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

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("head");
    }

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
            if (style.getClickEvent() instanceof ClickEvent.RunCommand(String var14)) {
                Objects.requireNonNull(player.getServer()).getCommandManager().executeWithPrefix(createCommandSource(player, world, pos), var14);
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
        return playerEntity == null || !playerEntity.canInteractWithBlockAt(this.getPos(), 4.0);
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

    @Override
    public @Nullable ProfileComponent getHeadProfile() {
        if (!this.headStack.isEmpty() && this.headStack.contains(DataComponentTypes.PROFILE)) {
            return this.headStack.get(DataComponentTypes.PROFILE);
        }
        if (!this.headStack.isEmpty()) {
            return new ProfileComponent(Optional.empty(), Optional.empty(), new PropertyMap());
        }
        return null;
    }

    public void setHeadStack(@Nullable LivingEntity entity, ItemStack headStack) {
        this.headStack = headStack.splitUnlessCreative(1, entity);
        this.updateListeners();
        Objects.requireNonNull(this.getWorld()).emitGameEvent(
                GameEvent.BLOCK_CHANGE, getPos(), GameEvent.Emitter.of(entity, getCachedState())
        );
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (this.world != null) {
            ItemScatterer.spawn(this.world, pos.getX(), pos.getY(), pos.getZ(), this.headStack);
        }
    }

    public ItemStack getHeadStack() {
        return headStack;
    }

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
