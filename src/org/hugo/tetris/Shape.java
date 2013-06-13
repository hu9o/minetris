package org.hugo.tetris;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Vector;

import com.developpez.son.Son;
import com.developpez.son.exceptions.SonException;



/**
 * Classe qui représente un tetrimino.
 * Elle est détruite quand elle est posée:
 * une fois la forme posée, les blocs passent directement dans Board.
 * 
 * Doit être étendue
 * 
 */

public abstract class Shape
{
	
	protected Board board;
	protected Vector<Block> activeBlocks = new Vector<Block>();
	protected int x, y;
	protected Block center = null;
	
	//private int dispY;
	private int normalSpeed = 0;
	protected int speed = normalSpeed;
	protected boolean endOfMove = false;
	private Son dropSound = null;
	private boolean hardDropActive = false;
	int tempSpeed = 0;
	int lockCount = Block.SIZE;
	private boolean wontMoveAnymore = false;
	
	
	protected Shape(Board b, int x, int y)
	{
		board = b;
		this.x = x;
		this.y = y;
	}

	/**
	 * FAjoute un bloc à la forme
	 * 
	 * @param tile apparence (position sur le tileset)
	 * @param x coordonnée X du bloc
	 * @param y coordonnée Y du bloc
	 */
	protected synchronized Block addBlock(int tile, int x, int y)
	{
		Block b = new Block(board);
		b.setX(x);
		b.setY(y);
		b.setTile(tile);
		
		activeBlocks.add(b);
		
		return b;
	}

	/**
	 * Fait tomber la forme selon sa vitesse.
	 */
	public void move()
	{
		if (hardDropActive)
			speed *= 1.5;
		
		moveDown(speed);
	}

	/**
	 * Faire tomber la forme d'un certain nombre de pixels.
	 * 
	 * @param y distance de chute
	 */
	public synchronized void moveDown(int y)
	{
		// si la forme est encore en mouvement
		if (!endOfMove)
		{
			//synchronized (activeBlocks)
			{			
				for (int i=0; i<y; i++)
	            {
					for (Block b : activeBlocks)
					{
						//b.move(speed);
					
		            	b.dispY++;
		            	
		            	if (b.dispY % Block.SIZE == 0)
		            	{
		            		// aligné sur la grille!
		                    b.y = b.dispY/Block.SIZE;

			            	if (board.isCollision(b, 0, 1))
			            	{
			            		// l emouvement s'arrête dès qu'on rencontre un bloc
							    endOfMove = true;
			            	}
			            	
		            		//dispY = y * SIZE;
		            	}
		            	

		                //y = Math.round(dispY/SIZE);
		            }
					
					if (endOfMove)
						break;
					
				}
				
			}
			
		}
		
		// si on a rencontré un bloc...
		if (endOfMove && !wontMoveAnymore)
		{
			// on décrémente le lockCount
			// tant que ce compteur > 0, on peut encore bouger
			lockCount -= y;
			
			// si on s'est mis dans une position où la pièce peut à nouveau tomber,
			// on reset endOfMove et le lockCount.
			endOfMove = false;
			for (Block b : activeBlocks)
			{
            	if (board.isCollision(b, 0, 1))
            	{
				    endOfMove = true;
            	}
			}
			
			if (!endOfMove)
			{
			    lockCount = Block.SIZE;
			}
		}
		
		// à la fin du lockCount, on place la pièce
		if (lockCount < 0)
			drop();
		
	}
	
	protected void drop()
	{
		wontMoveAnymore = true;
		playSound();

		board.dropShape(this, speed);
	}

	/**
	 * Fonction appelée pour faire tourner la pièce, fait les vérifications nécessaires.
	 */
	public synchronized void moveRotate()
	{
		// Appelle rotate(), et fait des vérifications.
		// La rotation sera réitérée dans l'autre sens si la pièce ne pouvait pas tourner (collision).
		
		boolean cantRotate = false;
		
		//synchronized (activeBlocks)
		{			
			rotate(1);
	
			for (Block b : activeBlocks)
			{
				if (b.y < 0 || board.isCollision(b, 0, 0) || board.isCollision(b, 0, 1))
					cantRotate = true;
			}
			
			if (cantRotate)
			{
				rotate(-1);
			}
		}
		

    	//moveDown(tempSpeed);
		
	}

	/**
	 * Règle la vitesse de chute normale
	 * 
	 * @param s vitesse de chute
	 */
	public synchronized void setSpeed(int s)
	{
		normalSpeed = s;
		setNormalSpeed();
	}

	/**
	 * Déplacement gauche/droite
	 * 
	 * @param dir direction (-1: gauche et 1: droite)
	 */
	public synchronized void moveLeftRight(int dir)
	{
		// -1: left; 1: right
		boolean cannotMove = false;
		
		
		for (Block b : activeBlocks)
		{
			if (!endOfMove)
				cannotMove = cannotMove || board.isCollision(b, dir, 1);
			else
				cannotMove = cannotMove || board.isCollision(b, dir, 0);
		}
		
		if (!cannotMove)
			for (Block b : activeBlocks)
			{
				b.setX(b.getX() + dir);
			}
	}

	/**
	 * Dessine la forme
	 */
	public synchronized void draw(Graphics2D g2d) {

		//synchronized (activeBlocks)
		{
			for (Block b : activeBlocks)
			{
				b.draw(g2d);
			}
		}
	}

	/**
	 * Fait tourner la forme CW ou CCW
	 * 
	 * @param dir direction (1: CW et -1: CCW)
	 */
    public void rotate(int dir)
    {
    	if (dir != 1 && dir != -1)
    		return;
    	
    	for (Block b : activeBlocks)
    	{
    		float tx = b.x - center.x;
    		float ty = b.y - center.y;
    		float t;
    		
			t = dir * tx;
			b.x = (int) (-dir * ty + center.x);
    		b.y = (int) (t + center.y);

    		tempSpeed = b.dispY % Block.SIZE;
    		
    		b.y = (int)(t + center.y);
    		b.dispY = b.y * Block.SIZE + (b.dispY % Block.SIZE); 		
    	}
    }

	/**
	 * Renvoie les blocs de la forme
	 * 
	 * @return les blocs
	 */
	public Vector<Block> getBlocks()
	{
		return activeBlocks;
	}

	/**
	 * Accélère la vitesse de chute de la pièce
	 */
	public synchronized void setHighSpeed()
	{
		if (center.y >= 1)
			speed = normalSpeed + 12;
		
	}

	/**
	 * Revient à la vitesse de chute normale
	 */
	public synchronized void setNormalSpeed()
	{
		speed = normalSpeed;
	}

	/**
	 * Choisit un son joué quand la forme est posée
	 * 
	 * @param path chemin du son
	 */
	protected void setSound(String path)
	{
		try
		{
	    	dropSound = new Son(new File(path));
			dropSound.setFermerALaFin(true);
	    } catch (SonException e) {
			// TODO Auto-generated catch block
			//System.out.println(e.getMessage());
			dropSound = null;
		}
	}

	protected void playSound()
	{
		if (dropSound != null)
			dropSound.jouer();
	}

	/**
	 * Fait tomber très rapidement la pièce
	 */
	public void hardDrop()
	{
		hardDropActive  = true;
	}

	public void render(Graphics g, int x, int y)
	{
		for (Block b : activeBlocks)
		{
			b.render((Graphics2D) g, b.x - center.x+x, b.y - center.y+y);
		}
	}
	
}
