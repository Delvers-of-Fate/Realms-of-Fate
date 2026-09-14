package com.realmsoffate.toolkit.ui.editors;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class WeaponEditorTest {

    public static void main(
        String[] args
    ) {

        try {

            UIManager.setLookAndFeel(
                new FlatDarkLaf()
            );

        }
        catch (
            Exception e
        ) {

            e.printStackTrace();
        }

        SwingUtilities.invokeLater(
            new Runnable() {

                @Override
                public void run() {

                    JFrame frame =
                        new JFrame(
                            "Weapon Editor Test"
                        );

                    frame.setDefaultCloseOperation(
                        JFrame.EXIT_ON_CLOSE
                    );

                    frame.setSize(
                        900,
                        760
                    );

                    frame.setMinimumSize(
                        new Dimension(
                            750,
                            600
                        )
                    );

                    frame.setLocationRelativeTo(
                        null
                    );

                    frame.setContentPane(
                        new WeaponEditorPanel()
                    );

                    frame.setVisible(
                        true
                    );
                }
            }
        );
    }
}
