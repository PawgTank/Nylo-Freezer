package com.nylofreezer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import net.runelite.client.ui.PluginPanel;

final class NyloFreezerPanel extends PluginPanel
{
    private static final Color BG = new Color(18, 9, 12);
    private static final Color PANEL = new Color(29, 15, 19);
    private static final Color PANEL_ALT = new Color(38, 18, 24);
    private static final Color BORDER = new Color(88, 29, 41);
    private static final Color BORDER_ACTIVE = new Color(194, 52, 73);
    private static final Color RED = new Color(221, 75, 96);
    private static final Color GOLD = new Color(221, 190, 143);
    private static final Color TEXT = new Color(244, 238, 239);
    private static final Color MUTED = new Color(190, 167, 172);
    private static final Color SUCCESS = new Color(161, 211, 168);

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final Font SECTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font RESULT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 30);

    private final JSpinner magicLevelSpinner = new JSpinner(new SpinnerNumberModel(99, 82, 99, 1));
    private final JCheckBox voidMage = createCheckBox("Void Mage  ×1.45");
    private final JCheckBox iceSceptre = createCheckBox("Ice Sceptre  ×1.10");
    private final JLabel resultValue = new JLabel("0", SwingConstants.RIGHT);
    private final DefaultTableModel tableModel;
    private final JTable decayTable;

    private final ButtonGroup boostGroup = new ButtonGroup();
    private final ButtonGroup prayerGroup = new ButtonGroup();
    private final Map<FreezeCalculator.Boost, JToggleButton> boostButtons =
        new EnumMap<>(FreezeCalculator.Boost.class);
    private final Map<FreezeCalculator.Prayer, JToggleButton> prayerButtons =
        new EnumMap<>(FreezeCalculator.Prayer.class);

    NyloFreezerPanel()
    {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 12, 10));

        add(createHeader());
        add(Box.createVerticalStrut(10));
        add(createSetupPanel());
        add(Box.createVerticalStrut(10));
        add(createResultPanel());
        add(Box.createVerticalStrut(10));

        tableModel = new DefaultTableModel(new Object[]{"Drain", "Eff. Magic", "Magic atk"}, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        decayTable = createTable(tableModel);
        add(createTablePanel());
        add(Box.createVerticalStrut(10));
        add(createActionRow());
        add(Box.createVerticalGlue());

        attachListeners();
        reset();
    }

    private JPanel createHeader()
    {
        JPanel panel = transparentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("NYLO FREEZER");
        title.setForeground(TEXT);
        title.setFont(TITLE_FONT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Maiden freeze calculator");
        subtitle.setForeground(MUTED);
        subtitle.setFont(BODY_FONT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createSetupPanel()
    {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(sectionLabel("Magic level"));
        card.add(Box.createVerticalStrut(6));

        JPanel levelRow = transparentPanel();
        levelRow.setLayout(new BorderLayout(8, 0));
        levelRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel levelHint = new JLabel("Base level");
        levelHint.setForeground(MUTED);
        levelHint.setFont(BODY_FONT);

        magicLevelSpinner.setFont(BODY_BOLD);
        magicLevelSpinner.setPreferredSize(new Dimension(72, 32));
        ((JSpinner.DefaultEditor) magicLevelSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        ((JSpinner.DefaultEditor) magicLevelSpinner.getEditor()).getTextField().setFont(BODY_BOLD);

        levelRow.add(levelHint, BorderLayout.WEST);
        levelRow.add(magicLevelSpinner, BorderLayout.EAST);
        card.add(levelRow);

        card.add(sectionSpacer());
        card.add(sectionLabel("Equipment"));
        card.add(Box.createVerticalStrut(6));

        JPanel equipment = transparentPanel();
        equipment.setLayout(new GridLayout(1, 2, 6, 0));
        equipment.add(voidMage);
        equipment.add(iceSceptre);
        equipment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(equipment);

        card.add(sectionSpacer());
        card.add(sectionLabel("Boost"));
        card.add(Box.createVerticalStrut(6));
        card.add(createBoostGrid());

        card.add(sectionSpacer());
        card.add(sectionLabel("Prayer"));
        card.add(Box.createVerticalStrut(6));
        card.add(createPrayerGrid());

        return card;
    }

    private JPanel createBoostGrid()
    {
        JPanel grid = transparentPanel();
        grid.setLayout(new GridLayout(3, 2, 6, 6));

        addBoostButton(grid, FreezeCalculator.Boost.SATURATED_HEART);
        addBoostButton(grid, FreezeCalculator.Boost.IMBUED_HEART);
        addBoostButton(grid, FreezeCalculator.Boost.FORGOTTEN_BREW);
        addBoostButton(grid, FreezeCalculator.Boost.ANCIENT_BREW);
        addBoostButton(grid, FreezeCalculator.Boost.MAGIC_POTION);
        addBoostButton(grid, FreezeCalculator.Boost.NONE);

        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        return grid;
    }

    private JPanel createPrayerGrid()
    {
        JPanel grid = transparentPanel();
        grid.setLayout(new GridLayout(2, 2, 6, 6));

        addPrayerButton(grid, FreezeCalculator.Prayer.AUGURY);
        addPrayerButton(grid, FreezeCalculator.Prayer.MYSTIC_VIGOUR);
        addPrayerButton(grid, FreezeCalculator.Prayer.MYSTIC_MIGHT);
        addPrayerButton(grid, FreezeCalculator.Prayer.NONE);

        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        return grid;
    }

    private void addBoostButton(JPanel parent, FreezeCalculator.Boost boost)
    {
        JToggleButton button = createChoiceButton(boost.getLabel(), boost.getFormula());
        boostButtons.put(boost, button);
        boostGroup.add(button);
        parent.add(button);
    }

    private void addPrayerButton(JPanel parent, FreezeCalculator.Prayer prayer)
    {
        JToggleButton button = createChoiceButton(prayer.getLabel(), prayer.getFormula());
        prayerButtons.put(prayer, button);
        prayerGroup.add(button);
        parent.add(button);
    }

    private JPanel createResultPanel()
    {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_ACTIVE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JPanel copy = transparentPanel();
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Required Magic attack");
        label.setForeground(TEXT);
        label.setFont(BODY_BOLD);
        JLabel sub = new JLabel("0 decay / stat drain");
        sub.setForeground(MUTED);
        sub.setFont(SMALL_FONT);

        copy.add(label);
        copy.add(Box.createVerticalStrut(2));
        copy.add(sub);

        resultValue.setForeground(GOLD);
        resultValue.setFont(RESULT_FONT);
        resultValue.setPreferredSize(new Dimension(74, 38));

        card.add(copy, BorderLayout.CENTER);
        card.add(resultValue, BorderLayout.EAST);
        return card;
    }

    private JPanel createTablePanel()
    {
        JPanel panel = createCard();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel titleRow = transparentPanel();
        titleRow.setLayout(new BorderLayout());
        JLabel title = sectionLabel("Boost decay / stat drain");
        JLabel range = new JLabel("1–10");
        range.setForeground(RED);
        range.setFont(BODY_BOLD);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(range, BorderLayout.EAST);

        panel.add(titleRow);
        panel.add(Box.createVerticalStrut(7));
        panel.add(decayTable.getTableHeader());
        panel.add(decayTable);
        return panel;
    }

    private JPanel createActionRow()
    {
        JPanel row = transparentPanel();
        row.setLayout(new GridLayout(1, 2, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton reset = createActionButton("Reset", false);
        reset.addActionListener(e -> reset());

        JButton copy = createActionButton("Copy table", true);
        copy.addActionListener(e -> copyTable());

        row.add(reset);
        row.add(copy);
        return row;
    }

    private void attachListeners()
    {
        magicLevelSpinner.addChangeListener(e -> recalculate());
        voidMage.addActionListener(e -> recalculate());
        iceSceptre.addActionListener(e -> recalculate());

        boostButtons.values().forEach(button -> button.addActionListener(e -> {
            refreshChoiceStyles(boostButtons.values());
            recalculate();
        }));

        prayerButtons.values().forEach(button -> button.addActionListener(e -> {
            refreshChoiceStyles(prayerButtons.values());
            recalculate();
        }));
    }

    private void reset()
    {
        magicLevelSpinner.setValue(99);
        voidMage.setSelected(false);
        iceSceptre.setSelected(false);
        boostButtons.get(FreezeCalculator.Boost.NONE).setSelected(true);
        prayerButtons.get(FreezeCalculator.Prayer.NONE).setSelected(true);
        refreshChoiceStyles(boostButtons.values());
        refreshChoiceStyles(prayerButtons.values());
        recalculate();
    }

    private void recalculate()
    {
        if (tableModel == null)
        {
            return;
        }

        int magicLevel = (int) magicLevelSpinner.getValue();
        FreezeCalculator.Boost boost = selectedBoost();
        FreezeCalculator.Prayer prayer = selectedPrayer();

        FreezeCalculator.Result baseline = FreezeCalculator.calculate(
            magicLevel,
            boost,
            prayer,
            voidMage.isSelected(),
            iceSceptre.isSelected(),
            0);

        resultValue.setText(formatAttack(baseline.getRequiredAttack()));
        resultValue.setForeground(baseline.getRequiredAttack() <= 0 ? SUCCESS : GOLD);

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

    private FreezeCalculator.Boost selectedBoost()
    {
        for (Map.Entry<FreezeCalculator.Boost, JToggleButton> entry : boostButtons.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                return entry.getKey();
            }
        }
        return FreezeCalculator.Boost.NONE;
    }

    private FreezeCalculator.Prayer selectedPrayer()
    {
        for (Map.Entry<FreezeCalculator.Prayer, JToggleButton> entry : prayerButtons.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                return entry.getKey();
            }
        }
        return FreezeCalculator.Prayer.NONE;
    }

    private void copyTable()
    {
        int magicLevel = (int) magicLevelSpinner.getValue();
        FreezeCalculator.Boost boost = selectedBoost();
        FreezeCalculator.Prayer prayer = selectedPrayer();

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
        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setSelectionBackground(PANEL_ALT);
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setFont(BODY_FONT);
        table.setRowHeight(27);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(false);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL_ALT);
        header.setForeground(GOLD);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 28));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        center.setBackground(PANEL);
        center.setForeground(TEXT);
        center.setFont(BODY_FONT);

        DefaultTableCellRenderer attack = new DefaultTableCellRenderer();
        attack.setHorizontalAlignment(SwingConstants.CENTER);
        attack.setBackground(PANEL);
        attack.setForeground(GOLD);
        attack.setFont(BODY_BOLD);

        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(attack);
        table.getColumnModel().getColumn(0).setPreferredWidth(46);
        table.getColumnModel().getColumn(1).setPreferredWidth(78);
        table.getColumnModel().getColumn(2).setPreferredWidth(82);

        return table;
    }

    private static JCheckBox createCheckBox(String text)
    {
        JCheckBox box = new JCheckBox(text);
        box.setOpaque(true);
        box.setBackground(PANEL_ALT);
        box.setForeground(TEXT);
        box.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        box.setFocusPainted(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(4, 5, 4, 5)));
        return box;
    }

    private static JToggleButton createChoiceButton(String label, String formula)
    {
        JToggleButton button = new JToggleButton(
            "<html><center><b>" + label + "</b><br><font size='2'>" + formula + "</font></center></html>");
        button.setFont(BODY_FONT);
        button.setForeground(TEXT);
        button.setBackground(PANEL_ALT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 2, 5, 2));
        button.setBorder(createChoiceBorder(false));
        return button;
    }

    private static void refreshChoiceStyles(Iterable<JToggleButton> buttons)
    {
        for (JToggleButton button : buttons)
        {
            boolean selected = button.isSelected();
            button.setBackground(selected ? new Color(72, 24, 34) : PANEL_ALT);
            button.setForeground(selected ? Color.WHITE : TEXT);
            button.setBorder(createChoiceBorder(selected));
        }
    }

    private static Border createChoiceBorder(boolean selected)
    {
        return BorderFactory.createLineBorder(selected ? BORDER_ACTIVE : BORDER, selected ? 2 : 1);
    }

    private static JButton createActionButton(String text, boolean primary)
    {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(TEXT);
        button.setBackground(primary ? new Color(116, 31, 46) : PANEL_ALT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(primary ? BORDER_ACTIVE : BORDER));
        return button;
    }

    private static JPanel createCard()
    {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private static JPanel transparentPanel()
    {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static JLabel sectionLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(GOLD);
        label.setFont(SECTION_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static Component sectionSpacer()
    {
        return Box.createVerticalStrut(12);
    }

    private static String formatAttack(int value)
    {
        return value > 0 ? "+" + value : Integer.toString(value);
    }
}
