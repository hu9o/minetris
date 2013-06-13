package com.developpez.son.exceptions;

/**
 * Exception lev�e pour signale un manque de droit, un flux coup� en cours de routez, une erreur ... <br>
 */

public class SonErreurDiverse
    extends SonException
{
  /**
   * Construit l'exception
   * @param e Exception g�n�r�e � la construction du son
   */
  public SonErreurDiverse(Exception e)
  {
    super("Une erreur s'est produite lors de l'analyse du son : " +
          e.getMessage());
  }
}