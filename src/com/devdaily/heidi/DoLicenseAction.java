package com.devdaily.heidi;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

public class DoLicenseAction extends MyAbstractAction
{
  public DoLicenseAction(CurtainFrame desktopShieldFrame, String name, KeyStroke keystroke)
  {
    super(desktopShieldFrame, name, null);
    setupMnemonicAndAccelerator(keystroke);
  }

  public void actionPerformed(ActionEvent e)
  {
    desktopShieldFrame.doInstallLicenseAction();
  }
}

