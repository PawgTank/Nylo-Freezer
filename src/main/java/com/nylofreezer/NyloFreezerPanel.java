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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.AsyncBufferedImage;

@Singleton
class NyloFreezerPanel extends PluginPanel
{
    private static final int VOID_MAGE_HELM_ID = 11663;
    private static final int ICE_ANCIENT_SCEPTRE_ID = 28262;

    private static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();
    private static final Font NORMAL_FONT = FontManager.getRunescapeFont();

    private final JTextField magicLevelField;
    private final JComboBox<FreezeCalculator.Boost> boostCombo =
        new JComboBox<>(FreezeCalculator.Boost.values());
    private final JComboBox<FreezeCalculator.Prayer> prayerCombo =
        new JComboBox<>(FreezeCalculator.Prayer.values());
    private final ItemToggle voidMage;
    private final ItemToggle iceSceptre;
    private final JLabel resultValue = new JLabel("+0", SwingConstants.CENTER);

    private int syncedMagicLevel = 99;

    @Inject
    NyloFreezerPanel(ItemManager itemManager)
    {
        super();

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Nylo Freezer");
        title.setForeground(Color.WHITE);
        title.setFont(NORMAL_FONT.deriveFont(Font.BOLD));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(10));

        FlatTextField magicInput = new FlatTextField();
        magicInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        magicInput.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        magicInput.setBorder(new EmptyBorder(5, 7, 5, 7));
        magicInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        magicLevelField = magicInput.getTextField();
        magicLevelField.setText("99");
        magicLevelField.setFont(SMALL_FONT);
        magicLevelField.setForeground(Color.WHITE);
        magicLevelField.setHorizontalAlignment(JTextField.LEFT);
        magicLevelField.setToolTipText("Automatically synced to your base Magic level. You can edit it for theorycrafting.");

        add(createLabeledField("Magic Level", magicInput));
        add(Box.createVerticalStrut(7));

        configureCombo(boostCombo);
        configureCombo(prayerCombo);
        add(createLabeledField("Boost", boostCombo));
        add(Box.createVerticalStrut(7));
        add(createLabeledField("Prayer", prayerCombo));
        add(Box.createVerticalStrut(11));

        JLabel equipmentLabel = new JLabel("Equipment");
        equipmentLabel.setFont(SMALL_FONT);
        equipmentLabel.setForeground(Color.WHITE);
        equipmentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(equipmentLabel);
        add(Box.createVerticalStrut(4));

        voidMage = new ItemToggle("Void mage", "Void mage helm", this::recalculate);
        iceSceptre = new ItemToggle("Ice sceptre", "Ice ancient sceptre", this::recalculate);

        AsyncBufferedImage voidImage = itemManager.getImage(VOID_MAGE_HELM_ID);
        voidImage.addTo(voidMage);
        AsyncBufferedImage sceptreImage = itemManager.getImage(ICE_ANCIENT_SCEPTRE_ID);
        sceptreImage.addTo(iceSceptre);

        JPanel equipment = new JPanel(new GridLayout(1, 2, 7, 0));
        equipment.setOpaque(false);
        equipment.setAlignmentX(Component.LEFT_ALIGNMENT);
        equipment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        equipment.add(voidMage);
        equipment.add(iceSceptre);
        add(equipment);
        add(Box.createVerticalStrut(11));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        resultPanel.setBorder(new EmptyBorder(7, 8, 7, 8));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel resultLabel = new JLabel("Required Magic attack");
        resultLabel.setFont(SMALL_FONT);
        resultLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        resultValue.setFont(NORMAL_FONT.deriveFont(Font.BOLD, 18f));
        resultValue.setForeground(Color.WHITE);

        resultPanel.add(resultLabel, BorderLayout.WEST);
        resultPanel.add(resultValue, BorderLayout.EAST);
        add(resultPanel);
        add(Box.createVerticalStrut(8));

        JButton reset = new JButton("Reset");
        reset.setFont(SMALL_FONT);
        reset.setForeground(Color.WHITE);
        reset.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        reset.setFocusPainted(false);
        reset.setAlignmentX(Component.LEFT_ALIGNMENT);
        reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        reset.addActionListener(e -> resetSelections());
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
        boostCombo.addActionListener(e -> recalculate());
        prayerCombo.addActionListener(e -> recalculate());

        resetSelections();
    }

    private static JPanel createLabeledField(String labelText, Component input)
    {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));

        JLabel label = new JLabel(labelText);
        label.setFont(SMALL_FONT);
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));

        container.add(label, BorderLayout.NORTH);
        container.add(input, BorderLayout.CENTER);
        return container;
    }

    private static void configureCombo(JComboBox<?> combo)
    {
        combo.setFont(SMALL_FONT);
        combo.setForeground(Color.WHITE);
        combo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        combo.setFocusable(false);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    void setMagicLevelFromClient(int level)
    {
        syncedMagicLevel = clampMagic(level);
        magicLevelField.setText(Integer.toString(syncedMagicLevel));
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
        return Math.max(1, Math.min(99, level));
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
        boostCombo.setSelectedItem(FreezeCalculator.Boost.NONE);
        prayerCombo.setSelectedItem(FreezeCalculator.Prayer.NONE);
        voidMage.setSelected(false);
        iceSceptre.setSelected(false);
        recalculate();
    }

    private void recalculate()
    {
        FreezeCalculator.Boost boost = (FreezeCalculator.Boost) boostCombo.getSelectedItem();
        FreezeCalculator.Prayer prayer = (FreezeCalculator.Prayer) prayerCombo.getSelectedItem();

        if (boost == null)
        {
            boost = FreezeCalculator.Boost.NONE;
        }
        if (prayer == null)
        {
            prayer = FreezeCalculator.Prayer.NONE;
        }

        int requiredAttack = FreezeCalculator.calculateRequiredAttack(
            getMagicLevel(),
            boost,
            prayer,
            voidMage.isSelected(),
            iceSceptre.isSelected());

        resultValue.setText(requiredAttack >= 0 ? "+" + requiredAttack : Integer.toString(requiredAttack));
    }

    /**
     * Independent toggle using the same visual rules as RuneLite's MaterialTab:
     * darker gray cell, darker-gray hover, and a 1px BRAND_ORANGE underline when selected.
     */
    private static final class ItemToggle extends JLabel
    {
        private static final Border SELECTED_BORDER = new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.BRAND_ORANGE),
            BorderFactory.createEmptyBorder(5, 10, 4, 10));
        private static final Border UNSELECTED_BORDER =
            BorderFactory.createEmptyBorder(5, 10, 5, 10);

        private boolean selected;
        private final Runnable onChange;

        ItemToggle(String accessibleName, String tooltip, Runnable onChange)
        {
            this.onChange = onChange;
            setName(accessibleName);
            setToolTipText(tooltip);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            setBorder(UNSELECTED_BORDER);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    setSelected(!selected);
                    ItemToggle.this.onChange.run();
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
            setBorder(selected ? SELECTED_BORDER : UNSELECTED_BORDER);
            repaint();
        }
    }
}
