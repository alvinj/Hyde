package com.devdaily.heidi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Trying to get my JTable header cells to be centered.
 */
public class CenteredTableHeaderRenderer extends DefaultTableCellRenderer
{
  private static final Font FONT = UIManager.getFont("Table.font");

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
      int column)
  {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    setHorizontalAlignment(SwingConstants.CENTER);
    
    //setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, table.getForeground(), table.getBackground()));
    //setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

    // this is extremely close
    //setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    // again, very close
    //setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
    
    //setBorder(BorderFactory.createLineBorder(table.getGridColor()));
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, table.getGridColor(), table.getBackground()));

    setOpaque(false);

    // got this string from looking at jformdesigner
    setFont(FONT);

    return this;
  }
}