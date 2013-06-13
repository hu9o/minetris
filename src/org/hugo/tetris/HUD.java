package org.hugo.tetris;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * HUD latérale (niveau, score)
 */

public class HUD extends JPanel
{
	private static final long serialVersionUID = 3253829121930528925L;
	

	JPanel main = new JPanel();
	JPanel nextShape = new NextShape();
	JLabel scoreLabel = new JLabel("");
	JLabel levelLabel = new JLabel("");
	Board board;
	
	public HUD()
	{
		setLayout(new BorderLayout());
		add(nextShape, BorderLayout.NORTH);
		add(main, BorderLayout.CENTER);
		
		main.setLayout(new GridLayout(14, 2));

		main.add(new JLabel("score:"));
		main.add(scoreLabel);

		main.add(new JLabel("niveau:"));
		main.add(levelLabel);

		main.add(new JLabel(""));
		main.add(new JLabel(""));
		
		main.add(new JLabel("flèches"));
		main.add(new JLabel("gauche/droite"));

		main.add(new JLabel("espace"));
		main.add(new JLabel("\"hard drop\""));

		main.add(new JLabel("[CTRL]"));
		main.add(new JLabel("TNT!"));
		
		main.add(new JLabel("[P]"));
		main.add(new JLabel("pause"));

		main.add(new JLabel("[R]"));
		main.add(new JLabel("abandon"));
		
		nextShape.setPreferredSize(nextShape.getSize());
/*
		JButton pause = new JButton("[P]ause");
		JButton reset = new JButton("[R]eset");

		main.add(pause);
		main.add(reset);

		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.togglePause();
			}
		});
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.reset();
			}
		});
*/
	}
	
	public void updateNextShape()
	{
		nextShape.repaint();
	}

	public void setScore(int score)
	{
		scoreLabel.setText(String.valueOf(score));
	}
	public void setLevel(int score)
	{
		levelLabel.setText(String.valueOf(score));
	}

	public void setBoard(Board b)
	{
		board = b;
	}
	
	private class NextShape extends JPanel
	{
		private static final long serialVersionUID = -8373200055126022988L;
		private Image fond;
		private final int WIDTH = 6;
		private final int HEIGHT = 6;
		
		public NextShape()
		{
			setSize(Block.SIZE * WIDTH, Block.SIZE * HEIGHT);

			try {
				fond = ImageIO.read(new File("background.png"));
			} catch (IOException e) {
				//System.out.println("fichier introuvable");
			}
		}
		
		@Override
		public void paint(Graphics g)
		{
			g.clearRect(0, 0, getWidth(), getHeight());
			
			
			// on ne trace pas le bord gauche
			for (int j=0; j<HEIGHT; j++)
			{
				for (int i=0; i<WIDTH; i++)
				{
					int tileH = fond.getHeight(null);
					int t = (i==0 || j==0 || i==WIDTH-1 || j==HEIGHT-1)? 7:0;
					
					g.drawImage(fond, i*Block.SIZE, j*Block.SIZE, (i+1)*Block.SIZE, (j+1)*Block.SIZE,
							t*tileH, 0, (t+1)*tileH, tileH, null);
				}
			}
			
			if (board != null)
			{
				Shape s = board.getNextShape();
				
				if (s != null)
				{
					s.render(g, 2, 2);
				}
			}
		}
	}
}
