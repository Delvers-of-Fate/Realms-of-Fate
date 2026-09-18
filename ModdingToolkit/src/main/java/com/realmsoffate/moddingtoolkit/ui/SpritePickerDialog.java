package com.realmsoffate.moddingtoolkit.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Visual helper for integer sprite/texture indices. It never changes atlas/path JSON fields. */
public final class SpritePickerDialog extends JDialog {
    private BufferedImage sheet;
    private File sheetFile;
    private int selectedIndex = -1;
    private boolean accepted;
    private final JPanel grid = new JPanel(new GridLayout(0, 8, 6, 6));
    private final JLabel fileLabel = new JLabel("No sprite sheet selected");
    private final JLabel selectionLabel = new JLabel("Select a sprite to continue");
    private final JSpinner tileW = new JSpinner(new SpinnerNumberModel(16, 1, 512, 1));
    private final JSpinner tileH = new JSpinner(new SpinnerNumberModel(16, 1, 512, 1));
    private final JTextField indexField = new JTextField(6);

    private SpritePickerDialog(Window owner, int current) {
        super(owner, "Select Sprite / Texture", ModalityType.APPLICATION_MODAL);
        selectedIndex = current;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 620));
        setSize(1000, 760);
        setLocationRelativeTo(owner);
        build();
    }

    public static Integer choose(Component parent, int current) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        SpritePickerDialog d = new SpritePickerDialog(owner, current);
        d.setVisible(true);
        return d.accepted ? Integer.valueOf(d.selectedIndex) : null;
    }

    private void build() {
        setLayout(new BorderLayout(0, 0));

        JPanel top = new JPanel(new BorderLayout(12, 8));
        top.setBorder(new EmptyBorder(12, 12, 12, 12));
        top.setBackground(Theme.PANEL);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton browse = new JButton("Browse Sprite Sheet...");
        browse.addActionListener(e -> browse());
        left.add(browse);
        left.add(fileLabel);
        top.add(left, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(new JLabel("Tile width:")); controls.add(tileW);
        controls.add(new JLabel("Tile height:")); controls.add(tileH);
        JButton rebuild = new JButton("Refresh Grid");
        rebuild.addActionListener(e -> rebuildGrid());
        controls.add(rebuild);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(new JLabel("Texture index:"));
        indexField.setText(String.valueOf(Math.max(0, selectedIndex)));
        controls.add(indexField);
        JButton jump = new JButton("Use Index");
        jump.addActionListener(e -> useTypedIndex());
        controls.add(jump);
        top.add(controls, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        grid.setBackground(Theme.BG);
        grid.setBorder(new EmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.PANEL);
        bottom.setBorder(new EmptyBorder(10, 12, 10, 12));
        selectionLabel.setForeground(Theme.MUTED);
        bottom.add(selectionLabel, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        JButton select = new JButton("Select Sprite");
        cancel.addActionListener(e -> dispose());
        select.addActionListener(e -> accept());
        buttons.add(cancel); buttons.add(select);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        selectionLabel.setText("Current JSON texture index: " + selectedIndex + "  •  Sprite sheet is preview-only; only tex is changed.");
    }

    private void browse() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose a sprite sheet / texture atlas");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedImage img = ImageIO.read(fc.getSelectedFile());
            if (img == null) throw new IllegalArgumentException("That file is not a supported image.");
            sheet = img;
            sheetFile = fc.getSelectedFile();
            fileLabel.setText(sheetFile.getName() + "  (" + sheet.getWidth() + "×" + sheet.getHeight() + ")");
            autoGuessTileSize();
            rebuildGrid();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not open sprite sheet", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void autoGuessTileSize() {
        int[] common = {16, 32, 64, 8, 24, 48, 128};
        for (int s : common) {
            if (sheet.getWidth() % s == 0 && sheet.getHeight() % s == 0) {
                tileW.setValue(Integer.valueOf(s));
                tileH.setValue(Integer.valueOf(s));
                return;
            }
        }
    }

    private void rebuildGrid() {
        grid.removeAll();
        if (sheet == null) {
            JLabel hint = new JLabel("Choose the sprite sheet used by this asset, then click the sprite you want.");
            hint.setForeground(Theme.MUTED);
            grid.add(hint);
            grid.revalidate(); grid.repaint();
            return;
        }
        int w = ((Number) tileW.getValue()).intValue();
        int h = ((Number) tileH.getValue()).intValue();
        int cols = sheet.getWidth() / w;
        int rows = sheet.getHeight() / h;
        if (cols <= 0 || rows <= 0) return;
        int count = cols * rows;
        for (int i = 0; i < count; i++) {
            int x = (i % cols) * w, y = (i / cols) * h;
            BufferedImage tile = sheet.getSubimage(x, y, w, h);
            grid.add(tileButton(tile, i));
        }
        grid.revalidate(); grid.repaint();
    }

    private JButton tileButton(BufferedImage tile, final int index) {
        int scale = Math.max(1, Math.min(4, 64 / Math.max(tile.getWidth(), tile.getHeight())));
        int dw = Math.max(tile.getWidth(), tile.getWidth() * scale);
        int dh = Math.max(tile.getHeight(), tile.getHeight() * scale);
        Image img = tile.getScaledInstance(dw, dh, Image.SCALE_FAST);
        JButton b = new JButton("#" + index, new ImageIcon(img));
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setToolTipText("Texture index " + index);
        b.setPreferredSize(new Dimension(Math.max(82, dw + 12), Math.max(92, dh + 28)));
        b.addActionListener(e -> select(index));
        if (index == selectedIndex) b.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 2));
        return b;
    }

    private void select(int index) {
        selectedIndex = index;
        indexField.setText(String.valueOf(index));
        selectionLabel.setText("Selected texture index #" + index + (sheetFile == null ? "" : " from " + sheetFile.getName()));
        rebuildGrid();
    }

    private void useTypedIndex() {
        try { select(Integer.parseInt(indexField.getText().trim())); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Enter a whole-number texture index."); }
    }

    private void accept() {
        try { selectedIndex = Integer.parseInt(indexField.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Choose a sprite or enter a whole-number texture index."); return; }
        accepted = true;
        dispose();
    }
}
