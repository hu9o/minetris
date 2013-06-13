package com.developpez.son.exceptions;

/**
 * Excpetion g�n�rale <br>
 */

public class SonException
    extends Exception
{
  /**
   * Construit l'exception
   * @param message Message de l'exception
   */
  public SonException(String message)
  {
    super(message);
  }
}