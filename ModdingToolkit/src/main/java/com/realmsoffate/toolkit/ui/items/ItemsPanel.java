package com.realmsoffate.toolkit.ui.items;

import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ItemsPanel extends JPanel {
    public interface Listener {
        void onNewSword();
        void onOpenSword(ItemsDataService.SwordEntry e);
        void onNewBow();
        void onOpenBow(ItemsDataService.BowEntry e);
        void onNewItem(ItemTypeEditor.Type t);
        void onOpenItem(ItemsDataService.ItemEntry e, ItemTypeEditor.Type t);
    }

    private final ModProject project;
    private final ItemsDataService items;
    private final Listener listener;
    private final JPanel list = new JPanel();
    private final JLabel count = new JLabel();
    private final JTextField search = new JTextField();

    public ItemsPanel(ModProject project, ItemsDataService items, Listener listener) {
        this.project = project;
        this.items = items;
        this.listener = listener;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Items");
        title.setForeground(ToolkitColors.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        count.setForeground(ToolkitColors.TEXT_SECONDARY);
        titleBox.add(title);
        titleBox.add(count);
        top.add(titleBox, BorderLayout.WEST);

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        search.setPreferredSize(new Dimension(260, 34));
        search.putClientProperty("JTextField.placeholderText", "Search items...");
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        JButton create = new JButton("+ Create Item");
        create.addActionListener(e -> showCreateDialog());
        actions.add(search, BorderLayout.CENTER);
        actions.add(create, BorderLayout.EAST);
        top.add(actions, BorderLayout.EAST);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(ToolkitColors.BACKGROUND);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    private void showCreateDialog() {
        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Create Item", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel();
        root.setBackground(ToolkitColors.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Create Item");
        heading.setForeground(ToolkitColors.TEXT_PRIMARY);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 20f));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(heading);
        root.add(Box.createVerticalStrut(16));

        addCreateGroup(root, dialog, "WEAPONS",
                new Choice("Sword", null), new Choice("Bow", null),
                new Choice("Gun", ItemTypeEditor.Type.GUN), new Choice("Wand", ItemTypeEditor.Type.WAND));
        addCreateGroup(root, dialog, "EQUIPMENT", new Choice("Armor", ItemTypeEditor.Type.ARMOR));
        addCreateGroup(root, dialog, "CONSUMABLES",
                new Choice("Potion", ItemTypeEditor.Type.POTION), new Choice("Scroll", ItemTypeEditor.Type.SCROLL),
                new Choice("Food", ItemTypeEditor.Type.FOOD));
        addCreateGroup(root, dialog, "OTHER",
                new Choice("Decoration", ItemTypeEditor.Type.DECORATION), new Choice("Junk", ItemTypeEditor.Type.JUNK),
                new Choice("Unique", ItemTypeEditor.Type.UNIQUE));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        footer.add(cancel);
        root.add(Box.createVerticalStrut(8));
        root.add(footer);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(500, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addCreateGroup(JPanel root, JDialog dialog, String title, Choice... choices) {
        JLabel label = new JLabel(title);
        label.setForeground(ToolkitColors.ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(label);
        root.add(Box.createVerticalStrut(7));

        JPanel buttons = new JPanel(new GridLayout(0, 4, 8, 8));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Choice choice : choices) {
            JButton button = new JButton(choice.name);
            button.setPreferredSize(new Dimension(105, 38));
            button.addActionListener(e -> {
                dialog.dispose();
                if ("Sword".equals(choice.name)) listener.onNewSword();
                else if ("Bow".equals(choice.name)) listener.onNewBow();
                else listener.onNewItem(choice.type);
            });
            buttons.add(button);
        }
        root.add(buttons);
        root.add(Box.createVerticalStrut(15));
    }

    public final void refresh() {
        list.removeAll();
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        try {
            int total = 0;
            List<ItemsDataService.SwordEntry> swords = items.listSwords(project);
            List<ItemsDataService.BowEntry> bows = items.listBows(project);
            total += swords.size() + bows.size();

            int shownSwords = 0;
            for (ItemsDataService.SwordEntry e : swords) if (matches(e.getName(), query)) shownSwords++;
            section("SWORDS", shownSwords);
            if (shownSwords == 0) empty(query.isEmpty() ? "No swords yet." : "No matching swords.");
            for (ItemsDataService.SwordEntry e : swords)
                if (matches(e.getName(), query)) row(e.getName() + "  •  Tier " + e.getTier(), () -> listener.onOpenSword(e));

            int shownBows = 0;
            for (ItemsDataService.BowEntry e : bows) if (matches(e.getName(), query)) shownBows++;
            section("BOWS", shownBows);
            if (shownBows == 0) empty(query.isEmpty() ? "No bows yet." : "No matching bows.");
            for (ItemsDataService.BowEntry e : bows)
                if (matches(e.getName(), query)) row(e.getName() + "  •  Tier " + e.getTier(), () -> listener.onOpenBow(e));

            total += family("GUNS", "ranged", ItemsDataService.GUN_CLASS, true, ItemTypeEditor.Type.GUN, query);
            total += family("WANDS", "wands", ItemsDataService.WAND_CLASS, false, ItemTypeEditor.Type.WAND, query);
            total += family("ARMOR", "armor", ItemsDataService.ARMOR_CLASS, true, ItemTypeEditor.Type.ARMOR, query);
            total += family("POTIONS", "potions", ItemsDataService.POTION_CLASS, false, ItemTypeEditor.Type.POTION, query);
            total += family("SCROLLS", "scrolls", ItemsDataService.SCROLL_CLASS, false, ItemTypeEditor.Type.SCROLL, query);
            total += family("FOOD", "food", ItemsDataService.FOOD_CLASS, false, ItemTypeEditor.Type.FOOD, query);
            total += family("DECORATIONS", "decorations", null, false, ItemTypeEditor.Type.DECORATION, query);
            total += family("JUNK", "junk", null, false, ItemTypeEditor.Type.JUNK, query);
            total += family("UNIQUE", "unique", null, false, ItemTypeEditor.Type.UNIQUE, query);
            count.setText(total + (total == 1 ? " item" : " items"));
        } catch (IOException ex) {
            count.setText("Could not load items.dat");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Items", JOptionPane.ERROR_MESSAGE);
        }
        list.revalidate();
        list.repaint();
    }

    private int family(String title, String category, String cls, boolean tiered, ItemTypeEditor.Type type, String query) throws IOException {
        List<ItemsDataService.ItemEntry> entries = items.listItems(project, category, cls, tiered);
        int shown = 0;
        for (ItemsDataService.ItemEntry e : entries) if (matches(e.getName(), query)) shown++;
        section(title, shown);
        if (shown == 0) empty(query.isEmpty() ? "No " + title.toLowerCase() + " yet." : "No matching " + title.toLowerCase() + ".");
        for (ItemsDataService.ItemEntry e : entries) {
            if (!matches(e.getName(), query)) continue;
            row(e.getName() + (tiered ? "  •  Tier " + e.getTier() : ""), () -> listener.onOpenItem(e, type));
        }
        return entries.size();
    }

    private boolean matches(String name, String query) {
        return query.isEmpty() || (name != null && name.toLowerCase().contains(query));
    }

    private void section(String text, int amount) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel(text);
        name.setForeground(ToolkitColors.TEXT_SECONDARY);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
        JLabel number = new JLabel(String.valueOf(amount));
        number.setForeground(ToolkitColors.TEXT_SECONDARY);
        header.add(name, BorderLayout.WEST);
        header.add(number, BorderLayout.EAST);
        list.add(header);
    }

    private void empty(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ToolkitColors.TEXT_SECONDARY);
        label.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        list.add(label);
    }

    private void row(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        list.add(button);
        list.add(Box.createVerticalStrut(5));
    }

    private static class Choice {
        final String name;
        final ItemTypeEditor.Type type;
        Choice(String name, ItemTypeEditor.Type type) { this.name = name; this.type = type; }
    }
}
