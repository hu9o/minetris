package com.developpez.son.ecouteurs;

import com.developpez.son.Son;

/**
 * Indique que l'objet �coute les �v�nements sons <br>
 */

public interface EcouteurSon
{
  /**
   * Indique qu'un son est termin�
   * @param son Son qui a finit de jou�
   */
  public void sonTermine(Son son);
  /**
   * Indique qu'un son vient d'avancer sur sa lecture
   * @param son Son qui est entrain d'avancer
   */
  public void sonChangePosition(Son son);
}
