package com.devdaily.heidi;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public abstract class MyAbstractAction extends AbstractAction
{
  protected CurtainFrame desktopShieldFrame;
  
  public MyAbstractAction(CurtainFrame desktopShieldFrame, String name, Icon icon)
  {
    super(name, icon);
    this.desktopShieldFrame = desktopShieldFrame;
  }
  
  void setupMnemonicAndAccelerator(KeyStroke keystroke)
  {
    putValue(MNEMONIC_KEY, keystroke.getKeyCode());
    putValue(ACCELERATOR_KEY, keystroke);
  }

}
