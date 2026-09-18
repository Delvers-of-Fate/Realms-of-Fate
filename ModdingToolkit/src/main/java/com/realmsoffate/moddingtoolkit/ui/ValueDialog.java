package com.realmsoffate.moddingtoolkit.ui;
import javax.swing.*;import java.awt.*;import java.math.BigDecimal;import java.util.*;
final class ValueDialog {
 static Object create(Component parent){
  JComboBox<String> type=new JComboBox<String>(new String[]{"Text (String)","Number","Yes / No (Boolean)","Object / Section","List / Array","Empty (Null)"});JTextField val=new JTextField(24);JComboBox<String> bool=new JComboBox<String>(new String[]{"Yes / true","No / false"});
  JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(new JLabel("What kind of value are you adding?"));p.add(Box.createVerticalStrut(5));p.add(type);p.add(Box.createVerticalStrut(10));p.add(new JLabel("Starting value (text or number):"));p.add(Box.createVerticalStrut(5));p.add(val);p.add(Box.createVerticalStrut(8));p.add(new JLabel("For Yes / No values:"));p.add(bool);
  if(JOptionPane.showConfirmDialog(parent,p,"Add Property Value",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return CANCEL;
  try{String t=(String)type.getSelectedItem();if(t.startsWith("Text"))return val.getText();if("Number".equals(t))return new BigDecimal(val.getText().trim().isEmpty()?"0":val.getText().trim());if(t.startsWith("Yes"))return bool.getSelectedIndex()==0;if(t.startsWith("Object"))return new LinkedHashMap<String,Object>();if(t.startsWith("List"))return new ArrayList<Object>();return null;}catch(Exception e){JOptionPane.showMessageDialog(parent,"That value could not be created.\n\n"+e.getMessage(),"Invalid value",JOptionPane.ERROR_MESSAGE);return CANCEL;}
 }
 static final Object CANCEL=new Object();
}
