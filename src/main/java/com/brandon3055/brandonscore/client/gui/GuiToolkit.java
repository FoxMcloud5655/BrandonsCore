package com.brandon3055.brandonscore.client.gui;

import codechicken.lib.gui.modular.ModularGui;
import codechicken.lib.gui.modular.elements.*;
import codechicken.lib.gui.modular.lib.Constraints;
import codechicken.lib.gui.modular.lib.GuiRender;
import codechicken.lib.gui.modular.lib.geometry.*;
import codechicken.lib.gui.modular.sprite.Material;
import com.brandon3055.brandonscore.BCConfig;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.client.BCGuiTextures;
import com.brandon3055.brandonscore.client.gui.modulargui.ShaderEnergyBar;
import com.brandon3055.brandonscore.client.gui.modulargui.ShaderEnergyBar.EnergyBar;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static codechicken.lib.gui.modular.lib.geometry.Constraint.*;
import static codechicken.lib.gui.modular.lib.geometry.GeoParam.*;
import static com.brandon3055.brandonscore.BCConfig.darkMode;

/**
 * Toolkit containing a bunch of standard gui code and elements for DE and BC Guis
 * <p>
 * Created by brandon3055 on 5/7/19.
 */
public class GuiToolkit {
    private static final String INTERNAL_TRANSLATION_PREFIX = "gui_tkt.brandonscore.";
    private String translationPrefix = "";

    public GuiToolkit() {
    }

    public GuiToolkit(String translationPrefix) {
        setTranslationPrefix(translationPrefix);
    }

    //region ############ Translation ############

    private GuiToolkit setTranslationPrefix(String translationPrefix) {
        if (!translationPrefix.endsWith(".") && !translationPrefix.isEmpty()) {
            translationPrefix = translationPrefix + ".";
        }
        this.translationPrefix = translationPrefix;
        return this;
    }

    public MutableComponent translate(String translationKey, Object... args) {
        if (translationKey.startsWith(".")) {
            translationKey = translationKey.substring(1);
        }
        return Component.translatable(translationPrefix + translationKey, args);
    }

    public Supplier<MutableComponent> translate(Supplier<String> translationKey) {
        return () -> Component.translatable(translationPrefix + translationKey.get());
    }

    /**
     * Internal translator for use inside Toolkit
     */
    protected static MutableComponent translateInternal(String translationKey) {
        if (translationKey.startsWith(".")) {
            translationKey = translationKey.substring(1);
        }
        return Component.translatable(INTERNAL_TRANSLATION_PREFIX + translationKey);
    }

    //endregion
    //region ############ Button Helpers ############

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int size, Supplier<Material> iconSupplier) {
        return createIconButton(parent, size, iconSupplier, false);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int size, Supplier<Material> iconSupplier, boolean transHighlight) {
        return createIconButton(parent, size, size, iconSupplier, transHighlight);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonSize, int iconSize, String iconString) {
        return createIconButton(parent, buttonSize, iconSize, iconString, false);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonSize, int iconSize, String iconString, boolean transHighlight) {
        return createIconButton(parent, buttonSize, iconSize, BCGuiTextures.getter(iconString), transHighlight);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonSize, int iconSize, Supplier<Material> iconSupplier) {
        return createIconButton(parent, buttonSize, iconSize, iconSupplier, false);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonSize, int iconSize, Supplier<Material> iconSupplier, boolean transHighlight) {
        return createIconButton(parent, buttonSize, buttonSize, iconSize, iconSize, iconSupplier, transHighlight);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, String iconString) {
        return createIconButton(parent, buttonWidth, buttonHeight, iconWidth, iconHeight, iconString, false);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, String iconString, boolean transHighlight) {
        return createIconButton(parent, buttonWidth, buttonHeight, iconWidth, iconHeight, BCGuiTextures.getter(iconString), transHighlight);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, Supplier<Material> iconSupplier) {
        return createIconButton(parent, buttonWidth, buttonHeight, iconWidth, iconHeight, iconSupplier, false);
    }

    public GuiButton createIconButton(@NotNull GuiParent<?> parent, int buttonWidth, int buttonHeight, int iconWidth, int iconHeight, Supplier<Material> iconSupplier, boolean transHighlight) {
        GuiButton button = new GuiButton(parent);
        Constraints.size(button, buttonWidth, buttonHeight);
        if (!transHighlight) addHoverHighlight(button, null, false);
        GuiTexture icon = new GuiTexture(button, iconSupplier);
        Constraints.size(icon, iconWidth, iconHeight);
        Constraints.center(icon, button);
        if (transHighlight) addHoverHighlight(button, null, true);
        return button;
    }

    public GuiButton createThemedIconButton(@NotNull GuiParent<?> parent, String iconString) {
        return createThemedIconButton(parent, 12, iconString);
    }

    public GuiButton createThemedIconButton(@NotNull GuiParent<?> parent, int size, String iconString) {
        return createIconButton(parent, size, BCGuiTextures.themedGetter(iconString));
    }

    public GuiButton createThemedIconButton(@NotNull GuiParent<?> parent, Supplier<Material> texture) {
        return createThemedIconButton(parent, 12, texture);
    }

    public GuiButton createThemedIconButton(@NotNull GuiParent<?> parent, int size, Supplier<Material> texture) {
        return createIconButton(parent, size, texture);
    }

    public static GuiRectangle addHoverHighlight(@NotNull GuiElement<?> parent) {
        return addHoverHighlight(parent, null, false);
    }

    public static GuiRectangle addHoverHighlight(@NotNull GuiElement<?> parent, @Nullable Borders borders, boolean transparent) {
        GuiRectangle rect = new GuiRectangle(parent)
                .fill(() -> Palette.Ctrl.fill(parent.isMouseOver()) & (transparent ? 0x80FFFFFF : 0xFFFFFFFF))
                .setEnabled(() -> parent.isMouseOver() || (parent instanceof GuiButton b && b.toggleState()));
        Constraints.bind(rect, parent, borders == null ? Borders.create(0) : borders);
        return rect;
    }

    //endregion
    //region ############ Standard Buttons ############

    public GuiButton createThemeButton(@NotNull GuiParent<?> parent) {
        GuiButton button = createThemedIconButton(parent, "theme");
        button.setTooltipSingle(() -> darkMode ? translateInternal("theme.light") : translateInternal("theme.dark"));
        button.onPress(() -> BCConfig.modifyClientProperty("darkMode", e -> e.setBoolean(!darkMode)));
        return button;
    }

    public GuiButton createInfoButton(GuiElement<?> parent, InfoPanel panel) {
        GuiButton button = createIconButton(parent, 12, BCGuiTextures.getter("info_panel"));
        button.setTooltipSingle(() -> translateInternal("info_panel." + (panel.expanded() ? "open" : "closed")));
        button.onPress(panel::toggleExpanded);
        return button;
    }

    public GuiButton createRSSwitch(@NotNull GuiParent<?> parent, IRSSwitchable switchable) {
        GuiButton button = new GuiButton(parent);
        Constraints.size(button, 12, 12);
        addHoverHighlight(button);
        GuiTexture icon = new GuiTexture(button, () -> BCGuiTextures.get("redstone/" + switchable.getRSMode().name().toLowerCase(Locale.ENGLISH)));
        Constraints.bind(icon, button);
        button.setTooltipSingle(() -> translateInternal("rs_mode." + switchable.getRSMode().name().toLowerCase(Locale.ENGLISH)));
        button.onPress(() -> switchable.setRSMode(switchable.getRSMode().next(Screen.hasShiftDown())), GuiButton.LEFT_CLICK);
        button.onPress(() -> switchable.setRSMode(switchable.getRSMode().next(true)), GuiButton.RIGHT_CLICK);
        return button;
    }

    public GuiButton createFlat3DButton(@NotNull GuiParent<?> parent, @Nullable Supplier<Component> label) {
        GuiButton button = new GuiButton(parent);
        GuiRectangle border = new GuiRectangle(button).border(() -> button.isMouseOver() ? 0xFFFFFFFF : 0xFF000000);
        GuiRectangle texture = new GuiRectangle(border);

        Supplier<Integer> tlCol = () -> button.isPressed() || button.toggleState() ? GuiToolkit.Palette.Ctrl.accentDark(true) : GuiToolkit.Palette.Ctrl.accentLight(button.isMouseOver());
        Supplier<Integer> brCol = () -> button.isPressed() || button.toggleState() ? GuiToolkit.Palette.Ctrl.accentLight(true) : GuiToolkit.Palette.Ctrl.accentDark(button.isMouseOver());

        texture.fill(() -> GuiToolkit.Palette.Ctrl.fill(border.isMouseOver() || button.isPressed() || button.toggleState()));
        texture.setEnabled(() -> !button.isDisabled());
        texture.setShadeTopLeft(tlCol);
        texture.setShadeBottomRight(brCol);
        texture.setShadeCorners(() -> GuiRender.midColour(tlCol.get(), brCol.get()));

        GuiTexture disabledBG = new GuiTexture(button, BCGuiTextures.themedGetter("button_disabled"))
                .setEnabled(button::isDisabled)
                .dynamicTexture();
        Constraints.bind(disabledBG, button);

        if (label != null) {
            Window window = parent.mc().getWindow();
            double xp = (float) window.getGuiScaledWidth() / window.getWidth();
            double yp = (float) window.getGuiScaledHeight() / window.getHeight();
            button.setLabel(new GuiText(button, label)
                    .constrain(TOP, Constraint.relative(button.get(TOP), () -> button.isPressed() ? (-0.5D - xp) : -0.5D).precise())
                    .constrain(LEFT, Constraint.relative(button.get(LEFT), () -> button.isPressed() ? (1.5D - yp) : 1.5D).precise())
                    .constrain(WIDTH, Constraint.relative(button.get(WIDTH), -4))
                    .constrain(HEIGHT, Constraint.match(button.get(HEIGHT)))
            );
        }

        Constraints.bind(border, button);
        Constraints.bind(texture, border, 1);
        return button;
    }

    public GuiButton createResizeButton(@NotNull GuiParent<?> parent) {
        GuiButton button = createThemedIconButton(parent, "resize");
        button.setTooltipSingle(() -> translateInternal("large_view"));
        return button;
    }

    public GuiButton createBorderlessButton(@NotNull GuiParent<?> parent) {
        return createBorderlessButton(parent, (Supplier<Component>) null);
    }

    public GuiButton createBorderlessButton(@NotNull GuiParent<?> parent, Component label) {
        return createBorderlessButton(parent, () -> label);
    }

    public GuiButton createBorderlessButton(@NotNull GuiParent<?> parent, @Nullable Supplier<Component> label) {
        GuiButton button = new GuiButton(parent);
        GuiTexture texture = new GuiTexture(button, () -> BCGuiTextures.getThemed("button_borderless" + (button.isPressed() ? "_invert" : "")))
                .dynamicTexture();
        GuiRectangle highlight = new GuiRectangle(button).border(() -> button.hoverTime() > 0 ? 0xFFFFFFFF : 0);
        Constraints.bind(texture, button);
        Constraints.bind(highlight, button);
        if (label != null) {
            Window window = parent.mc().getWindow();
            double xp = (float) window.getGuiScaledWidth() / window.getWidth();
            double yp = (float) window.getGuiScaledHeight() / window.getHeight();
            button.setLabel(new GuiText(button, label)
                    .constrain(TOP, Constraint.relative(button.get(TOP), () -> button.isPressed() ? (-0.5D - xp) : -0.5D).precise())
                    .constrain(LEFT, Constraint.relative(button.get(LEFT), () -> button.isPressed() ? (1.5D - yp) : 1.5D).precise())
                    .constrain(WIDTH, Constraint.relative(button.get(WIDTH), -3))
                    .constrain(HEIGHT, Constraint.match(button.get(HEIGHT)))
            );
        }
        return button;
    }

    //endregion
    //region ############ Text Components ############

    public GuiText playerInvTitle(GuiElement<?> slotsContainer) {
        GuiText title = new GuiText(slotsContainer, translateInternal("your_inventory"))
                .setAlignment(Align.LEFT)
                .setTextColour(Palette.BG::text)
                .setShadow(() -> darkMode)
                .constrain(WIDTH, match(slotsContainer.get(WIDTH)))
                .constrain(HEIGHT, literal(8));
        Constraints.placeInside(title, slotsContainer, Constraints.LayoutPos.TOP_LEFT, 0, -11);
        return title;
    }

    public GuiText createHeading(@NotNull GuiParent<?> parent, Component heading, boolean layout) {
        GuiText guiText = new GuiText(parent, heading);
        guiText.setTextColour(Palette.BG::text);
        guiText.setShadow(() -> darkMode);
        if (layout) {
            guiText.constrain(TOP, relative(parent.get(TOP), 4));
            guiText.constrain(LEFT, match(parent.get(LEFT)));
            guiText.constrain(RIGHT, match(parent.get(RIGHT)));
            guiText.constrain(HEIGHT, literal(8));
        }
        return guiText;
    }

    public GuiText createHeading(@NotNull GuiParent<?> parent, Component heading) {
        return createHeading(parent, heading, false);
    }


    //endregion
    //region ############ Bars ############

    public EnergyBar createEnergyBar(@NotNull GuiParent<?> parent, @Nullable IOPStorage storage) {
        Supplier<Integer> dark = () -> darkMode ? 0xFF808080 : 0xFF505050;
        GuiRectangle container = new GuiRectangle(parent)
                .setShadeTopLeft(dark)
                .setShadeBottomRight(() -> 0xFFFFFFFF)
                .setShadeCorners(() -> GuiRender.midColour(0xFFFFFFFF, dark.get()));
        ShaderEnergyBar energyBar = new ShaderEnergyBar(container);
        energyBar.setToolTipFormatter(ShaderEnergyBar.opEnergyFormatter(storage));
        energyBar.bindOpStorage(storage);

        Constraints.bind(energyBar, container, 1);
        return new EnergyBar(container, energyBar);
    }

    public GuiTexture energySlotArrow(@NotNull GuiParent<?> parent, boolean chargeItem, boolean bellowBar) {
        return new GuiTexture(parent, BCGuiTextures.get("item_charge/" + ((bellowBar ? "vertical" : "right") + "_" + (chargeItem ? "charge" : "discharge"))))
                .constrain(WIDTH, literal(bellowBar ? 12 : 14))
                .constrain(HEIGHT, literal(bellowBar ? 10 : 14));
    }


    public VanillaBar vanillaScrollBar(GuiElement<?> parent, Axis axis) {
        GuiTexture barBg = new GuiTexture(parent, BCGuiTextures.themedGetter("button_disabled"))
                .dynamicTexture();

        GuiSlider scrollBar = new GuiSlider(barBg, axis);
        Constraints.bind(scrollBar, barBg, 1);
        scrollBar.installSlider(GuiRectangle.planeButton(scrollBar))
                .bindSliderLength()
                .bindSliderWidth();
        GuiRectangle sliderHighlight = new GuiRectangle(scrollBar.getSlider())
                .fill(0x5000b6FF)
                .setEnabled(() -> scrollBar.getSlider().isMouseOver());
        Constraints.bind(sliderHighlight, scrollBar.getSlider());

        return new VanillaBar(barBg, scrollBar, sliderHighlight);
    }

    public record VanillaBar(GuiTexture container, GuiSlider slider, GuiRectangle highlight) {}


    //endregion
    //region ############ Basic Elements ############

    public GuiRectangle embossBorder(@NotNull GuiParent<?> parent) {
        return embossBorder(parent, false);
    }

    public GuiRectangle embossBorder(@NotNull GuiParent<?> parent, boolean invert) {
        GuiRectangle border = new GuiRectangle(parent)
                .setShadeBottomRight(() -> invert ? Palette.BG.accentLight() : Palette.BG.accentDark())
                .setShadeTopLeft(() -> invert ? Palette.BG.accentDark() : Palette.BG.accentLight())
                .setShadeCorners(Palette.BG::fill)
                .fill(0);
        Constraints.bind(GuiRectangle.vanillaSlot(border)
                        .setShadeBottomRight(() -> invert ? Palette.BG.accentDark() : Palette.BG.accentLight())
                        .setShadeTopLeft(() -> invert ? Palette.BG.accentLight() : Palette.BG.accentDark())
                        .setShadeCorners(Palette.BG::fill)
                        .fill(0),
                border, 1);
        return border;
    }

    public GuiRectangle debossBorder(@NotNull GuiParent<?> parent) {
        return embossBorder(parent, true);
    }

    public GuiRectangle shadedBorder(@NotNull GuiParent<?> parent) {
        return shadedBorder(parent, false);
    }

    public GuiRectangle shadedBorder(@NotNull GuiParent<?> parent, boolean invert) {
        return new GuiRectangle(parent)
                .setShadeBottomRight(() -> invert ? Palette.BG.accentDark() : Palette.BG.accentLight())
                .setShadeTopLeft(() -> invert ? Palette.BG.accentLight() : Palette.BG.accentDark())
                .setShadeCorners(Palette.BG::fill)
                .fill(0);
    }


    //#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // TODO, Everything bellow this point still needs to be evaluated / re-written
    //#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=


//    //Create Background (various sizes)
//    public GuiTexture createBackground(boolean addToManager, boolean center) {
//        if (layout.xSize == -1 || layout.ySize == -1) {  //TODO maybe add a way do provide a background builder in this case?
//            throw new UnsupportedOperationException("Layout type " + layout + " does not have an associated default background.");
//        }
//
//        //TODO move to a function in BCTextures?
//        GuiTexture texture = new GuiTexture(() -> BCGuiSprites.getThemed(layout.textureName()));
//        texture.setSize(layout.xSize, layout.ySize);
//        if (addToManager) {
//            gui.getManager().addChild(texture);
//        }
//        if (center) {
//            texture.onReload(guiTex -> guiTex.setPos(gui.guiLeft(), gui.guiTop()));
//        }
//        return texture;
//    }

//    public GuiTexture createBackground(boolean center) {
//        return createBackground(false, center);
//    }
//
//    public GuiTexture createBackground() {
//        return createBackground(false);
//    }

    //UI Heading

    public GuiElement<?> floatingHeading(ModularGui gui) {
        return floatingHeading(gui.getRoot(), gui.getGuiTitle());
    }

    public GuiElement<?> floatingHeading(@NotNull GuiElement<?> parent, Component heading) {
        GuiManipulable titleMovable = new GuiManipulable(parent)
                .addMoveHandle(13)
                .enableCursors(true);
        Constraints.size(titleMovable, () -> parent.font().width(heading) + 10D, () -> 13D);

        GuiElement<?> titleBackground = new GuiRectangle(titleMovable.getContentElement())
                .fill(0x80000000);
        Constraints.bind(titleBackground, titleMovable.getContentElement());
        Constraints.bind(createHeading(titleBackground, heading).setTextColour(0xDFD1D3), titleBackground);
        return titleMovable;
    }

//    /**
//     * Creates a generic set of inventory slots with the specified dimensions.
//     * background is an optional 16x16 sprite that will be used as the slot background.
//     *
//     * @param slotMapper (column, row, slotData)
//     */
//    public GuiElement<?> createSlots(GuiElement<?> parent, int columns, int rows, int spacing, BiFunction<Integer, Integer, SlotMover> slotMapper, Material background) {
//        GuiElement<?> element = new GuiElement() {
//            @Override
//            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//                Material slot = BCGuiSprites.getThemed("slot");
//                MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
//                VertexConsumer buffer = getter.getBuffer(BCGuiSprites.GUI_TYPE);
//
//                for (int x = 0; x < columns; x++) {
//                    for (int y = 0; y < rows; y++) {
//                        drawSprite(buffer, xPos() + (x * (18 + spacing)), yPos() + (y * (18 + spacing)), 18, 18, slot.sprite());
//                    }
//                }
//
//                if (background != null) {
//                    for (int x = 0; x < columns; x++) {
//                        for (int y = 0; y < rows; y++) {
//                            if (slotMapper != null) {
//                                SlotMover data = slotMapper.apply(x, y);
//                                if (data != null && data.slot.hasItem()) {
//                                    continue;
//                                }
//                            }
//                            drawSprite(buffer, xPos() + (x * (18 + spacing)) + 1, yPos() + (y * (18 + spacing)) + 1, 16, 16, background.sprite());
//                        }
//                    }
//                }
//
//                getter.endBatch();
//            }
//
//            @Override
//            public GuiElement<?> translate(int xAmount, int yAmount) {
//                GuiElement<?> ret = super.translate(xAmount, yAmount);
//                if (slotMapper != null) {
//                    for (int x = 0; x < columns; x++) {
//                        for (int y = 0; y < rows; y++) {
//                            SlotMover data = slotMapper.apply(x, y);
//                            if (data != null) {
//                                data.setPos((xPos() + (x * (18 + spacing))) - gui.guiLeft() + 1, (yPos() + (y * (18 + spacing))) - gui.guiTop() + 1);
//                            }
//                        }
//                    }
//                }
//                return ret;
//            }
//        };
//        element.setSize((columns * 18) + ((columns - 1) * spacing), (rows * 18) + ((rows - 1) * spacing));
//        if (parent != null) {
//            parent.addChild(element);
//        }
//
//        return element;
//    }
//
//    public GuiElement<?> createSlots(GuiElement<?> parent, int columns, int rows, int spacing) {
//        return createSlots(parent, columns, rows, spacing, null, null);
//    }
//
//    public GuiElement<?> createSlots(GuiElement<?> parent, int columns, int rows) {
//        return createSlots(parent, columns, rows, 0);
//    }
//
//    public GuiElement<?> createSlots(GuiElement<?> parent, int columns, int rows, int spacing, Material slotTexture) {
//        return createSlots(parent, columns, rows, spacing, null, slotTexture);
//    }
//
//    public GuiElement<?> createSlots(GuiElement<?> parent, int columns, int rows, Material slotTexture) {
//        return createSlots(parent, columns, rows, 0, null, slotTexture);
//    }
//
//    public GuiElement<?> createSlots(int columns, int rows) {
//        return createSlots(null, columns, rows, 0);
//    }
//
//    public GuiElement<?> createSlot(GuiElement<?> parent, SlotMover slotMover, Supplier<Material> background, boolean largeSlot) {
//        int size = largeSlot ? 26 : 18;
//        GuiElement<?> element = new GuiElement() {
//            @Override
//            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//                Material slot = BCGuiSprites.getThemed(largeSlot ? "slot_large" : "slot");
//                MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
//                VertexConsumer buffer = getter.getBuffer(BCGuiSprites.GUI_TYPE);
//                drawSprite(buffer, xPos(), yPos(), size, size, slot.sprite());
//                if (background != null && (slotMover == null || !slotMover.slot.hasItem())) {
//                    int offset = largeSlot ? 5 : 1;
//                    drawSprite(buffer, xPos() + offset, yPos() + offset, 16, 16, background.get().sprite());
//                }
//                getter.endBatch();
//            }
//
//            @Override
//            public GuiElement<?> translate(int xAmount, int yAmount) {
//                GuiElement<?> ret = super.translate(xAmount, yAmount);
//                if (slotMover != null) {
//                    slotMover.setPos(xPos() - gui.guiLeft() + (largeSlot ? 5 : 1), yPos() - gui.guiTop() + (largeSlot ? 5 : 1));
//                }
//                return ret;
//            }
//        };
//        element.setSize(size, size);
//        if (parent != null) {
//            parent.addChild(element);
//        }
//
//        return element;
//    }
//
//    /**
//     * Creates the standard player inventory slot layout.
//     */
//    public GuiElement<?> createPlayerSlots(GuiElement<?> parent, boolean title) {
//        return createPlayerSlots(parent, title, false, false);
//    }
//
//    public GuiElement<?> createPlayerSlots(GuiElement<?> parent, boolean title, boolean addArmor, boolean addOffHand) {
//        GuiElement<?> container = new GuiElement<>();
//        GuiElement<?> main = createSlots(container, 9, 3, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column + row * 9 + 9), null);
//        GuiElement<?> bar = createSlots(container, 9, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_INV, column), null);
//        bar.setYPos(main.maxYPos() + 3);
//
//        if (title) {
//            GuiLabel invTitle = new GuiLabel(translateIntern("your_inventory"));
//            invTitle.setAlignment(GuiAlign.LEFT).setHoverableTextCol(hovering -> Palette.BG.text());
//            invTitle.setShadowStateSupplier(() -> darkMode);
//            container.addChild(invTitle);
//            invTitle.setSize(main.xSize(), 8);
//            main.translate(0, 10);
//            bar.translate(0, 10);
//        }
//
//        if (addArmor) {
//            for (int i = 0; i < 4; i++) {
//                int finalI = 3 - i;
//                GuiElement<?> element = createSlots(container, 1, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_ARMOR, finalI), BCGuiSprites.getArmorSlot(finalI));
//                element.setMaxXPos(main.xPos() - 3, false);
//                element.setYPos(main.yPos() + (i * 19));
//            }
//        }
//
//        if (addOffHand) {
//            GuiElement<?> element = createSlots(container, 1, 1, 0, slotLayout == null ? null : (column, row) -> slotLayout.getSlotData(PLAYER_OFF_HAND, 4), BCGuiSprites.get("slots/armor_shield"));
//            element.setXPos(main.maxXPos() + 3);
//            element.setMaxYPos(bar.maxYPos(), false);
//        }
//
//        container.setBoundsToChildren();
//
//        if (parent != null) {
//            parent.addChild(container);
//        }
//
//        return container;
//    }
//
//    public GuiElement<?> createPlayerSlotsManualMovers(GuiElement<?> parent, boolean title, Function<Integer, SlotMover> slotGetter) {
//        GuiElement<?> container = new GuiElement<>();
//        GuiElement<?> main = createSlots(container, 9, 3, 0, (column, row) -> slotGetter.apply(column + row * 9 + 9), null);
//        GuiElement<?> bar = createSlots(container, 9, 1, 0, (column, row) -> slotGetter.apply(column), null);
//        bar.setYPos(main.maxYPos() + 3);
//
//        if (title) {
//            GuiLabel invTitle = new GuiLabel(translateIntern("your_inventory"));
//            invTitle.setAlignment(GuiAlign.LEFT).setHoverableTextCol(hovering -> Palette.BG.text());
//            invTitle.setShadowStateSupplier(() -> darkMode);
//            container.addChild(invTitle);
//            invTitle.setSize(main.xSize(), 8);
//            main.translate(0, 10);
//            bar.translate(0, 10);
//        }
//
//        container.setBoundsToChildren();
//
//        if (parent != null) {
//            parent.addChild(container);
//        }
//
//        return container;
//    }
//
//    public GuiElement<?> createEquipModSlots(GuiElement<?> parent, Player player, boolean jeiExclude, Predicate<ItemStack> showFilter) {
//        GuiElement<?> fallback = new GuiElement<>();
//        if (equipmentManager != null) {
//            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(player);
//            GuiElement<?> container = GuiTexture.newDynamicTexture(() -> BCGuiSprites.getThemed("bg_dynamic_small"));
//            container.setXSize(26);
//            optional.ifPresent(handler -> {
//                if (jeiExclude) {
//                    jeiExclude(container);
//                }
//                parent.addBackGroundChild(container);
//                int c = 0;
//                for (int i = 0; i < handler.getSlots(); i++) {
//                    int finalI = i;
//                    SlotMover data = slotLayout.getSlotData(PLAYER_EQUIPMENT, finalI);
//                    if (showFilter != null && !showFilter.test(data.slot.getItem())) {
//                        data.setPos(-9999, -9999);
//                        continue;
//                    }
//                    GuiElement<?> element = createSlots(container, 1, 1, 0, (column, row) -> data, null);
//                    element.setXPos(container.xPos() + 4, false);
//                    element.setYPos(container.yPos() + (c * 19) + 4);
//                    container.setMaxYPos(element.maxYPos() + 4, true);
//                    c++;
//                }
//            });
//            return container.getChildElements().isEmpty() ? fallback : container;
//        }
//        return fallback;
//    }
//
//    public GuiElement<?> createPlayerSlots() {
//        return createPlayerSlots(null, true);
//    }

    //region  Buttons
    //####################################################################################################

//    public GuiButton createVanillaButton(String unlocalizedText, @Nullable GuiElement<?> parent) {
//        GuiButton button = new GuiButton(I18n.get(unlocalizedText));
//        button.setHoverTextDelay(10);
//        button.enableVanillaRender();
//        if (parent != null) {
//            parent.addChild(button);
//        }
//        return button;
//    }
//
//    public GuiButton createVanillaButton(String unlocalizedText) {
//        return createVanillaButton(unlocalizedText, null);
//    }


//    @Deprecated
//    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement<?> parent, boolean inset3d, double doubleBoarder) {
//        GuiButton button = new GuiButton(I18n.get(unlocalizedText));
//        button.setInsets(5, 2, 5, 2);
//        button.setHoverTextDelay(10);
//        if (inset3d) {
//            button.set3dText(true);
//            GuiBorderedRect buttonBG = new GuiBorderedRect().setDoubleBorder(doubleBoarder);
//            //I use modifiers here to account for the possibility that this button may have modifiers. Something i need to account for when i re write modular gui
//            buttonBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
//            buttonBG.setFillColourL(hovering -> Palette.Ctrl.fill(hovering || button.isPressed()));
//            buttonBG.setBorderColourL(Palette.Ctrl::border3D);
//            buttonBG.set3dTopLeftColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentDark(true) : Palette.Ctrl.accentLight(hovering));
//            buttonBG.set3dBottomRightColourL(hovering -> button.isPressed() ? Palette.Ctrl.accentLight(true) : Palette.Ctrl.accentDark(hovering));
//            GuiTexture disabledBG = GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_disabled"));
//            disabledBG.setPosModifiers(button::xPos, button::yPos).setSizeModifiers(button::xSize, button::ySize);
//            disabledBG.setEnabledCallback(button::isDisabled);
//            buttonBG.addChild(disabledBG);
//
//            button.addChild(buttonBG);
//        } else {
//            button.setRectFillColourGetter((hovering, disabled) -> Palette.Ctrl.fill(hovering));
//            button.setRectBorderColourGetter((hovering, disabled) -> Palette.Ctrl.border(hovering));
//        }
//        button.setTextColGetter((hovering, disabled) -> Palette.Ctrl.textH(hovering));
//
//        if (parent != null) {
//            parent.addChild(button);
//        }
//        return button;
//    }
//
//    @Deprecated
//    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement<?> parent, boolean inset3d) {
//        return createButton_old(unlocalizedText, parent, inset3d, 1);
//    }
//
//    @Deprecated
//    public GuiButton createButton_old(String unlocalizedText, @Nullable GuiElement<?> parent) {
//        return createButton_old(unlocalizedText, parent, true);
//    }
//
//    @Deprecated
//    public GuiButton createButton_old(String unlocalizedText, boolean shadeEdges) {
//        return createButton_old(unlocalizedText, null, shadeEdges);
//    }
//
//    @Deprecated
//    public GuiButton createButton_old(String unlocalizedText) {
//        return createButton_old(unlocalizedText, null, true);
//    }

    public GuiButton createButton(@NotNull GuiParent<?> parent, @Nullable Supplier<Component> text, boolean inset3d, int doubleBoarder) {
        GuiButton button = new GuiButton(parent);

        if (inset3d) {
            GuiRectangle border = new GuiRectangle(button)
                    .borderWidth(doubleBoarder)
                    .border(() -> Palette.Ctrl.border3D(button.isMouseOver()));

            GuiRectangle background = new GuiRectangle(button)
                    .fill(() -> Palette.Ctrl.fill(button.isMouseOver() || button.isPressed()))
                    .setShadeTopLeft(() -> button.isPressed() ? Palette.Ctrl.accentDark(true) : Palette.Ctrl.accentLight(button.isMouseOver()))
                    .setShadeBottomRight(() -> button.isPressed() ? Palette.Ctrl.accentLight(true) : Palette.Ctrl.accentDark(button.isMouseOver()));

            GuiTexture disabledBG = new GuiTexture(button, BCGuiTextures.themedGetter("button_disabled"))
                    .setEnabled(button::isDisabled)
                    .dynamicTexture();

            Constraints.bind(border, button);
            Constraints.bind(disabledBG, button);
            Constraints.bind(background, button, doubleBoarder);
        } else {
            GuiRectangle border = new GuiRectangle(button)
                    .fill(() -> Palette.Ctrl.fill(button.isMouseOver()))
                    .border(() -> Palette.Ctrl.border(button.isMouseOver()));
            Constraints.bind(border, button);
        }

        if (text != null) {
            button.setLabel(new GuiText(button, text)
                    .setTextColour(() -> button.isDisabled() ? 0xa0a0a0 : Palette.Ctrl.textH(button.isMouseOver())));
            Constraints.bind(button.getLabel(), button, 0, 2, 0, 2);
        }
        return button;
    }

    public GuiButton createButton(@NotNull GuiParent<?> parent, @Nullable Supplier<Component> text) {
        return createButton(parent, text, true, 1);
    }


//    public GuiButton createGearButton() {
//        return createGearButton(null);
//    }

//    public GuiButton createGearButton(@NotNull GuiParent<?> parent) {
//        return createThemedIconButton(parent, "gear");
//    }


    //Create Text Field

    public GuiTextField.TextField createTextField(@NotNull GuiElement<?> parent) {
        GuiRectangle bg = new GuiRectangle(parent);
        bg.rectangle(() -> Palette.Ctrl.fill(bg.isMouseOver()), () -> Palette.Ctrl.accentLight(false));

        GuiTextField textField = new GuiTextField(bg)
                .setTextColor(Palette.Ctrl::text)
                .setShadow(false)
                .constrain(TOP, Constraint.relative(bg.get(TOP), 1))
                .constrain(BOTTOM, Constraint.relative(bg.get(BOTTOM), -1))
                .constrain(LEFT, Constraint.relative(bg.get(LEFT), 4))
                .constrain(RIGHT, Constraint.relative(bg.get(RIGHT), -4));

        return new GuiTextField.TextField(bg, textField);
    }

//    //Create Scroll Bars
//    public GuiSlideControl createVanillaScrollBar(GuiSlideControl.SliderRotation rotation, boolean forceEnabled) {
//        GuiSlideControl scrollBar = new GuiSlideControl(rotation);
//        scrollBar.setBackgroundElement(GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_disabled")));
//        scrollBar.setSliderElement(GuiTexture.newDynamicTexture(BCGuiSprites.themedGetter("button_borderless")));
//        if (forceEnabled) {
//            scrollBar.setEnabledCallback(() -> true);
//        }
//        scrollBar.onReload(GuiSlideControl::updateElements);
//        return scrollBar;
//    }
//
//    public GuiSlideControl createVanillaScrollBar(GuiSlideControl.SliderRotation rotation) {
//        return createVanillaScrollBar(rotation, true);
//    }
//
//    public GuiSlideControl createVanillaScrollBar() {
//        return createVanillaScrollBar(GuiSlideControl.SliderRotation.VERTICAL, true);
//    }


//    //LayoutUtils
//    public void center(GuiElement<?> element, GuiElement<?> centerOn, int xOffset, int yOffset) {
//        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2) + xOffset);
//        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2) + yOffset);
//    }
//
//    public void center(GuiElement<?> element, int xPos, int yPos) {
//        element.setXPos(xPos - (element.xSize() / 2));
//        element.setYPos(yPos - (element.ySize() / 2));
//    }
//
//    public void centerX(GuiElement<?> element, GuiElement<?> centerOn, int xOffset) {
//        element.setXPos(centerOn.xPos() + ((centerOn.xSize() - element.xSize()) / 2) + xOffset);
//    }
//
//    public void centerY(GuiElement<?> element, GuiElement<?> centerOn, int yOffset) {
//        element.setYPos(centerOn.yPos() + ((centerOn.ySize() - element.ySize()) / 2) + yOffset);
//    }

    //    public GuiElement<?> createHighlightIcon(GuiElement<?> parent, int xSize, int ySize, int xOversize, int yOversize, Supplier<Material> matSupplier) {
//        GuiElement<?> base = new GuiElement<>(parent).setSize(xSize, ySize);
//        GuiTexture icon = new GuiTexture(matSupplier).setSize(xSize, ySize);
//        addHoverHighlight(base, xOversize, yOversize).setEnabledCallback(() -> base.getHoverTime() > 0);
//        base.addChild(icon);
//        parent.addChild(base);
//        return base;
//    }
//
//    public GuiElement<?> createHighlightIcon(GuiElement<?> parent, int xSize, int ySize, int xOversize, int yOversize, Supplier<Material> matSupplier, Function<GuiElement<?>, Boolean> highlight) {
//        GuiElement<?> base = new GuiElement<>().setSize(xSize, ySize);
//        GuiTexture icon = new GuiTexture(matSupplier).setSize(xSize, ySize);
//        addHoverHighlight(base, xOversize, yOversize).setEnabledCallback(() -> highlight.apply(base));
//        base.addChild(icon);
//        parent.addChild(base);
//        return base;
//    }


    //endregion
    //Create Progress Bar..


//    //Templates
//    public <TEM extends IGuiTemplate> TEM loadTemplate(TEM template) {
//        template.addElements(gui.getManager(), this);
//        return template;
//    }
//
//    /**
//     * Place inside the target element https://ss.brandon3055.com/e89a6
//     */
//    public void placeInside(GuiElement<?> element, GuiElement<?> placeInside, LayoutPos position, int xOffset, int yOffset) {
//        //@formatter:off
//        switch (position) {
//            case TOP_LEFT:      element.setRelPos(placeInside, xOffset, yOffset); break;
//            case TOP_CENTER:    element.setRelPos(placeInside, ((placeInside.xSize() - element.xSize()) / 2) + xOffset, yOffset); break;
//            case TOP_RIGHT:     element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, yOffset); break;
//            case MIDDLE_RIGHT:  element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, ((placeInside.ySize() - element.ySize()) / 2) + yOffset); break;
//            case BOTTOM_RIGHT:  element.setRelPos(placeInside, (placeInside.xSize() - element.xSize()) + xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
//            case BOTTOM_CENTER: element.setRelPos(placeInside, ((placeInside.xSize() - element.xSize()) / 2) + xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
//            case BOTTOM_LEFT:   element.setRelPos(placeInside, xOffset, (placeInside.ySize() - element.ySize()) + yOffset); break;
//            case MIDDLE_LEFT:   element.setRelPos(xOffset, ((placeInside.ySize() - element.ySize()) / 2) + yOffset); break;
//        }
//        //@formatter:on
//    }
//
//    /**
//     * Place outside the target element https://ss.brandon3055.com/baa7c
//     */
//    public void placeOutside(GuiElement<?> element, GuiElement<?> placeOutside, LayoutPos position, int xOffset, int yOffset) {
//        //@formatter:off
//        switch (position) {
//            case TOP_LEFT:      element.setRelPos(placeOutside, -element.xSize() + xOffset, -element.ySize() + yOffset); break;
//            case TOP_CENTER:    element.setRelPos(placeOutside, ((placeOutside.xSize() - element.xSize()) / 2) + xOffset, -element.ySize() + yOffset); break;
//            case TOP_RIGHT:     element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, -element.ySize() + yOffset); break;
//            case MIDDLE_RIGHT:  element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, ((placeOutside.ySize() - element.ySize()) / 2) + yOffset); break;
//            case BOTTOM_RIGHT:  element.setRelPos(placeOutside, placeOutside.xSize() + xOffset, placeOutside.ySize() + yOffset); break;
//            case BOTTOM_CENTER: element.setRelPos(placeOutside, ((placeOutside.xSize() - element.xSize()) / 2) + xOffset, placeOutside.ySize() + yOffset); break;
//            case BOTTOM_LEFT:   element.setRelPos(placeOutside, -element.xSize() + xOffset, placeOutside.ySize() + yOffset); break;
//            case MIDDLE_LEFT:   element.setRelPos(placeOutside, -element.xSize() + xOffset, ((placeOutside.ySize() - element.ySize()) / 2) + yOffset); break;
//        }
//        //@formatter:on
//    }

    public void jeiExclude(GuiElement<?> element) {
//        jeiExclusions.add(element);
    }

//    public int guiLeft() {
//        return gui.guiLeft();
//    }
//
//    public int guiTop() {
//        return gui.guiTop();
//    }

    public Predicate<String> catchyValidator(Predicate<String> predicate) {
        return s -> {
            try {
                return predicate.test(s);
            } catch (Throwable e) {
                return false;
            }
        };
    }

//    public enum LayoutPos {
//        TOP_LEFT,
//        TOP_CENTER,
//        TOP_RIGHT,
//        MIDDLE_RIGHT,
//        MIDDLE_LEFT,
//        BOTTOM_RIGHT,
//        BOTTOM_CENTER,
//        BOTTOM_LEFT
//    }

    public static abstract class Palette {
        /**
         * Background colours. These match the colours used in the default background textures.
         */
        public static class BG {
            public static int fill() {
                return darkMode ? 0xFF3c3c3c : 0xFFc6c6c6;
            }

            public static int border() {
                return darkMode ? 0xFF141414 : 0xFF000000;
            }

            public static int accentLight() {
                return darkMode ? 0xFF5b5b5b : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF282828 : 0xFF555555;
            }

            public static int text() {
                return darkMode ? 0xAFB1B3 : 0x111111;
            }
        }

        /**
         * Slot Colours. These match the colours used in the default background textures.
         */
        public static class Slot {
            public static int fill() {
                return darkMode ? 0xFF6a6a6a : 0xFF8b8b8b;
            }

            public static int border3D(boolean hovering) {
                return darkMode ? 0xFFFFFFFF : 0xFF000000;
            }

            public static int accentLight() {
                return darkMode ? 0xFFc3c3c3 : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF2a2a2a : 0xFF373737;
            }

            public static int text() {
                return darkMode ? 0xdee0e2 : 0x1e2027;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0 : 0 : text();
            }
        }

        /**
         * Things like items/controls in a display list that uses the slot background.
         */
        public static class SubItem {
            public static int fill() {
                return darkMode ? 0xFF5e5f66 : 0xFFbdc6cf;
            }

//            public static int border() {
//                return darkMode ? 0xFF141414 : 0xFF000000;
//            }

            public static int accentLight() {
                return darkMode ? 0xFF77787f : 0xFFffffff;
            }

            public static int accentDark() {
                return darkMode ? 0xFF46474e : 0xFF4a5760;
            }

            public static int text() {
                return darkMode ? 0xdee0e2 : 0x1e2012;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0 : 0 : text();
            }

            public static int border3d() {
                return darkMode ? 0xFFFFFFFF : 0xFF000000;
            }
        }

        /**
         * Buttons and other controls that require a colour pallet.
         */
        public static class Ctrl {
            public static int fill(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF475b6a : 0xFF647baf;
                } else {
                    return darkMode ? 0xFF5b5b5b : 0xFF808080;
                }
            }

            public static int border(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFFa8b0e1 : 0xFF000000;
                } else {
                    return darkMode ? 0xFFd3d3d3 : 0xFF000000;
                }
            }

            public static int border3D(boolean hovering) {
                return 0xFF000000;
            }

            public static int accentLight(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF75a8c2 : 0xFFa8afe1;
                } else {
                    return darkMode ? 0xFFa8a8a8 : 0xFFffffff;
                }
            }

            public static int accentDark(boolean hovering) {
                if (hovering) {
                    return darkMode ? 0xFF21303f : 0xFF515a8a;
                } else {
                    return darkMode ? 0xFF303030 : 0xFF555555;
                }
            }

            public static int text() {
                return darkMode ? 0xe1e3e5 : 0xFFFFFF;
            }

            public static int textH(boolean hovering) {
                return hovering ? darkMode ? 0xffffa0 : 0xffffa0 : text();
            }
        }
    }
}
