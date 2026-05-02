package ru.fakefun.altmanager.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fakefun.altmanager.screen.AltManagerScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void altmanager$addAccountButton(CallbackInfo ci) {
        ClickableWidget multiplayerButton = null;
        String multiplayerText = Text.translatable("menu.multiplayer").getString();

        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getMessage().getString().equals(multiplayerText)) {
                multiplayerButton = button;
                break;
            }
        }

        int buttonX = multiplayerButton != null ? multiplayerButton.getRight() + 4 : this.width / 2 + 104;
        int buttonY = multiplayerButton != null ? multiplayerButton.getY() : this.height / 4 + 72;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("AM"), button -> {
            if (this.client != null) {
                this.client.setScreen(new AltManagerScreen(this));
            }
        }).dimensions(buttonX, buttonY, 24, 20).build());
    }
}
