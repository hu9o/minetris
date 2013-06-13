package org.hugo.tetris;

/*
 * Bonus TNT.
 * 
 */
public class BombShape extends Shape
{
	int bombTimer = 64;
	
	public BombShape(Board b, int x, int y)
	{
		super(b, x, y);
		
		setSound("explode.wav");
		
		center = addBlock(7, x, y);
		center.setTNTExplosion();
		
	}
	
	@Override
	protected void drop()
	{
		for (int j=-3; j<=3; j++)
		{
			for (int i=-3; i<=3; i++)
			{
				Block b = board.getBlockAt(center.x+i, center.y+j);
				
				if (b != null && b.isAlive() && i*i+j*j <= 3*3)
				{
					b.setTNTExplosion();
					b.kill();
				}
			}
			
			//board.checkForLine(center.y+j);
		}
		
		super.drop();
		
		center.kill();
	}
}
