package org.hugo.tetris;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

/**
 * Unité des tetriminos, appartient soit au tableau,
 * soit à une pièce.
 */

public class Block {

	private enum BlockState { ACTIVE, PLACED, DEAD, TOREMOVE };
	
    //private int dx;
    //private int speed = 1;
    int x;
    int y;
    int dispY;
    private int tile;
	//private Point nextMove = new Point(0, 0);
	private Vector<Particle> particles = new Vector<Particle>();
	
	//private boolean alive = true;
	//private boolean isToRemove = false;
	BlockState state;
	//private Board board;
	private int stepD;
	private int deathCount = 12;

	private static Image image = null;
	private Board board = null;
    
    public static final int SIZE = 32;
    
    int blastStrength = 2;
    float boomOffset = 0;
	private float sineCount = 0;
	

    
    public Block(Board b) {
    	
    	board = b;
    	state = BlockState.ACTIVE;
    	
    	try {
        	if (image == null)
        	{
        		image = ImageIO.read(new File("tiles.png"));
        	}
		} catch (IOException e) {
			//System.out.println("image introuvable");
		}

    	
        x = 2;
        y = 2;
        
    }


    public void move(int speed) {

    	if (state == BlockState.ACTIVE)
    	{

    	}
    	else if (state == BlockState.PLACED)
    	{
    		if (stepD > 0)
    		{
    			for (int i=0; i<speed; i++)
                {
                	dispY++;
                	
                	if (dispY % SIZE == 0)
                	{
                		// aligné sur la grille!
                        y = dispY/SIZE;
                		
                		stepD--;
                		break;
                	}
                }
    		}
    	}
    	else if (state == BlockState.DEAD)
    	{
    		for (int i=0; i<particles.size(); i++)
    		{
    			Particle p = particles.get(i);
    			
    			if (p.move())
    			{
    				// la particule est effacée
    				i--;
    			}
    		}
    		
    		// mort totale de l'objet
    		deathCount--;
    		if (particles.size() == 0)
    			state = BlockState.TOREMOVE;
    	}
    }
    
    public void stepDown()
    {
    	if (state == BlockState.PLACED)
    	{
    		stepD++;
    	}
    }
    
    
    public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		dispY = y * SIZE;
	}
	

	public void setTile(int t) {
		tile = t;
	}

    
    public void place()
    {
    	y = Math.round((float)dispY / Block.SIZE);
    	dispY = y * SIZE;
    	
    	state = BlockState.PLACED;
    }
    
    public void draw(Graphics2D g2d)
    {
    	//int absX = x + this.x * Block.SIZE;
    	//int absY = dispY + this.y * Block.SIZE;

		if ((tile == 6 || tile == 8) && state == BlockState.PLACED)
		{
    		boolean blockOver = board.isCollision(this, 0, -1);
    		
			if (blockOver && tile == 8)
			{
				setTile(6);
			}
			else if (!blockOver && tile == 6)
			{
				setTile(8);
			}
		}
		
    	if (isAlive())
    	{
	    	int absX = this.x * Block.SIZE;
	    	int absY =  dispY + (int)(Math.sin(sineCount) * boomOffset * boomOffset);
	    	
	    	boomOffset *= 0.9;
	    	sineCount++;
	    	
	    	int tileSize = image.getHeight(null);
	    	
			g2d.drawImage(image, absX, absY, absX+Block.SIZE, absY+Block.SIZE,
						  tile*tileSize, 0, (tile+1)*tileSize, tileSize, null);
    	}
    	else if (state == BlockState.DEAD)
    	{
    		for (int i=0; i<particles.size(); i++)
    		{
    			Particle p = particles.get(i);

    			p.draw(g2d);
    		}

    	}
    }
    
    public void render(Graphics2D g, int x, int y)
    {
    	int absX = x * Block.SIZE;
    	int absY = y * Block.SIZE;
    	
    	int tileSize = image.getHeight(null);
    	
		g.drawImage(image, absX, absY, absX+Block.SIZE, absY+Block.SIZE,
					  tile*tileSize, 0, (tile+1)*tileSize, tileSize, null);
    }

    public void kill()
    {
    	kill(true);
    }
    
    public void kill(boolean sound)
    {
    	if (isAlive())
    	{
	    	int nbParts = (Block.SIZE / Particle.SIZE);
	    	state = BlockState.DEAD;
	    	
	    	for (int i=0; i<nbParts; i++)
	    	{
	    		for (int j=0; j<nbParts; j++)
	    		{
	    			particles.add(new Particle(i - nbParts/2, j - nbParts/2));
	    		}
	    	}
	    	
    	}
    	
    }
    
    
    private class Particle
    {
    	public static final int SIZE = 8;
    	public int absX, absY;
    	private int tileX, tileY;
    	private float dx, dy;
    	
		private int tileSize;
		private int sx;
		private int sy;
		private int particleSizeOnTileset;
    	
    	public Particle(int _x, int _y)
    	{
    		int bx = x*Block.SIZE + Block.SIZE/2;
    		int by = y*Block.SIZE + Block.SIZE/2;
    		
    		absX = bx + _x*Particle.SIZE;
    		absY = by + _y*Particle.SIZE;
    		
    		tileX = _x;
    		tileY = _y;
    		
    		float angle = (float) Math.atan2(_y, _x);

    		float speed = (float) (Math.random()*5 + blastStrength);
    		
    		dx = (float) (Math.cos(angle) * speed);
    		dy = (float) (Math.sin(angle) * speed);
    		

        	tileSize = image.getHeight(null);
        	particleSizeOnTileset = Particle.SIZE * tileSize / Block.SIZE;

        	sx = tile*tileSize+tileSize/2+tileX*particleSizeOnTileset;
        	sy = tileSize/2+tileY*particleSizeOnTileset;
    	}
    	
    	public void draw(Graphics2D g2d)
        {
        	//int absX = x + this.x * Block.SIZE;
        	//int absY = dispY + this.y * Block.SIZE;
        	
    		g2d.drawImage(image, absX, absY, absX+Particle.SIZE, absY+Particle.SIZE,
    					  sx, sy,
    					  sx+particleSizeOnTileset, sy+particleSizeOnTileset, null);
        }
    	
    	public boolean move()
    	{
    		absX += dx;
    		absY += dy;
    		
    		dy += 1.5;
    		dx *= 0.9;
    		
    		if (absY > Board.ZONE_HY * Block.SIZE)
    		{
    			removeParticle(this);
    			return true;
    		}
    		else
    			return false;
    	}
    
    }

	public boolean isAlive() {
		return state != BlockState.DEAD &&
		       state != BlockState.TOREMOVE;
	}
	public boolean isToRemove() {
		return state == BlockState.TOREMOVE;
	}


	public void removeParticle(Particle particle) {
		particles.remove(particle);
	}


	public void setTNTExplosion() {
		blastStrength = 14;
	}

}
