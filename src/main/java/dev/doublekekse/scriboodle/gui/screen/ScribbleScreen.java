package dev.doublekekse.scriboodle.gui.screen;

import dev.doublekekse.scriboodle.gui.widget.button.ColorSelectButton;
import dev.doublekekse.scriboodle.gui.widget.button.PenSizeButton;
import dev.doublekekse.scriboodle.gui.widget.ScribbleArea;
import dev.doublekekse.scriboodle.gui.widget.button.ScribbleItemButton;
import dev.doublekekse.scriboodle.data.PaginatedScribbleData;
import dev.doublekekse.scriboodle.component.ScribbleStyle;
import dev.doublekekse.scriboodle.packet.ScribblePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScribbleScreen extends Screen {

    private final List<Renderable> customRenderables = new ArrayList<>();

    PaginatedScribbleData paginatedScribbleData;

    PageButton forwardButton;
    PageButton backButton;

    int currentPage = 0;

    final ScribbleStyle style;

    final Player owner;
    final int slot;

    ScribbleArea scribbleArea;

    public ScribbleScreen(Player owner, int slot, PaginatedScribbleData paintedScribbleData, ScribbleStyle style) {
        super(Component.translatable("scriboodle.screen.scribble"));

        this.owner = owner;
        this.slot = slot;

        this.style = style;
        this.paginatedScribbleData = paintedScribbleData.clone();

        scribbleArea = new ScribbleArea(
            foregroundLeft(),
            foregroundTop(),
            paginatedScribbleData.firstPage(),
            minecraft.getTextureManager(),
            minecraft.getSoundManager()
        );
    }

    @Override
    protected void init() {
        scribbleArea.setPosition(foregroundLeft(), foregroundTop());

        int margin = 5;
        int dist = 16 + margin;

        assert minecraft.level != null;
        var dyes = minecraft.level.registryAccess().get(ItemTags.DYES);
        var totalCount = new AtomicInteger();
        dyes.ifPresent(holders -> holders.forEach(dyeHolder -> {
            var dye = dyeHolder.components().get(DataComponents.DYE);
            if (dye != null) {
                totalCount.incrementAndGet();
            }
        }));

        int columnCount = 2;
        int rowCount = totalCount.get() / columnCount;

        int l = (foregroundLeft() - ((columnCount + 1) * dist));
        int t = foregroundTop();

        int r = (backgroundLeft() + (style.backgroundWidth()) - style.drawMarginRight());

        var i = new AtomicInteger();
        dyes.ifPresent(holders -> holders.forEach(dyeHolder -> {
            var dye = dyeHolder.components().get(DataComponents.DYE);
            if (dye != null) {
                addRenderableWidget(
                    new ColorSelectButton(l + ((i.get() / rowCount) * dist), t + (i.getAndIncrement() % rowCount) * dist, Component.literal(dye.getName()), new ItemStack(dyeHolder.value()), dye, scribbleArea)
                );
            }
        }));

        addRenderableWidget(new ScribbleItemButton(r + dist, t, new ItemStack(Items.GLOW_INK_SAC).getDisplayName(), _ -> scribbleArea.nextColorMode(), new ItemStack(Items.GLOW_INK_SAC), scribbleArea) {
            @Override
            protected boolean outlineVisible() {
                return scribbleArea.colorMode == ScribbleArea.ColorMode.TEXT;
            }
        });

        int left = this.backgroundLeft();
        int top = this.backgroundTop();

        this.backButton = this.addWidget(new PageButton(left + 43, top + 157, false, _ -> this.pageBack(), true));
        this.forwardButton = this.addWidget(new PageButton(left + 116, top + 157, true, _ -> this.pageForward(), true));

        this.customRenderables.add(backButton);
        this.customRenderables.add(forwardButton);

        addRenderableWidget(new PenSizeButton(r + dist, t + dist, Component.literal("pen size")));

        var c = 3;
        for (int toolIdx = 0; toolIdx < ScribbleArea.tools.size(); toolIdx++) {
            addRenderableWidget(new ToolSelectButton(r + dist, t + c * dist, toolIdx));
            c++;
        }

        updateButtonVisibility();
        scribbleArea.updateColor();
    }

    class ToolSelectButton extends ScribbleItemButton {
        int toolIndex;

        protected ToolSelectButton(int x, int y, int toolIndex) {
            var tool = ScribbleArea.tools.get(toolIndex);

            super(x, y, tool.itemStack().getItemName(), null, tool.itemStack(), scribbleArea);

            this.toolIndex = toolIndex;
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            if (ScribbleArea.toolIndex != toolIndex) {
                ScribbleArea.toolIndex = toolIndex;
            } else {
                if (minecraft.hasShiftDown()) {
                    scribbleArea.resetTool(toolIndex);
                    return;
                }
                minecraft.setScreen(new ToolModifyScreen((modified) -> {
                    ScribbleArea.tools.set(toolIndex, modified);
                    minecraft.setScreen(ScribbleScreen.this);
                }, ScribbleArea.tools.get(toolIndex), scribbleArea.realColor, ScribbleArea.radiusIndex));
            }
        }

        @Override
        protected boolean outlineVisible() {
            return ScribbleArea.toolIndex == toolIndex;
        }
    }

    private int backgroundLeft() {
        return (this.width - style.backgroundWidth()) / 2;
    }

    private int backgroundTop() {
        return 2;
    }

    private int foregroundLeft() {
        return backgroundLeft() + style.drawMarginLeft();
    }

    private int foregroundTop() {
        return backgroundTop() + style.drawMarginTop();
    }

    @Override
    public void extractBackground(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, style.backgroundTexture(), this.backgroundLeft(), this.backgroundTop(), 0.0F, 0.0F, style.backgroundWidth(), style.backgroundHeight(), style.backgroundTextureWidth(), style.backgroundTextureHeight());
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        var childAtCursor = this.getChildAt(mouseX, mouseY);

        for (var renderable : this.customRenderables) {
            if (scribbleArea.drawing || childAtCursor.isEmpty() || childAtCursor.get() != renderable) {
                renderable.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }


        scribbleArea.extractCustomState(graphics, mouseX, mouseY, childAtCursor.isPresent());

        if (!scribbleArea.drawing) {
            for (var renderable : this.customRenderables) {
                if (childAtCursor.isPresent() && childAtCursor.get() == renderable) {
                    renderable.extractRenderState(graphics, mouseX, mouseY, a);
                }
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        var hasClicked = super.mouseClicked(event, doubleClick);

        if (!hasClicked) {
            scribbleArea.mouseClicked(event, doubleClick);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (minecraft.hasAltDown()) {
            scribbleArea.changeToolOpacity(scrollY / 20);
        } else if (minecraft.hasShiftDown()) {
            if (scrollY > 0) {
                pageForward();
            } else if (scrollY < 0) {
                pageBack();
            }
        } else {
            if (scrollY > 0) {
                ScribbleArea.increaseRadius(false);
            } else if (scrollY < 0) {
                ScribbleArea.decreaseRadius(false);
            }
        }

        return scrollY != 0;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        scribbleArea.mouseReleased(event);
        return super.mouseReleased(event);
    }

    @Override
    public void mouseMoved(double x, double y) {
        scribbleArea.mouseMoved(x, y);
    }

    private void updateButtonVisibility() {
        this.backButton.visible = this.currentPage > 0;
        this.forwardButton.visible = !(this.currentPage + 2 > style.maxPageCount());
    }

    void savePage() {
        if (!scribbleArea.data.isEmpty()) {
            paginatedScribbleData.set(currentPage, scribbleArea.data);
        }
    }

    void pageForward() {
        if (this.currentPage + 2 > style.maxPageCount()) {
            return;
        }

        savePage();
        currentPage++;
        scribbleArea.setData(paginatedScribbleData.get(currentPage));
        updateButtonVisibility();
    }

    void pageBack() {
        if (this.currentPage <= 0) {
            return;
        }

        savePage();
        currentPage--;
        scribbleArea.setData(paginatedScribbleData.get(currentPage));
        updateButtonVisibility();
    }

    @Override
    public void tick() {
        super.tick();

        scribbleArea.tick();
    }

    private void saveChanges() {
        savePage();
        ClientPlayNetworking.send(new ScribblePacket(paginatedScribbleData, slot));
    }

    @Override
    public void onClose() {
        super.onClose();
        scribbleArea.dispose();

        saveChanges();
    }

    @Override
    protected void removeWidget(@NonNull GuiEventListener widget) {
        super.removeWidget(widget);
        if (widget instanceof Renderable) {
            customRenderables.remove(widget);
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        customRenderables.clear();
    }
}
