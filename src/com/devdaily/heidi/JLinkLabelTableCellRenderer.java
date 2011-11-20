package com.devdaily.heidi;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * 
 * NOTE: THIS CLASS IS DEAD, AT LEAST TEMPORARILY.
 * 
 * Display a JLinkLabel in the "Listen" column.
 */
public class JLinkLabelTableCellRenderer extends JLabel implements TableCellRenderer
{
  Border unselectedBorder = null;
  Border selectedBorder = null;
  boolean isBordered = true;

  public JLinkLabelTableCellRenderer(boolean isBordered)
  {
//    this.isBordered = isBordered;
//    setOpaque(true); // MUST do this for background to show up.
  }

  public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row,
      int column)
  {
    DCJLinkLabel jll = new DCJLinkLabel("Listen", "http://devdaily.com");
    return jll;
  }
}





