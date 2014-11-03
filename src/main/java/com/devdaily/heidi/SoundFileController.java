package com.devdaily.heidi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import com.devdaily.logging.DDLoggerInterface;

/**
 * 
 * TODO I will probably need to store the preferences for this class under the
 * main class. TODO Get sound selection stuff working. TODO Get sound-testing
 * stuff working. TODO Full soup to nuts test. TODO Give to Kim to test.
 * 
 */
public class SoundFileController implements TableModelListener
{
  // logging
  DDLoggerInterface logger;
  
  // main controller
  private Hyde desktopCurtain;
  private MacPreferencesController macPreferencesController;

  // our gui widgets
  private PreferencesSoundPanel preferencesSoundPanel;
  private NoSoundFilesPanel noSoundFilesPanel;
  private static final int SOUNDS_TABLE_WIDTH = 600;
  private static final int SOUNDS_TABLE_HEIGHT = 450;

  // where apple keeps their sound files
  private static final String APPLE_SOUND_FILE_ROOT_DIR = "/Library/Audio/Apple Loops/Apple/iLife Sound Effects";

  // where i'm going to copy the sound files to
  // private String desktopCurtainSoundfileDirectory;
  private static final int ILIFE_DIR_DOESNT_EXIST = 1;
  private static final int ILIFE_HAD_PROBLEM_COPYING_FILES = 2;
  private static final int ILIFE_FILES_COPIED_FINE = 3;

  // keep the filename or description?
  private String currentShowCurtainSoundFilename;
  private String currentQuitCurtainSoundFilename;
  private String currentRefillCurtainSoundFilename;

  // store selected sound filenames in java preferences
  private Preferences preferences;
  public static String SHOW_CURTAIN_SOUND_FILENAME   = "SHOW_CURTAIN_SOUND_FILENAME";
  public static String QUIT_CURTAIN_SOUND_FILENAME   = "QUIT_CURTAIN_SOUND_FILENAME";
  public static String REFILL_CURTAIN_SOUND_FILENAME = "REFILL_CURTAIN_SOUND_FILENAME";

  // the sound files we know
  List<SoundFile> currentSoundFiles;
  JTable soundsTable;
  SoundsTableModel soundsTableModel;

  // no need for a description here any more, they are generated dynamically
  private SoundFile[] appleSoundFiles =
  { new SoundFile("communication-engaged.aif", "Machines/Communication Engaged.aif"),
      new SoundFile("electricity-surge.aif", "Machines/Electricty Surge.aif"),
      new SoundFile("servo-movement-1.aif", "Machines/Servo Movement 01.aif"),
      new SoundFile("servo-movement-2.aif", "Machines/Servo Movement 02.aif"),
      new SoundFile("tape-rewinding-1.aif", "Machines/Tape Rewinding 01.aif"),
      new SoundFile("tape-rewinding-2.aif", "Machines/Tape Rewinding 02.aif"),
      new SoundFile("tape-rewinding-3.aif", "Machines/Tape Rewinding 03.aif"),
      new SoundFile("hydraulics-engaged.aif", "Machines/Hydraulics Engaged.aif"),
      new SoundFile("synth-singer-2.aif", "Stingers/Synth Zingers 02.aif"),
      new SoundFile("synth-singer-3.aif", "Stingers/Synth Zingers 03.aif"),
      new SoundFile("synth-singer-4.aif", "Stingers/Synth Zingers 04.aif"),
      new SoundFile("computer-data-1.aif", "Sci-Fi/Computer Data 01.aif"),
      new SoundFile("computer-data-2.aif", "Sci-Fi/Computer Data 02.aif"),
      new SoundFile("computer-data-3.aif", "Sci-Fi/Computer Data 03.aif"),
      new SoundFile("computer-data-4.aif", "Sci-Fi/Computer Data 04.aif"),
      new SoundFile("computer-data-5.aif", "Sci-Fi/Computer Data 05.aif"),
      new SoundFile("computer-data-6.aif", "Sci-Fi/Computer Data 06.aif"),
      new SoundFile("slamming-metal-lid.aif", "Booms/Slamming Metal Lid.aif"),
      new SoundFile("barn-door-close.aif", "Foley/Barn Door Close.aif"),
      new SoundFile("barn-door-open.aif", "Foley/Barn Door Open.aif"),
      new SoundFile("door-metal-squeak.aif", "Foley/Door Metal Squeak.aif"),
      new SoundFile("door-wood-squeak.aif", "Foley/Door Wood Squeak.aif"), };

  public SoundFileController(MacPreferencesController preferencesController, DDLoggerInterface logger)
  {
    this.macPreferencesController = preferencesController;
    this.logger = logger;
    connectToPreferences();
    populateCurrentSoundReferencesFromPreferences();
  }

  private void connectToPreferences()
  {
    // store our preferences w/ the main class so our class name can be obfuscated
    //preferences = Preferences.userNodeForPackage(macPreferencesController.getDesktopCurtain().getClass());
    preferences = Preferences.userNodeForPackage(Hyde.class);
  }

  /**
   * Get any previously selected sound filenames from our Preferences.
   */
  private void populateCurrentSoundReferencesFromPreferences()
  {
    logger.logDebug("ENTERED populateCurrentSoundReferencesFromPreferences()");
    // note - these may come back blank
    currentShowCurtainSoundFilename = getSoundFilenameFromPreferences(SHOW_CURTAIN_SOUND_FILENAME);
    currentQuitCurtainSoundFilename = getSoundFilenameFromPreferences(QUIT_CURTAIN_SOUND_FILENAME);
    currentRefillCurtainSoundFilename = getSoundFilenameFromPreferences(REFILL_CURTAIN_SOUND_FILENAME);
    logger.logDebug("   currentShowCurtainSoundFilename = " + currentShowCurtainSoundFilename);
    logger.logDebug("   currentQuitCurtainSoundFilename = " + currentQuitCurtainSoundFilename);
    logger.logDebug("   currentRefillCurtainSoundFilename = " + currentRefillCurtainSoundFilename);
  }

  // parameters is like "SHOW_CURTAIN_SOUND_FILENAME"
  // returns a filename like "star-trek-door-open.aif"
  public String getSoundFilenameFromPreferences(String preferencesKey)
  {
    return preferences.get(preferencesKey, "");
  }

  // parameters are like (SHOW_CURTAIN_SOUND_FILENAME, star-trek-door-open.aif)
  public void storeSelectedSoundFilenameInPreferences(String preferencesKey, String soundFilename)
  {
    // put it in preferences
    preferences.put(preferencesKey, soundFilename);

    // keep our current references in sync
    if (preferencesKey.equals(SHOW_CURTAIN_SOUND_FILENAME))
    {
      currentShowCurtainSoundFilename = soundFilename;
    }
    else if (preferencesKey.equals(QUIT_CURTAIN_SOUND_FILENAME))
    {
      currentQuitCurtainSoundFilename = soundFilename;
    }
    else if (preferencesKey.equals(REFILL_CURTAIN_SOUND_FILENAME))
    {
      currentRefillCurtainSoundFilename = soundFilename;
    }
  }

  public JPanel getPreferencesSoundPanel()
  {
    // the preferences should have the user's last sound selections
    populateCurrentSoundReferencesFromPreferences();

    if (soundFilesDirectoryIsEmpty())
    {
      // if there is no sound directory, or the sound directory has no files in
      // it, return the "no sound files" panel
      noSoundFilesPanel = new NoSoundFilesPanel();
      JButton importILifeSoundsButton = noSoundFilesPanel.getImportILifeSoundsButton();
      importILifeSoundsButton.addActionListener(new ImportILifeSoundsActionListener(this));
      return noSoundFilesPanel;
    }
    else
    {
      // otherwise, do the work to build the table of the latest sound effects and
      // corresponding events
      buildPreferencesSoundPanel();
      // preferencesController.setSoundPanel(preferencesSoundPanel);

      return preferencesSoundPanel;
    }
  }

  public void doImportILifeSoundsAction(boolean succeeded, int message, int numFilesCopied)
  {
    if (succeeded)
    {
      // let the user know it succeeded?
      JOptionPane.showMessageDialog(macPreferencesController.getPreferencesDialog(),
          numFilesCopied + " iLife Sound Effects files have been imported.", "Success", JOptionPane.INFORMATION_MESSAGE);

      // display the table of sounds
      logger.logDebug("CALLING buildPreferencesSoundPanel");
      buildPreferencesSoundPanel();

      logger.logDebug("CALLING macPreferencesController.setSoundPanel(preferencesSoundPanel)");
      macPreferencesController.setSoundPanel(preferencesSoundPanel);
    }
    else
    {
      showMessageForILifeImportFailedCase(message);
    }
  }
  
  public void playSoundForEvent(int desktopCurtainEvent)
  {
    String filenamePtr = null;
    if (desktopCurtainEvent == Hyde.SHOW_CURTAIN_EVENT)
    {
      filenamePtr = currentShowCurtainSoundFilename;
    }
    else if (desktopCurtainEvent == Hyde.QUIT_CURTAIN_EVENT)
    {
      filenamePtr = currentQuitCurtainSoundFilename;
    }
    else if (desktopCurtainEvent == Hyde.REFILL_CURTAIN_EVENT)
    {
      filenamePtr = currentRefillCurtainSoundFilename;
    }

    if (filenamePtr == null || filenamePtr.trim().equals("")) return;
    
    // build the canonical name
    String soundFilename = getDesktopCurtainSoundfileDirectory() + System.getProperty("file.separator") + filenamePtr;

    // play it
    Utilities u = new Utilities(logger);
    u.playSound(soundFilename, 5000);
  }

  /**
   * Play the sound effect shown in the table at the given row and column.
   */
  private void playSound(int row)
  {
    logger.logDebug("ENTERED playSound, row = " + row);

    // determine the name of the sound file
    SoundFile sf = soundsTableModel.getSoundFilename(row);
    
    // build the canonical name
    String soundFilename = getDesktopCurtainSoundfileDirectory() + System.getProperty("file.separator") + sf.newFilename;
    
    // play it
    Utilities u = new Utilities(logger);
    u.playSound(soundFilename, 5000);
  }

  private void buildPreferencesSoundPanel()
  {
    logger.logDebug("ENTERED SoundsController::buildPreferencesSoundPanel ");
    preferencesSoundPanel = new PreferencesSoundPanel();

    // build jtable and table model
    logger.logDebug("   buildPreferencesSoundPanel::getting sounds table");
    soundsTable = preferencesSoundPanel.getSoundsTable();

    // play the sound any time a cell is selected.
    // don't do this for the first row or first column.
    logger.logDebug("   buildPreferencesSoundPanel::adding ListSelectionListener to SoundsTable");
    soundsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent event)
      {
        if (!event.getValueIsAdjusting())
        {
          int row = soundsTable.getSelectedRow();
          int col = soundsTable.getSelectedColumn();
          int editingRow = soundsTable.getEditingRow();
          
          if (col == SoundsTableModel.FILE_DESCRIPTION_COLUMN_NUM) return;

          // need to use editingRow, at least on mac os x; otherwise, 'row' can
          // often be -1.
          // TODO i may need to check the values or row and editingRow here, but for
          //      now i'm just going with editingRow
          if (editingRow != 0)
          {
            // TODO when i click a checkbox, the row is no longer being selected
            playSound(editingRow);
          }
        }
      }
    });

    logger.logDebug("   buildPreferencesSoundPanel::updateSoundsTableModelFromFilesInDirectory");
    // make sure we have up-to-the-moment data
    updateSoundsTableModelFromFilesInDirectory();

    // TODO may want to do this differently; refresh the table model
    // instead of constantly whacking it
    logger.logDebug("   buildPreferencesSoundPanel::soundsTable.setModel(soundsTableModel)");
    if (soundsTable == null)
    {
      logger.logDebug("   ***** soundsTable IS NULL");
    }
    soundsTable.setModel(soundsTableModel);
    
    logger.logDebug("   buildPreferencesSoundPanel::calling tellTableModelWhichSoundsToSelect ...");
    tellTableModelWhichSoundsShouldBeSelected();

    // misc table things
    logger.logDebug("   buildPreferencesSoundPanel::soundsTable, getting header, setting size and selection model ...");
    soundsTable.getTableHeader().setReorderingAllowed(false);
    soundsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // did this work b/c the jtable size and viewport size need to be correct so the user
    // can see all their files, whether they have 20, or 500
    setSoundsTableSize();

    // use this to implement table tow striping; i had problems with quaqua table row striping
    // and checkboxes (booleans)
    soundsTable.setDefaultRenderer(Object.class, new StripedRowTableCellRenderer());
    soundsTable.setDefaultRenderer(Boolean.class, new StripedRowTableCellRenderer());
    
    // for use with quaqua
    //soundsTable.putClientProperty("Quaqua.Table.style", "striped");
    
    logger.logDebug("   buildPreferencesSoundPanel::looping through sounds table columns to set widths ...");
    for (int i = 0; i < soundsTableModel.getColumnCount(); i++)
    {
      logger.logDebug("  i = " + i);
      TableColumn column = soundsTable.getColumnModel().getColumn(i);
      if (i == 0)
        column.setPreferredWidth(180);
      else if (i == SoundsTableModel.SHOW_CURTAIN_COLUMN_NUM)
        column.setPreferredWidth(100);
      else if (i == SoundsTableModel.QUIT_CURTAIN_COLUMN_NUM)
        column.setPreferredWidth(100);
      else if (i == SoundsTableModel.REFILL_CURTAIN_COLUMN_NUM)
        column.setPreferredWidth(100);
    }
  }

  private void setSoundsTableSize()
  {
    //soundsTable.setSize(new Dimension(SOUNDS_TABLE_WIDTH, SOUNDS_TABLE_HEIGHT));

    int rowHeight = soundsTable.getRowHeight();
    int numRows = soundsTableModel.getRowCount();
    int neededTableHeight = rowHeight * numRows;
    neededTableHeight = (int)(neededTableHeight * 1.05);
    
    soundsTable.setPreferredScrollableViewportSize(new Dimension(450, 500));
    soundsTable.setMinimumSize(new Dimension(450, neededTableHeight));
    soundsTable.setPreferredSize(new Dimension(450, neededTableHeight));
  }

  // contract: tableModel needs to be hooked up before calling this
  private void tellTableModelWhichSoundsShouldBeSelected()
  {
    logger.logDebug("ENTERED tellTableModelWhichSoundsShouldBeSelected ...");
    if (soundsTableModel == null)
    {
      logger.logDebug("   soundsTableModel IS NULL");
    }
    logger.logDebug("   current 'show' sound:   " + currentShowCurtainSoundFilename);
    logger.logDebug("   current 'quit' sound:   " + currentQuitCurtainSoundFilename);
    logger.logDebug("   current 'refill' sound: " + currentRefillCurtainSoundFilename);
    soundsTableModel.setSelectedSound(SHOW_CURTAIN_SOUND_FILENAME, currentShowCurtainSoundFilename);
    soundsTableModel.setSelectedSound(QUIT_CURTAIN_SOUND_FILENAME, currentQuitCurtainSoundFilename);
    soundsTableModel.setSelectedSound(REFILL_CURTAIN_SOUND_FILENAME, currentRefillCurtainSoundFilename);
  }

  private void updateSoundsTableModelFromFilesInDirectory()
  {
    logger.logDebug("ENTERED updateSoundsTableModelFromDirectoryFiles ...");
    // get current list of songs
    currentSoundFiles = getDesktopCurtainSoundFilesDynamically();

    logger.logDebug("   # current sound files = " + currentSoundFiles.size());

    // if the table model is null, create a new one
    if (soundsTableModel == null)
    {
      logger.logDebug("   creating table model and adding sounds");
      soundsTableModel = new SoundsTableModel(this, currentSoundFiles, logger);
      soundsTableModel.addTableModelListener(this);
    }
    else
    {
      // otherwise, re-use the old one
      logger.logDebug("   adding sounds to existing table model");
      soundsTableModel.clearAll();
      soundsTableModel.addSoundsToModel(currentSoundFiles);
    }
  }

  /**
   * This is part of the TableModelListener interface. Hmm, I'm not sure when
   * this should be used, as the TableModel setValueAt() method is working fine
   * for what I need.
   */
  public void tableChanged(TableModelEvent e)
  {
    logger.logDebug("ENTERED SoundCtrl::tableChanged ");
  }

  /**
   * Show different messages depending on how the import process failed.
   */
  private void showMessageForILifeImportFailedCase(int message)
  {
    if (message == ILIFE_DIR_DOESNT_EXIST)
    {
      // the simple case where the user doesn't have ilife
      String userMessage = "I can't find the iLife sound effects directory. (It should be named '"
          + APPLE_SOUND_FILE_ROOT_DIR + "'.)\n" + "Therefore I could not import the iLife Sound Effects.\n\n"
          + "However, you can still use your own sound effects.\n"
          + "Please see the Hyde user manual for more information.";
      JComponent component = createWidgetForLongMessage(userMessage, 8, 42);
      JOptionPane.showMessageDialog(macPreferencesController.getPreferencesDialog(), component,
          "Could Not Find iLife Sound Effects", JOptionPane.WARNING_MESSAGE);
    }
    if (message == ILIFE_HAD_PROBLEM_COPYING_FILES)
    {
      // a harder case where i had a problem copying their ilife sound effects
      // files.
      // this is one of those things that shouldn't happen.
      String userMessage = "Your iLife sound effects folder exists, but I had a "
          + "problem copying the sound effects files (*.aif) I was looking for.\n\n"
          + "NOTE: The newest iLife file format has changed, and I can't use the new format. :(\n\n"
          + "I was trying to copy the files from this directory:\n\n" + "   " + APPLE_SOUND_FILE_ROOT_DIR + "\n\n"
          + "to this directory:\n\n" + "   " + getDesktopCurtainSoundfileDirectory() + "\n\n"
          + "Please use the Mac Console application to check\n" + "for possible error messages.\n\n"
          + "Also, please let us know about the problem.\n";
      JComponent component = createWidgetForLongMessage(userMessage, 12, 42);
      JOptionPane.showMessageDialog(macPreferencesController.getPreferencesDialog(), component,
          "Problem When Copying iLife Sound Effects", JOptionPane.WARNING_MESSAGE);
    }
    else
    {
      // the "should never happen" case
      String userMessage = "An unknown error happened when trying to\n" + "find and copy your iLife Sound Effects files.\n\n"
          + "Please use the Mac Console application to check for possible error messages.\n\n"
          + "Also, please let us know about the problem.\n";
      JComponent component = createWidgetForLongMessage(userMessage, 8, 42);
      JOptionPane.showMessageDialog(macPreferencesController.getPreferencesDialog(), component,
          "Problem When Copying iLife Sound Effects", JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * TODO The heigh and width logic here needs to be made smarter.
   * 
   * @param longMessage
   *          Your long text message.
   * @param height
   *          Currently refers to the height of our textarea.
   * @param width
   *          Currently refers to the width of our textarea.
   * @return A widget you can display in a JOptionPane.showMessageDialog
   */
  private JComponent createWidgetForLongMessage(String longMessage, int height, int width)
  {
    JTextArea textArea = new JTextArea(height, width);
    textArea.setText(longMessage);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setMargin(new Insets(5, 5, 5, 5));
    textArea.setEditable(false);
    textArea.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(textArea);
    return scrollPane;
  }

  /**
   * Handle the case where the "importILifeSounds" button is pressed.
   */
  class ImportILifeSoundsActionListener implements ActionListener
  {
    private SoundFileController soundFileController;

    public ImportILifeSoundsActionListener(SoundFileController soundFileController)
    {
      this.soundFileController = soundFileController;
    }

    public void actionPerformed(ActionEvent e)
    {
      // does the ilife directory exist?
      logger.logDebug("Getting name of Apple Sound File directory ...");
      File iLifeSoundDir = new File(APPLE_SOUND_FILE_ROOT_DIR);
      
      logger.logDebug("   ... checking for null ...");
      if (iLifeSoundDir == null)
      {
        logger.logDebug("iLife sound dir came back null");
        soundFileController.doImportILifeSoundsAction(false, ILIFE_DIR_DOESNT_EXIST, 0);
        return;
      }

      logger.logDebug("Checking to see if iLife Sounds directory exists ...");
      if (!iLifeSoundDir.exists())
      {
        logger.logDebug("   ... iLife sounds dir does not exist");
        soundFileController.doImportILifeSoundsAction(false, ILIFE_DIR_DOESNT_EXIST, 0);
        return;
      }

      // ilife dir exists, try to copy files
      logger.logDebug("   ... calling copyAppleSoundFilesToHydeSoundDirectory");
      int numFilesCopied = copyAppleSoundFilesToCurtainSoundDirectory();
      logger.logDebug("   numFilesCopied = " + numFilesCopied);

      // tell the controller what happened
      if (numFilesCopied <= 0)
      {
        // TODO something went wrong
        soundFileController.doImportILifeSoundsAction(false, ILIFE_HAD_PROBLEM_COPYING_FILES, numFilesCopied);
      }
      else
      {
        soundFileController.doImportILifeSoundsAction(true, ILIFE_FILES_COPIED_FINE, numFilesCopied);
      }
    }
  }

  /**
   * With no files in dir this returns true.
   * 
   * If there is no soundfile directory, this method traps the exception and
   * returns true.
   * 
   * This method uses the class field 'desktopCurtainSoundfileDirectory'.
   * 
   * Note: This method currently does not attempt to distinguish between 'sound'
   * files and other files types.
   */
  private boolean soundFilesDirectoryIsEmpty()
  {
    String[] filesInDir = null;
    try
    {
      File soundDir = new File(getDesktopCurtainSoundfileDirectory());
      filesInDir = soundDir.list();
    }
    catch (NullPointerException npe)
    {
      logger.logError("NullPointerException in soundFilesDirectoryIsEmpty(), message follows:");
      logger.logError(npe.getMessage());
      return true;
    }

    if (filesInDir == null || filesInDir.length == 0)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * This is dynamic, and is based on the sound files actually found in the
   * DesktopCurtain sound file directory.
   */
  public List<SoundFile> getDesktopCurtainSoundFilesDynamically()
  {
    logger.logDebug("ENTERED getDCSoundFilesDynamically ...");

    // create an empty list
    List<SoundFile> soundFiles = new ArrayList<SoundFile>();

    // generate a list of sound files in our soundfile directory
    // TODO filter to just allow sound files (au, aif, wav)
    
    logger.logDebug("   GETTING soundFileDir");
    File soundFileDir = new File(getDesktopCurtainSoundfileDirectory());
    try
    {
      logger.logDebug("   GETTING soundFileDir is " + soundFileDir.getCanonicalPath());
    }
    catch (Exception e)
    {
      logger.logError("Exception in getSoundsDynamically(), message follows:");
      logger.logError(e.getMessage());
    }

    // TODO get only files
    // TODO use a FilenameFilter
    
    class SoundFileFilter implements FilenameFilter
    {
      public boolean accept(File file, String s)
      {
        //if (file.isDirectory()) return false;
        if ( s.equalsIgnoreCase(".DS_Store")) return false;
        if ( s.matches(".*\\..*")) return true;
        return false;
      }
    }
    logger.logDebug("   CREATING soundFilenames[] from soundFileDir.list() ...");
    String[] soundFilenames = soundFileDir.list(new SoundFileFilter());
    
    // protection
    if (soundFilenames == null)
    {
      logger.logDebug("   soundFilenames was null, leaving getSoundFilesDynamically early");
      return soundFiles;
    }

    logger.logDebug("   found " + soundFilenames.length + " sound files.");

    // their description is implied by their filename, so generate those
    // dynamically
    logger.logDebug("   ABOUT TO LOOP THROUGH SOUND FILENAMES TO CREATE " + soundFilenames.length + " DESCRIPTIONS ...");
    int count = 1;
    for (String filename : soundFilenames)
    {
      String description = getDescriptionFromSoundfileName(filename);
      logger.logDebug("   filename =    " + filename);
      logger.logDebug("   description = " + description);

      // if we can't create a description, don't add it
      if (!description.equals(""))
      {
        logger.logDebug("   description was not blank, creating new SoundFile\n");
        // TODO may want a different SoundFile constructor; i was using (filename, description)
        //      here, which was wrong. Second param here should be AppleLocation, but I don't have that.
        SoundFile soundFile = new SoundFile(filename, "");
        soundFile.description = description;
        soundFiles.add(soundFile);
      }
    }

    // return the list
    return soundFiles;
  }

  /**
   * Convert a filename like "synth-zingers-2.aif" into "Synth Zingers 2". (This
   * method copied here from my test class CreateDescriptionFromFilename.java.)
   * Contract: If there is any problem, this method will return a blank string.
   */
  private String getDescriptionFromSoundfileName(String soundfileName)
  {
    logger.logDebug("ENTERED makeDescriptionFromFilename ...");

    // some self-protection
    if (soundfileName == null)
      return "";
    if (soundfileName.trim().equals(""))
      return "";

    logger.logDebug("sound filename init: " + soundfileName);

    // protection: only allow letters, numbers, decimals, hyphens, and underscores
    soundfileName = soundfileName.replaceAll("[^a-zA-Z0-9_.-]", "");
    logger.logDebug("sound filename replaced: " + soundfileName);

    // get the basename
    // assume the filename is split by the last "."
    int decimalLocation = soundfileName.lastIndexOf(".");
    if (decimalLocation <= 0)
      return "";
    String baseFilename = soundfileName.substring(0, decimalLocation);

    logger.logDebug("base filename: " + baseFilename);
    // split the base filename by "-" characters ("synth", "zingers", "2")
    String[] words = null;
    if (baseFilename.indexOf("-") > 0)
    {
      words = baseFilename.split("[.-]");
    }
    else if (baseFilename.indexOf("_") > 0)
    {
      words = baseFilename.split("[._]");
    }
    else
    {
      // hmm, nothing to split on, so we just have one word, like "foo", from "foo.wav"
      words = new String[1];
      words[0] = baseFilename;
    }

    for (int i = 0; i < words.length; i++)
    {
      logger.logDebug("word[i]: " + words[i]);
    }

    logger.logDebug("do uppercase ...");
    
    // uppercase the first letter in each word
    for (int i = 0; i < words.length; i++)
    {
      String word = words[i];
      word = word.toLowerCase();
      String firstLetterAsUpper = word.substring(0, 1).toUpperCase();
      words[i] = firstLetterAsUpper + word.substring(1);
      logger.logDebug("word[i]: " + words[i]);
    }

    logger.logDebug("add spaces to words");
    StringBuilder description = new StringBuilder();
    for (int i = 0; i < words.length; i++)
    {
      description.append(words[i]);
      if (i < words.length - 1)
      {
        description.append(" ");
      }
    }

    logger.logDebug("final description: '" + description.toString() + "'");
    return description.toString();
  }

  private String getDesktopCurtainSoundfileDirectory()
  {
    return macPreferencesController.getSoundFileDirectory();
  }

  /**
   * Copy all the sound files we know to the DesktopCurtain directory. Returns
   * the number of files copied during the copy process.
   */
  public int copyAppleSoundFilesToCurtainSoundDirectory()
  {
    int numFilesCopied = 0;

    logger.logDebug("IN copyAppleSoundFilesToHydeSoundDirectory");

    String fileSeparator = System.getProperty("file.separator");
    
    logger.logDebug("   about to try copying iLife Sound Effects into our Sounds folder ...");
    logger.logDebug("   # of apple sound effects we're looking for: " + appleSoundFiles.length);
    for (SoundFile soundFile : appleSoundFiles)
    {
      String canonSourceFilename = APPLE_SOUND_FILE_ROOT_DIR + fileSeparator + soundFile.appleLocation;
      String canonTargetFilename = getDesktopCurtainSoundfileDirectory() + fileSeparator + soundFile.newFilename;
      
      logger.logDebug("     canonSourceFilename = " + canonSourceFilename);
      logger.logDebug("     canonTargetFilename = " + canonTargetFilename);

      File canonSourceFile = new File(canonSourceFilename);
      File canonTargetFile = new File(canonTargetFilename);
      try
      {
        copyFile(canonSourceFile, canonTargetFile);
        numFilesCopied++;
      }
      catch (IOException e)
      {
        // TODO ignore for now
        logger.logError("IOException occurred in copyAppleSoundFiles, message follows: ");
        logger.logError(e.getMessage());
      }
    }
    return numFilesCopied;
  }
  
  /**
   * Copy one file from the Source location to the Destination location.
   * 
   * @param source
   * @param destination
   * @throws IOException
   */
  private void copyFile(File source, File destination) throws IOException
  {
    FileChannel inChannel = new FileInputStream(source).getChannel();
    FileChannel outChannel = new FileOutputStream(destination).getChannel();
    try
    {
      inChannel.transferTo(0, inChannel.size(), outChannel);
    }
    catch (IOException e)
    {
      throw e;
    }
    finally
    {
      if (inChannel != null)
        inChannel.close();
      if (outChannel != null)
        outChannel.close();
    }
  }

  public boolean makeDirectory(String directoryName)
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

  public String getCurrentShowCurtainSoundFilename()
  {
    return currentShowCurtainSoundFilename;
  }

  public String getCurrentQuitCurtainSoundFilename()
  {
    return currentQuitCurtainSoundFilename;
  }

  public String getCurrentRefillCurtainSoundFilename()
  {
    return currentRefillCurtainSoundFilename;
  }

}
