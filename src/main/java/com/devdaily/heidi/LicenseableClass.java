package com.devdaily.heidi;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A class that implements this interface can work with our LicenseController class.
 * Put another way, the LicenseController class only works with a class that 
 * implements this interface.
 */
public interface LicenseableClass
{
  public String getApplicationName();
  public InputStream getPublicKeystoreAsInputStream() throws FileNotFoundException;
  public String getAlias();
  public String getPublicKeystorePassword();
  public String getCipherParamPassword();
  public Class getClassToLicense();
  public void handleVerifyLicenseFailedEvent();
  
  // ftp support
  public String getFtpAlias();
  public String getFtpKeyPwd();


}
