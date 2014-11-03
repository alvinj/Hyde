package com.devdaily.heidi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import com.devdaily.logging.DDLoggerInterface;

public class CurtainFrame extends JFrame
{
  // logging
  DDLoggerInterface logger;

  private final CurtainFrame thisFrame;
  private JComponent component;
  private ColorChooserController colorChooserController;

  // our actions
  private Action fillWindowAction;
  private Action chooseColorAction;
  private Action doLicenseAction;
  
  private KeyStroke fillWindowKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.META_MASK);
  private KeyStroke chooseColorKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.META_MASK);
  private KeyStroke doLicenseActionKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.META_MASK);
  
  private Hyde mainController;
  private Color currentColor;
  
  public CurtainFrame(Hyde hyde, JComponent component, Color defaultColor, DDLoggerInterface logger)
  {
    this.mainController = hyde;
    this.component = component;
    this.logger = logger;
    thisFrame = this;
    this.setResizable(false);
    this.setUndecorated(true);
    
    // TODO this is a little mucked up, having this frame talk to a controller like this
    this.currentColor = defaultColor;
    colorChooserController = new ColorChooserController(this);    
    setAllColors(component, currentColor);

    this.getContentPane().add(component);
    
    addActions();
    this.setJMenuBar(createMenuBar());
  }

  /**
   * Not strictly necessary yet, but put here in case I want to let the user
   * choose their own background colors.
   */
  private void setAllColors(JComponent component, Color color)
  {
    this.getContentPane().setBackground(color);
    component.setBackground(color);
    this.setBackground(color);
  }

  private void addActions()
  {
    fillWindowAction = new FillWindowAction(this, "Re-Fill Window", fillWindowKeystroke);
    component.getInputMap().put(fillWindowKeystroke, "fillWindow");
    component.getActionMap().put("fillWindow", fillWindowAction);

    chooseColorAction = new ChooseColorAction(this, "Choose Color", chooseColorKeystroke);
    component.getInputMap().put(chooseColorKeystroke, "chooseColor");
    component.getActionMap().put("chooseColor", chooseColorAction);

    doLicenseAction = new DoLicenseAction(this, "Install License", doLicenseActionKeystroke);
    component.getInputMap().put(doLicenseActionKeystroke, "installLicense");
    component.getActionMap().put("installLicense", doLicenseAction);
  }
  
  private JMenuBar createMenuBar()
  {
    // create the menubar
    JMenuBar menuBar = new JMenuBar();

    // create menu
    JMenu actionsMenu = new JMenu("Actions");
    
    // create menu items
    JMenuItem fillWindowMenuItem = new JMenuItem(fillWindowAction);
    JMenuItem chooseColorMenuItem = new JMenuItem(chooseColorAction);
    JMenuItem installLicenseMenuItem = new JMenuItem(doLicenseAction);

    // add items to menu
    actionsMenu.add(fillWindowMenuItem);
    actionsMenu.add(chooseColorMenuItem);
    actionsMenu.add(installLicenseMenuItem);

    // add the menus to the menubar
    menuBar.add(actionsMenu);

    return menuBar;
  }
  
  void doRefillScreenAction()
  {
    // hide the frame
    this.hideShield();
    
    // re-get the screen size, re-size the frame, re-display the frame;
    // all of these happen in this method
    this.display();
  }

  public void display()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        thisFrame.setSize(dim);
        int width = dim.width;
        
        // TODO - working here
        mainController.playShowCurtainSound();

        thisFrame.setLocation(-width, 0);
        thisFrame.setVisible(true);
        int x = -width;
        int xCurr = x;
        int jump = 0;
        while (xCurr < -10)
        {
          thisFrame.setLocation(xCurr, 0);
          jump = (0 - xCurr) / 15;
          if (jump < 10)
            jump = 10;
          xCurr = xCurr + jump;
          if (Math.abs(xCurr) < 10)
            xCurr = 0;
          try
          {
            // kludge - slow it down for my fast pc
            // need a better way to do this
            Thread.sleep(5);
          }
          catch (InterruptedException e)
          {
            // TODO log this?
          }
        }
        thisFrame.setLocation(0, 0);
        thisFrame.transferFocus();
      }
    });
  }

  /**
   * Call this method to reposition the frame back to (0,0).
   */
  public void moveCurtainBackToZeroZero()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        
        // figure out where we are
        Point currentLocation = thisFrame.getLocationOnScreen();
        int initialX = currentLocation.x;
        int initialY = currentLocation.y;
        
        // we want to move to (0,0)
        int x0 = 0;
        int y0 = 0;
        
        int distanceX = initialX - x0;
        int distanceY = initialY - y0;
        
        // distance per step; assume 10 steps for animation
        int numSteps = 10;
        int xStep = distanceX / numSteps;
        int yStep = distanceY / numSteps;
        
        // move the frame from the current position to the (0,0) position
        int xPrev = initialX;
        int yPrev = initialY;
        
        // play a sound file
        mainController.playRefillScreenSound();

        // the frame-moving happens here
        for (int i=0; i<numSteps; i++)
        {
          xPrev = xPrev - xStep;
          yPrev = yPrev - yStep;
          thisFrame.setLocation(xPrev, yPrev);
          sleep(50);
        }
        thisFrame.setLocation(0, 0);
        thisFrame.transferFocus();
      }
    });
  }
  
  private void sleep(long sleepTime)
  {
    try
    {
      // kludge - slow it down for my faster imac;
      // need a better way to do this
      Thread.sleep(sleepTime);
    }
    catch (InterruptedException e)
    {
      // TODO bother to log this?
    }
  }
  
  public void doQuitAnimationAndQuit()
  {

    final int width = this.getWidth();
    final int height = this.getHeight();

    int finalWidth = 30;
    int finalHeight = 20;
    int numSteps = 50;
    int widthStepSize = (width-finalWidth) / numSteps;
    int heightStepSize = (height-finalHeight) /numSteps;
    int lastWidth = width;
    int lastHeight = height;

    // play appropriate sound
    mainController.playAppShutdownSound();
    
    // TODO it's just a stroke of timing that the app doesn't quit before the sound file
    //      is played; fix this by joining the soundfile animation before quitting.
    //      this does not seem to be a huge problem when the animation plays, but it 
    //      is a bug.
    
    for (int i=0; i< numSteps; i++)
    {
      lastWidth = lastWidth - widthStepSize;
      lastHeight = lastHeight - heightStepSize;
      thisFrame.setSize(lastWidth, lastHeight);
      int xSpaceOpen = width - lastWidth;
      int ySpaceOpen = height - lastHeight;
      int xLoc = xSpaceOpen / 2;
      int yLoc = ySpaceOpen / 2;
      thisFrame.setLocation(xLoc, yLoc);
    }

    sleep(20);
    System.exit(0);
  }

  public void hideShield()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        int width = dim.width;
        int xFinal = -width;
        int xCurr = 0;
        int jump = 0;
        while (xCurr > (xFinal))
        {
          // System.err.println("xCurr = " + xCurr);
          thisFrame.setLocation(xCurr, 0);
          jump = (Math.abs(xFinal) - Math.abs(xCurr)) / 20;
          // System.err.println("jump = " + jump);
          if (jump < 10)
            jump = 10;
          xCurr = xCurr - jump;
        }
        thisFrame.setVisible(false);
      }
    });
  }

  /**
   * Just reposition the window back to the current corner as fast as possible,
   * no fancy animation.
   */
  public void doQuickRepositionAction()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        thisFrame.setSize(dim);
        thisFrame.setLocation(0,0);
      }
    });
  }

  public void doChooseColorAction()
  {
    final CurtainFrame curtain = this;
    
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        Color newColor = colorChooserController.doChangeColorAction();
        if (newColor != null)
        {
          animateToNewColor(newColor);

          // make sure we get to the user's final desired color
          setAllColors(component, newColor);
          
          // color changed, update the preferences
          mainController.updateCurrentColor(newColor);

          // housekeeping
          currentColor = newColor;
        }
        // need to do this in both cases (user selects new color, or cancels dialog)
        curtain.transferFocus();
      }

      private void animateToNewColor(Color newColor)
      {
        int numSteps = 25;
        
        float[] oldCC = new float[4];
        oldCC = currentColor.getComponents(oldCC);
        
        float oldR = oldCC[0];
        float oldG = oldCC[1];
        float oldB = oldCC[2];
        float oldA = oldCC[3];
        
        float[] newCC = new float[4];
        newCC = newColor.getComponents(newCC);
        
        float newR = newCC[0];
        float newG = newCC[1];
        float newB = newCC[2];
        float newA = newCC[3];
        
        float rDelta = newR - oldR;
        float gDelta = newG - oldG;
        float bDelta = newB - oldB;
        float aDelta = newA - oldA;
        
        float rStep = rDelta / numSteps;
        float gStep = gDelta / numSteps;
        float bStep = bDelta / numSteps;
        float aStep = aDelta / numSteps;
        
        float lastR = oldR;
        float lastG = oldG;
        float lastB = oldB;
        float lastA = oldA;

        // for debugging
//        float[] rArr = new float[numSteps];
//        float[] gArr = new float[numSteps];
//        float[] bArr = new float[numSteps];
//        float[] aArr = new float[numSteps];
        
        for (int i=0; i<numSteps; i++)
        {
          lastR = lastR + rStep;
          lastG = lastG + gStep;
          lastB = lastB + bStep;
          lastA = lastA + aStep;
          
//          rArr[i] = lastR;
//          gArr[i] = lastG;
//          bArr[i] = lastB;
//          aArr[i] = lastA;
          
          if (lastR < 0f) lastR = 0f;
          if (lastG < 0f) lastG = 0f;
          if (lastB < 0f) lastB = 0f;
          if (lastA < 0f) lastA = 0f;
          if (lastR > 1f) lastR = 1f;
          if (lastG > 1f) lastG = 1f;
          if (lastB > 1f) lastB = 1f;
          if (lastA > 1f) lastA = 1f;
            
          Color tempColor = new Color(lastR, lastG, lastB, lastA);
          //Color tempColor = new Color(lastR, lastG, lastB);

          setAllColors(component, tempColor);
          sleep(30);
        }
      }
    });
  }
  
  
  /**
   * A lame pass-thru method.
   */
  public void doInstallLicenseAction()
  {
    mainController.doInstallLicenseAction();
  }
}














