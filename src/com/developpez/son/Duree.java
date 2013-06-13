package com.developpez.son;

/**
 * Repr�sente une dur�e
 */

public class Duree
{
  private long microsecondes;
  private int heure, minute, seconde, microseconde;
  public Duree()
  {
  }
  public Duree(long microsecondes)
  {
    this.microsecondes = microsecondes;
    long temps = this.microsecondes;
    this.microseconde = (int)(temps % 1000000L);
    temps = temps / 1000000L;
    this.seconde = (int)(temps % 60L);
    temps = temps / 60L;
    this.minute = (int)(temps % 60L);
    temps = temps / 60L;
    this.heure = (int)temps;
  }
  public Duree(int microseconde)
  {
    if(microseconde < 0)
    {
      throw new IllegalArgumentException(
          "Le nombre de microsecondes ne peut pas �tre n�gative");
    }
    if(microseconde > 999999)
    {
      throw new IllegalArgumentException("Le nombre de microsecondes ne peut pas �tre plus de 999999, sinon on a des secondes");
    }
    this.microseconde = microseconde;
    this.microsecondes = (long)this.microseconde;
  }
  public Duree(int seconde, int microseconde)
  {
    this(microseconde);
    if(seconde < 0)
    {
      throw new IllegalArgumentException(
          "Le nombre de secondes ne peut pas �tre n�gative");
    }
    if(seconde > 59)
    {
      throw new IllegalArgumentException(
          "Le nombre de secondes ne peut pas �tre plus de 59, sinon on a des minutes");
    }
    this.seconde = seconde;
    this.microsecondes += 1000000L * (long)this.seconde;
  }
  public Duree(int minute, int seconde, int microseconde)
  {
    this(seconde, microseconde);
    if(minute < 0)
    {
      throw new IllegalArgumentException(
          "Le nombre de minutes ne peut pas �tre n�gative");
    }
    if(minute > 59)
    {
      throw new IllegalArgumentException(
          "Le nombre minutes ne peut pas �tre plus de 59, sinon on a des heures");
    }
    this.minute = minute;
    this.microsecondes += 60L * 1000000L * (long)this.minute;
  }
  public Duree(int heure, int minute, int seconde, int microseconde)
  {
    this(minute, seconde, microseconde);
    if(heure < 0)
    {
      throw new IllegalArgumentException(
          "Le nombre d'heure ne peut pas �tre n�gative");
    }
    this.heure = heure;
    this.microsecondes += 60L * 60L * 1000000L * (long)this.heure;
  }
  public Duree(Duree duree)
  {
    this.microsecondes = duree.microsecondes;
    this.microseconde = duree.microseconde;
    this.seconde = duree.seconde;
    this.minute = duree.minute;
    this.heure = duree.heure;
  }
  public long enMicrosecondes()
  {
    return this.microsecondes;
  }
  public long enMillisecondes()
  {
    return this.microsecondes / 1000L;
  }
  public int getMicroseconde()
  {
    return this.microseconde;
  }
  public int getMilliseconde()
  {
    return this.microseconde / 1000;
  }
  public int getSeconde()
  {
    return this.seconde;
  }
  public int getMinute()
  {
    return this.minute;
  }
  public int getHeure()
  {
    return this.heure;
  }
}