package com.developpez.son;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.developpez.son.ecouteurs.EcouteurSon;
import com.developpez.son.exceptions.SonErreurDiverse;
import com.developpez.son.exceptions.SonErreurLecture;
import com.developpez.son.exceptions.SonException;
import com.developpez.son.exceptions.SonIntrouvableException;
import com.developpez.son.exceptions.SonTypeException;


/**
 * Repr�sente un son. <br>
 * Les sons les mieux suppoter par Java sont MIDI, AU et certains WAV. <br>
 * Pour savoir si un son est acc�pt�, le seul moyen est d'essayer. <br>
 * Si le son ne sera plus utilis�, il faut le d�truire par la m�thode fermer, <br>
 * afin de lib�rer la m�moire et le flux o� se trouve le son. <br>
 * Attention, une fois d�truit, le son n'est plus utilisable. <br>
 */

public class Son
    implements Runnable
{
  //Dur�e du son
  private Duree duree;
  //Flux de kecture audio
  private AudioInputStream lecteurAudio;
  //Format du fichier audio
  private AudioFileFormat formatFichier;
  //Format du son
  private AudioFormat format;
  //Clip jouant le son
  private Clip clip;
  //Thread permettant de jouer le son en t�che de fond
  private Thread thread;
  //Nombre de boucle restante � effectu�
  private int tour;
  //pause : inqique si le son est en pause ou non
  //fermerALaFin : indique si le son doit �tre d�truit une fois la derni�re boucle de son ex�cut�e
  private boolean pause, fermerALaFin;
  //Ecouteurs des �v�nement sons
  private Vector ecouteurs = new Vector();
  /**
   * Construit un son situ� � une URL pr�cise
   * @param url URL du son
   * @throws SonException Si il y a un probl�me de construction du son
   */
  public Son(URL url)
      throws SonException
  {
    this.initialise(url);
  }
  /**
   * Construit un son � partir d'un fichier
   * @param fichier Fichier contenant le son
   * @throws SonException Si il y a un probl�me de construction du son
   */
  public Son(File fichier)
      throws SonException
  {
    if(!fichier.exists())
    {
      throw new SonIntrouvableException(fichier);
    }
    this.initialise(fichier);
  }
  //Initialise le son
  private void initialise(File fichier)
      throws SonException
  {
    try
    {
      //Cr�e le flux
      this.lecteurAudio = AudioSystem.getAudioInputStream(fichier);
      //R�cup�re le format du fichier son
      this.formatFichier = AudioSystem.getAudioFileFormat(fichier);
      //R�cup�re le format de codage du son
      this.format = lecteurAudio.getFormat();

      //On ne peut pas ouvrir directement des format ALAW/ULA, il faut les convertir en PCM
      if((this.format.getEncoding() == AudioFormat.Encoding.ULAW) ||
         (this.format.getEncoding() == AudioFormat.Encoding.ALAW))
      {
        //convertion du format
        AudioFormat tmp = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            this.format.getSampleRate(),
            this.format.getSampleSizeInBits() * 2,
            this.format.getChannels(),
            this.format.getFrameSize() * 2,
            this.format.getFrameRate(),
            true);
        //convertion du flux
        this.lecteurAudio = AudioSystem.getAudioInputStream(tmp,
                                                            this.lecteurAudio);
        //On a convertit le format, si bien qu'il change
        this.format = tmp;
      }
      //On cr�e une information avec le format du flux et en caculant la logueneur totale du son
      DataLine.Info info = new DataLine.Info(
          Clip.class,
          this.lecteurAudio.getFormat(),
          ((int)this.lecteurAudio.getFrameLength() *
           this.format.getFrameSize()));
      //Grac � cette information, on peut creer un clip
      this.clip = (Clip)AudioSystem.getLine(info);
      //On ouvre le son
      reouvrir();
    }
    catch(UnsupportedAudioFileException uafe)
    {
      throw new SonTypeException();
    }
    catch(IOException ioe)
    {
      throw new SonErreurLecture();
    }
    catch(Exception e)
    {
      throw new SonErreurDiverse(e);
    }

    //On calcul la dur�e du son en microseconde
    this.duree = new Duree(this.longueurSonMicroseconde());
  }
  private void initialise(URL url)
      throws SonException
  {
    try
    {
      //Cr�e le flux
      this.lecteurAudio = AudioSystem.getAudioInputStream(url);
      //R�cup�re le format du fichier son
      this.formatFichier = AudioSystem.getAudioFileFormat(url);
      //R�cup�re le format de codage du son
      this.format = lecteurAudio.getFormat();

      //On ne peut pas ouvrir directement des format ALAW/ULA, il faut les convertir en PCM
      if((this.format.getEncoding() == AudioFormat.Encoding.ULAW) ||
         (this.format.getEncoding() == AudioFormat.Encoding.ALAW))
      {
        //convertion du format
        AudioFormat tmp = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            this.format.getSampleRate(),
            this.format.getSampleSizeInBits() * 2,
            this.format.getChannels(),
            this.format.getFrameSize() * 2,
            this.format.getFrameRate(),
            true);
        //convertion du flux
        this.lecteurAudio = AudioSystem.getAudioInputStream(tmp,
                                                            this.lecteurAudio);
        //On a convertit le format, si bien qu'il change
        this.format = tmp;
      }
      //On cr�e une information avec le format du flux et en caculant la logueneur totale du son
      DataLine.Info info = new DataLine.Info(
          Clip.class,
          this.lecteurAudio.getFormat(),
          ((int)this.lecteurAudio.getFrameLength() *
           this.format.getFrameSize()));
      //Grac � cette information, on peut creer un clip
      this.clip = (Clip)AudioSystem.getLine(info);
      //On ouvre le son
      reouvrir();
    }
    catch(UnsupportedAudioFileException uafe)
    {
      throw new SonTypeException();
    }
    catch(IOException ioe)
    {
      throw new SonErreurLecture();
    }
    catch(Exception e)
    {
      throw new SonErreurDiverse(e);
    }

    //On calcul la dur�e du son en microseconde
    this.duree = new Duree(this.longueurSonMicroseconde());
  }
  /**
   * Joue le son une fois
   */
  public void jouer()
  {
    //Si le son n'est pas initialiser, on l'initialise
    if(this.thread == null)
    {
      this.thread = new Thread(this);
      this.thread.start();
    }
    //On va le jouer une fois
    this.tour = 1;
  }
  /**
   * Joue le son plusieurs fois
   * @param nbFois Nombre de fois que le son est jou�
   */
  public void boucle(int nbFois)
  {
    //Si le son n'est pas initialiser, on l'initialise
    if(this.thread == null)
    {
      this.thread = new Thread(this);
      this.thread.start();
    }
    //On va le jouer nbFois fois
    this.tour = nbFois;
  }
  /**
   * Joue le son un tn tr�s grand nombre de fois
   */
  public void boucle()
  {
    this.boucle(Integer.MAX_VALUE);
  }
  /**
   * Action du son, ne jamais appel� cette m�thode directement, elle est public pour respecter l'impl�mentation de Runnable
   */
  public void run()
  {
    //Tant que le son est vivant
    while(this.thread != null)
    {
      //Pause de 0.123 seconde
      try
      {
        Thread.sleep(123);
      }
      catch(Exception e)
      {}
      
      //Si on doit jouer le son au moins une fois
      if(this.tour > 0)
      {
        //On lance le son
        this.clip.start();
        //pause de 0.099 seconde (le son est jouer pendant ce temps)
        try
        {
          Thread.sleep(99);
        }
        catch(Exception e)
        {
      	  //System.out.println(e.getMessage());
        }
        //Tant que le son n'est pas terminer ou que l'on soit en pause et est vivant
        while((this.clip.isRunning() || this.pause) && (this.thread != null))
        {
          //Si on est pas en pause, on avance sur le son
          if(!this.pause)
          {
        	  ////System.out.println("son avance");
            this.avancer();
          }

    	  ////System.out.println(this.clip.isActive());
          
          //Pause de 0.099 seconde
          try
          {
            Thread.sleep(99);
          }
          catch(Exception e)
          {
            break;
          }
        }
        //Arr�te le son
        this.clip.stop();
        //On se place au d�but du son
        this.placeMicroseconde(0);
        //On � un tour de moins � jouer
        this.tour--;
        if(this.tour < 1)
        {
          //Si on a fini de jouer, on tremine
          this.terminer();
          //Si on doit fermer � la fin, on ferme d�finitivement le son
          if(this.fermerALaFin)
          {
            this.fermer();
          }
        }
      }
    }
  }
  //Permet de r�ouvrir le son, ou de l'ouvrir
  private void reouvrir()
      throws Exception
  {
    this.clip.open(this.lecteurAudio);
  }
  /**
   * Met le son en pause
   */
  public void pause()
  {
    //Si on est pas d�j� en pause, on se met en pause
    if(!this.pause)
    {
      this.clip.stop();
      this.pause = true;
    }
  }
  /**
   * Reprend le son ou il �tait rendu (enl�ve la pause)
   */
  public void reprise()
  {
    //Si on est en pause, on enl�ve la pause
    if(this.pause)
    {
      pause = false;
      this.clip.start();
    }
  }
  /**
   * Arr�te de jouer le son et retour du son au d�but
   */
  public void stop()
  {
    this.clip.stop();
    this.placeMicroseconde(0);
    this.pause = false;
    this.tour = 0;
    this.thread = null;
  }
  /**
   * D�truit proprement le son
   */
  public void fermer()
  {
    this.stop();
    this.clip.close();
    this.clip = null;
    this.duree = null;
    this.ecouteurs.clear();
    this.ecouteurs = null;
    this.format = null;
    this.formatFichier = null;
  }
  /**
   * Indique si le son sera d�truit apr�s sa derni�re fois ou il joue
   * @return <b>true</b> si le son est d�truit quand c'est finit
   */
  public boolean estFermerALaFin()
  {
    return fermerALaFin;
  }
  /**
   * Change l'�tat de fermeture � la fin
   * @param fermer <b>true</b> pour indiqu� que l'on d�sire que le son soit d�truit apr�s la derni�re fois qu'il joue
   */
  public void setFermerALaFin(boolean fermer)
  {
    this.fermerALaFin = fermer;
  }
  /**
   * Longeur du son en microseconde
   * @return Longueur du son
   */
  public long longueurSonMicroseconde()
  {
    return this.clip.getMicrosecondLength();
  }
  /**
   * Nombre de microsecondes �coul�es depuis le d�but du son
   * @return Dur�e en microseconde de l'�coute
   */
  public long getRenduMicroseconde()
  {
    return this.clip.getMicrosecondPosition();
  }
  /**
   * Dur�e de l'�coute
   * @return Dur�e de l'�coute
   */
  public Duree getRendu()
  {
    return new Duree(this.getRenduMicroseconde());
  }
  /**
   * Place le son � cette dur�e en milliseconde.
   * @param microseconde Place � laquelle on d�sire commenc� le son
   */
  public void placeMicroseconde(long microseconde)
  {
    this.clip.setMicrosecondPosition(microseconde);
  }
  /**
   * Place le son � cette dur�e
   * @param duree Place � laquelle on d�sire commenc� le son
   */
  public void placeDuree(Duree duree)
  {
    this.placeMicroseconde(duree.getMicroseconde());
  }
  /**
   * Remet le son au d�part
   */
  public void placeDepart()
  {
    this.clip.setMicrosecondPosition(0);
  }
  /**
   * Indique si le son est en pause
   * @return <b> true</b> si le son est en pause
   */
  public boolean estEnPause()
  {
    return this.pause;
  }
  /**
   * Indique si le son est entrain d'�tre jouer
   * @return <b>true</b> si le son est entrain d'�tre jou�
   */
  public boolean estEntrainDeJouer()
  {
    return!this.pause && (this.tour > 0);
  }
  /**
   * Ajout un �couteur d'�v�nement son
   * @param ecouteur Ecouteur ajout�
   */
  public void ajouteEcouteurSon(EcouteurSon ecouteur)
  {
    if(ecouteur != null)
    {
      this.ecouteurs.addElement(ecouteur);
    }
  }
  /**
   * Retire un �couteur d'�v�nement son
   * @param ecouteur Ecouteur retir�
   */
  public void retireEcouteurSon(EcouteurSon ecouteur)
  {
    if(ecouteur != null)
    {
      this.ecouteurs.removeElement(ecouteur);
    }
  }
  //Indique � tout les �couteurs d'�v�nements son, que le son est termin�
  private void terminer()
  {
    Thread t = new Thread()
    {
      public void run()
      {
        Son.this.terminer1();
      }
    };
    t.start();
  }
  //Indique � tout les �couteurs d'�v�nements son, que le son est termin�
  private void terminer1()
  {
	  if (this.ecouteurs == null)
		  return;
    int nb = this.ecouteurs.size();
    for(int i = 0; i < nb; i++)
    {
      EcouteurSon ecouteur = (EcouteurSon)this.ecouteurs.elementAt(i);
      ecouteur.sonTermine(this);
    }
  }
  //Indique � tout les �couteurs d'�v�nements son, que le son a avanc�
  private void avancer()
  {
    Thread t = new Thread()
    {
      public void run()
      {
        Son.this.avancer1();
      }
    };
    t.start();
  }
  //Indique � tout les �couteurs d'�v�nements son, que le son a avanc�
  private void avancer1()
  {
    int nb = this.ecouteurs.size();
    for(int i = 0; i < nb; i++)
    {
      EcouteurSon ecouteur = (EcouteurSon)this.ecouteurs.elementAt(i);
      ecouteur.sonChangePosition(this);
    }
  }
  /**
   * Renvoie la dur�e du son
   * @return La dur�e du son
   */
  public Duree getDuree()
  {
    return this.duree;
  }
}