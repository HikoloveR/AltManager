package ru.fakefun.altmanager.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.fakefun.altmanager.screen.AltManagerScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void altmanager$replaceReportButton(CallbackInfo ci) {
        ClickableWidget reportButton = null;
        String reportText = Text.translatable("menu.playerReporting").getString();

        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getMessage().getString().equals(reportText)) {
                reportButton = button;
                break;
            }
        }

        if (reportButton != null) {
            int x = reportButton.getX();
            int y = reportButton.getY();
            int width = reportButton.getWidth();
            int height = reportButton.getHeight();

            this.remove(reportButton);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("AltManager"), button -> {
                if (this.client != null) {
                    this.client.setScreen(new AltManagerScreen(this));
                }
            }).dimensions(x, y, width, height).build());
        }
    }
}
