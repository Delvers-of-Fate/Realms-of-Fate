package com.realmsoffate.toolkit.ui.sprites;

import com.realmsoffate.toolkit.data.SpriteSheetService;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import java.awt.Color;

public class SpritePickerDialog extends JDialog {
    public static class Selection {
        private final String atlas;
        private final int index;
        private final BufferedImage image;
        public Selection(String atlas, int index, BufferedImage image) {
            this.atlas = atlas; this.index = index; this.image = image;
        }
        public String getAtlas() { return atlas; }
        public int getIndex() { return index; }
        public BufferedImage getImage() { return image; }
    }

    private final ModProject project;
    private final SpriteSheetService service = new SpriteSheetService();
    private final JComboBox<String> sheets = new JComboBox<String>();
    private final JPanel grid = new JPanel();
    private final JScrollPane scroll;
    private List<SpriteSheetService.SpriteSheet> available;
    private Selection selection;

    public SpritePickerDialog(Window owner, ModProject project, String initialAtlas, int initialIndex) {
        super(owner, "Choose Sprite", ModalityType.APPLICATION_MODAL);
        this.project = project;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 620);
        setMinimumSize(new Dimension(620, 440));
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 14, 16));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        JLabel atlasLabel = new JLabel("Sprite Sheet");
        atlasLabel.setForeground(ToolkitColors.TEXT_PRIMARY);
        top.add(atlasLabel, BorderLayout.WEST);
        top.add(sheets, BorderLayout.CENTER);

        grid.setOpaque(false);
        scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        bottom.add(cancel);

        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);

        try {
            available = service.loadAvailableSheets(project);
            for (SpriteSheetService.SpriteSheet sheet : available) sheets.addItem(sheet.getName());
            if (initialAtlas != null) sheets.setSelectedItem(initialAtlas);
            if (sheets.getSelectedIndex() < 0 && sheets.getItemCount() > 0) sheets.setSelectedIndex(0);
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sprites", JOptionPane.ERROR_MESSAGE);
        }

        sheets.addActionListener(e -> rebuildGrid(-1));
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { rebuildGrid(-1); }
        });
        rebuildGrid(initialIndex);
    }

    private void rebuildGrid(int selectedIndex) {
        grid.removeAll();
        SpriteSheetService.SpriteSheet sheet = selectedSheet();
        if (sheet == null) {
            grid.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No sprite sheets were found. Run the toolkit from the Delver project root.", SwingConstants.CENTER);
            empty.setForeground(ToolkitColors.TEXT_SECONDARY);
            grid.add(empty, BorderLayout.CENTER);
            grid.revalidate(); grid.repaint();
            return;
        }
        try {
            int tileSize = 82;
            int width = Math.max(300, scroll.getViewport().getWidth() - 16);
            int columns = Math.max(1, width / tileSize);
            int count = sheet.getSpriteCount();
            int rows = (int)Math.ceil(count / (double)columns);
            grid.setLayout(new GridLayout(rows, columns, 7, 7));
            grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            for (int i = 0; i < count; i++) grid.add(createTile(sheet, i, i == selectedIndex));
        }
        catch (IOException ex) {
            grid.setLayout(new BorderLayout());
            JLabel error = new JLabel(ex.getMessage(), SwingConstants.CENTER);
            error.setForeground(new Color(220, 80, 80));
            grid.add(error, BorderLayout.CENTER);
        }
        grid.revalidate(); grid.repaint();
    }

    private Component createTile(SpriteSheetService.SpriteSheet sheet, int index, boolean selected) throws IOException {
        BufferedImage sprite = sheet.getSprite(index);
        JButton button = new JButton(String.valueOf(index), new ImageIcon(scaleNearest(sprite, 52, 52)));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 11f));
        button.setPreferredSize(new Dimension(74, 78));
        if (selected) button.setBorder(BorderFactory.createLineBorder(ToolkitColors.ACCENT, 2));
        button.addActionListener(e -> {
            selection = new Selection(sheet.getName(), index, sprite);
            dispose();
        });
        return button;
    }

    private SpriteSheetService.SpriteSheet selectedSheet() {
        if (available == null) return null;
        Object selected = sheets.getSelectedItem();
        if (selected == null) return null;
        for (SpriteSheetService.SpriteSheet sheet : available) if (selected.equals(sheet.getName())) return sheet;
        return null;
    }

    public Selection getSelection() { return selection; }

    public static Image scaleNearest(BufferedImage source, int maxWidth, int maxHeight) {
        if (source == null) return null;
        int scale = Math.max(1, Math.min(maxWidth / Math.max(1, source.getWidth()), maxHeight / Math.max(1, source.getHeight())));
        int width = Math.max(1, source.getWidth() * scale);
        int height = Math.max(1, source.getHeight() * scale);
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return out;
    }
}
