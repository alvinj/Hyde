package com.devdaily.heidi;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics;

public class DCJLinkLabel extends JLabel
{
  private String url;
  final Color COLOR_NORMAL = Color.blue;
  final Color COLOR_HOVER = Color.red;
  final Color COLOR_ACTIVE = COLOR_NORMAL;
  final Color COLOR_BG_NORMAL = Color.white;
  final Color COLOR_BG_ACTIVE = Color.white;
  Color mouseOutDefault;

  public DCJLinkLabel(String displayText, String url)
  {
    addMouseListener();
    setTextAndUrl(displayText, url);
    setForeground(COLOR_NORMAL);
    //setBackground(COLOR_BG_NORMAL);
    mouseOutDefault = COLOR_NORMAL;
    this.setSize((int) this.getPreferredSize().getWidth(), 30);
    this.setOpaque(true);
  }

//  public static void main(String[] args)
//  {
//    javax.swing.JDialog dialog = new javax.swing.JDialog(new javax.swing.JFrame(), "Test");
//    java.awt.Container container = dialog.getContentPane();
//    container.setLayout(new java.awt.FlowLayout());
//    JLinkLabel linkLabel = new JLinkLabel("Click Me", "http://devdaily.com");
//    container.add(linkLabel);
//    container.add(new JLabel("test"));
//    dialog.setVisible(true);
//  }

  public void setTextAndUrl(String displayText, String url)
  {
    this.url = url;
    // this currently tells the label to be underlined
    setText("<html><a href=" + url + ">" + displayText + "</a>");
  }

  public void paint(Graphics g)
  {
    super.paint(g);
    // not needed atm; the html tag above does this
    //g.drawLine(2, getHeight() - 1, getPreferredSize().width - 2, getHeight() - 1);
  }

  public void addMouseListener()
  {

    addMouseListener(new MouseListener()
    {
      public void mouseClicked(MouseEvent ae)
      {
        setForeground(COLOR_ACTIVE);
        mouseOutDefault = COLOR_ACTIVE;
        // do something here
      }

      public void mousePressed(MouseEvent p0)
      {
        mouseOutDefault = COLOR_ACTIVE;
      }

      public void mouseReleased(MouseEvent p0)
      {
      }

      public void mouseEntered(MouseEvent p0)
      {
        setForeground(COLOR_HOVER);
        //setBackground(COLOR_BG_ACTIVE);
        Cursor cur = getCursor();
        setCursor(cur.getPredefinedCursor(Cursor.HAND_CURSOR));
      }

      public void mouseExited(MouseEvent p0)
      {
        setForeground(mouseOutDefault);
        //setBackground(COLOR_BG_NORMAL);
        Cursor cur = getCursor();
        setCursor(cur.getDefaultCursor());
      }
    });
  }
}
