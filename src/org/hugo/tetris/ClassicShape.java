package org.hugo.tetris;

/*
 * Tétrimino "classique": 7 formes
 * 
 */

public class ClassicShape extends Shape
{
	
	private final int[][][] shapes = {{{-1, 0}, {1, 0}, {0, -1}}, 
						{{0, -1}, {1, -1}, {0, 1}},
						{{0, -1}, {-1, -1}, {0, 1}},
					 	{{1, 0}, {0, 1}, {1, 1}},
					 	{{0, -1}, {0, 1}, {0, 2}},
					 	{{1, 0}, {1, -1}, {0, 1}},
					 	{{0, -1}, {1, 0}, {1, 1}}};
	
	private final String[] materials = {"stone", "wood", "sand", "stone", "wood", "stone", "dirt"};
	
	private int shapeNb = -1;
	

	/**
	 * Classe qui représente un tetrimino.
	 * Elle est détruite quand elle est posée:
	 * une fois la forme posée, les blocs passent directement dans Board.
	 * 
	 * @param b zone de jeu à laquelle appartient la forme
	 * @param nb numéro de la forme, -1 pour une forme aléatoire
	 * @param x coordonnée X du centre
	 * @param y coordonnée Y du centre
	 */
	public ClassicShape(Board b, int nb, int x, int y)
	{
		super(b, x, y);
		
		shapeNb = nb;
		
		// aléatoire
		if (shapeNb < 0)
			shapeNb = (int) Math.floor(Math.random() * shapes.length);

		setSound(materials[shapeNb] + ".wav");

		//synchronized (activeBlocks)
		{
			center = addBlock(shapeNb, x, y);
			
			for (int i = 0; i < shapes[shapeNb].length; i++)
			{
				int[] s = shapes[shapeNb][i];
				addBlock(shapeNb, s[0]+x, s[1]+y);
			}
		}
		
	}
	
}
