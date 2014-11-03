package com.devdaily.logging;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple logging class. Logs messages to the filename we're given,
 * and only logs messages at or above the current logging level.
 * 
 * This class is made a little more complicated in that our logfile may
 * not exist, in particular the first time the application is started up,
 * or if the user deletes out logging directory.
 * 
 * That's the rationale behind offering the option of writing to stderr.
 * 
 * Note: We don't want to create the logging directory, especially for this 
 * application.
 * 
 * Contract:
 * 
 *   - The caller needs to make sure the directory we're writing to exists;
 *     we won't try to create it.
 * 
 */
public class DDSimpleLogger implements DDLoggerInterface
{
  private String logFilename;
  private PrintWriter logfileWriter;

  // start off at a 'warning' level. the calling program can change this with a setter method below.
  private int currentLogLevel = DDLoggerInterface.LOG_WARNING;
  
  private static final String DATE_FORMAT_STRING = "yyyy-MM-dd hh:mm:ss";
  private static final String THREE_ARG_FORMAT_STRING = "%s | %s | %s\n";
  private static final String FOUR_ARG_FORMAT_STRING  = "%s | %s | %s\n";
  
  private boolean keepTryingToCreateLogfileOnFailure;
  private boolean writeToStderrIfCantCreateLogfile;
  
  private boolean createdFileWriterSuccessfully;
  private Throwable fileWriterStartupThrowable;
  
  
  /**
   * Throws an exception if we can't create a new PrintWriter,
   * which we use to write to the given logFilename.
   * @param logFilename
   * @throws FileNotFoundException
   */
  //
  // This would be the "normal" constructor for most applications, but b/c
  // of our unique requirements not to create the logging directory if need be,
  // i've removed it here.
  //
//  public DDSimpleLogger(String logFilename, int currentLogLevel) throws FileNotFoundException
//  {
//    this.logFilename = logFilename;
//    this.currentLogLevel = currentLogLevel;
//    createLogfileWriter();
//  }

  /**
   * This method was created solely for this application, because we can't count on the logging
   * directory to be there the first time the app is started up.
   * @param logFilename
   * @param currentLogLevel
   * @param keepTryingToCreateLogfileOnFailure
   * @param writeToStderrIfCantCreateLogfile
   * @throws FileNotFoundException
   */
  public DDSimpleLogger(String logFilename, 
                        int currentLogLevel, 
                        boolean keepTryingToCreateLogfileOnFailure,
                        boolean writeToStderrIfCantCreateLogfile) 
  {
    this.logFilename = logFilename;
    this.currentLogLevel = currentLogLevel;
    this.keepTryingToCreateLogfileOnFailure = keepTryingToCreateLogfileOnFailure;
    this.writeToStderrIfCantCreateLogfile = writeToStderrIfCantCreateLogfile;
    try
    {
      createLogfileWriter();
      createdFileWriterSuccessfully = true;
      fileWriterStartupThrowable = null;
    }
    catch (IOException ioe)
    {
      // need to eat this so our object reference can be created, even if this fails.
      createdFileWriterSuccessfully = false;
      fileWriterStartupThrowable = ioe.getCause();
    }
  }
  
  /**
   * Lets the calling program know whether our Writer was created successfully or not.
   */
  public boolean createdFileWriterSuccessfully()
  {
    return createdFileWriterSuccessfully;
  }

  /**
   * Gives the calling program the reason we couldn't create our Writer.
   * This will be null if we created our Writer okay.
   */
  public Throwable getFileWriterStartupThrowable()
  {
    return fileWriterStartupThrowable;
  }

  private void createLogfileWriter() throws IOException
  {
//    logfileWriter = new PrintWriter(logFilename);
    try
    {
      logfileWriter = new PrintWriter(new FileWriter(logFilename));
    }
    catch (IOException e)
    {
      throw e;
    }
  }

  public String getLogFilename()
  {
    return this.logFilename;
  }
  
  /**
   * Logs the message if the given logLevel is >= current log level.
   * Includes the classname if a Class is given.
   */
  private void log(int logLevel, String message, Class c)
  {
    if (logfileWriter == null)
    {
      // if the logfileWriter is null, try to repair it.
      // if that fails, write to stderr.
      if (keepTryingToCreateLogfileOnFailure)
      {
        // we're going to try to create it.
        // if we can create it, we'll write the current message to it, then return.
        try
        {
          createLogfileWriter();
          writeMessageToLog(logLevel, message, c);
          return;
        }
        catch (IOException e)
        {
          // creating the logfileWriter failed.
          // now we write to stderr if we were were told to do so, otherwise
          // we return.
          if (writeToStderrIfCantCreateLogfile)
          {
            writeMessageToStderr(logLevel, message, c);
            return;
          }
          else
          {
            return;
          }
        }
      }
      else
      {
        // the logfileWriter is null, but we're not going to 
        // keep trying to create it; write to stderr if we're
        // told to, otherwise just return.
        if (writeToStderrIfCantCreateLogfile)
        {
          writeMessageToStderr(logLevel, message, c);
          return;
        }
        else
        {
          return;
        }
      }
    }
    
    // hopefully this is the normal condition; the logfileWriter is not null
    writeMessageToLog(logLevel, message, c);
  }
  
  private void writeMessageToStderr(int logLevel, String message, Class c)
  {
    if (logLevel >= currentLogLevel)
    {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
      String formattedDate = sdf.format(new Date());

      if (c == null)
      {
        System.err.format(THREE_ARG_FORMAT_STRING, LOG_LEVEL_STRINGS[logLevel], formattedDate, message);
      }
      else
      {
        System.err.format(FOUR_ARG_FORMAT_STRING, LOG_LEVEL_STRINGS[logLevel], formattedDate, c.getName(), message);
      }
    }
  }
  
  private void writeMessageToLog(int logLevel, String message, Class c)
  {
    if (logLevel >= currentLogLevel)
    {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
      String formattedDate = sdf.format(new Date());

      if (c == null)
      {
        logfileWriter.printf(THREE_ARG_FORMAT_STRING, LOG_LEVEL_STRINGS[logLevel], formattedDate, message);
      }
      else
      {
        logfileWriter.printf(FOUR_ARG_FORMAT_STRING, LOG_LEVEL_STRINGS[logLevel], formattedDate, c.getName(), message);
      }
    }
  }

  public void logDebug(String message)
  {
    log(LOG_DEBUG, message, null);
  }

  public void logError(String message)
  {
    log(LOG_ERROR, message, null);
  }

  public void logFatal(String message)
  {
    log(LOG_FATAL, message, null);
  }

  public void logTrace(String message)
  {
    log(LOG_TRACE, message, null);
  }

  public void logWarning(String message)
  {
    log(LOG_WARNING, message, null);
  }

  public void logDebug(Class c, String message)
  {
    log(LOG_DEBUG, message, c);
  }

  public void logError(Class c, String message)
  {
    log(LOG_ERROR, message, c);
  }

  public void logFatal(Class c, String message)
  {
    log(LOG_FATAL, message, c);
  }

  public void logTrace(Class c, String message)
  {
    log(LOG_TRACE, message, c);
  }

  public void logWarning(Class c, String message)
  {
    log(LOG_WARNING, message, c);
  }

  public void setCurrentLogLevel(int newLogLevel)
  {
    if (newLogLevel >= LOG_DEBUG && newLogLevel <= LOG_FATAL)
    {
      currentLogLevel = newLogLevel;
    }
  }

}
