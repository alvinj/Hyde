package com.devdaily.heidi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PreferencesDialogWithEscSupport extends PreferencesDialog
implements ContainerListener, KeyListener
{

  public PreferencesDialogWithEscSupport(Frame frame)
  {
    super(frame);
    init();
  }

  public PreferencesDialogWithEscSupport(JDialog dialog)
  {
    super(dialog);
    init();
  }
  
  private void init()
  {
    try 
    {
      addKeyAndContainerListenerRecursively(this);
    }
    catch(Exception ex)
    {
      // TODO bother to add logging?
    }
  }

  /**
   * This method is called when the user hits the [Enter] key
   * (or "OK" command).The idea is for you to override this
   * and implement your own functionality in your subclass.
   *
   * @param e KeyEvent
   */
  public void okCommandPerformed(KeyEvent e)
  {
    
  }

  /**
   * This method is called when the user hits the [Esc] key.
   * The idea is for you to override this and implement your own
   * functionality in your subclass.
   *
   * @param e KeyEvent
   */
  public void escapeCommandPerformed(KeyEvent e)
  {
    this.setVisible(false);
  }

  protected void addKeyAndContainerListenerRecursively(Component c)
  {
    //Add KeyListener to the Component passed as an argument
    c.addKeyListener(this);

    //Check if the Component is a Container
    if (c instanceof Container) {

      //Component c is a Container. The following cast is safe.
      Container cont = (Container) c;

      //Add ContainerListener to the Container.
      cont.addContainerListener(this);

      //Get the Container's array of children Components.
      Component[] children = cont.getComponents();

      //For every child repeat the above operation.
      for (int i = 0; i < children.length; i++) {
        addKeyAndContainerListenerRecursively(children[i]);
      }
    }
  }

//The following function is the same as the function above with the exception that it does exactly the opposite - removes this Dialog
//from the listener lists of Components.

  protected void removeKeyAndContainerListenerRecursively(Component c)
  {
    c.removeKeyListener(this);
    if (c instanceof Container) {
      Container cont = (Container) c;
      cont.removeContainerListener(this);
      Component[] children = cont.getComponents();
      for (int i = 0; i < children.length; i++) {
        removeKeyAndContainerListenerRecursively(children[i]);
      }
    }
  }

  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      escapeCommandPerformed(e);
    }
    else if (keyCode == KeyEvent.VK_ENTER) {
      okCommandPerformed(e);
    }
  }

  public void keyReleased(KeyEvent e)
  {
  }

  public void keyTyped(KeyEvent e)
  {
  }

  public void componentAdded(ContainerEvent e)
  {
    addKeyAndContainerListenerRecursively(e.getChild());
  }

  public void componentRemoved(ContainerEvent e)
  {
    removeKeyAndContainerListenerRecursively(e.getChild());
  }

} // end of class



