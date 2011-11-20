package com.devdaily.logging;

public interface DDLoggerInterface
{
  public final int LOG_TRACE   = 0;
  public final int LOG_DEBUG   = 1;
  public final int LOG_INFO    = 2;
  public final int LOG_WARNING = 3;
  public final int LOG_ERROR   = 4;
  public final int LOG_FATAL   = 5;
  
  String[] LOG_LEVEL_STRINGS = {"TRACE", "DEBUG", "INFO", "WARNING", "ERROR", "FATAL"};
  
  // simple methods
  public void logTrace(String message);
  public void logDebug(String message);
  public void logWarning(String message);
  public void logError(String message);
  public void logFatal(String message);

  // for when you want to include class information
  public void logTrace(Class c, String message);
  public void logDebug(Class c, String message);
  public void logWarning(Class c, String message);
  public void logError(Class c, String message);
  public void logFatal(Class c, String message);
}
