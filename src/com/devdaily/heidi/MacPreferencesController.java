package com.devdaily.heidi;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.devdaily.logging.DDLoggerInterface;

/**
 * A controller for the Preferences dialog and related panels.
 */
public class MacPreferencesController
{
  
  private static final String DIALOG_TITLE = "Hyde Preferences";
  
  // logging
  DDLoggerInterface logger;

  private Hyde desktopCurtain;
  private CurtainFrame curtainFrame;
  
  private PreferencesDialog preferencesDialog;
  private JTabbedPane preferencesDialogTabbedPane;
  
  // sound files panels are different depending on circumstances
  private SoundFileController soundFileController;
  
  // the tabs in our preferences dialog
  private static final int SOUNDS_TAB_INDEX     = 0;
  private static final int COLORS_TAB_INDEX     = 1;
  private static final int ANIMATIONS_TAB_INDEX = 2;
  
  private static final int PREFS_DIALOG_WIDTH  = 700;
  private static final int PREFS_DIALOG_HEIGHT = 500;
  
  public MacPreferencesController(Hyde desktopCurtain, DDLoggerInterface logger)
  {
    this.desktopCurtain = desktopCurtain;
    this.curtainFrame = desktopCurtain.getDesktopCurtainFrame();
    this.logger = logger;
    this.soundFileController = new SoundFileController(this, logger);
  }
  
  // TODO i'm currently just using this to test how/where preferences are stored
  public Hyde getDesktopCurtain()
  {
    return desktopCurtain;
  }
  
  public void doHandlePreferencesEvent()
  {
    logger.logDebug("ENTERED PreferencesCtr::handlePreferencesEvent");
    makeSureCurtainFrameReferenceIsGood();

    // create dialog
    preferencesDialog = new PreferencesDialogWithEscSupport(curtainFrame);
    preferencesDialog.setTitle(DIALOG_TITLE);
    preferencesDialogTabbedPane = preferencesDialog.getTabbedPane();
    
    // add panels to dialog
    preferencesDialogTabbedPane.add("Sounds", soundFileController.getPreferencesSoundPanel());
   
    // TODO add Color and Animations panels
    
    // make dialog visible
    // TODO get the largest preferred size from our panels?
    preferencesDialog.setMinimumSize(new Dimension(PREFS_DIALOG_WIDTH, PREFS_DIALOG_HEIGHT));
    preferencesDialog.setPreferredSize(preferencesDialog.getMinimumSize());
    preferencesDialog.setModal(true);
    preferencesDialog.pack();
    preferencesDialog.setLocationRelativeTo(curtainFrame);
    preferencesDialog.setVisible(true);
    logger.logDebug("   PreferencesCtr::handlePreferencesEvent - Preferences dialog is now visible");

    desktopCurtain.giveFocusBackToCurtain();
  }
  
  public void setSoundPanel(JComponent newComponent)
  {
    logger.logDebug("ENTERED PreferencesCtr::setSoundPanel");
    preferencesDialogTabbedPane.setComponentAt(SOUNDS_TAB_INDEX, newComponent);
    preferencesDialogTabbedPane.validate();
  }
  
  public PreferencesDialog getPreferencesDialog()
  {
    return this.preferencesDialog;
  }

  private void makeSureCurtainFrameReferenceIsGood()
  {
    if (curtainFrame == null)
    {
      this.curtainFrame = desktopCurtain.getDesktopCurtainFrame();
    }
  }

  public String getSoundFileDirectory()
  {
    return desktopCurtain.getSoundFileDirectory();
  }

  public SoundFileController getSoundFileController()
  {
    return this.soundFileController;
  }
  
}





