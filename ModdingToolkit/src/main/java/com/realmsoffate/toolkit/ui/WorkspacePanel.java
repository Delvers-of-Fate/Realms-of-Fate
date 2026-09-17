package com.realmsoffate.toolkit.ui;

import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.data.SpellLibraryService;
import com.realmsoffate.toolkit.data.MonsterDataService;
import com.realmsoffate.toolkit.data.ClassDataService;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.items.ItemsPanel;
import com.realmsoffate.toolkit.ui.items.SwordEditor;
import com.realmsoffate.toolkit.ui.items.BowEditor;
import com.realmsoffate.toolkit.ui.items.ItemTypeEditor;
import com.realmsoffate.toolkit.ui.magic.MagicPanel;
import com.realmsoffate.toolkit.ui.magic.SpellEditor;
import com.realmsoffate.toolkit.ui.creatures.CreaturesPanel;
import com.realmsoffate.toolkit.ui.creatures.MonsterEditor;
import com.realmsoffate.toolkit.ui.characters.CharactersPanel;
import com.realmsoffate.toolkit.ui.characters.ClassEditor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public class WorkspacePanel extends JPanel {
    public interface Listener {
        void onBackHome();
    }

    private final ModProject project;
    private final ItemsDataService itemsData = new ItemsDataService();
    private final SpellLibraryService spellLibrary = new SpellLibraryService();
    private final MonsterDataService monsters = new MonsterDataService();
    private final ClassDataService classes = new ClassDataService();
    private final JPanel center = new JPanel(new BorderLayout());

    public WorkspacePanel(ModProject project, final Listener listener) {
        this.project = project;
        setLayout(new BorderLayout());
        setBackground(ToolkitColors.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ToolkitColors.PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel projectName = new JLabel(project.getName());
        projectName.setForeground(ToolkitColors.TEXT_PRIMARY);
        projectName.setFont(projectName.getFont().deriveFont(Font.BOLD, 18f));
        header.add(projectName, BorderLayout.WEST);
        JButton home = new JButton("Home");
        home.addActionListener(e -> listener.onBackHome());
        header.add(home, BorderLayout.EAST);

        JPanel sidebar = new JPanel();
        sidebar.setBackground(ToolkitColors.PANEL);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JLabel contentLabel = new JLabel("CONTENT");
        contentLabel.setForeground(ToolkitColors.TEXT_SECONDARY);
        contentLabel.setFont(contentLabel.getFont().deriveFont(Font.BOLD, 12f));
        sidebar.add(contentLabel);
        sidebar.add(Box.createVerticalStrut(12));

        JButton items = navButton("Items");
        JButton magic = navButton("Magic");
        JButton creatures = navButton("Creatures");
        JButton characters = navButton("Characters");
        items.addActionListener(e -> showItems());
        magic.addActionListener(e -> showMagic());
        creatures.addActionListener(e -> showCreatures());
        characters.addActionListener(e -> showCharacters());
        sidebar.add(items);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(magic);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(creatures);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(characters);

        center.setOpaque(false);
        add(header, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        showItems();
    }

    private void showItems() {
        setCenter(new ItemsPanel(project, itemsData, new ItemsPanel.Listener() {
            @Override
            public void onNewSword() {
                showSword(itemsData.createSwordDraft());
            }

            @Override
            public void onOpenSword(ItemsDataService.SwordEntry entry) {
                showSword(entry);
            }

            @Override
            public void onNewBow() {
                showBow(itemsData.createBowDraft());
            }

            @Override
            public void onOpenBow(ItemsDataService.BowEntry entry) {
                showBow(entry);
            }

            @Override public void onNewItem(ItemTypeEditor.Type type) { showItem(createDraft(type), type); }
            @Override public void onOpenItem(ItemsDataService.ItemEntry entry, ItemTypeEditor.Type type) { showItem(entry, type); }
        }));
    }


    private void showCreatures() {
        setCenter(new CreaturesPanel(project, monsters, new CreaturesPanel.Listener() {
            @Override public void onNewMonster() { showMonster(monsters.createDraft()); }
            @Override public void onOpenMonster(MonsterDataService.MonsterEntry entry) { showMonster(entry); }
        }));
    }

    private void showMonster(MonsterDataService.MonsterEntry entry) {
        setCenter(new MonsterEditor(project, monsters, entry, new MonsterEditor.Listener() {
            @Override public void onSaved() { showCreatures(); }
            @Override public void onCancel() { showCreatures(); }
        }));
    }

    private void showCharacters() {
        setCenter(new CharactersPanel(project, classes, new CharactersPanel.Listener() {
            @Override public void onNewClass() { showClass(classes.createDraft(project)); }
            @Override public void onOpenClass(ClassDataService.Entry entry) { showClass(entry); }
        }));
    }

    private void showClass(ClassDataService.Entry entry) {
        setCenter(new ClassEditor(project, classes, entry, new ClassEditor.Listener() {
            @Override public void onSaved() { showCharacters(); }
            @Override public void onCancel() { showCharacters(); }
        }));
    }

    private void showMagic() {
        setCenter(new MagicPanel(project, spellLibrary, new MagicPanel.Listener() {
            @Override public void onNewSpell() { showSpell(spellLibrary.createDraft()); }
            @Override public void onOpenSpell(SpellLibraryService.SpellDefinition spell) { showSpell(spell); }
        }));
    }

    private void showSpell(SpellLibraryService.SpellDefinition spell) {
        setCenter(new SpellEditor(project, spellLibrary, spell, new SpellEditor.Listener() {
            @Override public void onSaved() { showMagic(); }
            @Override public void onCancel() { showMagic(); }
        }));
    }

    private void showSword(ItemsDataService.SwordEntry entry) {
        setCenter(new SwordEditor(project, itemsData, entry, new SwordEditor.Listener() {
            @Override
            public void onSaved() {
                showItems();
            }

            @Override
            public void onCancel() {
                showItems();
            }
        }));
    }


    private void showBow(ItemsDataService.BowEntry entry) {
        setCenter(new BowEditor(project, itemsData, entry, new BowEditor.Listener() {
            @Override
            public void onSaved() { showItems(); }

            @Override
            public void onCancel() { showItems(); }
        }));
    }


    private ItemsDataService.ItemEntry createDraft(ItemTypeEditor.Type t) {
        switch(t) {
            case GUN: return itemsData.createGunDraft();
            case WAND: return itemsData.createWandDraft();
            case ARMOR: return itemsData.createArmorDraft();
            case POTION: return itemsData.createPotionDraft();
            case SCROLL: return itemsData.createScrollDraft();
            case FOOD: return itemsData.createFoodDraft();
            case DECORATION: return itemsData.createDecorationDraft();
            case UNIQUE: return itemsData.createUniqueDraft();
            default: return itemsData.createJunkDraft();
        }
    }
    private void showItem(ItemsDataService.ItemEntry entry, ItemTypeEditor.Type type) {
        setCenter(new ItemTypeEditor(project, itemsData, entry, type, new ItemTypeEditor.Listener() { public void onSaved(){showItems();} public void onCancel(){showItems();} }));
    }

    private void setCenter(JPanel panel) {
        center.removeAll();
        center.add(panel, BorderLayout.CENTER);
        center.revalidate();
        center.repaint();
    }

    private JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setHorizontalAlignment(JButton.LEFT);
        return button;
    }
}
