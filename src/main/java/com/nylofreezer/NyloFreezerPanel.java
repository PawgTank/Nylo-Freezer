package com.nylofreezer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Singleton
class NyloFreezerPanel extends PluginPanel
{
    private static final int MIN_MAGIC_LEVEL = 82;
    private static final int MAX_MAGIC_LEVEL = 99;

    private static final int VOID_MAGE_HELM_ID = 11663;
    private static final int ICE_ANCIENT_SCEPTRE_ID = 28262;

    private static final int SATURATED_HEART_ID = 27641;
    private static final int IMBUED_HEART_ID = 20724;
    private static final int FORGOTTEN_BREW_4_ID = 27629;
    private static final int ANCIENT_BREW_4_ID = 26340;
    private static final int MAGIC_POTION_4_ID = 3040;

    private static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();
    private static final Font NORMAL_FONT = FontManager.getRunescapeFont();

    private final JTextField magicLevelField;
    private final EquipmentToggle voidMage;
    private final EquipmentToggle iceSceptre;

    private final Map<FreezeCalculator.Boost, ChoiceTile> boostTiles =
        new EnumMap<>(FreezeCalculator.Boost.class);
    private final Map<FreezeCalculator.Prayer, ChoiceTile> prayerTiles =
        new EnumMap<>(FreezeCalculator.Prayer.class);

    private final JLabel targetValue = new JLabel("+0", SwingConstants.RIGHT);
    private final JLabel currentValue = new JLabel("—", SwingConstants.RIGHT);
    private final JLabel additionalValue = new JLabel("—", SwingConstants.RIGHT);

    private FreezeCalculator.Boost selectedBoost = FreezeCalculator.Boost.NONE;
    private FreezeCalculator.Prayer selectedPrayer = FreezeCalculator.Prayer.NONE;
    private Integer currentMagicAttackBonus;
    private int syncedMagicLevel = MAX_MAGIC_LEVEL;

    @Inject
    NyloFreezerPanel(ItemManager itemManager, SpriteManager spriteManager)
    {
        // This panel is compact enough that it does not need PluginPanel's default JScrollPane.
        // Using the unwrapped form also avoids the scroll pane outline around the whole sidebar.
        super(false);

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Nylo Freezer");
        title.setForeground(Color.WHITE);
        title.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(10));

        FlatTextField magicTextInput = new FlatTextField();
        magicTextInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        magicTextInput.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);

        magicLevelField = magicTextInput.getTextField();
        magicLevelField.setText(Integer.toString(MAX_MAGIC_LEVEL));
        magicLevelField.setFont(NORMAL_FONT);
        magicLevelField.setForeground(Color.WHITE);
        magicLevelField.setCaretColor(Color.WHITE);
        magicLevelField.setSelectionColor(ColorScheme.BRAND_ORANGE);
        magicLevelField.setSelectedTextColor(Color.WHITE);
        magicLevelField.setHorizontalAlignment(JTextField.LEFT);
        magicLevelField.setToolTipText(
            "Automatically synced to your base Magic level. Minimum for ice barrage is 82.");

        JPanel magicInput = new JPanel(new BorderLayout());
        magicInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        magicInput.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        magicInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        magicInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        magicInput.add(magicTextInput, BorderLayout.CENTER);

        JPanel arrows = new JPanel(new GridLayout(2, 1, 0, 0));
        arrows.setOpaque(false);
        arrows.setPreferredSize(new Dimension(24, 30));
        ArrowButton up = new ArrowButton(true, () -> adjustMagicLevel(1));
        ArrowButton down = new ArrowButton(false, () -> adjustMagicLevel(-1));
        up.setToolTipText("Increase Magic level");
        down.setToolTipText("Decrease Magic level");
        arrows.add(up);
        arrows.add(down);
        magicInput.add(arrows, BorderLayout.EAST);
        magicInput.addMouseWheelListener(e -> adjustMagicLevel(e.getWheelRotation() < 0 ? 1 : -1));

        add(createLabeledField("Magic Level", magicInput, 51));
        add(Box.createVerticalStrut(9));

        add(createSectionLabel("Equipment"));
        add(Box.createVerticalStrut(4));

        voidMage = new EquipmentToggle("Void Mage Set", this::recalculate);
        iceSceptre = new EquipmentToggle("Ice Ancient Sceptre", this::recalculate);
        itemManager.getImage(VOID_MAGE_HELM_ID).addTo(voidMage);
        itemManager.getImage(ICE_ANCIENT_SCEPTRE_ID).addTo(iceSceptre);

        JPanel equipmentPanel = createTileGrid(1, 2, 7, 0, 44);
        equipmentPanel.add(voidMage);
        equipmentPanel.add(iceSceptre);
        add(equipmentPanel);
        add(Box.createVerticalStrut(9));

        add(createSectionLabel("Boost"));
        add(Box.createVerticalStrut(4));

        JPanel boostPanel = createTileGrid(2, 3, 5, 5, 85);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.SATURATED_HEART,
            "Saturated Heart", SATURATED_HEART_ID, null);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.IMBUED_HEART,
            "Imbued Heart", IMBUED_HEART_ID, null);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.FORGOTTEN_BREW,
            "Forgotten Brew", FORGOTTEN_BREW_4_ID, null);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.ANCIENT_BREW,
            "Ancient Brew", ANCIENT_BREW_4_ID, null);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.MAGIC_POTION,
            "Magic Potion", MAGIC_POTION_4_ID, null);
        addBoostTile(itemManager, boostPanel, FreezeCalculator.Boost.NONE,
            "No Boost", -1, "None");
        add(boostPanel);
        add(Box.createVerticalStrut(9));

        add(createSectionLabel("Prayer"));
        add(Box.createVerticalStrut(4));

        JPanel prayerPanel = createTileGrid(1, 4, 4, 0, 38);
        addPrayerTile(spriteManager, prayerPanel, FreezeCalculator.Prayer.AUGURY,
            "Augury", SpriteID.Prayeron.AUGURY);
        addPrayerTile(spriteManager, prayerPanel, FreezeCalculator.Prayer.MYSTIC_VIGOUR,
            "Mystic Vigour", SpriteID.Prayeron.MYSTIC_VIGOUR);
        addPrayerTile(spriteManager, prayerPanel, FreezeCalculator.Prayer.MYSTIC_MIGHT,
            "Mystic Might", SpriteID.Prayeron.MYSTIC_MIGHT);
        addPrayerTile(spriteManager, prayerPanel, FreezeCalculator.Prayer.NONE,
            "No Prayer", -1);
        add(prayerPanel);
        add(Box.createVerticalStrut(10));

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        resultPanel.setBorder(new EmptyBorder(6, 8, 6, 8));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 69));

        configureResultValue(targetValue);
        configureResultValue(currentValue);
        configureResultValue(additionalValue);

        resultPanel.add(createResultRow("Target Magic attack bonus", targetValue));
        resultPanel.add(createResultRow("Current Magic attack bonus", currentValue));
        resultPanel.add(createResultRow("Additional required bonus", additionalValue));

        add(resultPanel);
        add(Box.createVerticalStrut(8));

        JButton reset = new JButton("Reset");
        SwingUtil.removeButtonDecorations(reset);
        reset.setUI(new BasicButtonUI());
        reset.setFont(SMALL_FONT);
        reset.setForeground(Color.WHITE);
        reset.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        reset.setFocusPainted(false);
        reset.setAlignmentX(Component.LEFT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        reset.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        reset.addActionListener(e -> resetSelections());
        reset.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                reset.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                reset.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        });
        add(reset);

        add(Box.createVerticalGlue());

        magicLevelField.addActionListener(e -> commitMagicLevel());
        magicLevelField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                commitMagicLevel();
            }
        });

        resetSelections();
    }

    private static JPanel createLabeledField(String labelText, Component input, int height)
    {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        JLabel label = createSectionLabel(labelText);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));

        container.add(label, BorderLayout.NORTH);
        container.add(input, BorderLayout.CENTER);
        return container;
    }

    private static JLabel createSectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_FONT);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel createResultLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_FONT);
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return label;
    }

    private static void configureResultValue(JLabel value)
    {
        value.setFont(SMALL_FONT.deriveFont(Font.BOLD));
        value.setForeground(Color.WHITE);
    }

    private static JPanel createResultRow(String text, JLabel value)
    {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
        row.add(createResultLabel(text), BorderLayout.CENTER);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private static JPanel createTileGrid(int rows, int columns, int horizontalGap, int verticalGap, int height)
    {
        JPanel panel = new JPanel(new GridLayout(rows, columns, horizontalGap, verticalGap));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return panel;
    }

    private void addBoostTile(
        ItemManager itemManager,
        JPanel panel,
        FreezeCalculator.Boost boost,
        String tooltip,
        int itemId,
        String text)
    {
        ChoiceTile tile = new ChoiceTile(tooltip, () -> selectBoost(boost));
        tile.setName(tooltip);
        if (text != null)
        {
            tile.setText(text);
        }
        if (itemId > 0)
        {
            AsyncBufferedImage image = itemManager.getImage(itemId);
            image.addTo(tile);
        }
        boostTiles.put(boost, tile);
        panel.add(tile);
    }

    private void addPrayerTile(
        SpriteManager spriteManager,
        JPanel panel,
        FreezeCalculator.Prayer prayer,
        String tooltip,
        int spriteId)
    {
        ChoiceTile tile = new ChoiceTile(tooltip, () -> selectPrayer(prayer));
        tile.setName(tooltip);

        if (spriteId > 0)
        {
            spriteManager.getSpriteAsync(spriteId, 0, sprite ->
            {
                if (sprite == null)
                {
                    return;
                }

                SwingUtilities.invokeLater(() ->
                {
                    // Keep all three prayer icons visually consistent with RuneLite's own
                    // compact sprite presentation.
                    BufferedImage icon = ImageUtil.resizeImage(
                        ImageUtil.resizeCanvas(sprite, 30, 30), 26, 26);
                    tile.setIcon(new ImageIcon(icon));
                    tile.setText(null);
                });
            });
        }
        else
        {
            tile.setText("None");
        }

        prayerTiles.put(prayer, tile);
        panel.add(tile);
    }

    private void selectBoost(FreezeCalculator.Boost boost)
    {
        selectedBoost = boost;
        for (Map.Entry<FreezeCalculator.Boost, ChoiceTile> entry : boostTiles.entrySet())
        {
            entry.getValue().setSelected(entry.getKey() == boost);
        }
        recalculate();
    }

    private void selectPrayer(FreezeCalculator.Prayer prayer)
    {
        selectedPrayer = prayer;
        for (Map.Entry<FreezeCalculator.Prayer, ChoiceTile> entry : prayerTiles.entrySet())
        {
            entry.getValue().setSelected(entry.getKey() == prayer);
        }
        recalculate();
    }

    void setMagicLevelFromClient(int level)
    {
        syncedMagicLevel = clampMagic(level);
        magicLevelField.setText(Integer.toString(syncedMagicLevel));
        recalculate();
    }

    void setCurrentMagicAttackBonus(Integer bonus)
    {
        currentMagicAttackBonus = bonus;
        recalculate();
    }

    private void adjustMagicLevel(int delta)
    {
        int level = clampMagic(getMagicLevel() + delta);
        magicLevelField.setText(Integer.toString(level));
        recalculate();
    }

    private void commitMagicLevel()
    {
        int level;
        try
        {
            level = Integer.parseInt(magicLevelField.getText().trim());
        }
        catch (NumberFormatException ex)
        {
            level = syncedMagicLevel;
        }

        level = clampMagic(level);
        magicLevelField.setText(Integer.toString(level));
        recalculate();
    }

    private static int clampMagic(int level)
    {
        return Math.max(MIN_MAGIC_LEVEL, Math.min(MAX_MAGIC_LEVEL, level));
    }

    private int getMagicLevel()
    {
        try
        {
            return clampMagic(Integer.parseInt(magicLevelField.getText().trim()));
        }
        catch (NumberFormatException ex)
        {
            return syncedMagicLevel;
        }
    }

    private void resetSelections()
    {
        magicLevelField.setText(Integer.toString(syncedMagicLevel));
        selectBoost(FreezeCalculator.Boost.NONE);
        selectPrayer(FreezeCalculator.Prayer.NONE);
        voidMage.setSelected(false);
        iceSceptre.setSelected(false);
        recalculate();
    }

    private void recalculate()
    {
        int targetAttack = FreezeCalculator.calculateRequiredAttack(
            getMagicLevel(),
            selectedBoost,
            selectedPrayer,
            voidMage.isSelected(),
            iceSceptre.isSelected());

        targetValue.setText(formatBonus(targetAttack));

        if (currentMagicAttackBonus == null)
        {
            currentValue.setText("—");
            additionalValue.setText("—");
            additionalValue.setToolTipText("Log in to compare against your equipped Magic attack bonus.");
            return;
        }

        currentValue.setText(formatBonus(currentMagicAttackBonus));
        int additionalRequired = Math.max(0, targetAttack - currentMagicAttackBonus);
        additionalValue.setText(formatBonus(additionalRequired));

        if (additionalRequired == 0)
        {
            int overTarget = currentMagicAttackBonus - targetAttack;
            additionalValue.setToolTipText(overTarget > 0
                ? "Target met. Current equipment is " + overTarget + " above the target."
                : "Target met exactly.");
        }
        else
        {
            additionalValue.setToolTipText(
                "You need " + additionalRequired + " more Magic attack bonus to reach the target.");
        }
    }

    private static String formatBonus(int bonus)
    {
        return bonus >= 0 ? "+" + bonus : Integer.toString(bonus);
    }

    /** Compact RuneLite-styled up/down control for the Magic level field. */
    private static final class ArrowButton extends JButton
    {
        private final boolean up;
        private boolean hovered;

        ArrowButton(boolean up, Runnable onClick)
        {
            this.up = up;
            setUI(new BasicButtonUI());
            SwingUtil.removeButtonDecorations(this);
            setOpaque(true);
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            setBorder(BorderFactory.createMatteBorder(0, 1, up ? 1 : 0, 0, ColorScheme.DARK_GRAY_COLOR));
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> onClick.run());
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics)
        {
            Graphics2D g = (Graphics2D) graphics.create();
            try
            {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(hovered ? ColorScheme.DARK_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                Polygon triangle = up
                    ? new Polygon(new int[]{cx - 3, cx + 3, cx}, new int[]{cy + 2, cy + 2, cy - 2}, 3)
                    : new Polygon(new int[]{cx - 3, cx + 3, cx}, new int[]{cy - 2, cy - 2, cy + 2}, 3);
                g.setColor(ColorScheme.LIGHT_GRAY_COLOR);
                g.fillPolygon(triangle);
            }
            finally
            {
                g.dispose();
            }
        }
    }

    /**
     * Independent equipment toggle using the same basic visual language as RuneLite's
     * MaterialTab: darker-gray cell, hover background, and an orange underline when selected.
     */
    private static final class EquipmentToggle extends JLabel
    {
        private boolean selected;
        private final Runnable onChange;

        EquipmentToggle(String tooltip, Runnable onChange)
        {
            this.onChange = onChange;
            configureTile(this, tooltip);

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    setSelected(!selected);
                    EquipmentToggle.this.onChange.run();
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    setBackground(ColorScheme.DARKER_GRAY_COLOR);
                }
            });
        }

        boolean isSelected()
        {
            return selected;
        }

        void setSelected(boolean selected)
        {
            this.selected = selected;
            setBorder(selected ? TileBorders.SELECTED : TileBorders.UNSELECTED);
            repaint();
        }
    }

    /** Mutually-exclusive selection tile used by boost and prayer groups. */
    private static final class ChoiceTile extends JLabel
    {
        private final Runnable onSelect;

        ChoiceTile(String tooltip, Runnable onSelect)
        {
            this.onSelect = onSelect;
            configureTile(this, tooltip);
            setFont(SMALL_FONT);
            setForeground(Color.WHITE);

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    ChoiceTile.this.onSelect.run();
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    setBackground(ColorScheme.DARKER_GRAY_COLOR);
                }
            });
        }

        void setSelected(boolean selected)
        {
            setBorder(selected ? TileBorders.SELECTED : TileBorders.UNSELECTED);
            repaint();
        }
    }

    private static void configureTile(JLabel tile, String tooltip)
    {
        tile.setToolTipText(tooltip);
        tile.setOpaque(true);
        tile.setHorizontalAlignment(SwingConstants.CENTER);
        tile.setVerticalAlignment(SwingConstants.CENTER);
        tile.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tile.setBorder(TileBorders.UNSELECTED);
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class TileBorders
    {
        private static final Border SELECTED = new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE),
            BorderFactory.createEmptyBorder(4, 4, 3, 4));
        private static final Border UNSELECTED =
            BorderFactory.createEmptyBorder(4, 4, 4, 4);

        private TileBorders()
        {
        }
    }
}
