package com.realmsoffate.moddingtoolkit;
import com.realmsoffate.moddingtoolkit.ui.*;import javax.swing.*;
public final class ModdingToolkitApp {
 public static void main(String[] args){SwingUtilities.invokeLater(()->{Theme.install();try{UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());Theme.install();}catch(Exception ignored){}new MainFrame().setVisible(true);});}
}
