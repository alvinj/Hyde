package com.devdaily.heidi;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import com.devdaily.logging.DDLoggerInterface;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class SoundsTableModel extends AbstractTableModel
{
  
  // logging
  DDLoggerInterface logger;
  
  private SoundFileController soundFileController;
  private List<SoundFile> listOfSoundFiles = new ArrayList<SoundFile>();
  
  private static final String NO_SOUND_FILE_FILENAME    = "None";
  private static final String NO_SOUND_FILE_DESCRIPTION = "<Don't Use a Sound>";
  
  public static final int FILE_DESCRIPTION_COLUMN_NUM = 0;
  public static final int SHOW_CURTAIN_COLUMN_NUM     = 1;
  public static final int REFILL_CURTAIN_COLUMN_NUM   = 2;
  public static final int QUIT_CURTAIN_COLUMN_NUM     = 3;

  private static final int COLUMN_COUNT  = 4;
  final String[] columnNames = {"Sound",
                                "Show Curtain",
                                "Refill Curtain",
                                "Quit Curtain"
                                };

  private SoundFile noSoundFile;

  public SoundsTableModel(SoundFileController soundFileController, List<SoundFile> sounds, DDLoggerInterface logger)
  {
    logger.logDebug("ENTERED SoundsTableModel constructor ...");
    this.soundFileController = soundFileController;
    this.logger = logger;

    // add the "No Sound File"
    noSoundFile = new SoundFile(NO_SOUND_FILE_FILENAME, "NO APPLE FILENAME");
    noSoundFile.description = NO_SOUND_FILE_DESCRIPTION;

    this.listOfSoundFiles.clear();
    this.listOfSoundFiles.add(noSoundFile);
    this.listOfSoundFiles.addAll(sounds);
    
    // TODO SHOULD I USE DEFAULT-TABLE-MODEL INSTEAD OF ABSTRACT-TABLE-MODEL???
    //this.setColumnIdentifiers(columnNames);
    this.fireTableDataChanged();
  }
  
  /**
   * De-selects all the other sound files other than the file given by
   * filename, for the given key, which should be one of:
   * 
   *   SoundFileController.SHOW_CURTAIN_SOUND_FILENAME
   *   SoundFileController.QUIT_CURTAIN_SOUND_FILENAME
   *   SoundFileController.REFILL_CURTAIN_SOUND_FILENAME
   *   
   * @param key
   * @param filename
   */
  private void deSelectAllOtherSoundEffects(String key, String filename)
  {
    logger.logDebug("ENTERED deSelectAllOtherSoundEffects key = " + key + ", filename = " + filename);
    logger.logDebug("   key      = " + key);
    logger.logDebug("   filename = " + filename);
    logger.logDebug("   listOfSoundFiles.size = " + listOfSoundFiles.size());

    for (SoundFile sf : listOfSoundFiles)
    {
      if (sf == null)
      {
        logger.logDebug("   CURRENT sf IS NULL (can't happen), skipping");
        continue;
      }
      logger.logDebug("   sf.newFilename   = " + sf.newFilename);
      logger.logDebug("   sf.description   = " + sf.description + "\n");

      if (!filename.equals(sf.newFilename))
      {
        if (key.equals(SoundFileController.SHOW_CURTAIN_SOUND_FILENAME))
        {
          sf.isCurrentShowCurtainSound = false;
        }
        else if (key.equals(SoundFileController.REFILL_CURTAIN_SOUND_FILENAME))
        {
          sf.isCurrentRefillCurtainSound = false;
        }
        else if (key.equals(SoundFileController.QUIT_CURTAIN_SOUND_FILENAME))
        {
          sf.isCurrentQuitCurtainSound = false;
        }
      }
    }
    // AJA just added
    this.fireTableDataChanged();

  }
  
  /**
   * @param key      Should be SHOW_CURTAIN_SOUND_FILENAME, QUIT_CURTAIN_SOUND_FILENAME,
   *                 or REFILL_CURTAIN_SOUND_FILENAME. 
   * @param filename Our name for the sound file (i.e., a valid name in the Hyde
   *                 "Sounds" directory.
   */
  public void setSelectedSound(String key, String filename)
  {
    // TODO long term - refactor this code; there is a lot of duplication here
    
    logger.logDebug("ENTERED SoundsTableModel::setSelectedSound()");
    logger.logDebug("   key      = " + key);
    logger.logDebug("   filename = " + filename);
    
    // if key = "SHOW" 
    if (key.equals(SoundFileController.SHOW_CURTAIN_SOUND_FILENAME))
    {
      logger.logDebug("   in SoundFileController.SHOW_CURTAIN_SOUND_FILENAME branch ...");
      // special case where the user doesn't have a sound effect
      if (filename.equals(NO_SOUND_FILE_FILENAME) || filename.trim().equals(""))
      {
        noSoundFile.isCurrentShowCurtainSound = true;
        deSelectAllOtherSoundEffects(key, filename);
        return;
      }
      
      if (listOfSoundFiles == null)
      {
        logger.logDebug("   listOfSoundFiles was null, about to bomb");
      }

      for (SoundFile sf : listOfSoundFiles)
      {
        logger.logDebug("LOOPING THROUGH SOUND FILES, CURRENT SF = " + sf.description);

        if (filename.equals(sf.newFilename))
        {
          // when you find the matching one, select it, deselect all others, and leave
          sf.isCurrentShowCurtainSound = true;
          deSelectAllOtherSoundEffects(key, filename);
          // AJA just added
          this.fireTableDataChanged();
          return;
        }
      }
    }
    
    // if key = "REFILL" 
    if (key.equals(SoundFileController.REFILL_CURTAIN_SOUND_FILENAME))
    {
      logger.logDebug("   in SoundFileController.REFILL_CURTAIN_SOUND_FILENAME branch ...");
      if (filename.equals(NO_SOUND_FILE_FILENAME) || filename.trim().equals(""))
      {
        noSoundFile.isCurrentRefillCurtainSound = true;
        deSelectAllOtherSoundEffects(key, filename);
        return;
      }
      for (SoundFile sf : listOfSoundFiles)
      {
        if (filename.equals(sf.newFilename))
        {
          sf.isCurrentRefillCurtainSound = true;
          deSelectAllOtherSoundEffects(key, filename);
          // AJA just added
          this.fireTableDataChanged();
          return;
        }
      }
    }
    // if key = "QUIT" 
    if (key.equals(SoundFileController.QUIT_CURTAIN_SOUND_FILENAME))
    {
      logger.logDebug("   in SoundFileController.QUIT_CURTAIN_SOUND_FILENAME branch ...");
      if (filename.equals(NO_SOUND_FILE_FILENAME) || filename.trim().equals(""))
      {
        noSoundFile.isCurrentQuitCurtainSound = true;
        deSelectAllOtherSoundEffects(key, filename);
        return;
      }
      for (SoundFile sf : listOfSoundFiles)
      {
        if (filename.equals(sf.newFilename))
        {
          sf.isCurrentQuitCurtainSound = true;
          deSelectAllOtherSoundEffects(key, filename);
          // AJA just added
          this.fireTableDataChanged();
          return;
        }
      }
    }
    
  }
  
  public void addSoundsToModel(List<SoundFile> sounds)
  {
    logger.logDebug("ENTERED addSoundsToTableModel ...");
    logger.logDebug("   # of sounds = " + sounds.size());
    
    listOfSoundFiles.clear();
    listOfSoundFiles.add(noSoundFile);
    listOfSoundFiles.addAll(sounds);
    this.fireTableDataChanged();
  }
  
  Class[] columnTypes = new Class[] {
      Object.class, Boolean.class, Boolean.class, Boolean.class
    };

  public Class<?> getColumnClass(int columnIndex) 
  {
    logger.logDebug("ENTERED getColumnClass ...");
    return columnTypes[columnIndex];
  }

  public void clearAll()
  {
    logger.logDebug("ENTERED SoundsTableModel::clearAll");
    listOfSoundFiles.clear();
    this.fireTableDataChanged();
  }

  public int getRowCount()
  {
    logger.logDebug("ENTERED getRowCount, # sound files = " + listOfSoundFiles.size());
    return listOfSoundFiles.size();
  }

  public int getColumnCount()
  {
    logger.logDebug("ENTERED getColumnCount ...");
    return COLUMN_COUNT;
  }

  public Object getValueAt(int row, int col)
  {
    logger.logDebug("ENTERED getValueAt ...");
    logger.logDebug("   SoundsTableModel::getValueAt, row = " + row + ", col = " + col);
    logger.logDebug("   SoundsTableModel::getValueAt, soundFiles.size = " + listOfSoundFiles.size());
    if ( row < listOfSoundFiles.size() )
    {
      SoundFile currentSoundFile = listOfSoundFiles.get(row);
      logger.logDebug("   SoundsTableModel::getValueAt, row = " + row + ", currentSoundFile = " + currentSoundFile.newFilename);

      if (col == FILE_DESCRIPTION_COLUMN_NUM) return currentSoundFile.description;
      else if (col == SHOW_CURTAIN_COLUMN_NUM) return currentSoundFile.isCurrentShowCurtainSound;
      else if (col == REFILL_CURTAIN_COLUMN_NUM) return currentSoundFile.isCurrentRefillCurtainSound;
      else if (col == QUIT_CURTAIN_COLUMN_NUM) return currentSoundFile.isCurrentQuitCurtainSound;
      else
      {
        logger.logDebug("   ERROR: SoundsTableModel::getValueAt, asked for a column that doesn't exist, col = " + col);
      }
    }
    else
    {
      logger.logDebug("   ERROR: SoundsTableModel::getValueAt, ROW SOMEHOW > SIZE;  row = " + row + ", listSize = " + listOfSoundFiles.size());
    }
    logger.logDebug("ERROR: SoundsTableModel::getValueAt, SOMEHOW RETURNING NULL for row = " + row + ", col = " + col);
    return null;
  }

  /**
   * This method is called when the user clicks one of our checkboxes in the
   * "Startup Sound", "Quit Sound", and "Refill Sound" columns.
   */
  public void setValueAt(Object boo, int row, int col)
  {
    logger.logDebug("ENTERED SoundsTableModel::setValueAt, row = " + row + ", col = " + col);
    // only handle events for the checkboxes
    if (!(boo instanceof Boolean)) 
    {
      logger.logDebug("   not a boolean, returning");
      return;
    }
    if (col == SHOW_CURTAIN_COLUMN_NUM || col == QUIT_CURTAIN_COLUMN_NUM || col == REFILL_CURTAIN_COLUMN_NUM)
    {
      setSoundFileValueAt((Boolean)boo, row, col);
    }
  }
  
  private void setSoundFileValueAt(Boolean b, int row, int col)
  {
    logger.logDebug("ENTERED SoundsTableModel::setSoundFileValueAt: boolean: " + b + ", row: " + row + ", col: " + col);
    SoundFile currentSoundFile = listOfSoundFiles.get(row);
    if (col == SHOW_CURTAIN_COLUMN_NUM)
    {
      doSetSoundFileWork(currentSoundFile, SoundFileController.SHOW_CURTAIN_SOUND_FILENAME);
    }
    else if (col == REFILL_CURTAIN_COLUMN_NUM)
    {
      doSetSoundFileWork(currentSoundFile, SoundFileController.REFILL_CURTAIN_SOUND_FILENAME);
    }
    else if (col == QUIT_CURTAIN_COLUMN_NUM)
    {
      doSetSoundFileWork(currentSoundFile, SoundFileController.QUIT_CURTAIN_SOUND_FILENAME);
    }
    fireTableCellUpdated(row, col);
  }
  
  private void doSetSoundFileWork(SoundFile currentSoundFile, String soundFileEventName)
  {
    logger.logDebug("ENTERED doSetSoundFileWork ...");

    if (soundFileEventName.equals(SoundFileController.SHOW_CURTAIN_SOUND_FILENAME)) currentSoundFile.isCurrentShowCurtainSound = true;
    else if (soundFileEventName.equals(SoundFileController.REFILL_CURTAIN_SOUND_FILENAME)) currentSoundFile.isCurrentRefillCurtainSound = true;
    else if (soundFileEventName.equals(SoundFileController.QUIT_CURTAIN_SOUND_FILENAME)) currentSoundFile.isCurrentQuitCurtainSound = true;
    
    soundFileController.storeSelectedSoundFilenameInPreferences(soundFileEventName, currentSoundFile.newFilename);
    deSelectAllOtherSoundEffects(soundFileEventName, currentSoundFile.newFilename);
  }

  public String getColumnName(int col) 
  {
    logger.logDebug("ENTERED getColumnName, col = " + col);
    return columnNames[col];
  }

  public boolean isCellEditable(int row, int col)
  {
    logger.logDebug("ENTERED isCellEditable, row = " + row + ", col = " + col);
    if (col == SHOW_CURTAIN_COLUMN_NUM || col == QUIT_CURTAIN_COLUMN_NUM || col == REFILL_CURTAIN_COLUMN_NUM)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Don't seem to have to implement this method to get the checkbox selection
   * events I want, so it's currently empty.
   */
//  public void doTableDataChanged(int row, int column)
//  {
//  }

  public SoundFile getSoundFilename(int row)
  {
    logger.logDebug("ENTERED SoundsTableModel::getSoundFilename for row = " + row);
    return listOfSoundFiles.get(row);
  }

}






