import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ShogiGUI extends JFrame implements MouseListener, MouseMotionListener, 
	ActionListener, ListSelectionListener, KeyListener {
	ImageIcon icon[][] = new ImageIcon[10][4];
	Color boardColor = new Color(255, 238, 203);
	
	public enum KomaType {
		Pawn(0), Rance(1), Knight(2), Silver(3), Gold(4), Bishop(5), Rook(6), King(7), Empty(8);
		private final int id;
		
		private KomaType(final int id) {
			this.id = id;
		}
		
		public int getInt() {
			return this.id;
		}
	};
	String[] komaName = {"歩", "香", "桂", "銀", "金", "角", "飛", "王", "と", "成香", "成桂", "成銀", "金", "馬", "龍", "王"};
	String[] senteGote = {"▲", "△"};
	
	ShogiData shogiData = new ShogiData();
	ShogiData shogiDataForKDB = new ShogiData();
	
	List<Kifu> kifuData = new ArrayList<Kifu>();
	List<KifuDataBase> kifuDB = new ArrayList<KifuDataBase>();
	String kifuFilePath = "./kifu/";
	String strategyFilePath = "./strategy/";
	String castleFilePath = "./castle/";
	List<StrategyData> strategyDataBase = new ArrayList<StrategyData>();
	List<StringCount> strategyCountData = new ArrayList<StringCount>();
	List<CastleData> castleDataBase = new ArrayList<CastleData>();
	List<StringCount> castleCountData = new ArrayList<StringCount>();
	
	List<PlayerData> playerDataBase = new ArrayList<PlayerData>();
	JLabel playerIconLabel[] = new JLabel[2];
	String playerIconPath = "./playerIcon/";
	
	Point mousePointDifference = new Point();
	int iconWidth;
	int iconHeight;
	CanvasBoard cv = new CanvasBoard();
	JButton button = new JButton("Initialize");
	JButton button2 = new JButton("Save");
	JButton button3 = new JButton("Load");
	JButton button4 = new JButton("Strategy");
	JButton button5 = new JButton("Castle");
	JLabel labelStrategy = new JLabel("Name");
	JLabel labelCastle = new JLabel("Name");
	JLabel labelSente = new JLabel("Sente");
	JLabel labelGote = new JLabel("Gote");
	JTextField textBoxStrategy = new JTextField();
	JTextField textBoxCastle = new JTextField();
	JTextField textBoxLoad = new JTextField();
	JTextField textBoxPlayerS = new JTextField();
	JTextField textBoxPlayerG = new JTextField();
	JCheckBox checkBoxEditMode = new JCheckBox("Edit Mode", false);
	JRadioButton radioButtonSente = new JRadioButton("Sente", true);
	JRadioButton radioButtonGote = new JRadioButton("Gote");
	JScrollPane sp = new JScrollPane();
	DefaultListModel<String> modelKifu = new DefaultListModel<String>();
	JList<String> listKifu = new JList<String>();
	JScrollPane sp2 = new JScrollPane();
	DefaultListModel<String> modelInfo = new DefaultListModel<String>();
	JList<String> listInfo = new JList<String>();
	JScrollPane sp3 = new JScrollPane();
	DefaultListModel<String> modelStrategy = new DefaultListModel<String>();
	JList<String> listStrategy = new JList<String>();
	JScrollPane sp4 = new JScrollPane();
	DefaultListModel<String> modelPlayer = new DefaultListModel<String>();
	JList<String> listPlayer = new JList<String>();
	JScrollPane sp5 = new JScrollPane();
	DefaultListModel<String> modelCastle = new DefaultListModel<String>();
	JList<String> listCastle = new JList<String>();
	Clip soundKoma;

	public static void main(String[] args) {
		ShogiGUI st = new ShogiGUI();
		st.setTitle("Shogi GUI");
		st.setVisible(true);
		st.shogiData.viewKomaOnBoard();
	}

	// -------------------------------------------------------------------------
	// ----------------------- << GUI Setting >> -------------------------------
	// -------------------------------------------------------------------------
	ShogiGUI() {		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initializeIcon();
		setSize(iconWidth*22, iconHeight*12);
		
		getContentPane().setLayout(null);
		
		shogiData.initializeKomaSetting();
		shogiData.initializeLabelSetting();
		shogiDataForKDB.initializeKomaSetting();
		initializeButtonSetting();
		initializeTextBoxSetting();
		initializePlayerIconLabel();
		initializeListBoxSetting();
		initializeCanvasSetting();
		initializeSoundSetting();
		
		contentPaneSetting();
		listenerSetting();
		
		actionForInitialize();
	}
	public void contentPaneSetting() {
		for(int x=0; x<40; x++) {
			getContentPane().add(shogiData.k[x]);
		}
		getContentPane().add(button);
		getContentPane().add(button2);
		getContentPane().add(button3);
		getContentPane().add(button4);
		getContentPane().add(button5);
		getContentPane().add(labelStrategy);
		getContentPane().add(labelCastle);
		getContentPane().add(labelSente);
		getContentPane().add(labelGote);
		getContentPane().add(playerIconLabel[0]);
		getContentPane().add(playerIconLabel[1]);
		getContentPane().add(textBoxStrategy);
		getContentPane().add(textBoxCastle);
		getContentPane().add(textBoxLoad);
		getContentPane().add(textBoxPlayerS);
		getContentPane().add(textBoxPlayerG);
		getContentPane().add(checkBoxEditMode);
		getContentPane().add(radioButtonSente);
		getContentPane().add(radioButtonGote);
		getContentPane().add(sp);
		getContentPane().add(sp2);
		getContentPane().add(sp3);
		getContentPane().add(sp4);
		getContentPane().add(sp5);
		for(int x=0; x<8; x++) {
			getContentPane().add(shogiData.labelNumOfKomaS[x]);
			getContentPane().add(shogiData.labelNumOfKomaG[x]);
		}
		getContentPane().add(cv);
	}
	public void listenerSetting() {
		addMouseListener(this);
		addMouseMotionListener(this);
		cv.addMouseListener(this);
		cv.addMouseMotionListener(this);
	}
	public void initializeSoundSetting() {
		try {
			String fileName = "./sound/Koma Oto.wav";
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
			soundKoma = AudioSystem.getClip();
			soundKoma.open(ais);
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	public void soundKoma() {
		soundKoma.stop();
		soundKoma.setFramePosition(0);
		soundKoma.start();
	}
	public void initializeCanvasSetting() {
		cv.setBounds(0, 0, this.getSize().width, this.getSize().height);
	}
	public void commonButtonSetting(JButton button, int x, int y, int width, int height) {
		button.addActionListener(this);
		button.setBounds(x, y, width, height);
	}
	public void initializeButtonSetting() {
		commonButtonSetting(button, 600, 15, 80, 20);
		commonButtonSetting(button2, 600, 35, 80, 20);
		commonButtonSetting(button3, 680, 35, 80, 20);
		commonButtonSetting(button4, 600, 55, 80, 20);
		commonButtonSetting(button5, 600, 75, 80, 20);
	}
	public void initializeTextBoxSetting() {
		labelStrategy.setBounds(685, 55, 100, 20);
		textBoxStrategy.setBounds(718, 55, 100, 20);
		labelCastle.setBounds(685, 75, 100, 20);
		textBoxCastle.setBounds(718, 75, 100, 20);
		radioButtonSente.setBounds(708, 95, 70, 10);
		radioButtonGote.setBounds(768, 95, 70, 10);
		textBoxLoad.setBounds(758, 35, 50, 20);
		labelSente.setBounds(810, 35, 100, 20);
		textBoxPlayerS.setBounds(840, 35, 100, 20);
		textBoxPlayerS.addActionListener(enterActionListener);
		labelGote.setBounds(950, 35, 100, 20);
		textBoxPlayerG.setBounds(975, 35, 100, 20);
		textBoxPlayerG.addActionListener(enterActionListener);
		checkBoxEditMode.setBounds(680, 20, 100, 10);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonSente);
		buttonGroup.add(radioButtonGote);
	}
	public void initializeListBoxSetting() {
		modelKifu.addElement("--------");
		listKifu.setModel(modelKifu);
		listKifu.addListSelectionListener(this);
		listKifu.addKeyListener(this);
		sp.getViewport().setView(listKifu);
		sp.setBounds(580, 250, 200, 100);
		
		listInfo.setModel(modelInfo);
		listInfo.addListSelectionListener(this);
		listInfo.addKeyListener(this);
		sp2.getViewport().setView(listInfo);
		sp2.setBounds(580, 350, 200, 100);
		
		listStrategy.setModel(modelStrategy);
		listStrategy.addListSelectionListener(this);
		listStrategy.addKeyListener(this);
		sp3.getViewport().setView(listStrategy);
		sp3.setBounds(780, 250, 200, 100);
		
		listCastle.setModel(modelCastle);
		listCastle.addListSelectionListener(this);
		listCastle.addKeyListener(this);
		sp5.getViewport().setView(listCastle);
		sp5.setBounds(780, 350, 200, 100);
		
		listPlayer.setModel(modelPlayer);
		listPlayer.addListSelectionListener(this);
		listPlayer.addKeyListener(this);
		sp4.getViewport().setView(listPlayer);
		sp4.setBounds(980, 250, 100, 200);
	}
	
	public void initializeIcon() {
		icon[KomaType.Pawn.getInt()][0] = new ImageIcon("./img/Pawn.png");
		icon[KomaType.Pawn.getInt()][1] = new ImageIcon("./img/Promoted Pawn.png");
		icon[KomaType.Pawn.getInt()][2] = new ImageIcon("./img/Pawn Gote.png");
		icon[KomaType.Pawn.getInt()][3] = new ImageIcon("./img/Promoted Pawn Gote.png");
		icon[KomaType.Rance.getInt()][0] = new ImageIcon("./img/Rance.png");
		icon[KomaType.Rance.getInt()][1] = new ImageIcon("./img/Promoted Rance.png");
		icon[KomaType.Rance.getInt()][2] = new ImageIcon("./img/Rance Gote.png");
		icon[KomaType.Rance.getInt()][3] = new ImageIcon("./img/Promoted Rance Gote.png");
		icon[KomaType.Knight.getInt()][0] = new ImageIcon("./img/Knight.png");
		icon[KomaType.Knight.getInt()][1] = new ImageIcon("./img/Promoted Knight.png");
		icon[KomaType.Knight.getInt()][2] = new ImageIcon("./img/Knight Gote.png");
		icon[KomaType.Knight.getInt()][3] = new ImageIcon("./img/Promoted Knight Gote.png");
		icon[KomaType.Silver.getInt()][0] = new ImageIcon("./img/Silver.png");
		icon[KomaType.Silver.getInt()][1] = new ImageIcon("./img/Promoted Silver.png");
		icon[KomaType.Silver.getInt()][2] = new ImageIcon("./img/Silver Gote.png");
		icon[KomaType.Silver.getInt()][3] = new ImageIcon("./img/Promoted Silver Gote.png");
		icon[KomaType.Gold.getInt()][0] = new ImageIcon("./img/Gold.png");
		icon[KomaType.Gold.getInt()][2] = new ImageIcon("./img/Gold Gote.png");
		icon[KomaType.Rook.getInt()][0] = new ImageIcon("./img/Rook.png");
		icon[KomaType.Rook.getInt()][1] = new ImageIcon("./img/Promoted Rook.png");
		icon[KomaType.Rook.getInt()][2] = new ImageIcon("./img/Rook Gote.png");
		icon[KomaType.Rook.getInt()][3] = new ImageIcon("./img/Promoted Rook Gote.png");
		icon[KomaType.Bishop.getInt()][0] = new ImageIcon("./img/Bishop.png");
		icon[KomaType.Bishop.getInt()][1]= new ImageIcon("./img/Promoted Bishop.png");
		icon[KomaType.Bishop.getInt()][2] = new ImageIcon("./img/Bishop Gote.png");
		icon[KomaType.Bishop.getInt()][3]= new ImageIcon("./img/Promoted Bishop Gote.png");
		icon[KomaType.King.getInt()][0] = new ImageIcon("./img/King.png");
		icon[KomaType.King.getInt()][2] = new ImageIcon("./img/King Gote.png");
		
		iconWidth = icon[KomaType.Pawn.getInt()][0].getIconWidth();
		iconHeight = icon[KomaType.Pawn.getInt()][0].getIconHeight();
	}
	public void initializePlayerIconLabel() {
		playerIconLabel[0] = new JLabel();
		playerIconLabel[1] = new JLabel();
		playerIconLabel[0].setBounds(840, 25, 100, 200);
		playerIconLabel[1].setBounds(970, 25, 100, 200);
	}
	public void clearTextBox() {
		textBoxPlayerS.setText("");
		textBoxPlayerG.setText("");
		textBoxStrategy.setText("");
		textBoxCastle.setText("");
		//textBoxLoad.setText("");
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Shogi Board >> -------------------------------
	// -------------------------------------------------------------------------
	public class ShogiData {
		Koma k[];
		Koma selectedKoma = null;
		Boolean turnIsSente;
		List<Koma> listKomaOnBoard;
		List<Koma> listKomaOnHandForSente;
		List<Koma> listKomaOnHandForGote;
		JLabel[] labelNumOfKomaS = new JLabel[8];
		JLabel[] labelNumOfKomaG = new JLabel[8];
		
		ShogiData() {
			k = new Koma[40];
			turnIsSente = true;
			listKomaOnBoard = new ArrayList<Koma>();
			listKomaOnHandForSente = new ArrayList<Koma>();
			listKomaOnHandForGote = new ArrayList<Koma>();
		}
		
		public void viewKomaOnBoard() {
			k[0].setLocation(600, 0); // なぜかCanvas枠外で一度描画が必要
			for(Koma k: listKomaOnBoard) {
				k.setOpaque(true);
				k.setLocation((9-k.px)*(iconWidth+10)+25, (k.py-1)*(iconHeight+10)+25);
			}
		}
		
		public void viewKomaOnHand() {
			int numOfKomaS[] = new int[8];
			int numOfKomaG[] = new int[8];
			
			for(int x=0; x<8; x++) {
				labelNumOfKomaS[x].setVisible(false);
				labelNumOfKomaG[x].setVisible(false);
				labelNumOfKomaS[x].setSize(20, 10);
				labelNumOfKomaG[x].setSize(20, 10);
				numOfKomaS[x] = 0;
				numOfKomaG[x] = 0;
			}
			
			for(Koma k: listKomaOnHandForSente) {
				k.setOpaque(false);
				k.setLocation((9+(k.type.getInt()%4))*(iconWidth+10)+55, (6+(k.type.getInt()/4))*(iconHeight+10)+25);
				numOfKomaS[k.type.getInt()]++;
				labelNumOfKomaS[k.type.getInt()].setText(Integer.valueOf(numOfKomaS[k.type.getInt()]).toString());
				labelNumOfKomaS[k.type.getInt()].setVisible(true);
			}
			
			for(Koma k: listKomaOnHandForGote) {
				k.setOpaque(false);
				k.setLocation((9+(k.type.getInt()%4))*(iconWidth+10)+55, (2-(k.type.getInt()/4))*(iconHeight+10)+40);
				numOfKomaG[k.type.getInt()]++;
				labelNumOfKomaG[k.type.getInt()].setText(Integer.valueOf(numOfKomaG[k.type.getInt()]).toString());
				labelNumOfKomaG[k.type.getInt()].setVisible(true);
			}
		}
		
		public void initializeKomaSetting() {
			// Sente
			for(int x=0; x<9; x++) {
				k[x] = new Koma(KomaType.Pawn, x+1, 7, 0, x);
			}
			k[9] = new Koma(KomaType.Rance, 9, 9, 0, 9);
			k[10] = new Koma(KomaType.Rance, 1, 9, 0, 10);
			k[11] = new Koma(KomaType.Knight, 8, 9, 0, 11);
			k[12] = new Koma(KomaType.Knight, 2, 9, 0, 12);
			k[13] = new Koma(KomaType.Silver, 7, 9, 0, 13);
			k[14] = new Koma(KomaType.Silver, 3, 9, 0, 14);
			k[15] = new Koma(KomaType.Gold, 6, 9, 0, 15);
			k[16] = new Koma(KomaType.Gold, 4, 9, 0, 16);
			k[17] = new Koma(KomaType.Rook, 2, 8, 0, 17);
			k[18] = new Koma(KomaType.Bishop, 8, 8, 0, 18);
			k[19] = new Koma(KomaType.King, 5, 9, 0, 19);
					
			// Gote
			for(int x=20; x<29; x++) {
				k[x] = new Koma(KomaType.Pawn, x-20+1, 3, 1, x);
			}
			k[29] = new Koma(KomaType.Rance, 9, 1, 1, 29);
			k[30] = new Koma(KomaType.Rance, 1, 1, 1, 30);
			k[31] = new Koma(KomaType.Knight, 8, 1, 1, 31);
			k[32] = new Koma(KomaType.Knight, 2, 1, 1, 32);
			k[33] = new Koma(KomaType.Silver, 7, 1, 1, 33);
			k[34] = new Koma(KomaType.Silver, 3, 1, 1, 34);
			k[35] = new Koma(KomaType.Gold, 6, 1, 1, 35);
			k[36] = new Koma(KomaType.Gold, 4, 1, 1, 36);
			k[37] = new Koma(KomaType.Rook, 8, 2, 1, 37);
			k[38] = new Koma(KomaType.Bishop, 2, 2, 1, 38);
			k[39] = new Koma(KomaType.King, 5, 1, 1, 39);
			
			for(int x=0; x<40; x++) {
				listKomaOnBoard.add(k[x]);
			}
		}
		
		public void resetAllKoma() {
			turnIsSente = true;
			listKomaOnBoard.clear();
			listKomaOnHandForSente.clear();
			listKomaOnHandForGote.clear();
			for(int x=0; x<9; x++) {
				k[x].reset(KomaType.Pawn, x+1, 7, 0);
			}
			k[9].reset(KomaType.Rance, 9, 9, 0);
			k[10].reset(KomaType.Rance, 1, 9, 0);
			k[11].reset(KomaType.Knight, 8, 9, 0);
			k[12].reset(KomaType.Knight, 2, 9, 0);
			k[13].reset(KomaType.Silver, 7, 9, 0);
			k[14].reset(KomaType.Silver, 3, 9, 0);
			k[15].reset(KomaType.Gold, 6, 9, 0);
			k[16].reset(KomaType.Gold, 4, 9, 0);
			k[17].reset(KomaType.Rook, 2, 8, 0);
			k[18].reset(KomaType.Bishop, 8, 8, 0);
			k[19].reset(KomaType.King, 5, 9, 0);
			for(int x=20; x<29; x++) {
				k[x].reset(KomaType.Pawn, x-20+1, 3, 1);
			}
			k[29].reset(KomaType.Rance, 9, 1, 1);
			k[30].reset(KomaType.Rance, 1, 1, 1);
			k[31].reset(KomaType.Knight, 8, 1, 1);
			k[32].reset(KomaType.Knight, 2, 1, 1);
			k[33].reset(KomaType.Silver, 7, 1, 1);
			k[34].reset(KomaType.Silver, 3, 1, 1);
			k[35].reset(KomaType.Gold, 6, 1, 1);
			k[36].reset(KomaType.Gold, 4, 1, 1);
			k[37].reset(KomaType.Rook, 8, 2, 1);
			k[38].reset(KomaType.Bishop, 2, 2, 1);
			k[39].reset(KomaType.King, 5, 1, 1);
			
			for(int x=0; x<40; x++) {
				listKomaOnBoard.add(k[x]);
			}
		}
		
		public Koma findTouchedKoma(Koma movedKoma) {
			for(Koma k: listKomaOnBoard) {
				if(k != movedKoma && k.px == movedKoma.px && k.py == movedKoma.py) {
					return k;
				}
			}
			
			return null;
		}
		
		public void initializeLabelSetting() {
			for(int x=0; x<8; x++) {
				labelNumOfKomaS[x] = new JLabel("0");
				labelNumOfKomaS[x].setBounds((10+(x%4))*(iconWidth+10)+38, (6+(x/4))*(iconHeight+10)+30, 80, 20);
				labelNumOfKomaS[x].setVisible(false);
				
				labelNumOfKomaG[x] = new JLabel("0");
				labelNumOfKomaG[x].setBounds((10+(x%4))*(iconWidth+10)+38, (3-(x/4))*(iconHeight+10)+15, 80, 20);
				labelNumOfKomaG[x].setVisible(false);
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Koma Action >> -------------------------------
	// -------------------------------------------------------------------------
	public class Koma extends JLabel {
		int px;
		int py;
		int promoted;
		int sente;
		int index;
		int drop;
		KomaType type;
		Koma(KomaType t, int x, int y, int s, int i) {
			this.setIcon(icon[t.getInt()][0+s*2]);
			this.setBounds((iconWidth+10)*(9-x)+25, (iconHeight+10)*(y-1)+25, iconWidth, iconHeight);
			this.setBackground(boardColor);
			this.setOpaque(true);
			type = t;
			px = x;
			py = y;
			sente = s;
			promoted = 0;
			index = i;
			drop = 0;
		}
		
		public void reset(KomaType t, int x, int y, int s) {
			type = t;
			px = x;
			py = y;
			if(sente != s) this.reverse();
			sente = s;
			if(promoted == 1) this.promote();
		}
		
		public void promote() {
			if(type == KomaType.Gold || type == KomaType.King) return;
			if(promoted == 0) {
				this.setIcon(icon[type.getInt()][sente*2+1]);
				promoted = 1;
			} else {
				this.setIcon(icon[type.getInt()][sente*2]);
				promoted = 0;
			}
		}
		
		public void reverse() {
			if(sente == 0) {
				sente = 1;
			} else {
				sente = 0;
			}
			this.setIcon(icon[type.getInt()][sente*2 + promoted]);
		}
		
		public Boolean isMovable(int x, int y) {
			switch(type) {
			case Pawn:
				return isPawnMove(x, y);
			case Rance:
				return isRanceMove(x, y);
			case Knight:
				return isKnightMove(x, y);
			case Silver:
				return isSilverMove(x, y);
			case Gold:
				return isGoldMove(x, y);
			case Bishop:
				return isBishopMove(x, y);
			case Rook:
				return isRookMove(x, y);
			case King:
				return isKingMove(x, y);
			default:
				break;
			}
			
			return false;
		}
		
		public Boolean checkKomaExistence(int x, int y, ShogiData sd) {
			for(Koma k: sd.listKomaOnBoard) if(k.px == x && k.py == y) return true;
			
			return false;
		}
		
		public Boolean isPawnMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if(x == px && y == (py-1)) return true;
			} else {
				if(x == px && y == (py+1)) return true;
			}
			return false;
		}
		
		public Boolean isRanceMove(int x, int y) {
			int ty;
			Boolean b;
			if(promoted == 1) return isGoldMove(x, y);
			if(x != px) return false;
			if(sente == 0) {
				if(y >= py) return false;
				ty = y;
				b = false;
				while(ty < py) {
					if(checkKomaExistence(x, ty, shogiData)) b = true;
					ty++;
				}
				if(b == false) return true;
			} else {
				if(y <= py) return false;
				ty = y;
				b = false;
				while(ty > py) {
					if(checkKomaExistence(x, ty, shogiData)) b = true;
					ty--;
				}
				if(b == false) return true;
			}
			return false;
		}
		
		public Boolean isKnightMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if( (x == (px+1) && y == (py-2)) || (x == (px-1)) && y == (py-2) ) return true;  
			} else {
				if( (x == (px+1) && y == (py+2)) || (x == (px-1)) && y == (py+2) ) return true;
			}
			return false;
		}
		
		public Boolean isSilverMove(int x, int y) {
			if(promoted == 1) return isGoldMove(x, y);
			if(sente == 0) {
				if( (x == px && y == (py-1)) || (x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) || 
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			} else {
				if( (x == px && y == (py+1)) || (x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) || 
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			}
			return false;
		}
		
		public Boolean isGoldMove(int x, int y) {
			if(sente == 0) {
				if( (x == px && y == (py-1)) || (x == px && y == (py+1)) || 
						(x == (px-1) && y == py) || (x == (px+1) && y == py) ||
						(x == (px+1) && y == (py-1)) || (x == (px-1) && y == (py-1)) ) return true;
			} else {
				if( (x == px && y == (py-1)) || (x == px && y == (py+1)) || 
						(x == (px-1) && y == py) || (x == (px+1) && y == py) ||
						(x == (px+1) && y == (py+1)) || (x == (px-1) && y == (py+1)) ) return true;
			}
			
			return false;
		}
		
		public Boolean isBishopMove(int x, int y) {
			int dx, dy;
			int tx, ty;
			Boolean b;
			if(promoted == 1) {
				if( (x == px && y == (py+1)) || (x == px && y == (py-1)) || 
						(x == (px+1) && y == py) || (x == (px-1) && y == py) ) return true;
			}
			
			dx = x - px;
			dy = y - py;
			if( dx != dy && dx != (-dy) ) return false;
			
			b = false;
			tx = x;
			ty = y;
			if(tx > px && ty > py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx--;
					ty--;
				}
			} else if(tx > px && ty < py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx--;
					ty++;
				}
			} else if(tx < px && ty > py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx++;
					ty--;
				}
			} else if(tx < px && ty < py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx++;
					ty++;
				}
			}
			if(b == true) return false;
			
			return true;
		}
		
		public Boolean isRookMove(int x, int y) {
			int tx, ty;
			Boolean b;
			if(promoted == 1) {
				if( (x == (px+1) && y == (py+1)) || (x == (px+1) && y == (py-1)) ||
						(x == (px-1) && y == (py+1)) || (x == (px-1) && y == (py-1)) ) return true;
			}
			
			if(x != px && y != py) return false;
			
			tx = x;
			ty = y;
			b = false;
			if(tx > px && ty == py) {
				while(tx > px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx--;
				}
			} else if(tx < px && ty == py) {
				while(tx < px) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					tx++;
				}
			} else if(tx == px && ty > py) {
				while(ty > py) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					ty--;
				}
			} else if(tx == px && ty < py) {
				while(ty < py) {
					if(checkKomaExistence(tx, ty, shogiData)) b = true;
					ty++;
				}
			}
			
			if(b == true) return false;
			
			return true;
		}
		
		public Boolean isKingMove(int x, int y) {
			if( (x == (px-1) && y == (py-1)) || (x == (px-1) && y == py) || (x == (px-1) && y == (py+1)) ||
					(x == px && y == (py-1)) || (x == px && y == (py+1)) ||
					(x == (px+1) && y == (py-1)) || (x == (px+1) && y == py) || (x == (px+1) && y == (py+1)) ) return true;
			return false;
		}
		
		public Boolean confirmPromotion(int x, int y, int preX, int preY, ShogiData sd) {
			if(this.promoted == 1) return true;
			if(this.type == KomaType.Gold || this.type == KomaType.King ) return true;
			
			if(x <= 0 || x >= 10 || y <= 0 || y >= 10) return true;
			if(sd.listKomaOnHandForSente.indexOf(this) != -1 || sd.listKomaOnHandForGote.indexOf(this) != -1) return true;
			
			if( (this.sente == 0 && y <= 3) || (this.sente == 1 && y >= 7) ||
					(this.sente == 0 && preY <= 3) || (this.sente == 1 && preY >= 7) ) {
				JFrame jf = new JFrame();
				int result = JOptionPane.showConfirmDialog(jf, "Promote?");
				if(result == 0) {
					System.out.println("Yes");
					this.promote();
					return true;
				} else if(result == 1) {
					System.out.println("No");
					return true;
				} else {
					System.out.println("Cancel");
					return false;
				}
			}
			
			return true;
		}
		
		public Boolean moveKoma(ShogiData sd, int x, int y, int promoted) {
			int preX = this.px;
			int preY = this.py;
			
			if(promoted == -1) {
				if(!this.confirmPromotion(x, y, preX, preY, sd)) {
					// case of cancel
					this.setLocation((9-preX)*(iconWidth+10)+25, (preY-1)*(iconHeight+10)+25);
					return false;
				}
			}
			else if(promoted == 1 && this.promoted == 0) this.promote(); // no confirm when kifuData
			
			this.px = x;
			this.py = y;
			this.drop = 0;
			this.setLocation((9-x)*(iconWidth+10)+25, (y-1)*(iconHeight+10)+25);
			
			if(x>0 && x<10 && y>0 && y<10) {
				if( sd.listKomaOnHandForSente.indexOf(this) != -1 ) moveFromStoB(sd);
				else if( sd.listKomaOnHandForGote.indexOf(this) != -1) moveFromGtoB(sd);
				else moveFromBtoB(sd);
			} else if(y>5) {
				if(sd.listKomaOnBoard.indexOf(this) != -1) moveFromBtoS(sd);
				else if(sd.listKomaOnHandForSente.indexOf(this) != -1) moveFromStoS(sd);
				else moveFromGtoS(sd);
				return false;
			} else {
				if(sd.listKomaOnBoard.indexOf(this) != -1) moveFromBtoG(sd);
				else if(sd.listKomaOnHandForGote.indexOf(this) != -1) moveFromGtoG(sd);
				else moveFromStoG(sd);
				return false;
			}
			
			return true;
		}
		// OnBoard -> OnBoard
		public void moveFromBtoB(ShogiData sd) {
			Koma k = sd.findTouchedKoma(this);
			if(k != null) {
				sd.listKomaOnBoard.remove(k);
				if(k.promoted == 1) k.promote();
				if(this.sente == 0) {
					if(k.sente == 1) k.reverse();
					if(k.type == KomaType.Pawn) k.px = 0;
					if(k.type == KomaType.Rance) k.px = -1;
					if(k.type == KomaType.Knight) k.px = -2;
					if(k.type == KomaType.Silver) k.px = -3;
					if(k.type == KomaType.Gold) k.px = 0;
					if(k.type == KomaType.Bishop) k.px = -1;
					if(k.type == KomaType.Rook) k.px = -2;
					if(k.type == KomaType.King) k.px = -3;
					if(k.type == KomaType.Pawn || k.type == KomaType.Rance 
							|| k.type == KomaType.Knight || k.type == KomaType.Silver) k.py = 7;
					else {
						k.py = 8;
					}
					sd.listKomaOnHandForSente.add(k);
				} else {
					if(k.sente == 0) k.reverse();
					if(k.type == KomaType.Pawn) k.px = 0;
					if(k.type == KomaType.Rance) k.px = -1;
					if(k.type == KomaType.Knight) k.px = -2;
					if(k.type == KomaType.Silver) k.px = -3;
					if(k.type == KomaType.Gold) k.px = 0;
					if(k.type == KomaType.Bishop) k.px = -1;
					if(k.type == KomaType.Rook) k.px = -2;
					if(k.type == KomaType.King) k.px = -3;
					if(k.type == KomaType.Pawn || k.type == KomaType.Rance 
							|| k.type == KomaType.Knight || k.type == KomaType.Silver) k.py = 3;
					else {
						k.py = 2;
					}
					sd.listKomaOnHandForGote.add(k);
				}
			}
		}
		// OnHandForSente -> OnBoard
		public void moveFromStoB(ShogiData sd) {
			this.drop = 1;
			sd.listKomaOnHandForSente.remove(this);
			sd.listKomaOnBoard.add(this);
		}
		// OnHandForGote -> OnBoard
		public void moveFromGtoB(ShogiData sd) {
			this.drop = 1;
			sd.listKomaOnHandForGote.remove(this);
			sd.listKomaOnBoard.add(this);
		}
		// OnBoard -> OnHandForSente
		public void moveFromBtoS(ShogiData sd) {
			sd.listKomaOnBoard.remove(this);
			
			if(this.sente == 1) this.reverse();
			if(this.promoted == 1) this.promote();
			sd.listKomaOnHandForSente.add(this);
		}
		// OnBoard -> OnHandForGote
		public void moveFromBtoG(ShogiData sd) {
			sd.listKomaOnBoard.remove(this);
			
			if(this.sente == 0) this.reverse();
			if(this.promoted == 1) this.promote();	
			sd.listKomaOnHandForGote.add(this);
		}
		// OnHandForSente -> OnHandForGote
		public void moveFromStoG(ShogiData sd) {
			sd.listKomaOnHandForSente.remove(this);
			this.reverse();
			sd.listKomaOnHandForGote.add(this);
		}
		// OnHandForGote -> OnHandForSente
		public void moveFromGtoS(ShogiData sd) {
			sd.listKomaOnHandForGote.remove(this);
			this.reverse();
			sd.listKomaOnHandForSente.add(this);
		}
		// OnHandForSente -> OnHandForSente
		public void moveFromStoS(ShogiData sd) {
			// do nothing
		}
		// OnHandForGote -> OnHandForGote
		public void moveFromGtoG(ShogiData sd) {
			// do nothing
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Canvas >> ------------------------------------
	// -------------------------------------------------------------------------
	public class CanvasBoard extends Canvas {
		int x;
		int y;
		Boolean mousePressed;
		Boolean enableLastPoint;
		List<Point> drawList = new ArrayList<Point>();
		List<Point> drawListBase = new ArrayList<Point>();
		public CanvasBoard() {
			x = -1;
			y = -1;
			mousePressed = false;
			enableLastPoint = false;
		}
		
		public void paint(Graphics g) {
			//System.out.println("repaint()");
			g.setColor(boardColor);
			g.fillRect(20, 20, (iconWidth+10)*9, (iconHeight+10)*9);
			
			g.setColor(Color.black);
			for(int x=0; x<9; x++)
				for(int y=0; y<9; y++) {
					g.drawRect(x*(iconWidth+10)+20, y*(iconHeight+10)+20, iconWidth+10, iconHeight+10);
				}
			
			if(mousePressed) {
				for(int x=1; x<10; x++) for(int y=1; y<10; y++) {
					if(shogiData.selectedKoma.isMovable(x, y)) cv.drawPoint(x, y, Color.pink);
				}
			}
			
			for(Point p: drawList) {
				Point pB = drawListBase.get(drawList.indexOf(p));
				Point pBase = new Point((9-pB.x)*(iconWidth+10)+25+iconWidth/2, (pB.y-1)*(iconHeight+10)+25+iconHeight/2);
				Point pTarget = new Point((9-p.x)*(iconWidth+10)+25+iconWidth/2, (p.y-1)*(iconHeight+10)+25+iconHeight/2);
				Arrow ar = new Arrow(pBase, pTarget);
				g.setColor(Color.blue);
				ar.draw((Graphics2D)g);
			}
			
			if(enableLastPoint) {
				drawPoint(x, y, Color.orange);
			}
		}
		
		public void drawPoint(int x, int y, Color cl) {
			Graphics g = getGraphics();
			g.setColor(cl);
			g.fillRect((9-x)*(iconWidth+10)+22, (y-1)*(iconHeight+10)+22, iconWidth+6, iconHeight+6);
		}
		
		public void setLastPoint(int px, int py, Boolean enable) {
			x = px;
			y = py;
			enableLastPoint = enable;
		}
		
		public void addDrawPoint(Point p1, Point p2) {
			drawList.add(p1);
			drawListBase.add(p2);
		}
		
		public void clearDrawPoint() {
			drawList.clear();
			drawListBase.clear();
		}
		
		public class Arrow {
			private final Point start = new Point();
			private final Point end = new Point();
			private final Path2D arrowHead;
			
			protected Arrow(Point start, Point end) {
				this.start.setLocation(start);
				this.end.setLocation(end);
				arrowHead = makeArrowHead(new Dimension(8, 8));
			}
			
			protected Path2D makeArrowHead(Dimension size) {
				Path2D path = new Path2D.Double();
				double t = size.height;
				double w = size.width * .5;
				path.moveTo(0d, -w);
				path.lineTo(t, 0d);
				path.lineTo(0d, w);
				path.closePath();
				return path;
			}
			
			public void draw(Graphics2D g2) {
				g2.drawLine(start.x, start.y, end.x, end.y);
				arrowHead.transform(AffineTransform.getRotateInstance(end.getX() - start.getX(), end.getY() - start.getY()));
				arrowHead.transform(AffineTransform.getTranslateInstance(end.getX(), end.getY()));
				g2.fill(arrowHead);
				g2.draw(arrowHead);
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Button Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForInitialize() {
		shogiData.resetAllKoma();	
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		loadStrategyData();
		loadCastleData();
		actionForDB();
		countStrategy();
		countCastle();
		createPlayerDataBase();
		clearTextBox();
		initializePlayerIcon();
	}
	public void actionForSave() {
		Path path;
		String fileName;
		int index = 1;
		
		while(true) {
			fileName = kifuFilePath + String.format("kifu%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
			fw.write(textBoxPlayerS.getText() + "\n");
			fw.write(textBoxPlayerG.getText() + "\n");
			for(Kifu kf: kifuData) fw.write(kf.k.index + "," + kf.x + "," + kf.y + "," + kf.p + "," + kf.pp + "," + kf.d + "\n");
			fw.close();
			
			System.out.println(fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void actionForLoad() {
		loadByNumber(textBoxLoad.getText());
	}
	public void loadByNumber(String numStr) {
		String fileName;
		if(numStr.equals("")) {
			Path path = Paths.get("").toAbsolutePath();
			FileDialog fd = new FileDialog(this, "Load", FileDialog.LOAD);
			fd.setDirectory(path.toString() + "/kifu/");
			fd.setVisible(true);
			if(fd.getFile() == null) return;
			fileName = kifuFilePath + fd.getFile();
		} else {
			fileName = kifuFilePath + "kifu" + numStr + ".txt";
		}
		
		// initialize before load
		shogiData.resetAllKoma();	
		shogiData.viewKomaOnBoard();
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cv.clearDrawPoint();
		clearTextBox();
				
		try {
			File file = new File(fileName);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String content;
			textBoxPlayerS.setText(br.readLine());
			textBoxPlayerG.setText(br.readLine());
			while((content = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(content,",");
				while(st.hasMoreTokens()) {
					int i = Integer.parseInt(st.nextToken()); // index
					int x = Integer.parseInt(st.nextToken()); // x
					int y = Integer.parseInt(st.nextToken()); // y
					int p = Integer.parseInt(st.nextToken()); // promote
					int pp = Integer.parseInt(st.nextToken()); // preP
					int d = Integer.parseInt(st.nextToken()); // drop
					updateListBox(shogiData.k[i].type, x, y, shogiData.k[i].sente, p, pp, d);
					Kifu kf = new Kifu(shogiData.k[i], x, y, p, pp, d);
					kifuData.add(kf);
					shogiData.k[i].moveKoma(shogiData, x, y, p);
				}
			}
			br.close();
			
			shogiData.resetAllKoma();	
			listKifu.setSelectedIndex(0);
			listKifu.ensureIndexIsVisible(0);
			
			shogiData.viewKomaOnBoard();
			shogiData.viewKomaOnHand();
			updatePlayerIcon();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void actionForDB() {
		try {
			int fileIndex = 1;
			kifuDB.clear();
			while(true) {
				shogiDataForKDB.resetAllKoma();
				String fileName = kifuFilePath + "kifu" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				KifuDataBase kdb = new KifuDataBase();
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				kdb.playerNameS = br.readLine();
				kdb.playerNameG = br.readLine();
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						int i = Integer.parseInt(st.nextToken()); // index
						int x = Integer.parseInt(st.nextToken()); // x
						int y = Integer.parseInt(st.nextToken()); // y
						int p = Integer.parseInt(st.nextToken()); // promote
						int pp = Integer.parseInt(st.nextToken()); // preP
						int d = Integer.parseInt(st.nextToken()); // drop
						Kifu kf = new Kifu(shogiData.k[i], x, y, p, pp, d);
						kdb.db.add(kf);
						shogiDataForKDB.k[kf.k.index].moveKoma(shogiDataForKDB, kf.x, kf.y, kf.p);
						if(kdb.strategyName.equals("")) {
							kdb.strategyName = checkStrategy(shogiDataForKDB);
						}
						if(kdb.castleNameS.equals("")) {
							kdb.castleNameS = checkCastle(shogiDataForKDB, true);
						}
						if(kdb.castleNameG.equals("")) {
							kdb.castleNameG = checkCastle(shogiDataForKDB, false);
						}
					}
				}
				br.close();
				kdb.isSenteWin = isSenteWin(kdb);
				kifuDB.add(kdb);
				fileIndex++;
			}
		} catch(FileNotFoundException en) {
			System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		listKifu.setSelectedIndex(0);
		commonListAction();
	}
	public void actionForStrategy() {
		Path path;
		String fileName;
		int index = 1;
		
		while(true) {
			fileName = strategyFilePath + String.format("strategy%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBoxStrategy.getText() + "\n");
			for(int i=0; i<40; i++) {
				fw.write(shogiData.k[i].px + "," + shogiData.k[i].py + "\n");
			}
			fw.close();
			
			System.out.println(fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForCastle() {
		Path path;
		String fileName;
		int index = 1;
		
		while(true) {
			fileName = castleFilePath + String.format("castle%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBoxCastle.getText() + "\n");
			for(Koma k: shogiData.k) {
				if((k.type == KomaType.King) && radioButtonSente.isSelected() && k.sente == 0) {
					saveListKomaAroundKing(shogiData, k, fw);
				}
				if((k.type == KomaType.King) && radioButtonGote.isSelected() && k.sente == 1) {
					saveListKomaAroundKing(shogiData, k, fw);
				}
			}
			fw.close();
			
			System.out.println(fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == button.getText()) { // initialize
			actionForInitialize();
		}
		if(e.getActionCommand() == button2.getText()) { // save
			actionForSave();
		}
		if(e.getActionCommand() == button3.getText()) { // load
			actionForLoad();
		}
		if(e.getActionCommand() == button4.getText()) { // strategy
			actionForStrategy();
		}
		if(e.getActionCommand() == button5.getText()) { // strategy
			actionForCastle();
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Strategy Data >> -----------------------------
	// -------------------------------------------------------------------------
	public class StrategyData {
		Point p[];
		String name;
		StrategyData(String strName) {
			name = strName;
			p = new Point[40];
		}
	}
	public void loadStrategyData() {
		strategyDataBase.clear();
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = strategyFilePath + "strategy" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				content = br.readLine(); // name
				StrategyData strData = new StrategyData(content);
				int i = 0;
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						int x = Integer.parseInt(st.nextToken()); // x
						int y = Integer.parseInt(st.nextToken()); // y
						strData.p[i] = new Point(x, y);
					}
					i++;
				}
				br.close();
				strategyDataBase.add(strData);
				fileIndex++;
			}
		} catch(FileNotFoundException en) {
			System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public String checkStrategy(ShogiData sd) {
		for(StrategyData strData: strategyDataBase) {
			Boolean isSame = true;
			for(int i=0; i<40; i++) {
				if(sd.k[i].px != strData.p[i].x || sd.k[i].py != strData.p[i].y) {
					isSame = false;
					break;
				}
			}
			if(isSame) {
				int index = strategyDataBase.indexOf(strData)+1;
				//System.out.println("strategy" + String.format("%03d.txt", index));
				return strData.name;
			}
		}
		
		return "";
	}
	
	public void countStrategy() {
		modelStrategy.clear();
		listStrategy.setModel(modelStrategy);
		strategyCountData.clear();
		
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: strategyCountData) {
				if(sc.str.equals(kdb.strategyName)) {
					sc.cnt++;
					if(kdb.isSenteWin) sc.senteWinCnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kdb.strategyName, kdb.isSenteWin);
				strategyCountData.add(sc);
			}
		}
		
		Collections.sort(
				strategyCountData,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		
		int totalCnt = 0;
		int totalSenteWinCnt = 0;
		for(StringCount sc: strategyCountData) {	
			totalCnt += sc.cnt;
			totalSenteWinCnt += sc.senteWinCnt;
		}
		Double d = (double)totalSenteWinCnt/(double)totalCnt*100;
		String str = "<Total:" + String.format("%2d", totalCnt)+" games" + "(Sente Winning Rate" + String.format("%.0f", d) + "%)>";
		modelStrategy.addElement(str);
		modelStrategy.addElement("----------");
		for(StringCount sc: strategyCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			modelStrategy.addElement(str);
		}
		listStrategy.setModel(modelStrategy);
	}
	
	public void updateListBox2ByStrategy() {
		int selectedIndex = listStrategy.getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndex4 = listPlayer.getSelectedIndex();
		String playerName = "";
		if(selectedIndex4 >= 2) {
			playerName = modelPlayer.getElementAt(selectedIndex4);
			//System.out.println(playerName);
		}
		int selectedIndexCastle = listCastle.getSelectedIndex();
		String castleName = "";
		if(selectedIndexCastle >= 2) {
			StringCount sc = castleCountData.get(selectedIndexCastle-2);
			castleName = sc.str;
		}
		
		StringCount sc = strategyCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String strategy = sc.str;
		
		modelInfo.clear();
		listInfo.setModel(modelInfo);

		if(playerName.equals("")) modelInfo.addElement("<"+ strategy + "'s Kifu>");
		else modelInfo.addElement("<"+ strategy + "(" + playerName + ")"+"'s Kifu>");
		modelInfo.addElement("-------------");
		for(KifuDataBase kdb: kifuDB) {
			if(kdb.strategyName.equals(strategy)) {
				String str = String.format("kf%03d:", kifuDB.indexOf(kdb)+1);
				str += kdb.playerNameS + "(" + kdb.castleNameS + ")" + " vs " + kdb.playerNameG + "(" + kdb.castleNameG + ")";
				if(kdb.isSenteWin) str+="(Sente Win)";
				else str+="(Gote Win)";
				if(playerName.equals("") && castleName.equals("")) {
					modelInfo.addElement(str);
				} else if(!playerName.equals("") && !castleName.equals("")) {
					if(str.contains(playerName) && (kdb.castleNameS.equals(castleName) || kdb.castleNameG.equals(castleName))) {
						modelInfo.addElement(str);
					}
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) modelInfo.addElement(str);
				} else if(!castleName.equals("")) {
					if(kdb.castleNameS.equals(castleName) || kdb.castleNameG.equals(castleName)) modelInfo.addElement(str);
				}
			}
		}
		listInfo.setModel(modelInfo);
	}
	public void getLoadNumberOnListBox2() {
		String str = modelInfo.getElementAt(listInfo.getSelectedIndex());
		String subStr = str.substring(2,5);
		for(int index=0; index<1000; index++) {
			String numStr = String.format("%03d", index);
			if(subStr.equals(numStr)) {
				textBoxLoad.setText(numStr);
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Castle Data >> -----------------------------
	// -------------------------------------------------------------------------
	public class CastleData {
		String name;
		List<int[]> data = new ArrayList<int[]>();
		CastleData(String castleName) {
			name = castleName;
		}
	}
	public void loadCastleData() {
		castleDataBase.clear();
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = castleFilePath + "castle" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				content = br.readLine(); // name
				CastleData castleData = new CastleData(content);
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						int s =  Integer.parseInt(st.nextToken()); // sente
						int x = Integer.parseInt(st.nextToken()); // x
						int y = Integer.parseInt(st.nextToken()); // y
						int type = Integer.parseInt(st.nextToken()); // type
						int data[] = new int[4];
						data[0] = s;
						data[1] = x;
						data[2] = y;
						data[3] = type;
						castleData.data.add(data);
					}
				}
				br.close();
				castleDataBase.add(castleData);
				fileIndex++;
				//System.out.println(castleData.name);
			}
		} catch(FileNotFoundException en) {
			System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void countCastle() {
		modelCastle.clear();
		listCastle.setModel(modelCastle);
		castleCountData.clear();
		
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kdb.castleNameS)) {
					sc.cnt++;
					if(kdb.isSenteWin) sc.senteWinCnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kdb.castleNameS, kdb.isSenteWin);
				castleCountData.add(sc);
				//System.out.println(sc.str);
			}
		}
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kdb.castleNameG)) {
					sc.cnt++;
					if(kdb.isSenteWin) sc.senteWinCnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kdb.castleNameG, kdb.isSenteWin);
				castleCountData.add(sc);
				//System.out.println(sc.str);
			}
		}
		
		Collections.sort(
				castleCountData,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		
		int totalCnt = 0;
		int totalSenteWinCnt = 0;
		for(StringCount sc: castleCountData) {	
			totalCnt += sc.cnt;
			totalSenteWinCnt += sc.senteWinCnt;
		}
		Double d = (double)totalSenteWinCnt/(double)totalCnt*100;
		String str = "<Total:" + String.format("%2d", totalCnt)+" Castles>";
		modelCastle.addElement(str);
		modelCastle.addElement("----------");
		for(StringCount sc: castleCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			modelCastle.addElement(str);
		}
		listCastle.setModel(modelCastle);
	}
	
	public void updateListInfoByCastle() {
		int selectedIndex = listCastle.getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndexStrategy = listStrategy.getSelectedIndex();
		String strategyName = "";
		if(selectedIndexStrategy >= 2) {
			StringCount sc = strategyCountData.get(selectedIndexStrategy-2);
			strategyName = sc.str;
		}
		int selectedIndexPlayer = listPlayer.getSelectedIndex();
		String playerName = "";
		if(selectedIndexPlayer >= 2) {
			playerName = modelPlayer.getElementAt(selectedIndexPlayer);
			//System.out.println(playerName);
		}
		
		StringCount sc = castleCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String castleName = sc.str;
		
		modelInfo.clear();
		listInfo.setModel(modelInfo);

		if(strategyName.equals("")) modelInfo.addElement("<"+ castleName + "'s Kifu>");
		else modelInfo.addElement("<"+ strategyName + "(" + castleName + ")"+"'s Kifu>");
		modelInfo.addElement("-------------");
		for(KifuDataBase kdb: kifuDB) {
			if(kdb.castleNameS.equals(castleName) || kdb.castleNameG.equals(castleName)) {
				String str = String.format("kf%03d:", kifuDB.indexOf(kdb)+1);
				str += kdb.playerNameS + "(" + kdb.castleNameS + ")" + " vs " + kdb.playerNameG + "(" + kdb.castleNameG + ")";
				if(kdb.isSenteWin) str+="(Sente Win)";
				else str+="(Gote Win)";
				
				if(strategyName.equals("") && playerName.equals("")) {
					modelInfo.addElement(str);
				} else if(!strategyName.equals("") && !playerName.equals("")) {
					if(kdb.strategyName.equals(strategyName) && str.contains(playerName)) modelInfo.addElement(str);
				} else if(!strategyName.equals("")) {
					if(kdb.strategyName.equals(strategyName)) modelInfo.addElement(str);
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) modelInfo.addElement(str);
				}
			}
		}
		listInfo.setModel(modelInfo);
	}
	
	public String checkCastle(ShogiData sd, Boolean isSente) {
		for(Koma k: sd.k) {
			for(CastleData cd: castleDataBase) {
				if((k.type == KomaType.King) && isSente && k.sente == 0) {
					if(isSameCastle(sd, cd, k.sente)) {
						//System.out.println("castle" + String.format("%03d", castleDataBase.indexOf(cd)+1) + ".txt matched");
						return cd.name;
					}
				}
				if((k.type == KomaType.King) && !isSente && k.sente == 1) {
					if(isSameCastle(sd, cd, k.sente)) {
						//System.out.println("castle" + String.format("%03d", castleDataBase.indexOf(cd)+1) + ".txt matched");
						return cd.name;
					}
				}
			}
		}
		
		return "";
	}
	
	public Boolean isSameCastle(ShogiData sd, CastleData cd, int sente) {
		Boolean matched = false;
		for(int[] data: cd.data) {
			Koma k = getKomaByPosition(sd, data[1], data[2], data[0]);
			if(k == null) {
				if(data[3] == KomaType.Empty.id && data[0] == sente) {
					matched = true;
				}
				else {
					return false;
				}
			}
			else if(k.type.id == data[3] && sente == data[0]) {
				matched = true;
			} else {
				return false;
			}
		}
		if(matched) return true;
		else return false;
	}
	
	public void saveListKomaAroundKing(ShogiData sd, Koma king, FileWriter fw) {
		Koma k;
		try {
			for(int x=-2; x<=2; x++) 
				for(int y=-2; y<=2; y++) {
					k = getKomaByPosition(sd, king.px+x, king.py+y, king.sente);
					if(k != null) {
						fw.write(k.sente + "," + k.px + "," + k.py + "," + k.type.id + "\n");
					} else {
						fw.write(king.sente + "," + String.valueOf(king.px+x) + "," + String.valueOf(king.py+y) + "," + KomaType.Empty.id + "\n");
					}
				}
		
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public Koma getKomaByPosition(ShogiData sd, int x, int y, int sente) {
		for(Koma k: sd.k) {
			if(k.sente == sente && k.px == x && k.py == y) {
				return k;
			}
		}
		return null;
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Kifu Data >> -----------------------------
	// -------------------------------------------------------------------------
	public class StringCount {
		String str;
		int cnt;
		Point target;
		Point base;
		int index;
		int senteWinCnt;
		StringCount(String s, Boolean isSenteWin) {
			str = s;
			cnt = 1;
			if(isSenteWin) {
				senteWinCnt = 1;
			}
			else {
				senteWinCnt = 0;
			}
		}
	}
	public class KifuDataBase {
		List<Kifu> db = new ArrayList<Kifu>();
		String playerNameS;
		String playerNameG;
		String strategyName;
		String castleNameS;
		String castleNameG;
		Boolean isSenteWin;
		
		KifuDataBase() {
			strategyName = "";
			castleNameS = "";
			castleNameG = "";
		}
	}	
	public class Kifu {
		Koma k;
		int x;
		int y;
		int p;
		int pp;
		int d;
		
		Kifu(Koma koma, int px, int py, int promote, int preP, int drop) {
			k = koma;
			x = px;
			y = py;
			p = promote;
			pp = preP;
			d = drop;
		}
	}
	public String createMoveKomaName(KomaType type, int sente, int x, int y, int promoted, int preP, int drop) {
		String s = senteGote[sente] + String.valueOf(x)+String.valueOf(y);
		if(preP == 0 && promoted == 1) {
			s += komaName[type.getInt()] + "成";
		} else {
			s += komaName[type.getInt()+8*promoted];
		}
		if(drop == 1) s += "打";
		
		return s;
	}
	public void checkKDB(int index) {
		int i;
		Boolean isSame;
		List<StringCount> listSC = new ArrayList<StringCount>();
		
		for(KifuDataBase kdb: kifuDB) {
			i=0;
			isSame = true;
			// check same moves
			while(i<index && i<kdb.db.size()) {
				if( kifuData.get(i).k.type != kdb.db.get(i).k.type ||
						kifuData.get(i).x != kdb.db.get(i).x || 
						kifuData.get(i).y != kdb.db.get(i).y || 
						kifuData.get(i).p != kdb.db.get(i).p ) {
					// check same position if moves were different
					isSame = checkSamePositionKDB(index, kdb);
					break;
				}
				i++;
			}
			
			// count next move on kdb
			if(isSame && index < kdb.db.size()) {
				countNextMoveOnKDB(listSC, kdb, index);
			}
		}
		
		updateListBox2(listSC);
	}
	
	public void countNextMoveOnKDB(List<StringCount> listSC, KifuDataBase kdb, int index) {
		Kifu kf = kdb.db.get(index);
		String str = createMoveKomaName(kf.k.type, kf.k.sente, kf.x, kf.y, kf.p, kf.pp, kf.d);
		Boolean found = false;
		// create message for debug
		String msg = str + ":kifu"+String.format("%03d", kifuDB.indexOf(kdb)+1)+".txt is matched.";
		if(isSenteWin(kdb)) {
			msg += "(" + kdb.strategyName + ":Sente Win)";
		} else {
			msg += "(" + kdb.strategyName + ":Gote win)";
		}
		//System.out.println(msg);
		
		// count string data if same string
		for(StringCount sc: listSC) {
			if(sc.str.equals(str)) {
				sc.cnt++;
				if(isSenteWin(kdb)) sc.senteWinCnt++;
				found = true;
			}
		}
		// add new string data
		if(!found) {
			StringCount sc = new StringCount(str, isSenteWin(kdb));
			sc.target = new Point(kf.x, kf.y);
			sc.base = new Point(shogiData.k[kf.k.index].px, shogiData.k[kf.k.index].py);
			listSC.add(sc);
		}
	}
	
	public Boolean isSenteWin(KifuDataBase kdb) {
		int index = kdb.db.size()-1;
		if((index%2) == 0) return true;
		return false;
	}
	
	public Boolean checkSamePositionKDB(int index, KifuDataBase kdb) {
		shogiDataForKDB.resetAllKoma();
		
		int i = 0;
		while(i<index && index<kdb.db.size()) {
			Kifu kf = kdb.db.get(i);
			shogiDataForKDB.k[kf.k.index].moveKoma(shogiDataForKDB, kf.x, kf.y, kf.p);
			i++;
		}
		Boolean isSame = true;
		for(int x=0; x<40; x++) {
			if(shogiData.k[x].type != shogiDataForKDB.k[x].type || 
					shogiData.k[x].px != shogiDataForKDB.k[x].px || 
					shogiData.k[x].py != shogiDataForKDB.k[x].py ||
					shogiData.k[x].promoted != shogiDataForKDB.k[x].promoted) {
				isSame = false;
				break;
			}
		}
		
		return isSame;
	}
	
	
	// -------------------------------------------------------------------------
	// ----------------------- << Player Data >> -----------------------------
	// -------------------------------------------------------------------------
	public class PlayerData {
		String playerName;
		ImageIcon playerIcon;
		List<GameResult> grList = new ArrayList<GameResult>();
		
		PlayerData(String name) {
			playerName = name;
			playerIcon = new ImageIcon(playerIconPath + name + ".jpg");
			Image image = playerIcon.getImage();
			Image newImage = image.getScaledInstance(100, 133, java.awt.Image.SCALE_SMOOTH);
			playerIcon = new ImageIcon(newImage);
		}
	}
	public class GameResult {
		String strategy;
		Boolean isPlayerWin;
		Boolean isSente;
		
		GameResult() {}
	}
	public class GameResultCount {
		String str;
		int cnt;
		int senteWinCnt = 0;
		int senteLoseCnt = 0;
		int goteWinCnt = 0;
		int goteLoseCnt = 0;
		GameResultCount(String s, Boolean isPlayerWin, Boolean isSenteWin) {
			str = s;
			cnt = 1;
			if(isPlayerWin) {
				if(isSenteWin) {
					senteWinCnt = 1;
				} else {
					goteWinCnt = 1;
				}
			} else {
				if(isSenteWin) {
					senteLoseCnt = 1;
				} else {
					goteLoseCnt = 1;
				}
			}
		}
	}
	public void createPlayerDataBase() {
		modelPlayer.clear();
		listPlayer.setModel(modelPlayer);
		playerDataBase.clear();
		
		for(KifuDataBase kdb: kifuDB) {
			Boolean foundS = false;
			Boolean foundG = false;
			GameResult grS = new GameResult();
			GameResult grG = new GameResult();
			grS.strategy = kdb.strategyName;
			grG.strategy = kdb.strategyName;
			grS.isSente = true;
			grG.isSente = false;
			if(kdb.isSenteWin) {
				grS.isPlayerWin = true;
				grG.isPlayerWin = false;
			}
			else {
				grS.isPlayerWin = false;
				grG.isPlayerWin = true;
			}
			
			for(PlayerData pd: playerDataBase) {
				if(kdb.playerNameS.equals(pd.playerName)) {
					foundS = true;
					pd.grList.add(grS);
				} else if(kdb.playerNameG.equals(pd.playerName)) {
					foundG = true;
					pd.grList.add(grG);
				}
			}
			
			if(!foundS) {
				PlayerData pd = new PlayerData(kdb.playerNameS);
				pd.grList.add(grS);
				playerDataBase.add(pd);
			}
			if(!foundG) {
				PlayerData pd = new PlayerData(kdb.playerNameG);
				pd.grList.add(grG);
				playerDataBase.add(pd);
			}
		}
		
		Collections.sort(
				playerDataBase,
				new Comparator<PlayerData>() {
					@Override
					public int compare(PlayerData obj1, PlayerData obj2) {
						return obj2.grList.size() - obj1.grList.size();
					}
				}
				);
		
		modelPlayer.addElement("<Name List>");
		modelPlayer.addElement("---------");
		
		for(PlayerData pd: playerDataBase) {
			modelPlayer.addElement(pd.playerName);
		}
		listPlayer.setModel(modelPlayer);
	}
	public void updateListBox2ByPlayerName() {
		int selectedIndex = listPlayer.getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		
		PlayerData pd = playerDataBase.get(selectedIndex);
		textBoxPlayerS.setText(pd.playerName);
		updatePlayerIcon();
		
		// count strategy data
		List<GameResultCount> grcList = new ArrayList<GameResultCount>();
		for(GameResult gr: pd.grList) {
			Boolean found = false;
			for(GameResultCount grc: grcList) {
				if(grc.str.equals(gr.strategy)) {
					found = true;
					grc.cnt++;
					if(gr.isPlayerWin) {
						if(gr.isSente) grc.senteWinCnt++;
						else grc.goteWinCnt++;
					} else {
						if(gr.isSente) grc.senteLoseCnt++;
						else grc.goteLoseCnt++;
					}
				}
			}
			if(!found) {
				GameResultCount grc = new GameResultCount(gr.strategy, gr.isPlayerWin, gr.isSente);
				grcList.add(grc);
			}
		}
		
		Collections.sort(
				grcList,
				new Comparator<GameResultCount>() {
					@Override
					public int compare(GameResultCount obj1, GameResultCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		
		modelInfo.clear();
		listInfo.setModel(modelInfo);
		
		modelInfo.addElement("<" + pd.playerName + "'s Winning Rate>");
		
		int totalCnt = 0;
		int totalWinCnt = 0;
		int totalSenteWinCnt = 0;
		int totalGoteWinCnt = 0;
		int totalSenteLoseCnt = 0;
		int totalGoteLoseCnt = 0;
		for(GameResultCount grc: grcList) {
			totalCnt += grc.cnt;
			totalWinCnt += grc.senteWinCnt + grc.goteWinCnt;
			totalSenteWinCnt += grc.senteWinCnt;
			totalGoteWinCnt += grc.goteWinCnt;
			totalSenteLoseCnt += grc.senteLoseCnt;
			totalGoteLoseCnt += grc.goteLoseCnt;
		}
		String str = "Total " + String.format("%d games %d Win:%d Lose", totalCnt, totalWinCnt, totalCnt-totalWinCnt);
		Double d = (double)totalWinCnt/(double)(totalCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		modelInfo.addElement(str);
		
		str = String.format("Sente %d games %d Win:%d Lose", totalSenteWinCnt+totalSenteLoseCnt, totalSenteWinCnt, totalSenteLoseCnt);
		d = (double)totalSenteWinCnt/(double)(totalSenteWinCnt+totalSenteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		modelInfo.addElement(str);
		str = String.format("Gote %d games %d Win:%d Lose", totalGoteWinCnt+totalGoteLoseCnt, totalGoteWinCnt, totalGoteLoseCnt);
		d = (double)totalGoteWinCnt/(double)(totalGoteWinCnt+totalGoteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		modelInfo.addElement(str);
		
		modelInfo.addElement("---------");
		str = "Total Strategies: " + grcList.size() + " patterns";
		modelInfo.addElement(str);
		for(GameResultCount grc: grcList) {
			str = grc.str;
			d = (double)(grc.senteWinCnt+grc.goteWinCnt)/(double)(grc.cnt)*100;
			str += ":" + String.format("%d", grc.cnt)+" games";
			str += "(Winning Rate" + String.format("%.0f", d) + "%)";
			modelInfo.addElement(str);
		}
		
		listInfo.setModel(modelInfo);
	}
	public void updatePlayerIcon() {
		String playerNameS = textBoxPlayerS.getText();
		String playerNameG = textBoxPlayerG.getText();
		
		playerIconLabel[0].setIcon(null);
		playerIconLabel[1].setIcon(null);
		
		for(PlayerData pd: playerDataBase) {
			if(pd.playerName.equals(playerNameS)) {
				playerIconLabel[0].setIcon(pd.playerIcon);
			}
			if(pd.playerName.equals(playerNameG)) {
				playerIconLabel[1].setIcon(pd.playerIcon);
			}
		}
	}
	public void initializePlayerIcon() {
		playerIconLabel[0].setIcon(null);
		playerIconLabel[1].setIcon(null);
	}
	
	private ActionListener enterActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updatePlayerIcon();
        }
    };

	// -------------------------------------------------------------------------
	// ----------------------- << ListBox Action >> -----------------------------
	// -------------------------------------------------------------------------
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) {
			if(e.getSource() == listKifu) {
				commonListAction();
			}
			if(e.getSource() == listInfo) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listStrategy) {
				updateListBox2ByStrategy();
			}
			if(e.getSource() == listPlayer) {
				updateListBox2ByPlayerName();
			}
			if(e.getSource() == listCastle) {
				updateListInfoByCastle();
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		//System.out.println("Key Typed");
	}
	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println("Key Pressed");
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("Key Released");
		// TODO 自動生成されたメソッド・スタブ
		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(e.getSource() == listKifu) {
				commonListAction();
				soundKoma();
			}
			if(e.getSource() == listInfo) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listStrategy) {
				updateListBox2ByStrategy();
			}
			if(e.getSource() == listPlayer) {
				updateListBox2ByPlayerName();
			}
			if(e.getSource() == listCastle) {
				updateListInfoByCastle();
			}
		}
	}
	public void clearListBox() {
		modelKifu.clear();
		modelKifu.addElement("--------");
		listKifu.setModel(modelKifu);
		listKifu.setSelectedIndex(0);
	}
	public void updateListBox(KomaType type, int x, int y, int sente, int promoted, int preP, int drop) {
		// remove items under selected item 
		int selectedIndex = listKifu.getSelectedIndex();
		if(selectedIndex != -1 && selectedIndex <= modelKifu.size()-1) {
			int index = modelKifu.size()-1;
			while(index > selectedIndex) {
				modelKifu.remove(index);
				kifuData.remove(index-1);
				index--;
			}
		}
		
		// add new item
		String s = createMoveKomaName(type, sente, x, y, promoted, preP, drop);
		s = modelKifu.size() + ":"+s;
		modelKifu.addElement(s);
		listKifu.setModel(modelKifu);
		listKifu.ensureIndexIsVisible(modelKifu.size()-1);
		listKifu.setSelectedIndex(modelKifu.size()-1);
	}
	public void commonListAction() {
		int selectedIndex = listKifu.getSelectedIndex();
		//System.out.println("list selected:" + selectedIndex);
		shogiData.resetAllKoma();
		shogiData.viewKomaOnBoard();
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(shogiData, kf.x, kf.y, kf.p);
				shogiData.turnIsSente = !shogiData.turnIsSente;
				cv.setLastPoint(kf.x, kf.y, true);
				if(textBoxStrategy.getText().equals("")) textBoxStrategy.setText(checkStrategy(shogiData));
				if(textBoxCastle.getText().equals("")) {
					textBoxCastle.setText(checkCastle(shogiData, true));
				}
				if(textBoxCastle.getText().equals("")) {
					textBoxCastle.setText(checkCastle(shogiData, false));
				}
			}
		}
		if(selectedIndex == 0) cv.setLastPoint(-1, -1, false);
		
		checkKDB(selectedIndex);
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
	}
	public void updateListBox2(List<StringCount> listSC) {
		modelInfo.clear();
		listInfo.setModel(modelInfo);
		cv.clearDrawPoint();
		
		Collections.sort(
				listSC,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		for(StringCount sc: listSC) {
			String str = sc.str;
			Double d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			cv.addDrawPoint(sc.target,  sc.base);
			modelInfo.addElement(str);
		}
		listInfo.setModel(modelInfo);
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Mouse Action >> -----------------------------
	// -------------------------------------------------------------------------
	// 6, @Overrideアノテーションを付ける。
	public void mouseDragged(MouseEvent e) {
		//System.out.println("mouse dragged");
		/*
		if(shogiData.selectedKoma != null) {
			Point mp = e.getPoint();			
			shogiData.selectedKoma.setLocation(mp.x - sh.x, mp.y - sh.y);
		}
		*/
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		//if(shogiData.selectedKoma == null) return;
		//if(shogiData.listKomaOnBoard.indexOf(shogiData.selectedKoma) == -1) return;
		if(e.getClickCount() == 2) {
			//System.out.println("double clicked");
			//selectedKoma.reverse();
		} else if(e.getClickCount() == 1){
			//System.out.println("mouse clicked");
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		//System.out.println("mouse pressed");
		if(shogiData.selectedKoma == null) {
			selectKoma(e);
		} else {
			releaseKoma();
		}
	}
	public void selectKoma(MouseEvent e) {
		shogiData.selectedKoma = null;
		Point mp = e.getPoint();
		for(Koma k: shogiData.listKomaOnBoard) {
			Point lp = k.getLocation();
			if(mp.x > lp.x && mp.x < lp.x+iconWidth && mp.y > lp.y && mp.y < lp.y+iconHeight*2) {
				commonMousePressed(mp, lp, k, true);
				return;
			}
		}
		for(Koma k: shogiData.listKomaOnHandForSente) {
			Point lp = k.getLocation();
			if(mp.x > lp.x && mp.x < lp.x+iconWidth && mp.y > lp.y && mp.y < lp.y+iconHeight*2) {
				commonMousePressed(mp, lp, k, false);
				return;
			}
		}
		for(Koma k: shogiData.listKomaOnHandForGote) {
			Point lp = k.getLocation();
			if(mp.x > lp.x && mp.x < lp.x+iconWidth && mp.y > lp.y && mp.y < lp.y+iconHeight*2) {
				commonMousePressed(mp, lp, k, false);
				return;
			}
		}
	}
	
	public void commonMousePressed(Point mp, Point lp, Koma k, Boolean isOnBoard) {
		if(shogiData.turnIsSente && k.sente == 1 && !checkBoxEditMode.isSelected()) {
			shogiData.selectedKoma = null;
			return;
		}
		if(!shogiData.turnIsSente && k.sente == 0 && !checkBoxEditMode.isSelected()) {
			shogiData.selectedKoma = null;
			return;
		}
		shogiData.selectedKoma = k;
		mousePointDifference.x = mp.x - lp.x;
		mousePointDifference.y = mp.y - lp.y;
		if(isOnBoard) cv.mousePressed = true;
		cv.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		//System.out.println("mouse released");
	}
	
	public void releaseKoma() {
		Koma selectedKoma = shogiData.selectedKoma;
		if(selectedKoma == null) return;
		int x = (selectedKoma.getLocation().x - 20 + iconWidth/2) / (iconWidth+10);
		int y = (selectedKoma.getLocation().y - 20 + iconHeight/2) / (iconHeight+10) + 1;
		int preX = selectedKoma.px;
		int preY = selectedKoma.py;
		int preP = selectedKoma.promoted;
		
		System.out.println("moveKoma("+(9-x)+","+y+")");
		Boolean result = selectedKoma.moveKoma(shogiData, 9-x, y, -1);
		soundKoma();
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
		
		cv.mousePressed = false;
		cv.repaint();
		
		if(result == false) {
			shogiData.selectedKoma = null;
			return;
		}
		
		KomaType type = selectedKoma.type;
		int sente = selectedKoma.sente;
		int drop = selectedKoma.drop;
		int promoted = selectedKoma.promoted;
		
		// update kifu listbox and kifuData
		if(9-x != preX || y != preY) {
			if(9-x>0 && 9-x<10 && y>0 && y<10) {
				cv.setLastPoint(9-x, y, true);
				updateListBox(type, 9-x, y, sente, promoted, preP, drop);
			}
			Kifu kf = new Kifu(selectedKoma, 9-x, y, promoted, preP, drop);
			kifuData.add(kf);
			checkKDB(modelKifu.size()-1);
			shogiData.turnIsSente = !shogiData.turnIsSente;
		}
		
		// check strategy
		if(textBoxStrategy.getText().equals("")) textBoxStrategy.setText(checkStrategy(shogiData));
		if(textBoxCastle.getText().equals("")) {
			textBoxCastle.setText(checkCastle(shogiData, true));
		}
		if(textBoxCastle.getText().equals("")) {
			textBoxCastle.setText(checkCastle(shogiData, false));
		}
		
		shogiData.selectedKoma = null;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		//System.out.println("Mouse moved");
		if(shogiData.selectedKoma != null) {
			Point mp = e.getPoint();			
			shogiData.selectedKoma.setLocation(mp.x -mousePointDifference.x, mp.y - mousePointDifference.y);
		}
	}
}