package com.realmsoffate.moddingtoolkit.ui;
import javax.swing.*;import javax.swing.border.*;import java.awt.*;
public final class Theme {
 public static final Color BG=new Color(16,18,23), PANEL=new Color(23,27,34), PANEL2=new Color(31,36,45), PANEL3=new Color(38,44,54), BORDER=new Color(52,60,72), TEXT=new Color(232,235,241), MUTED=new Color(151,160,177), ACCENT=new Color(205,157,78), DANGER=new Color(205,83,83), SUCCESS=new Color(111,184,132);
 private Theme(){}
 public static void install(){
  UIManager.put("control",PANEL);UIManager.put("info",PANEL2);UIManager.put("nimbusBase",PANEL2);UIManager.put("nimbusLightBackground",PANEL2);UIManager.put("text",TEXT);UIManager.put("window",BG);UIManager.put("menu",PANEL);UIManager.put("menuText",TEXT);
  UIManager.put("Panel.background",BG);UIManager.put("Label.foreground",TEXT);UIManager.put("Label.disabledForeground",MUTED);
  UIManager.put("TextField.background",PANEL2);UIManager.put("TextField.foreground",TEXT);UIManager.put("TextField.caretForeground",TEXT);UIManager.put("TextField.selectionBackground",new Color(72,84,104));UIManager.put("TextField.selectionForeground",Color.WHITE);UIManager.put("TextField.border",new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(5,7,5,7)));
  UIManager.put("TextArea.background",new Color(18,21,27));UIManager.put("TextArea.foreground",TEXT);UIManager.put("TextArea.caretForeground",TEXT);UIManager.put("TextArea.selectionBackground",new Color(72,84,104));UIManager.put("TextArea.selectionForeground",Color.WHITE);
  UIManager.put("List.background",PANEL);UIManager.put("List.foreground",TEXT);UIManager.put("List.selectionBackground",new Color(78,64,43));UIManager.put("List.selectionForeground",Color.WHITE);
  UIManager.put("ScrollPane.background",BG);UIManager.put("ScrollPane.border",new LineBorder(BORDER));UIManager.put("Viewport.background",BG);
  UIManager.put("Button.background",PANEL2);UIManager.put("Button.foreground",TEXT);UIManager.put("Button.select",PANEL3);UIManager.put("Button.focus",new Color(0,0,0,0));
  UIManager.put("CheckBox.background",BG);UIManager.put("CheckBox.foreground",TEXT);UIManager.put("ComboBox.background",PANEL2);UIManager.put("ComboBox.foreground",TEXT);UIManager.put("ComboBox.selectionBackground",PANEL3);UIManager.put("ComboBox.selectionForeground",TEXT);
  UIManager.put("MenuBar.background",PANEL);UIManager.put("MenuBar.foreground",TEXT);UIManager.put("Menu.background",PANEL);UIManager.put("Menu.foreground",TEXT);UIManager.put("MenuItem.background",PANEL);UIManager.put("MenuItem.foreground",TEXT);UIManager.put("MenuItem.selectionBackground",PANEL3);UIManager.put("MenuItem.selectionForeground",Color.WHITE);UIManager.put("PopupMenu.background",PANEL);UIManager.put("PopupMenu.border",new LineBorder(BORDER));
  UIManager.put("OptionPane.background",PANEL);UIManager.put("OptionPane.messageForeground",TEXT);UIManager.put("FileChooser.background",PANEL);UIManager.put("ToolTip.background",PANEL3);UIManager.put("ToolTip.foreground",TEXT);UIManager.put("ToolTip.border",new LineBorder(BORDER));
  UIManager.put("SplitPane.background",BORDER);UIManager.put("SplitPane.dividerSize",5);UIManager.put("Separator.foreground",BORDER);UIManager.put("Separator.background",BORDER);
 }
 public static Border pad(int n){return new EmptyBorder(n,n,n,n);} public static Border line(){return new LineBorder(BORDER);}
}
