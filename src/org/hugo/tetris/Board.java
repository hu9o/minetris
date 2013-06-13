package org.hugo.tetris;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class Board extends JPanel
{
	
	private static final long serialVersionUID = 2L;
	
	public static final int ZONE_X = 1;
	public static final int ZONE_Y = 1;
	public static final int ZONE_W = 10;
	public static final int ZONE_H = 20;
	public static final int ZONE_WX = ZONE_X + ZONE_W;
	public static final int ZONE_HY = ZONE_Y + ZONE_H;
	public static final int ZONE_LIMIT = 2;
	public static final int SPAWN_X = ZONE_X+ZONE_W/2-1;
	public static final int SPAWN_Y = 1;
	
	private static final int[] LINES_SCORES = {0, 10, 30, 50, 80};
	

	Vector<Block> blocks = new Vector<Block>();
	Vector<Block> toKill = new Vector<Block>();
	private Vector<Block> toStepDown = new Vector<Block>();
	//Vector<Block> activeBlocks = new Vector<Block>();
	//KAdapter kadapter = new KAdapter();
	Background bg = new Background();

	private Shape currentShape = null;
	private Shape nextShape = null;
	
	private boolean gameOver = false;
	private int score;
	private int linesCleared;
	private int level;
	private int TNTleft = 0;
	
	private Message message = new Message(100, 100);


	private boolean gamePaused = false;

	private HUD hud;

	private KeyListener kadapter = new KAdapter();
	
	
	public Board(HUD h)
	{
		hud = h;
		h.setBoard(this);

		setLayout(new FlowLayout());
	    setSize((ZONE_WX+1)*Block.SIZE, (ZONE_HY+1)*Block.SIZE);
	    
	    // on met gameOver à true, permet de commencer une nouvelle partie.
	    gameOver = true;
	    
		addKeyListener(kadapter);
		requestFocus();
		
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				gamePaused = true;
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
	    
	    // tout initialiser avant ça:
	    
		Timer time = new Timer();
		time.schedule(new TimerTask()
						{
							public void run() {
								update();
							}
						}, 0, 40);
		
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

		bg.paint(g2d);

		synchronized (blocks)
		{
			Iterator<Block> it = blocks.iterator();
			
			while (it.hasNext())
			{
				Block b = it.next();
				
				b.draw(g2d);
			}
		}

		if (currentShape != null)
			currentShape.draw(g2d);
		
		message.draw(g2d);
	}
	

	/**
	 * Ajoute la forme suivante à la zone de eu, et génère une autre forme qui tombera après
	 */
	public void addShape()
	{		
		currentShape = nextShape;
		currentShape.setSpeed(level*1+3);
		
		nextShape = new ClassicShape(this, -1, SPAWN_X, SPAWN_Y);
		hud.updateNextShape();
	}

	/**
	 * Détruit les blocs marqués à la destruction,
	 * puis passe à la forme suivante.
	 * 
	 * Détruit tous les blocs en cas de gameOver.
	 */
	private void nextTurn()
	{
		//System.out.println(toKill.size());
		if (toKill.size() > 0)
		{
			//int nbLignes = 1;
			
			//for (; nbLignes > 0 && toKill.size() > 0; nbLignes--)
			{
				int i = (int) Math.floor( Math.random() * toKill.size() );
				toKill.get(i).kill();
				
				Iterator<Block> it = toStepDown.iterator();
				//Vector<Block> lineToStepDown = new Vector<Block>();

				while (it.hasNext())
				{
					Block b = it.next();
					
					if (b.getX() == toKill.get(i).getX() && b.getY() < toKill.get(i).getY())
					{
						//lineToStepDown.add(b);
						b.stepDown();
						it.remove();
						
						//if (toKill.contains(b))
						//{
						//	lineToStepDown.clear();
						//	break;
						//}
					}
				}
				
				/*
				for (Block b : lineToStepDown)
				{
					b.stepDown();
					toStepDown.remove(b);
				}
				lineToStepDown.clear();
				*/

				toKill.remove(i);
			}
			
		}
		else
		{
			// il peut y avoir des stepDown sans kill:
			for (Block b : toStepDown)
			{
				b.stepDown();
			}
			toStepDown.clear();
			
			/////
			Block b = getHighestBlock();
			if (b != null && b.getY() < 3)
			{
				System.out.print("game over: ");
				//System.out.println(score);
				gameOver = true;
			}
			
			
			if (!gameOver)
			{
				// ajoute un tetrimino au milieu de la zone de jeu
				addShape();
			}
			else
			{
				// game over: on détruit un des blocs les plus hauts usqu'à ce qu'il n'y ait plus rien
				
				b = getHighestBlock();
				if (b != null)
				{
					b.kill();
				}
				else
				{
					//System.out.println("start");
					resetStats();
					gameOver = false;
					
					// change pièce suivante
					nextShape = new ClassicShape(this, -1, SPAWN_X, SPAWN_Y);
					hud.updateNextShape();
				}
			}
		}

	}

	/**
	 * Remet score et niveau à zéro (appelée lors d'un gameOver)
	 */
	private void resetStats()
	{
		setScore(0);
		setLevel(1);
		linesCleared = 0;
		TNTleft = 3;
	}

	/**
	 * Met le score à la valeur spécifiée,
	 * et met à jour le HUD.
	 */
	private void setScore(int s)
	{
		//message.dispMessage("+" + String.valueOf(s));
		score = s;
		hud.setScore(s);
	}

	/**
	 * Renvoie un des plus haut blocs.
	 * 
	 * @return le bloc
	 */
	private Block getHighestBlock()
	{
		if (blocks.size() == 0)
			return null;
		
		Block block = null;
		
		for (Block b : blocks)
		{
			if (block == null || (b.isAlive() && b.getY() < block.getY()))
				block = b;
		}
		
		return block;
	}

	/**
	 * Ajoute des blocs (qui viennent en général d'une Shape posée)
	 */
	public void addBlocks(Vector<Block> blockList, int strength)
	{
		blocks.addAll(blockList);
		
		int nbLines = 0;
		
		Iterator<Block> it = blockList.iterator();
		
		while (it.hasNext())
		{
			Block b = it.next();
			
			b.place();
			
			if (checkForLine(b.getY()))
			{
				nbLines++;
			}

			if (strength >= 0)
			{
				if (strength > 96)
					strength = 96;
				
				for (Block block : blocks)
				{
					if (block.getX() == b.getX() && block.getY() >= b.getY() && block.getY() <= b.getY() + 4)
					{
						int offset = (4 - (block.getY() - b.getY())) * strength/128;
						
						if (block.boomOffset < offset)
							block.boomOffset = offset;
					}
				}
			}
			
		}
		
		blockList.clear();
		
		
		linesCleared += nbLines;
		
		int lvl = linesCleared/10 + 1;
		
		if (lvl > level)
		{
			setLevel(lvl);
			message.dispMessage("LEVEL UP!");
		}
		else if (nbLines == 4)
		{
			message.dispMessage("TETRIS!");
		}
		
		setScore(score + LINES_SCORES[nbLines]);
		
		if (nbLines > 0)
		{
			System.out.print("score: ");
			//System.out.println(score);
			System.out.print("level: ");
			//System.out.println(level);
			//System.out.println();
		}
		
	}

	private void setLevel(int lvl)
	{
		level = lvl;
		hud.setLevel(level);
	}

	/**
	 * Renvoie l'objet KAdapter qui gère les entrées clavier
	 */
	public KeyListener getKAdapter()
	{
		return kadapter;
	}

	/**
	 * Déplace les blocs/formes et rafraîchit l'affichage
	 */
	public void update()
	{
		if (!gamePaused )
		{
			if (currentShape != null)
				currentShape.move();
			else
			{
				//System.out.println("next1");
				nextTurn();
			}
				
			moveBlocks();
		}
	
		this.repaint();
	}


	/**
	 * Déplace et supprime les blocs marqués à la destruction
	 */
	private void moveBlocks()
	{
		Block b;
		Iterator<Block> it = blocks.iterator();

		while (it.hasNext())
		{
			b = it.next();
			
			b.move(5);
		}
		
		synchronized (blocks)
		{
			it = blocks.iterator();
			
			while (it.hasNext())
			{
				b = it.next();
				
				if (b.isToRemove())
				{
					it.remove();
				}
			}
		}
		
	}

	/**
	 * Vérification de collision:
	 * Renvoie true s'il y a un bloc à la position:
	 * (bloc.x + offsetX,
	 * 	bloc.y + offsetY)
	 * 
	 * @param offsetX décalage X
	 * @param offsetY décalage Y
	 * 
	 * @return true si collision il y a
	 */
	public boolean isCollision(Block block, int offsetX, int offsetY)
	{
		int x = block.getX() + offsetX;
		int y = block.getY() + offsetY;
		
		if (y < Board.ZONE_Y || y >= ZONE_HY || x < Board.ZONE_X || x >= ZONE_WX)
			return true;
		
		for (Block b : blocks)
		{
			if (b.isAlive() && b.getX() == x && b.getY() == y)
				return true;
		}
		
		return false;
	}
	

	/**
	 * Vérifie si une ligne est remplie,
	 * et marque ses blocs "à détruire" si c'est le cas.
	 * 
	 * @param y position de la ligne à vérifier
	 * 
	 * @return true si la ligne était complète
	 */
	boolean checkForLine(int y)
	{
		int i = 0;
		boolean foundLine = false;
		
		if (y >= Board.ZONE_HY)
			return false;

		for (Block b : blocks)
		{
			if (!toKill.contains(b) && b.isAlive() && b.getY() == y)
			{
				i++;

				if (i == ZONE_W)
				{
					break;
				}
			}
		}
		

		if (i == ZONE_W)
		{
			foundLine = true;
			
			for (Block b : blocks)
			{
				if (b.getY() == y)
				{
					toKill.add(b);
				}
				else if (b.getY() < y)
				{
					toStepDown.add(b);
				}
			}
		}
		
		return foundLine;
	}

	/**
	 * Gère les évènements clavier de la zone de jeu
	 */
	class KAdapter implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			
			// ignorer les keypress si le jeu est en pause
			if (gamePaused)
				return;

			if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT)
			{
				currentShape.moveLeftRight((key == KeyEvent.VK_LEFT)? -1 : 1);
			}

			if (key == KeyEvent.VK_UP)
			{
				currentShape.moveRotate();
			}

			if (key == KeyEvent.VK_DOWN)
			{
				currentShape.setHighSpeed();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

			int key = e.getKeyCode();
			
			if (key == KeyEvent.VK_DOWN)
			{
				currentShape.setNormalSpeed();
			}

			if (key == KeyEvent.VK_P)
				togglePause();

			if (key == KeyEvent.VK_R)
				giveUp();

			if (key == KeyEvent.VK_CONTROL)
				special();

			if (key == KeyEvent.VK_SPACE)
				currentShape.hardDrop();
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			
		}
	
	}

	/**
	 * Fond de la zone de jeu
	 */
	private class Background extends JComponent
	{
		private static final long serialVersionUID = 1L;

		private Image tuile;
		BufferedImage bufImg;
		
		private int[] probas = {90, 28, 12, 6, 3, 2};
		private int[][] tetris = {{1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1},
				{0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0},
				{0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0},
				{0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1},
				{0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0}};

		private Graphics g2d;

		/**
		 * Charge le tileset
		 */
		public Background()
		{
			try {
				tuile = ImageIO.read(new File("background.png"));
			} catch (IOException e) {
				//System.out.println("fichier introuvable");
			}
			
			init();			
		}

		/**
		 * Crée l'image de fond aléatoirement à partir des tiles
		 */
		private void init()
		{
			int total = 0;
			int w = Board.ZONE_WX;
			int h = Board.ZONE_HY;
			
			// additionne les chances de chaque tile
			for (int p : probas)
			{
				total += p;
			}
			
			
			bufImg = new BufferedImage((w+1)*Block.SIZE, (h+1)*Block.SIZE, BufferedImage.TYPE_INT_ARGB);
			
			g2d = (Graphics2D) bufImg.createGraphics();

			for (int i=Board.ZONE_X-1; i<=w; i++)
			{
				for (int j=Board.ZONE_Y-1; j<=h; j++)
				{
					int t;
					
					if (i < Board.ZONE_X || i >= Board.ZONE_WX || j >= Board.ZONE_HY)
					{
						// ona ffiche la tuile des bords
						
						t = 7;
					}
					else
					{
						// sélectionne une tuile de fond au hasard
						
						int nb = (int) Math.floor(Math.random()*total);
						t = 0;
						
						for (int p : probas)
						{
							if (nb <= p)
							{
								break;
							}
							
							nb -= p;
							t++;
						}
						
					}

					blitTile(t, Block.SIZE, i, j);
					
				}
				
			}
			
			for (int i=0; i<tetris.length; i++)
			{
				for (int j=0; j<tetris[0].length; j++)
				{
					if (tetris[i][j] == 1)
						blitTile(6, Block.SIZE/2, Board.ZONE_X*2 + j, i+1);
				}
			}
		}

		/**
		 * Affiche le fond généré
		 */
		@Override
		public void paint(Graphics g)
		{
			g.drawImage(bufImg, 0, 0, null);
		}

		/**
		 * affiche une tuile sur le fond
		 * 
		 * @param tile Le numéro de la tuile
		 * @param size taille en pixels de la tuile à afficher
		 * @param x coordonnée X de destination
		 * @param y coordonnée Y de destination
		 * 
		 * @return le bloc
		 */
		private void blitTile(int tile, int size,  int x, int y)
		{
			// taille de la tuile sur le tileset (une seule tuile en hauteur)
			int tileSize = tuile.getHeight(null);
			
			g2d.drawImage(tuile, x*size, y*size, (x+1)*size, (y+1)*size,
					tile*tileSize, 0, (tile+1)*tileSize, tileSize, null);
		}
		
	}

	/**
	 * Sert à afficher un message pendant une courte durée
	 */
	private class Message
	{
		private int visibleCount = 0;
		private String message = "";
		int x, y;
		
		public Message(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public void dispMessage(String msg)
		{
			visibleCount = 36;
			message = msg;
		}
		
		public void draw(Graphics2D g2d)
		{
			if (visibleCount > 0)
			{
				g2d.setFont(new Font("04b03", 0, 48));
				//g2d.setColor(c);
				g2d.drawString(message, x, y);
				
				visibleCount--;
			}
		}
	}
	
	/**
	 * Renvoie le bloc à la position spécifiée,
	 * null s'il n'y en a pas.
	 * 
	 * @param x coordonnée X du bloc
	 * @param y coordonnée Y du bloc
	 * 
	 * @return le bloc
	 */
	public Block getBlockAt(int x, int y)
	{
		for (Block b : blocks)
		{
			if (b.getX() == x && b.getY() == y)
				return b;
		}
		
		return null;
	}

	public void dropShape(Shape s, int strength)
	{
		Vector<Block> shapesBlocks = s.getBlocks();
		addBlocks(shapesBlocks, strength);

		nextTurn();
	}
	
	private void special()
	{
		if (TNTleft > 0 && !(nextShape instanceof BombShape))
		{
			nextShape = new BombShape(this, SPAWN_X, SPAWN_Y);
			hud.updateNextShape();
			
			TNTleft--;
		}
	}

	/**
	 * Met sur pause si le jeu n'est pa sen pause;
	 * Et reprend s'il est en pause.
	 */
	public void togglePause()
	{
		gamePaused = !gamePaused;
	}

	/**
	 * Met fin à la partie
	 */
	public void giveUp()
	{
		currentShape.hardDrop();
		gameOver = true;
		//dropShape(currentShape);
	}

	/**
	 * Renvoie le prochaine pièce
	 */
	public Shape getNextShape()
	{
		return nextShape;
	}
	
}
