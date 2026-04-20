package com.playertag.client.gui;

import com.playertag.client.config.TagConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.*;

public class TagConfigScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget nameInput;
    private int scrollOffset = 0;

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 360;
    private static final int ROW_H = 22;

    public TagConfigScreen(Screen parent) {
        super(Text.literal("PlayerTag — Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // Input field
        nameInput = new TextFieldWidget(textRenderer, cx + 10, cy + 40, PANEL_W - 80, 18,
                Text.literal("Никнейм"));
        nameInput.setMaxLength(40);
        nameInput.setPlaceholder(Text.literal("Введи никнейм..."));
        addDrawableChild(nameInput);

        // Add as friend
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("✔ Друг"),
                        btn -> addPlayer(TagConfig.PlayerCategory.FRIEND))
                .dimensions(cx + PANEL_W - 68, cy + 40, 60, 18)
                .tooltip(Tooltip.of(Text.literal("Добавить как друга (зелёный)")))
                .build());

        // Add as enemy — second row
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("✘ Враг"),
                        btn -> addPlayer(TagConfig.PlayerCategory.ENEMY))
                .dimensions(cx + PANEL_W - 68, cy + 62, 60, 18)
                .tooltip(Tooltip.of(Text.literal("Добавить как врага (красный)")))
                .build());

        // Toggle mod on/off
        addDrawableChild(ButtonWidget.builder(
                        Text.literal(TagConfig.get().enabled ? "Мод: ВКЛ" : "Мод: ВЫКЛ"),
                        btn -> {
                            TagConfig.get().enabled = !TagConfig.get().enabled;
                            TagConfig.save();
                            btn.setMessage(Text.literal(TagConfig.get().enabled ? "Мод: ВКЛ" : "Мод: ВЫКЛ"));
                        })
                .dimensions(cx + 10, cy + PANEL_H - 30, 90, 18)
                .build());

        // Toggle glow
        addDrawableChild(ButtonWidget.builder(
                        Text.literal(TagConfig.get().showGlow ? "Свечение: ВКЛ" : "Свечение: ВЫКЛ"),
                        btn -> {
                            TagConfig.get().showGlow = !TagConfig.get().showGlow;
                            TagConfig.save();
                            btn.setMessage(Text.literal(TagConfig.get().showGlow ? "Свечение: ВКЛ" : "Свечение: ВЫКЛ"));
                        })
                .dimensions(cx + 110, cy + PANEL_H - 30, 110, 18)
                .build());

        // Close
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Закрыть"),
                        btn -> close())
                .dimensions(cx + PANEL_W - 80, cy + PANEL_H - 30, 70, 18)
                .build());

        // Scroll buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("▲"),
                        btn -> { if (scrollOffset > 0) scrollOffset--; })
                .dimensions(cx + PANEL_W - 20, cy + 90, 16, 14)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▼"),
                        btn -> scrollOffset++)
                .dimensions(cx + PANEL_W - 20, cy + PANEL_H - 55, 16, 14)
                .build());
    }

    private void addPlayer(TagConfig.PlayerCategory cat) {
        String name = nameInput.getText().trim();
        if (name.isEmpty()) return;
        if (cat == TagConfig.PlayerCategory.FRIEND) TagConfig.get().addFriend(name);
        else TagConfig.get().addEnemy(name);
        nameInput.setText("");
        scrollOffset = 0;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // Panel background
        ctx.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, 0xCC1a1a2e);
        ctx.fill(cx, cy, cx + PANEL_W, cy + 2, 0xFF4ade80);   // green top border
        ctx.fill(cx, cy + PANEL_H - 2, cx + PANEL_W, cy + PANEL_H, 0xFFf87171); // red bottom

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§a● §fPlayerTag§7  —  §fНастройки §a●"),
                cx + PANEL_W / 2, cy + 10, 0xFFFFFF);

        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§7Добавить игрока:"),
                cx + 10, cy + 28, 0xAAAAAA);

        ctx.drawTextWithShadow(textRenderer,
                Text.literal("§7Список игроков:"),
                cx + 10, cy + 78, 0xAAAAAA);

        // Player list area background
        ctx.fill(cx + 8, cy + 88, cx + PANEL_W - 24, cy + PANEL_H - 40, 0xAA0d0d1a);

        // Build combined list: friends then enemies
        List<Map.Entry<String, TagConfig.PlayerCategory>> entries = new ArrayList<>();
        for (String f : TagConfig.get().friends) entries.add(Map.entry(f, TagConfig.PlayerCategory.FRIEND));
        for (String e : TagConfig.get().enemies) entries.add(Map.entry(e, TagConfig.PlayerCategory.ENEMY));
        entries.sort(Comparator.comparing(Map.Entry::getKey));

        int listY = cy + 91;
        int listH = PANEL_H - 40 - 88 - 3;
        int visibleRows = listH / ROW_H;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, entries.size() - visibleRows)));

        // Scissor / clip
        ctx.enableScissor(cx + 8, cy + 88, cx + PANEL_W - 24, cy + PANEL_H - 40);

        for (int i = scrollOffset; i < entries.size() && i < scrollOffset + visibleRows + 1; i++) {
            Map.Entry<String, TagConfig.PlayerCategory> entry = entries.get(i);
            int rowY = listY + (i - scrollOffset) * ROW_H;
            boolean isFriend = entry.getValue() == TagConfig.PlayerCategory.FRIEND;

            // Row bg
            ctx.fill(cx + 10, rowY, cx + PANEL_W - 26, rowY + ROW_H - 2,
                    isFriend ? 0x2200cc44 : 0x22cc2200);

            // Category dot
            ctx.fill(cx + 13, rowY + 7, cx + 19, rowY + 13,
                    isFriend ? 0xFF4ade80 : 0xFFf87171);

            // Name
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal((isFriend ? "§a" : "§c") + entry.getKey()),
                    cx + 24, rowY + 7, 0xFFFFFF);

            // Category label
            String label = isFriend ? "§7[друг]" : "§7[враг]";
            ctx.drawTextWithShadow(textRenderer, Text.literal(label),
                    cx + PANEL_W - 100, rowY + 7, 0x888888);
        }

        ctx.disableScissor();

        // Scroll indicator
        if (entries.size() > visibleRows) {
            int totalH = listH - 4;
            int barH = Math.max(10, totalH * visibleRows / entries.size());
            int barY = cy + 90 + (scrollOffset * (totalH - barH) / Math.max(1, entries.size() - visibleRows));
            ctx.fill(cx + PANEL_W - 23, cy + 90, cx + PANEL_W - 19, cy + 88 + listH, 0x55ffffff);
            ctx.fill(cx + PANEL_W - 23, barY, cx + PANEL_W - 19, barY + barH, 0xAAaaaaaa);
        }

        if (entries.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7Список пуст. Добавьте игроков выше."),
                    cx + (PANEL_W - 24) / 2 + 8, cy + 88 + listH / 2 - 4, 0x666666);
        }

        // Keybind hint
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8Клик на ник в игре → ПКМ для быстрого добавления  •  [Y] открыть меню"),
                cx + PANEL_W / 2, cy + PANEL_H + 5, 0x555555);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) scrollOffset++;
        else if (scrollOffset > 0) scrollOffset--;
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
