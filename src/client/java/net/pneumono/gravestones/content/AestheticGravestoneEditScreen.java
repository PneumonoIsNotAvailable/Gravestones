package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.stream.IntStream;

public class AestheticGravestoneEditScreen extends Screen {
    public static final int TEXT_WIDTH = 86;
    private static final int TEXT_LINE_HEIGHT = 14;
    protected final AestheticGravestoneBlockEntity blockEntity;
    private SignText text;
    private final String[] messages;
    protected final Block block;
    private int ticksSinceOpened;
    private int currentRow;
    @Nullable
    private SelectionManager selectionManager;
    private final Identifier texture;

    public AestheticGravestoneEditScreen(AestheticGravestoneBlockEntity blockEntity, boolean filtered) {
        super(Text.translatable("gravestones.edit_text"));
        this.blockEntity = blockEntity;
        this.text = blockEntity.getText();
        this.messages = IntStream.range(0, 4).mapToObj(line -> this.text.getMessage(line, filtered)).map(Text::getString).toArray(String[]::new);
        World world = blockEntity.getWorld();
        if (world != null) {
            this.block = world.getBlockState(blockEntity.getPos()).getBlock();
        } else {
            this.block = GravestonesRegistry.GRAVESTONE;
        }
        this.texture = getTexture(this.block);
    }

    private Identifier getTexture(Block block) {
        String name;
        if (block == GravestonesRegistry.GRAVESTONE_CHIPPED) {
            name = "chipped";
        } else if (block == GravestonesRegistry.GRAVESTONE_DAMAGED) {
            name = "damaged";
        } else {
            name = "default";
        }

        return Gravestones.identifier("textures/gui/gravestone_" + name + ".png");
    }

    @Override
    protected void init() {
        Objects.requireNonNull(this.client);
        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> this.finishEditing()).dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
        );
        this.selectionManager = new SelectionManager(
                () -> this.messages[this.currentRow],
                this::setCurrentRowMessage,
                SelectionManager.makeClipboardGetter(this.client),
                SelectionManager.makeClipboardSetter(this.client),
                string -> this.client.textRenderer.getWidth(string) <= TEXT_WIDTH
        );
    }

    @Override
    public void tick() {
        this.ticksSinceOpened++;
        if (!this.canEdit()) {
            this.finishEditing();
        }
    }

    private boolean canEdit() {
        return this.client != null
                && this.client.player != null
                && !this.blockEntity.isRemoved()
                && !this.blockEntity.isPlayerTooFarToEdit(this.client.player.getUuid());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Objects.requireNonNull(this.selectionManager);
        if (keyCode == GLFW.GLFW_KEY_UP) {
            this.currentRow = this.currentRow - 1 & 3;
            this.selectionManager.putCursorAtEnd();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.currentRow = this.currentRow + 1 & 3;
            this.selectionManager.putCursorAtEnd();
            return true;
        } else {
            return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        Objects.requireNonNull(this.selectionManager).insert(chr);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.draw();
        DiffuseLighting.disableGuiDepthLighting();
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 16777215);
        this.renderSign(context);
        context.draw();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderInGameBackground(context);
    }

    @Override
    public void close() {
        this.finishEditing();
    }

    @Override
    public void removed() {
        UpdateGravestoneC2SPayload payload = new UpdateGravestoneC2SPayload(this.blockEntity.getPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]);
        ClientPlayNetworking.send(payload);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void renderSign(DrawContext context) {
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2.0F, 125.0F, 50.0F);
        context.getMatrices().push();
        this.renderSignBackground(context);
        context.getMatrices().pop();
        this.renderSignText(context);
        context.getMatrices().pop();
    }

    protected void renderSignBackground(DrawContext context) {
        context.getMatrices().scale(7.0F, 7.0F, 1.0F);
        context.drawTexture(RenderLayer::getGuiTextured, this.texture, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    private void renderSignText(DrawContext context) {
        context.getMatrices().translate(0.0F, 0.0F, 4.0F);
        Vector3f textScale = new Vector3f(1.0F, 1.0F, 1.0F);
        context.getMatrices().scale(textScale.x(), textScale.y(), textScale.z());
        int color = this.text.isGlowing() ? this.text.getColor().getSignColor() : AbstractSignBlockEntityRenderer.getTextColor(this.text);
        boolean shouldFlashCursor = this.ticksSinceOpened / 6 % 2 == 0;
        Objects.requireNonNull(this.selectionManager);
        int selectionStart = this.selectionManager.getSelectionStart();
        int selectionEnd = this.selectionManager.getSelectionEnd();
        int lineHeightOffset = 4 * TEXT_LINE_HEIGHT / 2;
        int adjustedY = this.currentRow * TEXT_LINE_HEIGHT - lineHeightOffset;

        for (int i = 0; i < this.messages.length; i++) {
            String message = this.messages[i];
            if (message != null) {
                if (this.textRenderer.isRightToLeft()) {
                    message = this.textRenderer.mirror(message);
                }

                int x = -this.textRenderer.getWidth(message) / 2;
                context.drawText(this.textRenderer, message, x, i * TEXT_LINE_HEIGHT - lineHeightOffset, color, false);
                if (i == this.currentRow && selectionStart >= 0 && shouldFlashCursor) {
                    int substringWidth = this.textRenderer.getWidth(message.substring(0, Math.min(selectionStart, message.length())));
                    int adjustedX = substringWidth - this.textRenderer.getWidth(message) / 2;
                    if (selectionStart >= message.length()) {
                        context.drawText(this.textRenderer, "_", adjustedX, adjustedY, color, false);
                    }
                }
            }
        }

        for (int i = 0; i < this.messages.length; i++) {
            String message = this.messages[i];
            if (message != null && i == this.currentRow && selectionStart >= 0) {
                int substringWidth = this.textRenderer.getWidth(message.substring(0, Math.min(selectionStart, message.length())));
                int adjustedX = substringWidth - this.textRenderer.getWidth(message) / 2;
                if (shouldFlashCursor && selectionStart < message.length()) {
                    context.fill(adjustedX, adjustedY - 1, adjustedX + 1, adjustedY + TEXT_LINE_HEIGHT, ColorHelper.fullAlpha(color));
                }

                if (selectionEnd != selectionStart) {
                    int start = Math.min(selectionStart, selectionEnd);
                    int end = Math.max(selectionStart, selectionEnd);
                    int widthStart = this.textRenderer.getWidth(message.substring(0, start)) - this.textRenderer.getWidth(message) / 2;
                    int widthEnd = this.textRenderer.getWidth(message.substring(0, end)) - this.textRenderer.getWidth(message) / 2;
                    int idk = Math.min(widthStart, widthEnd);
                    int idk2 = Math.max(widthStart, widthEnd);
                    context.fill(RenderLayer.getGuiTextHighlight(), idk, adjustedY, idk2, adjustedY + TEXT_LINE_HEIGHT, Colors.BLUE);
                }
            }
        }
    }

    private void setCurrentRowMessage(String message) {
        this.messages[this.currentRow] = message;
        this.text = this.text.withMessage(this.currentRow, Text.literal(message));
        this.blockEntity.setText(this.text);
    }

    private void finishEditing() {
        Objects.requireNonNull(this.client).setScreen(null);
    }
}
