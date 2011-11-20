package com.devdaily.heidi;

import java.awt.Color;

import java.awt.FileDialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import ch.randelshofer.quaqua.QuaquaManager;
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.devdaily.logging.DDLoggerInterface;
import com.devdaily.logging.DDSimpleLogger;
import de.schlichtherle.util.ObfuscatedString;

public class Hyde implements LicenseableClass
{
  private CurtainFrame desktopCurtainFrame;
  private static final String APP_NAME = "Hyde";
  private JPanel curtainPanel = new JPanel();
  
  private static final String DC_LICENSING_URL = "http://devdaily.com/hide-your-desktop";
  
  private static final String ABOUT_DIALOG_MESSAGE = "<html><center><p>Hyde, Version 1.1.0</p></center>\n\n"
    + "<center><p><a href=\"http://devdaily.com/hide-your-desktop\">http://devdaily.com/hide-your-desktop</a></p><center>\n";

  // "please license" dialog stuff
  private static final String LICENSE_DIALOG_TITLE = "devdaily.com - Hyde";
  private LicenseController licenseController;
  private LicenseReminderDialog licenseReminderDialog;
  private int numTimesUsed;
  private ApplyLicenseDialog applyLicenseDialog;
  
  private boolean licenseWasSuccessfullyApplied;
    
  // license stuff
  private static String keystoreFilename = "this should not matter any more";
  private static String alias = "this should not matter any more";
  private static String publicCertStorePassword = "this should not matter any more";
  private static String cipherParamPassword = "this should not matter any more";

  // ftp license stuff
  private static String ftpAlias = "this should not matter any more";
  private static String ftpKeyPwd = "this should not matter any more";

  // prompt for this
  private String licenseFilename;

  // so we can share our events with other controllers
  public static final int SHOW_CURTAIN_EVENT   = 1;
  public static final int QUIT_CURTAIN_EVENT   = 2;
  public static final int REFILL_CURTAIN_EVENT = 3;
  
  // preferences stuff
  private MacPreferencesController preferencesController;
  private Preferences preferences;
  private static String CURTAIN_R = "CURTAIN_R";
  private static String CURTAIN_G = "CURTAIN_G";
  private static String CURTAIN_B = "CURTAIN_B";
  private static String CURTAIN_A = "CURTAIN_A";
  private Color currentColor;
  
  // prefs - how many times the app has been started.
  // (changed this to a one-letter name b/c it shows up as text in the plist file)
  private static String NUM_USES_PREF = "C";
  
  // sounds
  public static final String CURTAIN_DIR_NAME = ".devdaily-hyde";
  String curtainHomeDirectory;
  String soundFileDirectory;
  private SoundFileController soundFileController;

  // TODO move this to a properties file or class
  private static final String FILE_PATH_SEPARATOR = System.getProperty("file.separator");
  private static final String USER_HOME_DIR = System.getProperty("user.home");

  private static final String RELATIVE_SOUNDS_DIR_NAME = "Sounds";
  private static final String homeLibraryApplicationSupportDirname = "Library/Application Support/DevDaily/Hyde";
  public  static final String CANON_SOUNDS_DIR = USER_HOME_DIR + FILE_PATH_SEPARATOR + homeLibraryApplicationSupportDirname 
                                               + FILE_PATH_SEPARATOR + RELATIVE_SOUNDS_DIR_NAME;

  // ------------------------------ LOGGING ---------------------------------//
  // TODO move this to a properties file or class (though USER_HOME_DIR is dynamic)
  //      these also vary by windows/mac
  private static final String REL_LOGFILE_DIRECTORY = "Library/Logs/DevDaily/Hyde";
  private static final String CANON_LOGFILE_DIR     = USER_HOME_DIR + FILE_PATH_SEPARATOR + REL_LOGFILE_DIRECTORY; 
  private static final String CANON_DEBUG_FILENAME  = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "Hyde.log";
  
  private static final int DEFAULT_LOG_LEVEL = DDLoggerInterface.LOG_WARNING;
  
  // user can "touch" these files to force logging at these levels (only checks at startup)
  private static final String DEBUG_LOG_FILENAME   = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "DEBUG";
  private static final String WARNING_LOG_FILENAME = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "WARNING";
  private static final String ERROR_LOG_FILENAME   = CANON_LOGFILE_DIR + FILE_PATH_SEPARATOR + "ERROR";


  DDSimpleLogger logger;
  
  public static void main(String[] args)
  {
    new Hyde();
  }

  public Hyde()
  {
    dieIfNotRunningOnMacOsX();
    
    configureForMacOSX();
    
    connectToLogfile();
    
    // get default color from preferences
    connectToPreferences();
    getDefaultColor();
    
    // need this to verify and install a license
    licenseController = new LicenseController(this, logger);
    
    // handle preferences
    preferencesController = new MacPreferencesController(this, logger);
    soundFileController = preferencesController.getSoundFileController();
    soundFileDirectory = CANON_SOUNDS_DIR;
    
    // create ~/.desktopcurtain, ~/.desktopcurtain/Sounds
    createDesktopCurtainHomeDirsIfNeeded();
    
    desktopCurtainFrame = new CurtainFrame(this, curtainPanel, currentColor, logger);
    DragWindowListener mml = new DragWindowListener(curtainPanel);
    curtainPanel.addMouseListener(mml);
    curtainPanel.addMouseMotionListener(mml);

    // display the jframe
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          // did this to get the quaqua jcolorchooser, which looks much more
          // mac-like; if it creates a problem, switch back
          UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        }
        catch (Exception e)
        {
          // TODO log this? where does system err output on mac os x go?
        }
        
        // display the curtain
        playShowCurtainSound();
        desktopCurtainFrame.display();
      }
    });
    
    // verify the license.
    // this makes a callback to handleVerifyLicenseFailedEvent() if the verification fails.
    // commenting this out on August 5, 2010. App is now free.
    //licenseController.verifyLicense();
    this.giveFocusBackToCurtain();
  }
  
  /**
   * If the app is not running on mac os x, die right away.
   */
  private void dieIfNotRunningOnMacOsX()
  {
    boolean mrjVersionExists = System.getProperty("mrj.version") != null;
    boolean osNameExists = System.getProperty("os.name").startsWith("Mac OS");
    
    if ( !mrjVersionExists || !osNameExists)
    {
      System.err.println("Not running on a Mac OS X system.");
      System.exit(1);
    }
  }
  
  /**
   * An event here should be SHOW_CURTAIN_EVENT, QUIT_CURTAIN_EVENT, or REFILL_CURTAIN_EVENT.
   */
  public void playSoundForEvent(int desktopCurtainEvent)
  {
    if (soundFileController == null)
    {
      soundFileController = preferencesController.getSoundFileController();
    }
    
    soundFileController.playSoundForEvent(desktopCurtainEvent);
  }

  /**
   * Returns true if a license is properly installed.
   */
  private boolean productIsLicensed()
  {
    System.err.println("productIsLicensed was called");
    boolean b = licenseController.verifyLicense();
    this.giveFocusBackToCurtain();
    return b;
  }

  /**
   * Display the dialog to "nag" the user to license this app.
   */
//  void displayPleaseLicenseDialog(final boolean useRandomLocation)
//  {
//    String licenseText = "<p><font face=\"Monaco\" size=\"-1\">Thank you for using the Desktop Curtain application.</font></p>"
//      + "<p><font face=\"Monaco\" size=\"-1\">This application has been used " + numTimesUsed + " times.</font></p>"
//      + "<p><font face=\"Monaco\" size=\"-1\">The Desktop Curtain is available as \"donation ware\"."
//      + "<br>Please donate $0.99 to our PayPal account to"
//      + "<br>help support our development costs, and we'll provide"
//      + "<br>a license key to disable this periodic message.</font></p>"
//      + "<p><font face=\"Monaco\" size=\"-1\">License URL: <a href=\"http://devdaily.com/desktopcurtain/license\">Get a license now</a></font></p>";
//    licenseReminderDialog = new LicenseReminderDialog(desktopCurtainFrame);
//    JButton closeButton = licenseReminderDialog.getCloseButton();
//    closeButton.addActionListener(new ActionListener()
//    {
//      public void actionPerformed(ActionEvent arg0)
//      {
//        licenseReminderDialog.setVisible(false);
//      }
//    });
//    
//    licenseReminderDialog.getHtmlMessageArea().addHyperlinkListener(new HyperlinkListener() {
//      public void hyperlinkUpdate(HyperlinkEvent hev) 
//      {
//        if (hev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
//        {
//          Runtime runtime = Runtime.getRuntime();
//          String[] args = { "osascript", "-e", "open location \"http://devdaily.com/software/desktopcurtain/license\"" };
//          try
//          {
//            Process process = runtime.exec(args);
//          }
//          catch (IOException e)
//          {
//            // ignore this
//          }
//        }
//      }});
//    
//    licenseReminderDialog.setModal(true);
//    licenseReminderDialog.setTitle(LICENSE_DIALOG_TITLE);
//    licenseReminderDialog.getHtmlMessageArea().setText(licenseText);
//    licenseReminderDialog.setPreferredSize(new Dimension(480, 310));
//    licenseReminderDialog.setMinimumSize(new Dimension(480, 310));
//    licenseReminderDialog.pack();
//    licenseReminderDialog.setLocationRelativeTo(desktopCurtainFrame);
//
//    if (useRandomLocation)
//    {
//      // i'm doing this for users that have used the application more than
//      // fifty times, but who have chosen not to license it.
//      Random random = new Random();
//      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//      Dimension dialogSize = licenseReminderDialog.getSize();
//      int maxX = screenSize.width - dialogSize.width - 20;
//      int maxY = screenSize.height - dialogSize.height -20;
//      int randomX = random.nextInt(maxX);
//      int randomY = random.nextInt(maxY);
//      // make sure it's not at (0,0)
//      licenseReminderDialog.setLocation(randomX+20, randomY+20);
//    }
//
//    licenseReminderDialog.setVisible(true);
//  }
  
  /**
   * Do everything we need to configure the app for Mac OS X systems.
   */
  private void configureForMacOSX()
  {
    // set some mac-specific properties; helps when i don't use ant to build the code
    System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);

    // create an instance of the Mac Application class, so i can handle the 
    // mac quit event with the Mac ApplicationAdapter
    Application macApplication = Application.getApplication();
    MyApplicationAdapter macAdapter = new MyApplicationAdapter(this);
    macApplication.addApplicationListener(macAdapter);
    
    // must enable the preferences option manually
    macApplication.setEnabledPreferencesMenu(true);
    
    // use these quaqua components
    Set includes = new HashSet();
    includes.add("ColorChooser");
    //includes.add("Table");
    QuaquaManager.setIncludedUIs(includes);
    
    // this did not work to get tables striped
    //UIManager.put("Table.alternateRowColor", UIManager.getColor("Table.focusCellForeground"));

  }
  
  private int getAndUpdateUsageCounter()
  {
    // get the number of times the app has been accessed already
    int numUses = preferences.getInt(NUM_USES_PREF, 0);
    
    // update the number of uses by 1
    preferences.putInt(NUM_USES_PREF, numUses+1);
    
    return numUses;
  }

  private void connectToPreferences()
  {
    preferences = Preferences.userNodeForPackage(this.getClass());
  }
  
  /**
   * Get the default color from the Preferences, or default to black.
   */
  private void getDefaultColor()
  {
    int r = preferences.getInt(CURTAIN_R, 0);
    int g = preferences.getInt(CURTAIN_G, 0);
    int b = preferences.getInt(CURTAIN_B, 0);
    int a = preferences.getInt(CURTAIN_A, 255);
    currentColor = new Color(r,g,b,a);
  }
  
  public void updateCurrentColor(Color newColor)
  {
    currentColor = newColor;

    // update the preferences
    preferences.putInt(CURTAIN_R, currentColor.getRed());
    preferences.putInt(CURTAIN_G, currentColor.getGreen());
    preferences.putInt(CURTAIN_B, currentColor.getBlue());
    preferences.putInt(CURTAIN_A, currentColor.getAlpha());
  }
  
  public Color getCurrentColor()
  {
    return currentColor;
  }

  public String getAlias()
  {
    return alias;
  }

  public String getFtpKeyPwd()
  {
    return ftpKeyPwd;
  }
  
  public String getFtpAlias()
  {
    return ftpAlias;
  }

  public String getCipherParamPassword()
  {
    return cipherParamPassword;
  }

  public Class getClassToLicense()
  {
    return this.getClass();
  }

  public String getPublicKeystorePassword()
  {
    return publicCertStorePassword;
  }

  public String getApplicationName()
  {
    return APP_NAME;
  }

  public InputStream getPublicKeystoreAsInputStream() throws FileNotFoundException
  {
    final String resourceName = keystoreFilename;
    final InputStream in = getClass().getResourceAsStream(resourceName);
    if (in == null)
    {
      throw new FileNotFoundException(resourceName);
    }
    return in;
  }
  
  public void doQuitAction()
  {
    desktopCurtainFrame.doQuitAnimationAndQuit();
  }
  
  public void playShowCurtainSound()
  {
    playSoundForEvent(SHOW_CURTAIN_EVENT);
  }
  
  public void playRefillScreenSound()
  {
    playSoundForEvent(REFILL_CURTAIN_EVENT);
  }
  
  public void playAppShutdownSound()
  {
    playSoundForEvent(QUIT_CURTAIN_EVENT);
  }
  
//  private void playSound(String soundfileName)
//  {
//    try
//    {
//      ClassLoader CLDR = this.getClass().getClassLoader();
//      InputStream inputStream = CLDR.getResourceAsStream("com/devdaily/desktopcurtain/sounds/" + soundfileName);
//      AudioStream audioStream = new AudioStream(inputStream);
//      AudioPlayer.player.start(audioStream);
//
//      // other methods (not needed here; just kept here for reference)
//      //audioStream.close();
//      //AudioPlayer.player.stop(as);
//    }
//    catch (Exception e)
//    {
//      // log this
//    }
//  }
  
  public CurtainFrame getDesktopCurtainFrame()
  {
    return this.desktopCurtainFrame;
  }
  
  
  //---------------------------------------------------------------------------//
  //                              SOUND STUFF                                  //
  //---------------------------------------------------------------------------//
  
  public static boolean makeDirectory(String directoryName)
  {
    try
    {
      boolean result = (new File(directoryName)).mkdir();
      return result;
    }
    catch (RuntimeException re)
    {
      // ignore exception
      return false;
    }
  }

  // TODO this was another late addition, primarily to support creation of the
  //      new Sounds folder location
  public static boolean makeDirectories(String directoryName)
  {
    try
    {
      boolean result = (new File(directoryName)).mkdirs();
      return result;
    }
    catch (RuntimeException re)
    {
      // ignore exception
      return false;
    }
  }

  private void createDesktopCurtainHomeDirsIfNeeded()
  {
    String homeDir = System.getProperty("user.home");
    String fileSeparator = System.getProperty("file.separator");
    curtainHomeDirectory = homeDir + fileSeparator + CURTAIN_DIR_NAME;
    makeDirectory(curtainHomeDirectory);
  }

  public String getCurrentHomeDirectory()
  {
    return this.curtainHomeDirectory;
  }
  
  public String getSoundFileDirectory()
  {
    return this.soundFileDirectory;
  }


  //---------------------------------------------------------------------------//
  //                          LICENSING STUFF                                  //
  //---------------------------------------------------------------------------//
  
  private class ApplyLicenseActionListener implements ActionListener
  {
    /**
     * "Apply" button was clicked, so get the filename from the textfield and 
     * see if we can apply it as a license
     */
    public void actionPerformed(ActionEvent e)
    {
      // get the filename
      licenseFilename = applyLicenseDialog.getFileTextField().getText();
      if (licenseFilename == null || licenseFilename.trim().equals(""))
      {
        JOptionPane.showMessageDialog(desktopCurtainFrame, 
            "Sorry, it does not look like a file was selected.",
            "No Filename",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      // TODO should probably do this in a thread and indeterminate dialog.
      // got a filename. try to install it with the license controller.
      boolean result = licenseController.installLicense(licenseFilename);
      
      // let the user know it went good or bad
      if (result)
      {

        // the license was applied successfully; show the user their information
        licenseWasSuccessfullyApplied = true;
        
        String message = "The devdaily.com Hyde license was installed -- thank you!\n\n"
          + "License holder information:\n\n  (" + licenseController.getLicenseContent().getHolder().getName() + ")\n"; 
        
        JTextArea textArea = new JTextArea(8, 31);
        textArea.setText(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(desktopCurtainFrame,
            scrollPane,
            "License Installed",
            JOptionPane.INFORMATION_MESSAGE);
        // also close the apply dialog
        applyLicenseDialog.setVisible(false);
        applyLicenseDialog.dispose();
      }
      else
      {
        String message = "I'm sorry, there was a problem applying the license. "
          + "Please contact devdaily@gmail.com for assistance.\n\n"
          + "A system error message should be shown below here:\n\n"
          + licenseController.getErrorMessage();
        
        JTextArea textArea = new JTextArea(10, 35);
        textArea.setText(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // display them in a message dialog
        JOptionPane.showMessageDialog(desktopCurtainFrame, 
            scrollPane,
            "Problem Installing License",
            JOptionPane.WARNING_MESSAGE);
      }

      // always need to do this after dialog calls until i find out how to do this right
      giveFocusBackToCurtain();

    } 
  }
  
  private class BrowseForLicenseFileActionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      // browser button was clicked, show a FileDialog to prompt for a filename
      String canonicalFilename = promptForFilenameWithFileDialog(desktopCurtainFrame, "Select Your License File", null, "*.lic");
      
      // if the name isn't null, put it in the textfield
      if (canonicalFilename != null)
      {
        applyLicenseDialog.getFileTextField().setText(canonicalFilename);
      }
      
      // that's all we do
    }
  }

  /**
   * TODO If I port this to Windows, I need to use the different file chooser over there.
   * TODO Also, see if Quaqua has a file chooser to use instead.
   * 
   * @param frame - parent frame
   * @param dialogTitle - dialog title
   * @param defaultDirectory - default directory
   * @param fileType - something like "*.lic"
   * @return Returns null if the user selected nothing, otherwise returns the canonical filename (directory + fileSep + filename).
   */
  String promptForFilenameWithFileDialog (Frame frame, String dialogTitle, String defaultDirectory, String fileType) 
  {
    FileDialog fd = new FileDialog(frame, dialogTitle, FileDialog.LOAD);
    fd.setFile(fileType);
    fd.setDirectory(defaultDirectory);
    fd.setLocationRelativeTo(frame);
    fd.setVisible(true);
    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (directory == null || filename == null || directory.trim().equals("") || filename.trim().equals(""))
    {
      return null;
    }
    else
    {
      // this was not needed on mac os x:
      //return directory + System.getProperty("file.separator") + filename;
      return directory + filename;
    }
  }
  
  private void openUrlInBrowser(String url)
  {
    Runtime runtime = Runtime.getRuntime();
    String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
    try
    {
      Process process = runtime.exec(args);
    }
    catch (IOException e)
    {
      // ignore this
    }
  }
  
  /**
   * This process is invoked by the system when the FTP license
   * period has expired. We need to show a slightly-modified version of
   * the ApplyLicenseDialog, and we need to shut down the application if
   * the user does not apply a valid license.
   */
  public void handleVerifyLicenseFailedEvent()
  {

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        // create the dialog
        applyLicenseDialog = new ApplyLicenseDialog(desktopCurtainFrame);
        
        // need to add this to catch the event where the user presses the 
        // red close event on our dialog; in that case, we need to quit the app
        // if a license has not been successfully applied.
        // TODO this was another late patch, and this code can probably be consolidated
        //      with the Quit button code.
        applyLicenseDialog.addWindowListener(new WindowAdapter() 
        {
          public void windowClosed(WindowEvent e)
          {
            if (!licenseWasSuccessfullyApplied)
            {
              doQuitAction();
            }
          }
          public void windowClosing(WindowEvent e)
          {
            if (!licenseWasSuccessfullyApplied)
            {
              doQuitAction();
            }
          }
        });
        
        // change text to let user know the trial period is over
        applyLicenseDialog.getHeaderHelpText().setText("The free trial period has ended. Please install a license to continue.");
        
        // change the cancel button to say "quit"
        JButton forcedQuitButton = applyLicenseDialog.getCancelButton();
        forcedQuitButton.setText("Quit");
        forcedQuitButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            // shut down the app if the user selects this button at this time
            applyLicenseDialog.setVisible(false);
            doQuitAction();
          } 
        });

        // TODO this code is copied and pasted from the doInstallLicenseAction method
        DCJLinkLabel linkLabel = applyLicenseDialog.getNeedALicenseLabel();
        linkLabel.addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent arg0)
          {
            // take the user to the proper url
            openUrlInBrowser(DC_LICENSING_URL);
          }
        });
        
        // add a listener to the Browser button
        JButton browseForLicenseFileButton = applyLicenseDialog.getBrowseButton();
        browseForLicenseFileButton.addActionListener(new BrowseForLicenseFileActionListener());
           
        // add a complicated listener to the Apply button
        JButton applyLicenseButton = applyLicenseDialog.getApplyLicenseButton();
        applyLicenseButton.addActionListener(new ApplyLicenseActionListener());
        
        // set the dialog visible, and leave it to the listeners to handle the action
        applyLicenseDialog.setResizable(false);
        applyLicenseDialog.pack();
        applyLicenseDialog.setModal(true);
        applyLicenseDialog.setLocationRelativeTo(desktopCurtainFrame);
        applyLicenseDialog.setVisible(true);  
        
      }
    });
  }
  
  /**
   * Come here when the user selects the "Apply License" menu item.
   */
  public void doInstallLicenseAction()
  {
    // prompt the user for the license file
    applyLicenseDialog = new ApplyLicenseDialog(desktopCurtainFrame);

    // TODO do this much better; just added as a quick go-live fix
    // TODO modified the ApplyLicenseDialog manually to add a JLinkLabel ***
    DCJLinkLabel linkLabel = applyLicenseDialog.getNeedALicenseLabel();
    linkLabel.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent arg0)
      {
        // take the user to the proper url
        openUrlInBrowser(DC_LICENSING_URL);
      }
    });
    
    // add a listener to the Browser button
    JButton browseForLicenseFileButton = applyLicenseDialog.getBrowseButton();
    browseForLicenseFileButton.addActionListener(new BrowseForLicenseFileActionListener());
       
    // add a complicated listener to the Apply button
    JButton applyLicenseButton = applyLicenseDialog.getApplyLicenseButton();
    applyLicenseButton.addActionListener(new ApplyLicenseActionListener());

    // add a simple listener to the Cancel button
    JButton cancelButton = applyLicenseDialog.getCancelButton();
    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        applyLicenseDialog.setVisible(false);
      } 
    });
    
    // set the dialog visible, and leave it to the listeners to handle the action
    applyLicenseDialog.pack();
    applyLicenseDialog.setModal(true);
    applyLicenseDialog.setLocationRelativeTo(desktopCurtainFrame);
    applyLicenseDialog.setVisible(true);
  }

  public void doPreferencesAction()
  {
    preferencesController.doHandlePreferencesEvent();
  }

  /**
   * A simple method others can use to return focus back to the curtain
   * after they have done something like displaying a dialog.
   */
  public void giveFocusBackToCurtain()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        desktopCurtainFrame.transferFocus();
      }
    });
  }

  public boolean licenseWasSuccessfullyApplied()
  {
    return licenseWasSuccessfullyApplied;
  }

  public void setLicenseWasSuccessfullyApplied(boolean licenseWasSuccessfullyApplied)
  {
    this.licenseWasSuccessfullyApplied = licenseWasSuccessfullyApplied;
  }
  
  public void handleAboutAction()
  {
    // create an html editor/renderer
    JEditorPane editor = new JEditorPane();
    editor.setContentType("text/html");
    editor.setEditable(false);
    editor.setSize(new Dimension(400,300));
    editor.setFont(UIManager.getFont("EditorPane.font"));
    // note: had to include this line to get it to use my font
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setMargin(new Insets(5,15,25,15));
    editor.setText(ABOUT_DIALOG_MESSAGE);
    editor.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(editor);

    // add the hyperlink listener so the user can click my link
    // and go right to the website
    editor.addHyperlinkListener(new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent hev) 
    {
      if (hev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
      {
        Runtime runtime = Runtime.getRuntime();
        String[] args = { "osascript", "-e", "open location \"" + DC_LICENSING_URL + "\"" };
        try
        {
          Process process = runtime.exec(args);
        }
        catch (IOException e)
        {
          // ignore this
        }
      }
    }});

    // display our message
    JOptionPane.showMessageDialog(desktopCurtainFrame, scrollPane,
        "About Hyde", JOptionPane.INFORMATION_MESSAGE);
  }

  //---------------------------------- logging ------------------------------------

  private void connectToLogfile()
  {
    int currentLoggingLevel = DEFAULT_LOG_LEVEL;
    
    File errorFile = new File(ERROR_LOG_FILENAME);
    File warningFile = new File(WARNING_LOG_FILENAME);
    File debugFile = new File(DEBUG_LOG_FILENAME);
    
    // order of checks is important; want to go with more granular if multiple files exist
    if (errorFile.exists()) currentLoggingLevel = DDLoggerInterface.LOG_ERROR;
    if (warningFile.exists()) currentLoggingLevel = DDLoggerInterface.LOG_WARNING;
    if (debugFile.exists()) currentLoggingLevel = DDLoggerInterface.LOG_DEBUG;
    
    System.err.println("Hyde debug file: " + CANON_DEBUG_FILENAME);
    logger = new DDSimpleLogger(CANON_DEBUG_FILENAME, currentLoggingLevel, true, true);
    
  }

}









