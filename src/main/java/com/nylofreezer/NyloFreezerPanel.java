package com.nylofreezer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

final class NyloFreezerPanel extends PluginPanel
{
    private static final Color TOB_RED = new Color(202, 63, 74);
    private static final Color TOB_RED_BRIGHT = new Color(235, 112, 122);
    private static final Color TOB_RED_DARK = new Color(84, 28, 34);
    private static final Color TEXT = new Color(220, 220, 220);
    private static final Color MUTED = new Color(155, 155, 155);

    private static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();
    private static final Font SMALL_BOLD = SMALL_FONT.deriveFont(Font.BOLD);
    private static final Font RESULT_FONT = SMALL_FONT.deriveFont(Font.BOLD, 16f);

    private final JSpinner magicLevelSpinner = new JSpinner(new SpinnerNumberModel(99, 1, 99, 1));
    private final JComboBox<FreezeCalculator.Boost> boostCombo = new JComboBox<>(FreezeCalculator.Boost.values());
    private final JComboBox<FreezeCalculator.Prayer> prayerCombo = new JComboBox<>(FreezeCalculator.Prayer.values());
    private final JCheckBox voidMage = new JCheckBox("Void mage");
    private final JCheckBox iceSceptre = new JCheckBox("Ice sceptre");
    private final JLabel resultValue = new JLabel("+0", SwingConstants.RIGHT);
    private final DefaultTableModel tableModel;
    private final JTable decayTable;

    @Inject
    NyloFreezerPanel()
    {
        super();

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        add(createHeader(), c);
        c.gridy++;
        c.insets = new Insets(8, 0, 0, 0);
        add(createSettingsPanel(), c);
        c.gridy++;
        c.insets = new Insets(8, 0, 0, 0);
        add(createResultPanel(), c);

        tableModel = new DefaultTableModel(new Object[]{"Drain", "Eff.", "Atk"}, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        decayTable = createTable(tableModel);

        c.gridy++;
        c.insets = new Insets(8, 0, 0, 0);
        add(createDecayPanel(), c);
        c.gridy++;
        c.insets = new Insets(8, 0, 0, 0);
        add(createActionRow(), c);

        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), c);

        configureInputs();
        attachListeners();
        resetSelections();
    }

    private JPanel createHeader()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("Nylo Freezer");
        title.setForeground(Color.WHITE);
        title.setFont(SMALL_FONT.deriveFont(Font.BOLD, 15f));

        JLabel subtitle = new JLabel("Maiden");
        subtitle.setForeground(TOB_RED_BRIGHT);
        subtitle.setFont(SMALL_BOLD);

        panel.add(title, BorderLayout.WEST);
        panel.add(subtitle, BorderLayout.EAST);
        return panel;
    }

    private JPanel createSettingsPanel()
    {
        JPanel panel = sectionPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createInputRow("Magic level", magicLevelSpinner));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createInputRow("Boost", boostCombo));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createInputRow("Prayer", prayerCombo));
        panel.add(Box.createVerticalStrut(7));

        JPanel equipment = new JPanel(new GridLayout(1, 2, 4, 0));
        equipment.setOpaque(false);
        equipment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 23));
        equipment.add(voidMage);
        equipment.add(iceSceptre);
        panel.add(equipment);

        return panel;
    }

    private JPanel createInputRow(String labelText, Component input)
    {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel label = new JLabel(labelText);
        label.setFont(SMALL_FONT);
        label.setForeground(TEXT);

        row.add(label, BorderLayout.WEST);
        row.add(input, BorderLayout.EAST);
        return row;
    }

    private JPanel createResultPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(TOB_RED_DARK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TOB_RED, 1),
            new EmptyBorder(6, 8, 6, 8)));

        JLabel label = new JLabel("Required attack  ·  0 drain");
        label.setFont(SMALL_BOLD);
        label.setForeground(Color.WHITE);

        resultValue.setFont(RESULT_FONT);
        resultValue.setForeground(TOB_RED_BRIGHT);

        panel.add(label, BorderLayout.WEST);
        panel.add(resultValue, BorderLayout.EAST);
        return panel;
    }

    private JPanel createDecayPanel()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Boost decay / stat drain");
        label.setFont(SMALL_BOLD);
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tableHolder = new JPanel(new BorderLayout());
        tableHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableHolder.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        tableHolder.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));
        tableHolder.add(decayTable.getTableHeader(), BorderLayout.NORTH);
        tableHolder.add(decayTable, BorderLayout.CENTER);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(tableHolder);
        return panel;
    }

    private JPanel createActionRow()
    {
        JPanel row = new JPanel(new GridLayout(1, 2, 5, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 26));

        JButton reset = compactButton("Reset");
        reset.addActionListener(e -> resetSelections());

        JButton copy = compactButton("Copy");
        copy.addActionListener(e -> copyTable());

        row.add(reset);
        row.add(copy);
        return row;
    }

    private void configureInputs()
    {
        magicLevelSpinner.setFont(SMALL_FONT);
        magicLevelSpinner.setPreferredSize(new Dimension(58, 24));
        magicLevelSpinner.setToolTipText("Automatically synced from your logged-in character; you can still edit it manually.");
        if (magicLevelSpinner.getEditor() instanceof JSpinner.DefaultEditor)
        {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) magicLevelSpinner.getEditor();
            editor.getTextField().setFont(SMALL_FONT);
            editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        }

        boostCombo.setFont(SMALL_FONT);
        boostCombo.setPreferredSize(new Dimension(126, 24));
        boostCombo.setFocusable(false);

        prayerCombo.setFont(SMALL_FONT);
        prayerCombo.setPreferredSize(new Dimension(126, 24));
        prayerCombo.setFocusable(false);

        configureCheckbox(voidMage, "Apply Void mage's 1.45x Magic multiplier");
        configureCheckbox(iceSceptre, "Apply Ice sceptre's 1.10x freeze modifier");
    }

    private static void configureCheckbox(JCheckBox box, String tooltip)
    {
        box.setOpaque(false);
        box.setForeground(TEXT);
        box.setFont(SMALL_FONT);
        box.setFocusPainted(false);
        box.setMargin(new Insets(0, 0, 0, 0));
        box.setToolTipText(tooltip);
    }

    private void attachListeners()
    {
        magicLevelSpinner.addChangeListener(e -> recalculate());
        boostCombo.addActionListener(e -> recalculate());
        prayerCombo.addActionListener(e -> recalculate());
        voidMage.addActionListener(e -> recalculate());
        iceSceptre.addActionListener(e -> recalculate());
    }

    void setMagicLevelFromClient(int level)
    {
        int clamped = Math.max(1, Math.min(99, level));
        if ((int) magicLevelSpinner.getValue() != clamped)
        {
            magicLevelSpinner.setValue(clamped);
        }
    }

    private void resetSelections()
    {
        voidMage.setSelected(false);
        iceSceptre.setSelected(false);
        boostCombo.setSelectedItem(FreezeCalculator.Boost.NONE);
        prayerCombo.setSelectedItem(FreezeCalculator.Prayer.NONE);
        recalculate();
    }

    private void recalculate()
    {
        if (tableModel == null)
        {
            return;
        }

        int magicLevel = (int) magicLevelSpinner.getValue();
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

        FreezeCalculator.Result baseline = FreezeCalculator.calculate(
            magicLevel,
            boost,
            prayer,
            voidMage.isSelected(),
            iceSceptre.isSelected(),
            0);

        resultValue.setText(formatAttack(baseline.getRequiredAttack()));

        tableModel.setRowCount(0);
        for (int drain = 1; drain <= 10; drain++)
        {
            FreezeCalculator.Result result = FreezeCalculator.calculate(
                magicLevel,
                boost,
                prayer,
                voidMage.isSelected(),
                iceSceptre.isSelected(),
                drain);

            tableModel.addRow(new Object[]{
                result.getDrain(),
                result.getEffectiveMagic(),
                formatAttack(result.getRequiredAttack())
            });
        }
    }

    private void copyTable()
    {
        int magicLevel = (int) magicLevelSpinner.getValue();
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

        List<String> lines = new ArrayList<>();
        lines.add("Nylo Freezer — Magic " + magicLevel);
        lines.add(boost.getLabel() + " · " + prayer.getLabel()
            + " · Void " + (voidMage.isSelected() ? "On" : "Off")
            + " · Ice Sceptre " + (iceSceptre.isSelected() ? "On" : "Off"));

        for (int drain = 0; drain <= 10; drain++)
        {
            FreezeCalculator.Result result = FreezeCalculator.calculate(
                magicLevel,
                boost,
                prayer,
                voidMage.isSelected(),
                iceSceptre.isSelected(),
                drain);

            lines.add("Drain " + drain + ": " + formatAttack(result.getRequiredAttack())
                + " Magic attack (effective Magic " + result.getEffectiveMagic() + ")");
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
            new StringSelection(String.join(System.lineSeparator(), lines)), null);
    }

    private static JTable createTable(DefaultTableModel model)
    {
        JTable table = new JTable(model);
        table.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        table.setForeground(TEXT);
        table.setSelectionBackground(ColorScheme.DARK_GRAY_COLOR);
        table.setSelectionForeground(TEXT);
        table.setGridColor(ColorScheme.DARK_GRAY_COLOR);
        table.setFont(SMALL_FONT);
        table.setRowHeight(21);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(ColorScheme.DARK_GRAY_COLOR);
        header.setForeground(MUTED);
        header.setFont(SMALL_BOLD);
        header.setPreferredSize(new Dimension(0, 22));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer normal = new DefaultTableCellRenderer();
        normal.setHorizontalAlignment(SwingConstants.CENTER);
        normal.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        normal.setForeground(TEXT);
        normal.setFont(SMALL_FONT);
        normal.setBorder(new EmptyBorder(0, 2, 0, 2));

        DefaultTableCellRenderer attack = new DefaultTableCellRenderer();
        attack.setHorizontalAlignment(SwingConstants.CENTER);
        attack.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        attack.setForeground(TOB_RED_BRIGHT);
        attack.setFont(SMALL_BOLD);
        attack.setBorder(new EmptyBorder(0, 2, 0, 2));

        table.getColumnModel().getColumn(0).setCellRenderer(normal);
        table.getColumnModel().getColumn(1).setCellRenderer(normal);
        table.getColumnModel().getColumn(2).setCellRenderer(attack);
        table.getColumnModel().getColumn(0).setPreferredWidth(52);
        table.getColumnModel().getColumn(1).setPreferredWidth(62);
        table.getColumnModel().getColumn(2).setPreferredWidth(62);

        return table;
    }

    private static JPanel sectionPanel()
    {
        JPanel panel = new JPanel();
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setBorder(new EmptyBorder(7, 7, 7, 7));
        return panel;
    }

    private static JButton compactButton(String text)
    {
        JButton button = new JButton(text);
        button.setFont(SMALL_FONT);
        button.setForeground(TEXT);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setFocusPainted(false);
        button.setMargin(new Insets(2, 4, 2, 4));
        return button;
    }

    private static String formatAttack(int value)
    {
        return value > 0 ? "+" + value : Integer.toString(value);
    }
}
