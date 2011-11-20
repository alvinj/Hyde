package com.devdaily.heidi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import de.schlichtherle.util.ObfuscatedString;

public class AATester
{
  public static void main(String[] args)
  {
    new AATester();
  }

  // serialize the DateInfo class to these two files
  private String varTmpFullFilename1 = new ObfuscatedString(new long[] {0xD31C760B010F2759L, 0x3F6C53524EBB278BL, 0xA3CFC17D069F19A9L, 0xF582D86B9A3638FDL}).toString(); /* => "/var/tmp/.dc41026417" */
  private String varTmpFullFilename2 = new ObfuscatedString(new long[] {0x7E8826DCC70F33A2L, 0x465D20C13295A4B5L, 0x64C40A1904AB9EFEL, 0xE0EC6D90574B89BCL, 0xBFFB1B1A52BC9489L}).toString(); /* => "/var/tmp/.dd45f488df95c99" */

  private static final String FILE_PATH_SEPARATOR = System.getProperty("file.separator");


  
  // make sure i can create all these filenames
  // make sure i can change the date on these filenames as well
  public AATester()
  {


    
//    DCRuntime di1 = new DCRuntime(new Date());
//    serializeObjectToFile(di1, varTmpFullFilename1);
//    File f1 = new File(varTmpFullFilename1);
//    f1.setLastModified(getRandomTimestampForFile());
//    System.out.format("varTmpFullFilename1 is: %s\n", varTmpFullFilename1);
//    
//    DCRuntime di2 = new DCRuntime(new Date());
//    serializeObjectToFile(di2, varTmpFullFilename2);
//    File f2 = new File(varTmpFullFilename2);
//    f2.setLastModified(getRandomTimestampForFile());
//    System.out.format("varTmpFullFilename2 is: %s\n", varTmpFullFilename2);
//
//    DCRuntime d1 = (DCRuntime)getObjectBackFromSerializedFile(varTmpFullFilename1);
//    System.out.format("Date-1a: %s\n", d1.getIdal());
//    System.out.format("Date-1b: %s\n", d1.getFdal());
//    
//    DCRuntime d2 = (DCRuntime)getObjectBackFromSerializedFile(varTmpFullFilename1);
//    System.out.format("Date-2a: %s\n", d2.getIdal());
//    System.out.format("Date-2b: %s\n", d2.getFdal());
  }
  

  /**
   * Build a String that contains the full path to folder in the user's 
   * home directory.
   */
  private String getAbsoluteUserHomeDir(String relativeDir)
  {
    String homeDir = System.getProperty("user.home");
    return homeDir + FILE_PATH_SEPARATOR + relativeDir;
  }

  
  private Date getCurrentDate()
  {
    Calendar calendar = Calendar.getInstance();
    return calendar.getTime();
  }
  
  // this method has been changed
  private long getRandomTimestampForFile()
  {
    Random r = new Random(getCurrentDate().getTime());
    // get a random number between 0 and 44 
    int randomInt = r.nextInt(45);
    // make this 30-75 days ago
    int daysAgo = randomInt + 30;
    daysAgo = 0 - daysAgo;

    // use this value to write the time stamp on the file
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, daysAgo);
    Date semiRandomDate = calendar.getTime();
    return semiRandomDate.getTime();
  }

  private void serializeObjectToFile(Serializable s, String filename)
  {
    FileOutputStream fos = null;
    ObjectOutputStream out = null;
    try
    {
      fos = new FileOutputStream(filename);
      out = new ObjectOutputStream(fos);
      out.writeObject(s);
      out.close();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }
  
  private Object getObjectBackFromSerializedFile(String filename)
  {
    Object object = null;
    FileInputStream fis = null;
    ObjectInputStream in = null;
    try
    {
      fis = new FileInputStream(filename);
      in = new ObjectInputStream(fis);
      object = in.readObject();
      in.close();
      return object;
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
      return null;
    }
    catch (ClassNotFoundException ex)
    {
      ex.printStackTrace();
      return null;
    }
  }
  
}







