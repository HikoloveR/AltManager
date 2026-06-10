package ru.fakefun.altmanager.screen;

import net.minecraft.client.MinecraftClient;
//? if >=1.21.9
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.fakefun.altmanager.account.AltAccountStore;
import ru.fakefun.altmanager.account.AltNicknameGenerator;
import ru.fakefun.altmanager.account.SessionSwitcher;

import java.util.List;

public class AltManagerScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_WIDTH = 280;
    private static final int DELETE_BUTTON_SIZE = 12;
    private static final int DOUBLE_CLICK_MS = 350;

    private final Screen parent;
    private final AltAccountStore store = new AltAccountStore();
    private TextFieldWidget nicknameField;
    private String status = "";
    private int scrollOffset;
    private long lastClickTime;
    private int lastClickIndex = -1;
    private int draggedIndex = -1;

    public AltManagerScreen(Screen parent) {
        super(Text.literal("AltManager"));
        this.parent = parent;
        this.store.load();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = getTop();
        int panelWidth = getPanelWidth();
        int left = centerX - panelWidth / 2;

        this.nicknameField = new TextFieldWidget(this.textRenderer, left, top + 8, panelWidth, 20, Text.literal("Nickname"));
        this.nicknameField.setMaxLength(16);
        this.nicknameField.setTextPredicate(AltAccountStore::isValidNicknameInput);
        this.addDrawableChild(this.nicknameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Добавить аккаунт"), button -> addTypedAccount())
                .dimensions(left, top + 34, panelWidth / 2 - 3, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Рандом"), button -> addRandomAccount())
                .dimensions(centerX + 3, top + 34, panelWidth / 2 - 3, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Очистить список аккаунтов"), button -> {
                    this.store.clear();
                    this.status = "";
                    this.scrollOffset = 0;
                })
                .dimensions(left, this.height - 52, panelWidth, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> close())
                .dimensions(left, this.height - 28, panelWidth, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        int centerX = this.width / 2;
        int top = getTop();
        int listTop = getListTop();
        int listHeight = getListHeight();
        int panelWidth = getPanelWidth();
        int listLeft = centerX - panelWidth / 2;
        int listRight = centerX + panelWidth / 2;

        context.fill(listLeft - 2, listTop - 2, listRight + 2, listTop + listHeight + 2, 0xAA000000);
        context.fill(listLeft, listTop, listRight, listTop + listHeight, 0x66333333);

        List<String> accounts = this.store.accounts();
        int visibleRows = Math.max(1, listHeight / ROW_HEIGHT);
        clampScroll(visibleRows);

        if (accounts.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Список аккаунтов пуст"), centerX, listTop + 12, 0xFFA0A0A0);
            return;
        }

        for (int row = 0; row < visibleRows; row++) {
            int index = row + scrollOffset;
            if (index >= accounts.size()) {
                break;
            }

            String account = accounts.get(index);
            int rowTop = listTop + row * ROW_HEIGHT;
            boolean hovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT;
            boolean selected = account.equals(this.store.selectedAccount());
            int background = hovered ? 0x66555555 : 0x33000000;
            int color = selected ? 0xFF55FF55 : 0xFFFFFFFF;

            boolean pinned = this.store.isPinned(account);
            boolean pinHovered = hovered && mouseX >= listRight - 34 && mouseX <= listRight - 22 && mouseY >= rowTop + 5 && mouseY <= rowTop + 17;
            int headColor = pinned ? 0xFFFF3838 : (pinHovered ? 0xFF888888 : 0xFF555555);
            int needleColor = pinned ? 0xFFDDDDDD : (pinHovered ? 0xFF777777 : 0xFF444444);

            context.fill(listLeft, rowTop, listRight, rowTop + ROW_HEIGHT - 1, background);
            context.drawTextWithShadow(this.textRenderer, account, listLeft + 8, rowTop + 7, color);
            
            int pinX = listRight - 34;
            int pinY = rowTop + 5;
            context.fill(pinX + 3, pinY + 2, pinX + 8, pinY + 3, headColor);
            context.fill(pinX + 4, pinY + 3, pinX + 7, pinY + 5, headColor);
            context.fill(pinX + 3, pinY + 5, pinX + 8, pinY + 6, headColor);
            context.fill(pinX + 5, pinY + 6, pinX + 6, pinY + 10, needleColor);
            
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("x"), listRight - 12, rowTop + 7, 0xFFFF5555);
        }
    }

    @Override
    //? if >=1.21.9 {
    public boolean mouseClicked(Click click, boolean doubled) {
        return handleAccountClick(click.x(), click.y(), click.button(), doubled) || super.mouseClicked(click, doubled);
    }
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return handleAccountClick(mouseX, mouseY, button, false) || super.mouseClicked(mouseX, mouseY, button);
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            this.draggedIndex = -1;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (click.button() == 0 && this.draggedIndex >= 0) {
            int hoverIndex = getClickedAccountIndex(click.x(), click.y());
            int pinnedCount = this.store.getPinnedCount();
            if (hoverIndex >= 0 && hoverIndex != this.draggedIndex && hoverIndex < pinnedCount && this.draggedIndex < pinnedCount) {
                this.store.movePinned(this.draggedIndex, hoverIndex);
                this.draggedIndex = hoverIndex;
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }
    //?} else {
    /*@Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggedIndex = -1;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && this.draggedIndex >= 0) {
            int hoverIndex = getClickedAccountIndex(mouseX, mouseY);
            int pinnedCount = this.store.getPinnedCount();
            if (hoverIndex >= 0 && hoverIndex != this.draggedIndex && hoverIndex < pinnedCount && this.draggedIndex < pinnedCount) {
                this.store.movePinned(this.draggedIndex, hoverIndex);
                this.draggedIndex = hoverIndex;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    *///?}

    private boolean handleAccountClick(double mouseX, double mouseY, int button, boolean doubled) {
        if (button == 0) {
            int clickedIndex = getClickedAccountIndex(mouseX, mouseY);
            if (clickedIndex >= 0) {
                String account = this.store.accounts().get(clickedIndex);
                if (isDeleteButtonClicked(mouseX, mouseY, clickedIndex)) {
                    this.store.remove(account);
                    //? if >=1.21.11 {
                    MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.ui(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 0.7f)
                    );
                    //?} else {
                    /*MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 0.7f)
                    );
                    *///?}
                    this.lastClickIndex = -1;
                    clampScroll(Math.max(1, getListHeight() / ROW_HEIGHT));
                    return true;
                }

                if (isPinButtonClicked(mouseX, mouseY, clickedIndex)) {
                    boolean currentlyPinned = this.store.isPinned(account);
                    this.store.setPinned(account, !currentlyPinned);
                    //? if >=1.21.11 {
                    MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.ui(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.5f)
                    );
                    //?} else {
                    /*MinecraftClient.getInstance().getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.5f)
                    );
                    *///?}
                    return true;
                }

                if (this.store.isPinned(account)) {
                    this.draggedIndex = clickedIndex;
                }

                long now = System.currentTimeMillis();
                if (doubled || clickedIndex == lastClickIndex && now - lastClickTime <= DOUBLE_CLICK_MS) {
                    SessionSwitcher.switchTo(account);
                    this.store.select(account);
                    this.status = "";
                    lastClickIndex = -1;
                    reconnectIfOnServer();
                    return true;
                }

                this.lastClickIndex = clickedIndex;
                this.lastClickTime = now;
                this.status = "";
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listHeight = getListHeight();
        int visibleRows = Math.max(1, listHeight / ROW_HEIGHT);
        this.scrollOffset -= (int) Math.signum(verticalAmount);
        clampScroll(visibleRows);
        return true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void addTypedAccount() {
        String nickname = this.nicknameField.getText();
        if (this.store.add(nickname)) {
            this.nicknameField.setText("");
            this.status = "";
        } else {
            this.status = "";
        }
    }

    private void addRandomAccount() {
        String nickname = AltNicknameGenerator.createUnique(store);
        if (this.store.add(nickname)) {
            this.status = "";
        }
    }

    private int getClickedAccountIndex(double mouseX, double mouseY) {
        int centerX = this.width / 2;
        int listTop = getListTop();
        int listHeight = getListHeight();
        int panelWidth = getPanelWidth();
        int listLeft = centerX - panelWidth / 2;
        int listRight = centerX + panelWidth / 2;

        if (mouseX < listLeft || mouseX > listRight || mouseY < listTop || mouseY > listTop + listHeight) {
            return -1;
        }

        int index = scrollOffset + ((int) mouseY - listTop) / ROW_HEIGHT;
        return index >= 0 && index < this.store.accounts().size() ? index : -1;
    }

    private boolean isDeleteButtonClicked(double mouseX, double mouseY, int accountIndex) {
        int centerX = this.width / 2;
        int panelWidth = getPanelWidth();
        int listRight = centerX + panelWidth / 2;
        int rowTop = getListTop() + (accountIndex - scrollOffset) * ROW_HEIGHT;
        return isDeleteButtonHovered(mouseX, mouseY, listRight, rowTop);
    }

    private boolean isDeleteButtonHovered(double mouseX, double mouseY, int listRight, int rowTop) {
        int buttonLeft = listRight - 18;
        int buttonTop = rowTop + 5;
        return mouseX >= buttonLeft
                && mouseX <= buttonLeft + DELETE_BUTTON_SIZE
                && mouseY >= buttonTop
                && mouseY <= buttonTop + DELETE_BUTTON_SIZE;
    }

    private boolean isPinButtonClicked(double mouseX, double mouseY, int accountIndex) {
        int centerX = this.width / 2;
        int panelWidth = getPanelWidth();
        int listRight = centerX + panelWidth / 2;
        int rowTop = getListTop() + (accountIndex - scrollOffset) * ROW_HEIGHT;
        return isPinButtonHovered(mouseX, mouseY, listRight, rowTop);
    }

    private boolean isPinButtonHovered(double mouseX, double mouseY, int listRight, int rowTop) {
        int buttonLeft = listRight - 34;
        int buttonTop = rowTop + 5;
        return mouseX >= buttonLeft
                && mouseX <= buttonLeft + 12
                && mouseY >= buttonTop
                && mouseY <= buttonTop + 12;
    }

    private int getListTop() {
        return getTop() + 64;
    }

    private int getListHeight() {
        return Math.max(88, Math.min(220, this.height - getListTop() - 62));
    }

    private int getTop() {
        return Math.max(26, this.height / 2 - 145);
    }

    private int getPanelWidth() {
        return Math.min(LIST_WIDTH, this.width - 32);
    }

    private void clampScroll(int visibleRows) {
        int maxOffset = Math.max(0, this.store.accounts().size() - visibleRows);
        if (scrollOffset < 0) {
            scrollOffset = 0;
        } else if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }
    }

    private void reconnectIfOnServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        net.minecraft.client.network.ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null) {
            net.minecraft.client.gui.screen.Screen parentScreen = new net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen(new net.minecraft.client.gui.screen.TitleScreen());
            net.minecraft.client.network.ServerAddress serverAddress = net.minecraft.client.network.ServerAddress.parse(serverInfo.address);
            //? if >=1.21.6 {
            net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(parentScreen, client, serverAddress, serverInfo, false, null);
            //?} else {
            /*net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(parentScreen, client, serverAddress, serverInfo, false, null);
            *///?}
        }
    }
}
