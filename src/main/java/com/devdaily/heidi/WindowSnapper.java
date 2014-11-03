package com.devdaily.heidi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This is Swing Hack #33 from the SwingHacks book.
 * This probably works well, but it goes crazy with my large window.
 */
public class WindowSnapper extends ComponentAdapter
{

  public WindowSnapper()
  {
  }

  private boolean locked = false;
  private int sd = 50;

  public void componentMoved(ComponentEvent evt)
  {
    if (locked)
      return;
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    int nx = evt.getComponent().getX();
    int ny = evt.getComponent().getY();
    // top
    if (ny < 0 + sd)
    {
      ny = 0;
    }
    // left
    if (nx < 0 + sd)
    {
      nx = 0;
    }
    // right
    if (nx > size.getWidth() - evt.getComponent().getWidth() - sd)
    {
      nx = (int) size.getWidth() - evt.getComponent().getWidth();
    }
    // bottom
    if (ny > size.getHeight() - evt.getComponent().getHeight() - sd)
    {
      ny = (int) size.getHeight() - evt.getComponent().getHeight();
    }
    // make sure we don't get into a recursive loop when the
    // set location generates more events
    locked = true;
    evt.getComponent().setLocation(nx, ny);
    locked = false;
  }
}




