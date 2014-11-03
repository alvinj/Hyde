package com.devdaily.heidi;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import de.schlichtherle.util.ObfuscatedString;

/**
 * This class uses a fake/misleading name. It is actually used as a means
 * to store "cookies" on the user's computer system so I'll know when the
 * system was installed. This is only needed to help protect people from stealing
 * (not paying for) this application.
 * 
 * Note that this had to be a public class for the serialization to work.
 * (Actually, it had to be a class in a separate file; it wasn't working as an
 * inner class, and that's probably b/c I named it wrong.)
 */
public class DCRuntime implements Serializable
{
  private static final long serialVersionUID = -9148740033560647673L;

  private Date idXya; // installation date
  private Date fdZaf; // fake date

  // just some junk to throw people off the trail

  // array size
  private static final int AS = 10;

  private long idal;                        // install date as long
  private long fdal;                        // fake date as long
  private long[] laFcd = new long[AS];      // fake/unneeded long array
  private String rssQtV[] = new String[AS]; // fake/unneeded string array

  /* => "atKLuCcIJdbABMnoNRiYZjkOPqxrwQlmGHpyzDsvEFSTWXefUVgh" */
  private static final String AX = new ObfuscatedString(new long[]
  { 0xB148475DF0C36AA5L, 0x60BED86F6B41B84FL, 0x7186C6C7B80E30C1L, 0x65B96644D0642DC8L, 0x1946CB76DB2F2344L, 0x698382FD6ECA8761L,
      0x12A2010AA11AC95AL, 0xDD6A481C97D50E41L }).toString();

  /**
   * Tells the class that is serializing us not to write our static fields,
   * i.e., our constants. The class that is serializing us knows to check for
   * this method. See http://oreilly.com/catalog/javarmi/chapter/ch10.html
   */
  private synchronized void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException
  {
    stream.defaultWriteObject();
  }

  private static final long getSerialVersionUID()
  {
    return serialVersionUID;
  }

  public DCRuntime(Date d)
  {
    setIdal(d);

    // and now some junk here to throw people off the scent
    Random random = new Random(idXya.getTime());
    long l = random.nextLong();
    fdZaf = new Date(l);
    fdal = fdZaf.getTime();

    // more junk
    for (int i = 0; i < AS; i++)
    {
      laFcd[i] = random.nextLong();
    }

    // more junk here, this time to make each file a different size
    for (int i = 0; i < AS; i++)
    {
      int length = random.nextInt(40);
      rssQtV[i] = AX.substring(10, 10 + length);
    }
  }

  public void setIdal(Date d)
  {
    this.idXya = d;
    // junk to throw people off
    idal = d.getTime();
  }

  public Date getIdal()
  {
    return idXya;
  }

  public Date getFdal()
  {
    return fdZaf;
  }
}
