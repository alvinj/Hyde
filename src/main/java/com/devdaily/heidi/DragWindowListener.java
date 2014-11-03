package com.devdaily.heidi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This was named MoveMouseListener in the Swing Hacks book.
 * This is Swing Hack #34.
 */
public class DragWindowListener implements MouseListener, MouseMotionListener
{
  JComponent jComponent;
  Point startDragPoint;
  Point startLocPoint;

  public DragWindowListener(JComponent jComponent)
  {
    this.jComponent = jComponent;
  }

  public static JFrame getFrame(Container container)
  {
    if (container instanceof JFrame)
    {
      return (JFrame) container;
    }
    return getFrame(container.getParent());
  }

  Point getScreenLocation(MouseEvent e)
  {
    Point cursor = e.getPoint();
    Point targetLocation = this.jComponent.getLocationOnScreen();
    return new Point((int) (targetLocation.getX() + cursor.getX()), (int) (targetLocation.getY() + cursor.getY()));
  }

  public void mousePressed(MouseEvent e)
  {
    this.startDragPoint = this.getScreenLocation(e);
    this.startLocPoint = this.getFrame(this.jComponent).getLocation();
  }

  public void mouseDragged(MouseEvent e)
  {
    Point current = this.getScreenLocation(e);
    Point offset = new Point((int) current.getX() - (int) startDragPoint.getX(), (int) current.getY() - (int) startDragPoint.getY());
    JFrame frame = this.getFrame(jComponent);
    Point new_location = new Point((int) (this.startLocPoint.getX() + offset.getX()), (int) (this.startLocPoint.getY() + offset.getY()));
    frame.setLocation(new_location);
  }

  public void mouseReleased(MouseEvent e)
  {
  }

  public void mouseMoved(MouseEvent e)
  {
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }

}






