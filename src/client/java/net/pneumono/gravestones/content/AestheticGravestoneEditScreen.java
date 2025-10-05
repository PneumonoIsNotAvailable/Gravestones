package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;
import org.jetbrains.annotations.Nullable;

//? if >=1.21.9 {
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}

//? if >=1.21.8 {
import net.minecraft.client.gl.RenderPipelines;
//?} else {
/*import net.minecraft.client.render.RenderLayer;
*///?}

//? if >=1.21.4 {
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
//?} else {
/*import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
*///?}

//? if >=1.21.3 {
import net.minecraft.util.math.ColorHelper;
//?}

//? if >=1.21.1 {
import net.minecraft.util.Colors;
//?}

import java.util.Objects;
import java.util.stream.IntStream;

public class AestheticGravestoneEditScreen extends Screen {
    public static final int TEXT_WIDTH = 86;
    private static final int TEXT_LINE_HEIGHT = 14;
    protected final AestheticGravestoneBlockEntity blockEntity;
    private SignText text;
    private final String[] messages;
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
        Block block;
        if (world != null) {
            block = world.getBlockState(blockEntity.getPos()).getBlock();
        } else {
            block = GravestonesRegistry.GRAVESTONE;
        }
        this.texture = getTexture(block);
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

        return Gravestones.id("textures/gui/gravestone_" + name + ".png");
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
    //? if >=1.21.9 {
    public boolean keyPressed(KeyInput input) {
    //?} else {
    /*public boolean keyPressed(int input, int scanCode, int modifiers) {
    *///?}
        Objects.requireNonNull(this.selectionManager);
        if (isUp(input)) {
            this.currentRow = this.currentRow - 1 & 3;
            this.selectionManager.putCursorAtEnd();
            return true;
        } else if (isDownOrEnter(input)) {
            this.currentRow = this.currentRow + 1 & 3;
            this.selectionManager.putCursorAtEnd();
            return true;
        } else {
            if (this.selectionManager.handleSpecialKey(input)) {
                return true;
            } else {
                //? if >=1.21.9 {
                return super.keyPressed(input);
                //?} else {
                /*return super.keyPressed(input, scanCode, modifiers);
                *///?}
            }
        }
    }

    //? if >=1.21.9 {
    public boolean isUp(KeyInput input) {
        return input.isUp();
    }
    //?} else {
    /*public boolean isUp(int input) {
        return input == GLFW.GLFW_KEY_UP;
    }
    *///?}

    //? if >=1.21.9 {
    public boolean isDownOrEnter(KeyInput input) {
        return input.isDown() || input.isEnter();
    }
    //?} else {
    /*public boolean isDownOrEnter(int input) {
        return input == GLFW.GLFW_KEY_DOWN || input == GLFW.GLFW_KEY_ENTER || input == GLFW.GLFW_KEY_KP_ENTER;
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public boolean charTyped(CharInput input) {
    //?} else {
    /*public boolean charTyped(char input, int modifiers) {
    *///?}
        Objects.requireNonNull(this.selectionManager).insert(input);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 16777215);
        this.renderGravestone(context);
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

    private void renderGravestone(DrawContext context) {
        //? if >=1.21.8 {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.width / 2.0F, 125.0F);
        context.getMatrices().pushMatrix();
        this.renderGravestoneBackground(context);
        context.getMatrices().popMatrix();
        this.renderGravestoneText(context);
        context.getMatrices().popMatrix();
        //?} else {
        /*context.getMatrices().push();
        context.getMatrices().translate(this.width / 2.0F, 125.0F, 50.0F);
        context.getMatrices().push();
        this.renderGravestoneBackground(context);
        context.getMatrices().pop();
        this.renderGravestoneText(context);
        context.getMatrices().pop();
        *///?}
    }

    protected void renderGravestoneBackground(DrawContext context) {
        //? if >=1.21.8 {
        context.getMatrices().scale(7.0F, 7.0F);
        //?} else {
        /*context.getMatrices().scale(7.0F, 7.0F, 1.0F);
        *///?}
        context.drawTexture(
                /*? if >=1.21.8 {*/RenderPipelines.GUI_TEXTURED,/*?} else if >=1.21.3 {*//*RenderLayer::getGuiTextured,*//*?}*/
                this.texture,
                -8, -8,
                0.0F, 0.0F,
                16, 16,
                16, 16
        );
    }

    private void renderGravestoneText(DrawContext context) {
        int color = this.text.isGlowing() ? this.text.getColor().getSignColor() : /*? if >=1.21.4 {*/AbstractSignBlockEntityRenderer.getTextColor(this.text)/*?} else {*//*SignBlockEntityRenderer.getColor(this.text)*//*?}*/;
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
                    context.fill(adjustedX, adjustedY - 1, adjustedX + 1, adjustedY + TEXT_LINE_HEIGHT, /*? if >=1.21.3 {*/ColorHelper.fullAlpha(color)/*?} else {*//*-16777216 | color*//*?}*/);
                }

                if (selectionEnd != selectionStart) {
                    int start = Math.min(selectionStart, selectionEnd);
                    int end = Math.max(selectionStart, selectionEnd);
                    int widthStart = this.textRenderer.getWidth(message.substring(0, start)) - this.textRenderer.getWidth(message) / 2;
                    int widthEnd = this.textRenderer.getWidth(message.substring(0, end)) - this.textRenderer.getWidth(message) / 2;
                    int startX = Math.min(widthStart, widthEnd);
                    int endX = Math.max(widthStart, widthEnd);
                    context.fill(
                            /*? if >=1.21.8 {*/RenderPipelines.GUI_TEXT_HIGHLIGHT/*?} else {*//*RenderLayer.getGuiTextHighlight()*//*?}*/,
                            startX,
                            adjustedY,
                            endX,
                            adjustedY + TEXT_LINE_HEIGHT,
                            /*? if >=1.21.1 {*/Colors.BLUE/*?} else {*//*-16776961*//*?}*/
                    );
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
