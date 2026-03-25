package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignText;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.block.AestheticGravestoneBlockEntity;
import net.pneumono.gravestones.networking.UpdateGravestoneC2SPayload;
import org.jetbrains.annotations.Nullable;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}


//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?} else {
/*import org.lwjgl.glfw.GLFW;
*///?}

//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
*///?}

//? if >=1.21.4 {
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
//?} else {
/*import net.minecraft.client.renderer.blockentity.SignRenderer;
*///?}

//? if >=1.21.2
import net.minecraft.util.ARGB;

//? if <1.20.2 {
/*import com.mojang.blaze3d.platform.Lighting;
*///?}

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
    private TextFieldHelper selectionManager;
    private final Identifier texture;

    public AestheticGravestoneEditScreen(AestheticGravestoneBlockEntity blockEntity, boolean filtered) {
        super(Component.translatable("gravestones.edit_text"));
        this.blockEntity = blockEntity;
        this.text = blockEntity.getText();
        this.messages = IntStream.range(0, 4).mapToObj(line -> this.text.getMessage(line, filtered)).map(Component::getString).toArray(String[]::new);
        Level level = blockEntity.getLevel();
        Block block;
        if (level != null) {
            block = level.getBlockState(blockEntity.getBlockPos()).getBlock();
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
        Objects.requireNonNull(this.minecraft);
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> this.finishEditing()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
        );
        this.selectionManager = new TextFieldHelper(
                () -> this.messages[this.currentRow],
                this::setCurrentRowMessage,
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                string -> this.minecraft.font.width(string) <= TEXT_WIDTH
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
        return this.minecraft != null
                && this.minecraft.player != null
                && !this.blockEntity.isRemoved()
                && !this.blockEntity.isPlayerTooFarToEdit(this.minecraft.player.getUUID());
    }

    @Override
    //? if >=1.21.9 {
    public boolean keyPressed(KeyEvent input) {
    //?} else {
    /*public boolean keyPressed(int input, int scanCode, int modifiers) {
    *///?}
        Objects.requireNonNull(this.selectionManager);
        if (isUp(input)) {
            this.currentRow = this.currentRow - 1 & 3;
            this.selectionManager.setCursorToEnd();
            return true;
        } else if (isDownOrEnter(input)) {
            this.currentRow = this.currentRow + 1 & 3;
            this.selectionManager.setCursorToEnd();
            return true;
        } else {
            if (this.selectionManager.keyPressed(input)) {
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
    public boolean isUp(KeyEvent input) {
        return input.isUp();
    }
    //?} else {
    /*public boolean isUp(int input) {
        return input == GLFW.GLFW_KEY_UP;
    }
    *///?}

    //? if >=1.21.9 {
    public boolean isDownOrEnter(KeyEvent input) {
        return input.isDown() || input.isConfirmation();
    }
    //?} else {
    /*public boolean isDownOrEnter(int input) {
        return input == GLFW.GLFW_KEY_DOWN || input == GLFW.GLFW_KEY_ENTER || input == GLFW.GLFW_KEY_KP_ENTER;
    }
    *///?}

    @Override
    //? if >=1.21.9 {
    public boolean charTyped(CharacterEvent input) {
    //?} else {
    /*public boolean charTyped(char input, int modifiers) {
    *///?}
        Objects.requireNonNull(this.selectionManager).charTyped(input);
        return true;
    }

    @Override
    public void /*? if >=26.1 {*/extractRenderState(GuiGraphicsExtractor/*?} else {*//*render(GuiGraphics*//*?}*/ context, int mouseX, int mouseY, float deltaTicks) {
        //? if <1.20.2 {
        /*Lighting.setupForFlatItems();
        this.renderBackground(context);
        *///?}
        context./*? if >=26.1 {*/centeredText/*?} else {*//*drawCenteredString*//*?}*/(this.font, this.title, this.width / 2, 40, 16777215);
        this.renderGravestone(context);
        //? if <1.20.2 {
        /*Lighting.setupFor3DItems();
        *///?}
        super./*? if >=26.1 {*/extractRenderState/*?} else {*//*render*//*?}*/(context, mouseX, mouseY, deltaTicks);
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
        this.extractTransparentBackground(graphics);
    }
    //?} else if >=1.20.2 {
    /*@Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        this.renderTransparentBackground(context);
    }
    *///?}

    @Override
    public void onClose() {
        this.finishEditing();
    }

    @Override
    public void removed() {
        UpdateGravestoneC2SPayload payload = new UpdateGravestoneC2SPayload(this.blockEntity.getBlockPos(), this.messages[0], this.messages[1], this.messages[2], this.messages[3]);
        //? if >=1.20.5 {
        ClientPlayNetworking.send(payload);
        //?} else {
        /*ClientPlayNetworking.send(payload.id(), payload.write());
        *///?}
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderGravestone(/*? if >=26.1 {*/GuiGraphicsExtractor/*?} else {*//*GuiGraphics*//*?}*/ context) {
        //? if >=1.21.6 {
        context.pose().pushMatrix();
        context.pose().translate(this.width / 2.0F, 125.0F);
        context.pose().pushMatrix();
        this.renderGravestoneBackground(context);
        context.pose().popMatrix();
        this.renderGravestoneText(context);
        context.pose().popMatrix();
        //?} else {
        /*context.pose().pushPose();
        context.pose().translate(this.width / 2.0F, 125.0F, 50.0F);
        context.pose().pushPose();
        this.renderGravestoneBackground(context);
        context.pose().popPose();
        this.renderGravestoneText(context);
        context.pose().popPose();
        *///?}
    }

    protected void renderGravestoneBackground(/*? if >=26.1 {*/GuiGraphicsExtractor/*?} else {*//*GuiGraphics*//*?}*/ context) {
        //? if >=1.21.6 {
        context.pose().scale(7.0F, 7.0F);
        //?} else {
        /*context.pose().scale(7.0F, 7.0F, 1.0F);
        *///?}
        context.blit(
                /*? if >=1.21.6 {*/RenderPipelines.GUI_TEXTURED,/*?} else if >=1.21.2 {*//*RenderType::guiTextured,*//*?}*/
                this.texture,
                -8, -8,
                0.0F, 0.0F,
                16, 16,
                16, 16
        );
    }

    private void renderGravestoneText(/*? if >=26.1 {*/GuiGraphicsExtractor/*?} else {*//*GuiGraphics*//*?}*/ context) {
        int color = this.text.hasGlowingText() ? this.text.getColor().getTextColor() : /*? if >=1.21.4 {*/AbstractSignRenderer.getDarkColor(this.text)/*?} else {*//*SignRenderer.getDarkColor(this.text)*//*?}*/;
        boolean shouldFlashCursor = this.ticksSinceOpened / 6 % 2 == 0;
        Objects.requireNonNull(this.selectionManager);
        int selectionStart = this.selectionManager.getCursorPos();
        int selectionEnd = this.selectionManager.getSelectionPos();
        int lineHeightOffset = 4 * TEXT_LINE_HEIGHT / 2;
        int adjustedY = this.currentRow * TEXT_LINE_HEIGHT - lineHeightOffset;

        for (int i = 0; i < this.messages.length; i++) {
            String message = this.messages[i];
            if (message != null) {
                if (this.font.isBidirectional()) {
                    message = this.font.bidirectionalShaping(message);
                }

                int x = -this.font.width(message) / 2;
                context./*? if >=26.1 {*/text/*?} else {*//*drawString*//*?}*/(this.font, message, x, i * TEXT_LINE_HEIGHT - lineHeightOffset, color, false);
                if (i == this.currentRow && selectionStart >= 0 && shouldFlashCursor) {
                    int substringWidth = this.font.width(message.substring(0, Math.min(selectionStart, message.length())));
                    int adjustedX = substringWidth - this.font.width(message) / 2;
                    if (selectionStart >= message.length()) {
                        context./*? if >=26.1 {*/text/*?} else {*//*drawString*//*?}*/(this.font, "_", adjustedX, adjustedY, color, false);
                    }
                }
            }
        }

        for (int i = 0; i < this.messages.length; i++) {
            String message = this.messages[i];
            if (message != null && i == this.currentRow && selectionStart >= 0) {
                int substringWidth = this.font.width(message.substring(0, Math.min(selectionStart, message.length())));
                int adjustedX = substringWidth - this.font.width(message) / 2;
                if (shouldFlashCursor && selectionStart < message.length()) {
                    context.fill(adjustedX, adjustedY - 1, adjustedX + 1, adjustedY + TEXT_LINE_HEIGHT, /*? if >=1.21.2 {*/ARGB.opaque(color)/*?} else {*//*-16777216 | color*//*?}*/);
                }

                if (selectionEnd != selectionStart) {
                    int start = Math.min(selectionStart, selectionEnd);
                    int end = Math.max(selectionStart, selectionEnd);
                    int widthStart = this.font.width(message.substring(0, start)) - this.font.width(message) / 2;
                    int widthEnd = this.font.width(message.substring(0, end)) - this.font.width(message) / 2;
                    int startX = Math.min(widthStart, widthEnd);
                    int endX = Math.max(widthStart, widthEnd);
                    context.fill(
                            /*? if >=1.21.6 {*/RenderPipelines.GUI_TEXT_HIGHLIGHT/*?} else {*//*RenderType.guiTextHighlight()*//*?}*/,
                            startX,
                            adjustedY,
                            endX,
                            adjustedY + TEXT_LINE_HEIGHT,
                            /*? if >=1.21 {*/CommonColors.BLUE/*?} else {*//*-16776961*//*?}*/
                    );
                }
            }
        }
    }

    private void setCurrentRowMessage(String message) {
        this.messages[this.currentRow] = message;
        this.text = this.text.setMessage(this.currentRow, Component.literal(message));
        this.blockEntity.setText(this.text);
    }

    private void finishEditing() {
        Objects.requireNonNull(this.minecraft).setScreen(null);
    }
}
