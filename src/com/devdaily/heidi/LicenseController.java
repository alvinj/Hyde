package com.devdaily.heidi;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.devdaily.logging.DDLoggerInterface;
import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;

// changed these up so i'm now using the "free trial period" (ftp) classes
import de.schlichtherle.license.ftp.LicenseManager;
import de.schlichtherle.license.ftp.LicenseParam;
import de.schlichtherle.util.ObfuscatedString;

public class LicenseController
{
  // TODO set this back after testing
  private static final int NUM_FTP_DAYS = 10;
  
  // logging
  DDLoggerInterface logger;

  // our local variables
  LicenseableClass licenseableClass;
  private KeyStoreParam publicKeyStoreParam;
  private CipherParam cipherParam;
  private LicenseParam licenseParam;
  
  // to let others access the LicenseContent information
  private LicenseContent licenseContent;

  // FTP config
  private KeyStoreParam ftpKeyStoreParam;
  
  // make an error message accessible, so the using class can let the user
  // know what went wrong; populate this message whenever there is an exception.
  private String errorMessage;
  
  private static final String VAR_SYSTEM_LOG_FILENAME             = new ObfuscatedString(new long[] {0x399BCB471E92AEF7L, 0x81D088D9F7AC8EA0L, 0x6E5EC04487915ABFL, 0x92E95D00D7B5B8EFL}).toString(); /* => "/var/log/system.log" */
  private static final String SKIP_VAR_SYSTEM_LOG_CHECK_FILENAME  = new ObfuscatedString(new long[] {0x5D77A62BB5D07C7L, 0x3E11F364A72B0FE7L, 0xB673F210572E75F3L, 0x24670DC24B202D5FL}).toString(); /* => "SkipVarSystemLogCheck" */
  private static final String VAR_SYSTEM_LOG_FTP_INSTALLED_STRING = new ObfuscatedString(new long[] {0xAD6AB058091EF6E0L, 0xE86EB2CB0572AAB4L, 0x36CF7BC7763B4E18L, 0xC00C58D879B611FBL, 0xE519A9AB44BF1E04L}).toString(); /* => "HYDE FTP License v1.1 installed" */
  
  // TODO i can't easily tell when an ftp license expires versus, so i'm not doing this for now.
  //      i was going to write this to the system.log file, and then check for it.
  private static final String VAR_SYSTEM_LOG_FTP_EXPIRED_STRING    = new ObfuscatedString(new long[] {0x7D004019F311C3B3L, 0x16883755CDADC6F5L, 0x93006796F4666AE9L, 0x6F6EE163D6BFB883L, 0xAA1B633E5805AEA7L}).toString(); /* => "HYDE FTP License v1.1 expired" */
  
  // serialize the DateInfo class to these two files
  private String varTmpFullFilename1 = new ObfuscatedString(new long[] {0xD31C760B010F2759L, 0x3F6C53524EBB278BL, 0xA3CFC17D069F19A9L, 0xF582D86B9A3638FDL}).toString(); /* => "/var/tmp/.dc41026417" */
  private String varTmpFullFilename2 = new ObfuscatedString(new long[] {0x7E8826DCC70F33A2L, 0x465D20C13295A4B5L, 0x64C40A1904AB9EFEL, 0xE0EC6D90574B89BCL, 0xBFFB1B1A52BC9489L}).toString(); /* => "/var/tmp/.dd45f488df95c99" */
  
  // ~/Library/DevDaily/DesktopCurtain
  private static final String homeLibraryDirname = "Library/DevDaily/Hyde";
  private static final String homeLibraryFilename = "Hyde.library";

  // ~/Library/Logs/DevDaily/DesktopCurtain
  private static final String homeLibraryLogsDirname = "Library/Logs/DevDaily/Hyde";
  private static final String homeLibraryLogsFilename = "Hyde.log";

  // ~/Library/Caches/DevDaily/DesktopCurtain
  private static final String homeLibraryCachesDirname = "Library/Caches/DevDaily/Hyde";
  private static final String homeLibraryCachesFilename = "Hyde.caches";

  // ~/Library/Application Support/DevDaily
  // Should probably make this the Sounds directory; easier for normal users to get to.
  private static final String homeLibraryApplicationSupportDirname = "Library/Application Support/DevDaily/Hyde";
  private static final String homeLibraryApplicationSupportFilename = "Hyde.info";

  private static final String FILE_PATH_SEPARATOR = System.getProperty("file.separator");
  
  
  public LicenseController(LicenseableClass licenseableClass, DDLoggerInterface logger)
  {
    this.licenseableClass = licenseableClass;
    this.logger = logger;
  }
  
  public LicenseContent getLicenseContent()
  {
    return this.licenseContent;
  }
  
  public String getErrorMessage()
  {
    return errorMessage;
  }
  
  /**
   * An implementation of the KeyStoreParam interface that returns 
   * the information required to work with the keystore containing the private key
   */
  private void initKeyStoreParam()
  {
    publicKeyStoreParam = new KeyStoreParam()
    {
      public InputStream getStream() throws IOException 
      {
        return licenseableClass.getPublicKeystoreAsInputStream();
      }
      public String getAlias() 
      {
        return licenseableClass.getAlias();
      }
      public String getStorePwd() 
      {
        return licenseableClass.getPublicKeystorePassword();
      }
      public String getKeyPwd() 
      {
        // These parameters are not used to create any licenses. 
        // Therefore there should never be a private key in the keystore 
        // entry. To enforce this policy, we return null here. 
        return null; // causes failure if private key is found in this entry
     }
    };
  }
  
  /**
   *                ********** FTP ***********
   * As near as I can tell from the docs, I have to implement this so
   * the FTP package can install a temporary "FTP" license.
   * I've created an "ftp alias" just for this; everything else is the 
   * same as the 'publicKeyStoreParam', as we share the same 
   * public key store file.
   */
  private void initFtpKeyStoreParam()
  {
    logger.logDebug("Entered initFtpKeyStoreParam()");

    // the same thing as 'publicKeyStoreParam', but do this for the ftpKey.
    ftpKeyStoreParam = new KeyStoreParam()
    {
      public InputStream getStream() throws IOException 
      {
        logger.logDebug("License::initFtpKeyStoreParam::getStream (FTP) was called");
        // same as other class, as we are using the same keystore
        return licenseableClass.getPublicKeystoreAsInputStream();
      }
      public String getAlias() 
      {
        logger.logDebug("License::getAlias() (FTP) was called.");
        // this is the alias for the ftp key
        return licenseableClass.getFtpAlias();
      }
      public String getStorePwd() 
      {
        // same as 'publicKeyStoreParam' (using same public key store)
        logger.logDebug("License::getStorePwd() (FTP) was called.");
        return licenseableClass.getPublicKeystorePassword();
      }
      public String getKeyPwd() 
      {
        // for ftp purposes we need the password for this keystore
        logger.logDebug("License::getKeyPwd() (FTP) was called.");
        return licenseableClass.getFtpKeyPwd();
     }
    };
  }
  
  private void initCipherParam()
  {
    // Set up an implementation of the CipherParam interface to return the password to be
    // used when performing the PKCS-5 encryption.
    cipherParam = new CipherParam() 
    {
      public String getKeyPwd() 
      {
        return licenseableClass.getCipherParamPassword();
      }
    };
  }
  
  /**
   * Create/populate the "licenseParm" field.
   */
  private void initLicenseParam()
  {
    logger.logDebug("Entered License::initLicenseParam()");
    
    // Set up an implementation of the LicenseParam interface.
    // Note that the subject string returned by getSubject() must match the subject property
    // of any LicenseContent instance to be used with this LicenseParam instance.
    licenseParam = new LicenseParam() 
    {
      public String getSubject() 
      {
        logger.logDebug("License::getSubject() was called, Subject is " + licenseableClass.getApplicationName());
        return licenseableClass.getApplicationName();
      }
      public Preferences getPreferences() 
      {
        logger.logDebug("License::getPreferences() was called.");
        return Preferences.userNodeForPackage(licenseableClass.getClassToLicense());
      }
      public KeyStoreParam getKeyStoreParam() 
      {
        logger.logDebug("License::getKeyStoreParam() was called.");
        return publicKeyStoreParam;
      }
      public CipherParam getCipherParam() 
      {
        logger.logDebug("License::getCipherParam() was called.");
        return cipherParam;
      }
      
      // TODO fill out these new methods
      public int getFTPDays()
      {
        logger.logDebug("License::getFTPDays() was called.");
        return NUM_FTP_DAYS;
      }
      public LicenseContent createFTPLicenseContent()
      {
        logger.logDebug("License::createFTPLicenseContent() was called.");
        /*
         * I found in the TLC source code that they just used a simple string for this, so that's
         * what I did here.
         */
        /* => "This is a Free Trial Period (FTP) license for the devdaily.com Hyde application, v1.x" */
        String LICENSE_CONTENT_INFO = new ObfuscatedString(new long[] {0x65C7CC3ADE5CDB2EL, 0x1B2A5CF871C1BBAFL, 
            0xFBBFFE0B09A8B2D9L, 0x48F224E807AA9897L, 0x1AA5E8307BC02667L, 0xF01FF56A26658F79L, 0x6B38BB8DDE53CC69L, 
            0xCBF34E28A2BC6E85L, 0x59626A2A2B0B8FBL, 0xD4E0ACC31AC8BC4DL, 0x56341EAEAA8AF522L, 0xBA6D890689A9017L}).toString();
        
        LicenseContent content = new LicenseContent();
        content.setInfo(LICENSE_CONTENT_INFO);
        return content;
      }
      public void ftpGranted(LicenseContent licenseContent)
      {
        // TODO this is a callback method to let me know that an ftp
        // license key was created automatically.
        // I'm guessing I need to call removeFTPEligibility from here.
        logger.logDebug("License::ftpGranted() was called.");
        JOptionPane.showMessageDialog(null, 
            "A 10-day Free Trial Period license has been installed. Enjoy!", 
            "Hyde Trial License", 
            JOptionPane.INFORMATION_MESSAGE);
      }
      public KeyStoreParam getFTPKeyStoreParam()
      {
        logger.logDebug("License::getFTPKeyStoreParam() was called.");
        // these are the parameters for the ftp key store
        return ftpKeyStoreParam;
      }
      public boolean isFTPEligible()
      {
        logger.logDebug("License::isFTPEligible() was called.");
        // this is whether the user is eleigible for the creation of an ftp license or not.
        // this should return true only the first time the application is run.
        
        // if any of our files exist, return false
        if (appFilesOrFoldersExist())
        {
          logger.logDebug("License::isFTPEligible, failed Test 7.");
          return false;
        }
        
        // if any of our preferences exist, return false
        if (preferencesExistAndContainKeys(Hyde.class))
        {
          logger.logDebug("License::isFTPEligible, failed Test 11.");
          return false;
        }

        //
        if (nameExistsInSystemLogFile(VAR_SYSTEM_LOG_FILENAME, VAR_SYSTEM_LOG_FTP_INSTALLED_STRING))
        {
          logger.logDebug("License::isFTPEligible, failed Test 14.");
          return false;
        }

        // passed all those checks; user/system is "ftp eligible"
        logger.logDebug("License::isFTPEligible() -- returning 'true'.");
        return true;
      }
      public void removeFTPEligibility()
      {
        // i think this is the method that is called so we can put "cookies" on the user's
        // computer so we'll know they've already used an ftp license.
        // TODO Auto-generated method stub
        logger.logDebug("License::removeFTPEligibility() was called.");
        createAllCookieFilesDirsAndStrings();
      }
    };
  }

  /**
   * If the "magic cookie file" exists on the user's system, return true.
   */
  private boolean varSystemLogMagicCookieExists()
  {
    String homeDir = System.getProperty("user.home");
    String canonSkipFileCheckFilename = homeDir + FILE_PATH_SEPARATOR + Hyde.CURTAIN_DIR_NAME + FILE_PATH_SEPARATOR + SKIP_VAR_SYSTEM_LOG_CHECK_FILENAME;
    File skipCheckFile = new File(canonSkipFileCheckFilename);
    if (skipCheckFile.exists())
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Returns true if the given string is found in the given system log file.
   * 
   * @return
   */
  private boolean nameExistsInSystemLogFile(String logFilename, String stringToLookFor)
  {
    // if a magic cookie exists on the user's filesystem, skip this test
    if (varSystemLogMagicCookieExists())
    {
      logger.logDebug("License::isFTPEligible, bypassing cookie test.");
      return false;
    }

    // proceed with test; scan /var/log/system.log to see if app was already installed
    try
    {
      logger.logDebug("License::isFTPEligible, scanning for footprints ...");
      BufferedReader bufferedReader = new BufferedReader(new FileReader(logFilename));
      String line = null;
      while ((line = bufferedReader.readLine()) != null)
      {
        if (line.indexOf(stringToLookFor) >= 0)
        {
          bufferedReader.close();
          logger.logDebug("   ... footprints found.");
          return true;
        }
      }
      
      // close the BufferedReader when we're done
      bufferedReader.close();
    }
    catch (Exception e)
    {
      logger.logError(e.getMessage());
      return false;
    }
    return false;
  }
  
  
  private void doInitWork()
  {
    logger.logDebug("License::init()");
    errorMessage = "";
    if (publicKeyStoreParam == null) initKeyStoreParam();
    if (cipherParam == null) initCipherParam();
    if (licenseParam == null) initLicenseParam();
    initFtpKeyStoreParam();
    logger.logDebug("   leaving init()");
  }
  
  /**
   * Prompt the user for the location of their license file,
   * get the filename,
   * then try to install the file.
   * 
   * @return true if the license installed properly, false otherwise.
   */
  public boolean installLicense(String licenseFilename)
  {
    // TODO technically this is done when the app is first started and our
    // controller is called ... this call may not be needed.
    doInitWork();
    
    LicenseManager lm = new LicenseManager(licenseParam);
    File licenseFile = new File(licenseFilename); 
    try
    {
      licenseContent = lm.install(licenseFile);
      // you know it worked b/c it didn't throw an exception
      return true;
    }
    catch (Exception e)
    {
      logger.logError("License::installLicense(), exception thrown, message follows:");
      logger.logError(e.getMessage());
      errorMessage = e.getMessage();
      return false;
    }
  }
  
  public boolean verifyLicense()
  {
    logger.logDebug("verifyLicense: calling doInitWork()");
    doInitWork();

    logger.logDebug("verifyLicense: creating new LicenseManager");
    LicenseManager lm = new LicenseManager(licenseParam); 

    try
    {
      logger.logDebug("verifyLicense: calling lm.verify()");
      licenseContent = lm.verify();
      logger.logDebug("I think I verified the license okay ...");
      logger.logDebug(licenseContent.getSubject() +" licensed for use for up to " 
          +licenseContent.getConsumerAmount() + " " + licenseContent.getConsumerType());
      return true;
    }
    catch (Exception e)
    {
      logger.logError("Exception thrown in License::verify, message follows:");
      logger.logError(e.getMessage());
      licenseableClass.handleVerifyLicenseFailedEvent();
      errorMessage = e.getMessage();
      return false;
    }
  }
  
  // ***************** COOKIE FILES AND SERIALIZATION STUFF *********************//
  
  private void writeSerializedFiles()
  {
    DCRuntime di1 = new DCRuntime(new Date());
    serializeObjectToFile(di1, varTmpFullFilename1);
    File f1 = new File(varTmpFullFilename1);
    f1.setLastModified(getRandomTimestampForFile());
    
    DCRuntime di2 = new DCRuntime(new Date());
    serializeObjectToFile(di2, varTmpFullFilename2);
    File f2 = new File(varTmpFullFilename2);
    f2.setLastModified(getRandomTimestampForFile());
  }

  /**
   * This will return a date from one of our serialized files, or it will
   * return null if everything fails. 
   */
  private Date getInstallationDateFromSerializedFiles()
  {
    try
    {
      DCRuntime d1 = (DCRuntime)getObjectBackFromSerializedFile(varTmpFullFilename1);
      if (d1 != null && d1.getIdal() != null) return d1.getIdal();
      
      DCRuntime d2 = (DCRuntime)getObjectBackFromSerializedFile(varTmpFullFilename1);
      if (d2 != null && d2.getIdal() != null) return d2.getIdal();
    }
    catch (RuntimeException re)
    {
      // ignore
    }
    return null;
  }

  /**
   * Call this when the application is first "initialized".
   */
  private void createAllCookieFilesDirsAndStrings()
  {
    writeSerializedFiles();
    createDirectoryAndInitFile(homeLibraryDirname, homeLibraryFilename);
    createDirectoryAndInitFile(homeLibraryLogsDirname, homeLibraryLogsFilename);
    createDirectoryAndInitFile(homeLibraryCachesDirname, homeLibraryCachesFilename);
    createDirectoryAndInitFile(homeLibraryApplicationSupportDirname, homeLibraryApplicationSupportFilename);
    
    // create the "Sounds" folder; this was a late addition, and was moved here (a) so it would happen
    // after licensing, and (b) not interfere with our license file/dir checks
    makeDirectories(Hyde.CANON_SOUNDS_DIR);
    
    // intentionally write to /var/log/system.log
    logger.logDebug(VAR_SYSTEM_LOG_FTP_INSTALLED_STRING);
  }
  
  /**
   * If *any* of our known application files exist, return true.
   */
  private boolean appFilesOrFoldersExist()
  {
    String absPath = getAbsoluteUserHomeDir(homeLibraryDirname);
    File file = new File(absPath);
    if (file.exists()) return true;
    
    absPath = getAbsoluteUserHomeDir(homeLibraryLogsDirname);
    file = new File(absPath);
    if (file.exists()) return true;
    
    absPath = getAbsoluteUserHomeDir(homeLibraryCachesDirname);
    file = new File(absPath);
    if (file.exists()) return true;
    
    absPath = getAbsoluteUserHomeDir(homeLibraryApplicationSupportDirname);
    file = new File(absPath);
    if (file.exists()) return true;
    
    absPath = getAbsoluteUserHomeDir(varTmpFullFilename1);
    file = new File(absPath);
    if (file.exists()) return true;
    
    absPath = getAbsoluteUserHomeDir(varTmpFullFilename2);
    file = new File(absPath);
    if (file.exists()) return true;
    
    return false;
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

  /**
   * Returns true if creating the relativeDir succeeded.
   * @param relativeDir A directory path that is relative to the user's home directory.
   *                    Should not begin with a "/".
   * @param relativeFilename Just a filename, with no leading directory information.
   * @return
   */
  private boolean createDirectoryAndInitFile(String relativeDir, String relativeFilename)
  {
    String homeDir = System.getProperty("user.home");
    String canonDirname = homeDir + FILE_PATH_SEPARATOR + relativeDir;
    File folderToCreate = new File(canonDirname);
    boolean succeeded = folderToCreate.mkdirs();
    //System.out.format("Creating %s succeeded: %s\n", canonDirname, succeeded);
    String canonFilename = canonDirname + FILE_PATH_SEPARATOR + relativeFilename;
    File f = new File(canonFilename);
    writeToFile(f, "");
    return succeeded;
  }

  /**
   * Create the directory given by the absolute path.
   */
  private boolean makeDirectories(String absoluteDirPath)
  {
    File folderToCreate = new File(absoluteDirPath);
    boolean succeeded = folderToCreate.mkdirs();
    return succeeded;
  }
  
  private Date getCurrentDate()
  {
    Calendar calendar = Calendar.getInstance();
    return calendar.getTime();
  }
  
  private void writeToFile(File file, String content)
  {
    try
    {
      BufferedWriter out = new BufferedWriter(new FileWriter(file)); 
      out.write(content);
      out.close();
    }
    catch (IOException e)
    {
      // do nothing
    }
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
      logger.logError("License:serialize(), exception thrown.");
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
      logger.logError("License:deSerialize(), IOException thrown.");
      return null;
    }
    catch (ClassNotFoundException ex)
    {
      logger.logError("License:deSerialize(), ClassNotFoundException thrown.");
      return null;
    }
  }
  
  /**
   * Returns true if there are actual preferences stored for the given class,
   * otherwise, return false. Note that at least one preference must be
   * stored for the given class for this method to return true.
   */
  private boolean preferencesExistAndContainKeys(Class theClass)
  {
    Preferences preferences = Preferences.userNodeForPackage(theClass);

    if (preferences == null) return false;
    
    try
    {
      String [] keys = preferences.keys();
      if (keys == null) return false;
      if (keys.length <= 0) return false;
      // made it through those checks; keys must exist and have length > 0
      return true;
    }
    catch (BackingStoreException e)
    {
      // err on the conservative side, and say that the prefs do not exist
      return false;
    }  
  }  
  
  
}





