package com.developpez.son.exceptions;

/**
 * Exception lev�e pour signaler une erreur de lecture du son <br>
 */

public class SonErreurLecture
    extends SonException
{
  /**
   * Construit l'exception
   */
  public SonErreurLecture()
  {
    super("Erreur lors de la lecture du son");
  }

}