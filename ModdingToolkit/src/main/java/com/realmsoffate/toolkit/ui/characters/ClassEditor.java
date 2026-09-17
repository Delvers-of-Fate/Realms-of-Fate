package com.realmsoffate.toolkit.ui.characters;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.data.*;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.ui.ToolkitColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ClassEditor extends JPanel {
    public interface Listener { void onSaved(); void onCancel(); }

    private final ModProject p;
    private final ClassDataService s;
    private final ClassDataService.Entry e;
    private final JsonValue j;

    private final JTextField id = new JTextField(), display = new JTextField(), hp = new JTextField(), mp = new JTextField(),
            atk = new JTextField(), def = new JTextField(), dex = new JTextField(), spd = new JTextField(), mag = new JTextField(),
            end = new JTextField(), jump = new JTextField(), sprint = new JTextField(), crouch = new JTextField(), regen = new JTextField();
    private final JCheckBox mana = new JCheckBox("Mana regeneration enabled");
    private final DefaultTableModel resistanceModel = new DefaultTableModel(new Object[]{"Damage Type", "Resistance"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return true; }
    };
    private final JTable resistanceTable = new JTable(resistanceModel);
    private final DefaultTableModel startingItemsModel = new DefaultTableModel(new Object[]{"Item Name", "Auto Equip"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return true; }
        @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 1 ? Boolean.class : String.class; }
    };
    private final JTable startingItemsTable = new JTable(startingItemsModel);

    public ClassEditor(ModProject p, ClassDataService s, ClassDataService.Entry e, Listener l) {
        this.p = p; this.s = s; this.e = e; this.j = copy(e.getJson());
        setLayout(new BorderLayout()); setBackground(ToolkitColors.BACKGROUND);
        JPanel f = new JPanel(new GridBagLayout()); f.setBackground(ToolkitColors.BACKGROUND); f.setBorder(BorderFactory.createEmptyBorder(24, 28, 30, 28));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.fill = GridBagConstraints.HORIZONTAL; int y = 0;

        y = sec(f,c,y,"Class Identity");
        id.setText(ItemsDataService.getString(j,"id","")); display.setText(ItemsDataService.getString(j,"displayName",""));
        y=row(f,c,y,"ID",id); y=row(f,c,y,"Display Name",display);

        y=sec(f,c,y,"Starting Stats");
        for(Object[] a:new Object[][]{{"HP",hp,"hp",100},{"MP",mp,"mp",20},{"Attack",atk,"atk",10},{"Defense",def,"def",10},{"Dexterity",dex,"dex",10},{"Speed",spd,"spd",10},{"Magic",mag,"mag",10},{"Endurance",end,"end",10}}){
            JTextField x=(JTextField)a[1]; x.setText(String.valueOf(ItemsDataService.getInt(j,(String)a[2],(Integer)a[3]))); y=row(f,c,y,(String)a[0],x);
        }

        y=sec(f,c,y,"Movement & Mana");
        jump.setText(String.valueOf(ItemsDataService.getFloat(j,"jumpHeight",.05f))); sprint.setText(String.valueOf(ItemsDataService.getFloat(j,"sprintSpeedMultiplier",1.65f)));
        crouch.setText(String.valueOf(ItemsDataService.getFloat(j,"crouchSpeedMultiplier",.5f))); regen.setText(String.valueOf(ItemsDataService.getFloat(j,"manaRegenRate",.1f)));
        mana.setSelected(ItemsDataService.getBoolean(j,"manaRegenerationEnabled",true));
        y=row(f,c,y,"Jump Height",jump); y=row(f,c,y,"Sprint Multiplier",sprint); y=row(f,c,y,"Crouch Multiplier",crouch); y=row(f,c,y,"Mana Regen Rate",regen); y=row(f,c,y,"",mana);

        y=sec(f,c,y,"Resistances");
        loadResistances();
        resistanceTable.setFillsViewportHeight(true); resistanceTable.setRowHeight(24);
        JComboBox<String> resistanceTypes = new JComboBox<>(new String[]{"fire", "ice", "poison", "lightning", "magic"});
        TableColumn damageTypeColumn = resistanceTable.getColumnModel().getColumn(0);
        damageTypeColumn.setCellEditor(new DefaultCellEditor(resistanceTypes));
        JScrollPane resistanceScroll = new JScrollPane(resistanceTable); resistanceScroll.setPreferredSize(new Dimension(520, 145));
        c.gridx=0; c.gridy=y++; c.gridwidth=2; c.weightx=1; f.add(resistanceScroll,c); c.gridwidth=1;
        JPanel rb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); rb.setOpaque(false);
        JButton addResistance = new JButton("+ Add Resistance"), removeResistance = new JButton("Remove Selected");
        addResistance.addActionListener(x -> resistanceModel.addRow(new Object[]{"fire", "0.0"}));
        removeResistance.addActionListener(x -> { int r=resistanceTable.getSelectedRow(); if(r>=0) resistanceModel.removeRow(r); });
        rb.add(addResistance); rb.add(removeResistance);
        c.gridx=0; c.gridy=y++; c.gridwidth=2; f.add(rb,c); c.gridwidth=1;
        JLabel rn = new JLabel("Resistance values: 0.50 = 50% resistance, -0.15 = 15% vulnerability. Available types: Fire, Ice, Poison, Lightning, Magic.");
        rn.setForeground(ToolkitColors.TEXT_SECONDARY); c.gridx=0; c.gridy=y++; c.gridwidth=2; f.add(rn,c); c.gridwidth=1;

        y=sec(f,c,y,"Starting Inventory");
        loadStartingItems();
        startingItemsTable.setFillsViewportHeight(true); startingItemsTable.setRowHeight(24);
        JComboBox<String> itemChoices = new JComboBox<>();
        itemChoices.setEditable(false);
        for(String itemName : loadItemNames()) itemChoices.addItem(itemName);
        startingItemsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemChoices));
        startingItemsTable.getColumnModel().getColumn(1).setMaxWidth(100);
        JScrollPane startingItemsScroll = new JScrollPane(startingItemsTable); startingItemsScroll.setPreferredSize(new Dimension(520, 145));
        c.gridx=0; c.gridy=y++; c.gridwidth=2; c.weightx=1; f.add(startingItemsScroll,c); c.gridwidth=1;
        JPanel sib = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); sib.setOpaque(false);
        JButton addStartingItem = new JButton("+ Add Item"), removeStartingItem = new JButton("Remove Selected");
        addStartingItem.addActionListener(x -> startingItemsModel.addRow(new Object[]{itemChoices.getItemCount() > 0 ? itemChoices.getItemAt(0) : "", Boolean.FALSE}));
        removeStartingItem.addActionListener(x -> { int r=startingItemsTable.getSelectedRow(); if(r>=0) startingItemsModel.removeRow(r); });
        sib.add(addStartingItem); sib.add(removeStartingItem);
        c.gridx=0; c.gridy=y++; c.gridwidth=2; f.add(sib,c); c.gridwidth=1;
        JLabel sin = new JLabel("Choose an item from items.dat. Auto Equip is intended for starting armor/equipment that should be worn immediately.");
        sin.setForeground(ToolkitColors.TEXT_SECONDARY); c.gridx=0; c.gridy=y++; c.gridwidth=2; f.add(sin,c); c.gridwidth=1;

        JScrollPane sp=new JScrollPane(f); sp.setBorder(null); add(sp,BorderLayout.CENTER);
        JPanel b=new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton back=new JButton("Back"),save=new JButton("Save Class");
        back.addActionListener(x->l.onCancel()); save.addActionListener(x->{try{save();l.onSaved();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Class",JOptionPane.ERROR_MESSAGE);}});
        b.add(back);b.add(save);add(b,BorderLayout.SOUTH);
    }

    private void loadResistances() {
        JsonValue rs = j.get("resistances");
        if(rs == null || !rs.isObject()) return;
        for(JsonValue r = rs.child; r != null; r = r.next) resistanceModel.addRow(new Object[]{r.name, String.valueOf(r.asFloat())});
    }

    private void saveResistances() {
        if(resistanceTable.isEditing()) resistanceTable.getCellEditor().stopCellEditing();
        JsonValue rs = new JsonValue(JsonValue.ValueType.object);
        for(int row=0; row<resistanceModel.getRowCount(); row++) {
            String type = String.valueOf(resistanceModel.getValueAt(row,0)).trim();
            String raw = String.valueOf(resistanceModel.getValueAt(row,1)).trim();
            if(type.isEmpty() && raw.isEmpty()) continue;
            if(type.isEmpty()) throw new IllegalArgumentException("Resistance row " + (row+1) + " needs a damage type.");
            float value;
            try { value = Float.parseFloat(raw); } catch(Exception ex) { throw new IllegalArgumentException("Resistance for '"+type+"' must be a number."); }
            if(rs.get(type) != null) throw new IllegalArgumentException("Duplicate resistance damage type: " + type);
            JsonTree.put(rs, type, new JsonValue(value));
        }
        if(rs.child != null) JsonTree.put(j, "resistances", rs);
        else JsonTree.remove(j, "resistances");
    }


    private java.util.List<String> loadItemNames() {
        java.util.TreeSet<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        try {
            JsonValue root = new ItemsDataService().loadOrCreate(p);
            collectItemNames(root, names);
        } catch(Exception ignored) { }
        return new java.util.ArrayList<>(names);
    }

    private void collectItemNames(JsonValue node, java.util.Set<String> names) {
        if(node == null) return;
        if(node.isObject()) {
            JsonValue name = node.get("name");
            JsonValue clazz = node.get("class");
            if(name != null && name.isString() && clazz != null) {
                String value = name.asString().trim();
                if(!value.isEmpty()) names.add(value);
            }
        }
        for(JsonValue child=node.child; child!=null; child=child.next) collectItemNames(child, names);
    }

    private void loadStartingItems() {
        JsonValue items = j.get("startingItems");
        if(items == null || !items.isArray()) return;
        for(JsonValue item = items.child; item != null; item = item.next) {
            if(item.isString()) {
                startingItemsModel.addRow(new Object[]{item.asString(), Boolean.FALSE});
            } else if(item.isObject()) {
                String name = ItemsDataService.getString(item, "name", ItemsDataService.getString(item, "item", ""));
                boolean autoEquip = ItemsDataService.getBoolean(item, "autoEquip", false);
                if(!name.trim().isEmpty()) startingItemsModel.addRow(new Object[]{name, autoEquip});
            }
        }
    }

    private void saveStartingItems() {
        if(startingItemsTable.isEditing()) startingItemsTable.getCellEditor().stopCellEditing();
        JsonValue items = new JsonValue(JsonValue.ValueType.array);
        for(int row=0; row<startingItemsModel.getRowCount(); row++) {
            Object cell = startingItemsModel.getValueAt(row,0);
            String name = cell == null ? "" : String.valueOf(cell).trim();
            if(name.isEmpty()) continue;
            boolean autoEquip = Boolean.TRUE.equals(startingItemsModel.getValueAt(row,1));
            if(autoEquip) {
                JsonValue entry = new JsonValue(JsonValue.ValueType.object);
                ItemsDataService.putString(entry, "name", name);
                ItemsDataService.putBoolean(entry, "autoEquip", true);
                JsonTree.append(items, entry);
            } else {
                JsonTree.append(items, new JsonValue(name));
            }
        }
        if(items.child != null) JsonTree.put(j, "startingItems", items);
        else JsonTree.remove(j, "startingItems");
    }

    private int sec(JPanel f,GridBagConstraints c,int y,String t){c.gridx=0;c.gridy=y;c.gridwidth=2;JLabel l=new JLabel(t);l.setFont(l.getFont().deriveFont(Font.BOLD,17f));l.setForeground(ToolkitColors.ACCENT);f.add(l,c);c.gridwidth=1;return y+1;}
    private int row(JPanel f,GridBagConstraints c,int y,String n,Component x){c.gridy=y;c.gridx=0;c.weightx=.25;f.add(new JLabel(n),c);c.gridx=1;c.weightx=.75;f.add(x,c);return y+1;}
    private int I(JTextField f,String n){try{return Integer.parseInt(f.getText().trim());}catch(Exception x){throw new IllegalArgumentException(n+" must be a whole number.");}}
    private float F(JTextField f,String n){try{return Float.parseFloat(f.getText().trim());}catch(Exception x){throw new IllegalArgumentException(n+" must be a number.");}}

    private void save()throws Exception{
        if(id.getText().trim().isEmpty()||display.getText().trim().isEmpty())throw new IllegalArgumentException("Class ID and display name are required.");
        ItemsDataService.putString(j,"id",id.getText().trim()); ItemsDataService.putString(j,"displayName",display.getText().trim());
        JTextField[] fs={hp,mp,atk,def,dex,spd,mag,end}; String[] ns={"hp","mp","atk","def","dex","spd","mag","end"};
        for(int i=0;i<fs.length;i++)ItemsDataService.putInt(j,ns[i],I(fs[i],ns[i]));
        ItemsDataService.putFloat(j,"jumpHeight",F(jump,"Jump Height")); ItemsDataService.putFloat(j,"sprintSpeedMultiplier",F(sprint,"Sprint Multiplier"));
        ItemsDataService.putFloat(j,"crouchSpeedMultiplier",F(crouch,"Crouch Multiplier")); ItemsDataService.putFloat(j,"manaRegenRate",F(regen,"Mana Regen Rate"));
        ItemsDataService.putBoolean(j,"manaRegenerationEnabled",mana.isSelected()); saveResistances(); saveStartingItems(); s.save(p,e,j);
    }
    private static JsonValue copy(JsonValue q){return JsonTree.copy(q);}
}
