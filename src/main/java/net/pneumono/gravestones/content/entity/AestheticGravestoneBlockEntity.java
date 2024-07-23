package net.pneumono.gravestones.content.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
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
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AestheticGravestoneBlockEntity extends AbstractGravestoneBlockEntity {
    private SignText text = createText();
    private boolean waxed;

    public AestheticGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.AESTHETIC_GRAVESTONE_ENTITY, pos, state);
    }

    protected SignText createText() {
        return new SignText();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        // Will re-implement later, womp womp it was unused anyway
        //RegistryOps<NbtElement> dynamicOps = registryLookup.getOps(NbtOps.INSTANCE);
        //SignText.CODEC.encodeStart(dynamicOps, this.text).resultOrPartial(Gravestones.LOGGER::error).ifPresent(frontText -> nbt.put("text", frontText));
        nbt.putBoolean("is_waxed", this.waxed);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        // Will re-implement later, womp womp it was unused anyway
        //RegistryOps<NbtElement> dynamicOps = registryLookup.getOps(NbtOps.INSTANCE);
        //if (nbt.contains("text")) {
        //    SignText.CODEC.parse(dynamicOps, nbt.getCompound("text")).resultOrPartial(Gravestones.LOGGER::error).ifPresent(signText -> this.text = this.parseLines(signText));
        //}
        this.waxed = nbt.getBoolean("is_waxed");
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

    public boolean canRunCommandClickEvent(PlayerEntity player) {
        return this.isWaxed() && this.getText().hasRunCommandClickEvent(player);
    }

    public boolean runCommandClickEvent(PlayerEntity player, World world, BlockPos pos) {
        boolean bl = false;
        for (Text text : this.getText().getMessages(player.shouldFilterText())) {
            Style style = text.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            Objects.requireNonNull(player.getServer()).getCommandManager().executeWithPrefix(AestheticGravestoneBlockEntity.createCommandSource(player, world, pos), clickEvent.getValue());
            bl = true;
        }
        return bl;
    }

    private static ServerCommandSource createCommandSource(@Nullable PlayerEntity player, World world, BlockPos pos) {
        String string = player == null ? "Sign" : player.getName().getString();
        Text text = player == null ? Text.literal("Sign") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(pos), Vec2f.ZERO, (ServerWorld)world, 2, string, text, world.getServer(), player);
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

    public SignText getText() {
        return this.text;
    }

    public boolean isWaxed() {
        return this.waxed;
    }
}
