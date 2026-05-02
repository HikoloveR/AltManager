package ru.fakefun.altmanager.screen;

import net.minecraft.client.MinecraftClient;
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
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
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

            context.fill(listLeft, rowTop, listRight, rowTop + ROW_HEIGHT - 1, background);
            context.drawTextWithShadow(this.textRenderer, account, listLeft + 8, rowTop + 7, color);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("x"), listRight - 12, rowTop + 7, 0xFFFF5555);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            int clickedIndex = getClickedAccountIndex(click.x(), click.y());
            if (clickedIndex >= 0) {
                String account = this.store.accounts().get(clickedIndex);
                if (isDeleteButtonClicked(click.x(), click.y(), clickedIndex)) {
                    this.store.remove(account);
                    this.lastClickIndex = -1;
                    clampScroll(Math.max(1, getListHeight() / ROW_HEIGHT));
                    return true;
                }

                long now = System.currentTimeMillis();
                if (doubled || clickedIndex == lastClickIndex && now - lastClickTime <= DOUBLE_CLICK_MS) {
                    SessionSwitcher.switchTo(account);
                    this.store.select(account);
                    this.status = "";
                    lastClickIndex = -1;
                    return true;
                }

                this.lastClickIndex = clickedIndex;
                this.lastClickTime = now;
                this.status = "";
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
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
}
