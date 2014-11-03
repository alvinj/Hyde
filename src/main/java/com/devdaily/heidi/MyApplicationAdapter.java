package com.devdaily.heidi;

import javax.swing.JOptionPane;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class MyApplicationAdapter extends ApplicationAdapter
{
  private Hyde handler;
  
  public MyApplicationAdapter(Hyde handler)
  {
    this.handler = handler;
  }

  public void handleQuit(ApplicationEvent e)
  {
    handler.doQuitAction();
  }

  public void handlePreferences(ApplicationEvent e)
  {
    handler.doPreferencesAction();
  }

  public void handleAbout(ApplicationEvent e)
  {
    // tell the system we're handling this, so it won't display
    // the default system "about" dialog after ours is shown.
    e.setHandled(true);
    handler.handleAboutAction();
  }
}












