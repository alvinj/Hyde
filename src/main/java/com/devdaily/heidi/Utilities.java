package com.devdaily.heidi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.devdaily.logging.DDLoggerInterface;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class Utilities
{
  private DDLoggerInterface logger;
  
  public Utilities(DDLoggerInterface logger)
  {
    this.logger = logger;
  }

  /**
   * Plays a sound file for no longer than the given time (in millis).
   * @param canonicalSoundFilename The full path to the sound file.
   */
  public void playSound(final String canonicalSoundFilename, final long maxTimeToPlay)
  {
    try
    {
      Runnable r = new Runnable() {
        public void run()
        {
          FileInputStream fis;
          try
          {
            fis = new FileInputStream(canonicalSoundFilename);
            AudioStream audioStream = new AudioStream(fis);
            AudioPlayer.player.start(audioStream);
            AudioPlayer.player.join(maxTimeToPlay);
            fis.close();
          }
          catch (Exception e1)
          {
            logger.logError(e1.getMessage());
          }
        }
      };
      Thread thread = new Thread(r);
      thread.start();
    }
    catch (Exception e2)
    {
      logger.logError(e2.getMessage());
    }
  }
  

}
