package org.hugo.tetris;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.*;


public class MainWin extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private Board board;

	public MainWin()
	{
		this.setLayout(new BorderLayout());

		// panneau latéral
		HUD sidePanel = new HUD();
		add(sidePanel, BorderLayout.CENTER);
		sidePanel.setSize(100, 640);
		
		// zone de jeu
		board = new Board(sidePanel);
		board.setPreferredSize(board.getSize());
		add(board, BorderLayout.WEST);
		//addKeyListener(board.getKAdapter());

		// on s'assure que la zone de jeu garde le focus
		board.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				board.requestFocus();
			}
		});
		
		
		// propriétés...
	    setTitle("Minetris");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setBounds(new Rectangle(0, 0, 584, board.getHeight()+28));
	    setLocationRelativeTo(null);
	    setVisible(true);
	    setResizable(true);
	    
	    board.requestFocus();
	    
	    
	    
	    Music m = new Music("music-263722.mp3");
	    m.run();
	    try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	}
	
	private class Music extends Thread
	{
		private String path = "";
		
		public Music(String p)
		{
			path = p;
		}
		
		@Override
		public void run() {
			
			jlp player = new jlp(path);
			
			try
			{
				while (true)
				{
					player.play();
				}
			}
			catch (JavaLayerException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
