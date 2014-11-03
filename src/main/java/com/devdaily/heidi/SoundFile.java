package com.devdaily.heidi;

//a class i can use instead of a big nasty array
public class SoundFile
{
  String newFilename;
  String appleLocation;
  
  boolean isCurrentShowCurtainSound;
  boolean isCurrentQuitCurtainSound;
  boolean isCurrentRefillCurtainSound;
  
  // this is generated dynamically, not sure if i need it here
  String description;

  public SoundFile(String newFilename, String appleLocation, String description)
  {
    this.newFilename = newFilename;
    this.appleLocation = appleLocation;
    this.description = description;
  }

  public SoundFile(String newFilename, String appleLocation)
  {
    this.newFilename = newFilename;
    this.appleLocation = appleLocation;
  }
}

