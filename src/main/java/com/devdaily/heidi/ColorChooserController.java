package com.devdaily.heidi;

import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

public class ColorChooserController
{
  private Color oldColor;
  private Color newColor;

  private CurtainFrame desktopCurtainFrame;
  
  public ColorChooserController(CurtainFrame desktopCurtainFrame)
  {
    this.desktopCurtainFrame = desktopCurtainFrame;
  }

  /**
   * If you get a new Color back from this method, it's the 
   * color the user chose. Otherwise this will be null.
   */
  public Color doChangeColorAction()
  {
    JColorChooser chooser = new JColorChooser();
    int choice = showJColorChooserDialog("Select a Curtain Color", chooser, oldColor);
    
    // testing to see if this will help with the problem where i can't use [Command][R]
    // in the app after i hit [enter] on the color dialog; this only happens with the mac
    // app build, and not when i run inside eclipse.
    chooser.transferFocus();

    // if user clicked ok, get the new color; otherwise, ignore
    if (choice == JOptionPane.OK_OPTION)
    {
      Color c = chooser.getColor();
      oldColor = c;
      newColor = c;
      return newColor;
    }
    else
    {
      return null;
    }
  }

  private int showJColorChooserDialog(String title, JColorChooser colorChooser, Color defaultColor)
  {
    colorChooser.setColor(defaultColor);
    int choice = JOptionPane.showOptionDialog(null, 
        colorChooser,
        title, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE, 
        null, null, null);
    return choice;
  }
  
  public Color getNewColor()
  {
    return newColor;
  }

}

