package com.devdaily.heidi;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

public class ChooseColorAction extends MyAbstractAction
{
  public ChooseColorAction(CurtainFrame desktopShieldFrame, String name, KeyStroke keystroke)
  {
    super(desktopShieldFrame, name, null);
    setupMnemonicAndAccelerator(keystroke);
  }

  public void actionPerformed(ActionEvent e)
  {
    desktopShieldFrame.doChooseColorAction();
  }
}


