import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class KifuAnalyzer extends JFrame implements MouseListener, MouseMotionListener, 
	ActionListener, ListSelectionListener, KeyListener{
	// -------------------------------------------------------------------------
	// ----------------------- << Global Variables >> --------------------------
	// -------------------------------------------------------------------------
	Color boardColor = new Color(255, 238, 203);
	Color backGroundColor = new Color(224, 255, 255);
	Color buttonColor = new Color(180, 245, 250);
	Color buttonFocusedColor = new Color(120, 245, 250);
	Color buttonBorderColor = new Color(0, 145, 20);
	
	String imgFilePath = "./img/";
	String kifuFilePath = "./kifu";
	String strategyFilePath = "./strategy/";
	String soundFilePath = "./sound/";
	JLabel castleIconLabel = new JLabel();
	
	public enum SenteGote {
		Sente(0), Gote(1);
		private final int id;
		private SenteGote(final int id) {
			this.id = id;
		}
	};
	JLabel playerIconLabel[] = new JLabel[2];
	
	public enum ButtonType {
		Initialize(0), Save(1), Load(2), Strategy(3), Castle(4), Tesuji(5), Kifu(6);
		private final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	JButton button[] = new JButton[ButtonType.values().length];
	public enum LabelType {
		Strategy(0), Castle(1), Tesuji(2), Sente(3), Gote(4);
		private final int id;
		private LabelType(final int id) {
			this.id = id;
		}
	};
	JLabel label[] = new JLabel[LabelType.values().length];
	public enum TextBoxType {
		LoadFile(0), LoadStep(1), LoadYear(2), Player1(3), Player2(4), Strategy(5), Tesuji(6), Castle(7);
		private final int id;
		private TextBoxType(final int id) {
			this.id = id;
		}
	};
	JTextField textBox[] = new JTextField[TextBoxType.values().length];
	public enum CheckBoxType {
		Edit(0), Reverse(1), Draw(2);
		private final int id;
		private CheckBoxType(final int id) {
			this.id = id;
		}
	};
	JCheckBox checkBox[] = new JCheckBox[CheckBoxType.values().length];
	JRadioButton radioButtonSente = new JRadioButton("Sente", true);
	JRadioButton radioButtonGote = new JRadioButton("Gote");
	JComboBox<String> comboBox;
	
	public enum ListBoxType {
		Kifu(0), Info(1), Strategy(2), Player(3), Castle(4), Tesuji(5);
		private final int id;		
		private ListBoxType(final int id) {
			this.id = id;
		}
	};
	JScrollPane scrollPane[] = new JScrollPane[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	DefaultListModel<String> listModel[] = new DefaultListModel[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	JList<String> listBox[] = new JList[ListBoxType.values().length];
	Clip soundKoma;
	
	public enum MenuType {
		Color(0), Capture(1), KomaInHand(2), StartEngine(3), StopEngine(4);
		private final int id;		
		private MenuType(final int id) {
			this.id = id;
		}
	};
	JMenuBar menuBar = new JMenuBar() {
		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(backGroundColor);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.dispose();
			}
		};
	JMenu menu = new JMenu("Menu");
	JMenuItem menuItem[] = new JMenuItem[MenuType.values().length];
	
	JLabel labelNumberRow[] = new JLabel[9];
	JLabel labelNumberCol[] = new JLabel[9];
	
	// -------------------------------------------------------------------------
	// ----------------------- << Main >> --------------------------------------
	// -------------------------------------------------------------------------
	public static void main(String[] args) {
		KifuAnalyzer ka = new KifuAnalyzer();
		ka.setTitle("KifuAnalyzer");
		ka.setLocationRelativeTo(null);
		ka.setVisible(true);
		ka.shogiData.viewKomaOnBoard();
	}

	// -------------------------------------------------------------------------
	// ----------------------- << GUI Setting >> -------------------------------
	// -------------------------------------------------------------------------
	KifuAnalyzer() {		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		shogiData.initializeIcon();
		setSize(shogiData.iconWidth*22, shogiData.iconHeight*12);
		initializeAppIcon();
		initializeGUISetting();
		contentPaneSetting();
		listenerSetting();		
		actionForInitialize();
	}
	public void initializeAppIcon() {
		ImageIcon icon = new ImageIcon(imgFilePath + "Shogi Ramen TV.jpg");
		setIconImage(icon.getImage());
	}
	public void initializeGUISetting() {
		shogiData.initializeKomaSetting();
		shogiData.initializeLabelSetting();
		shogiDataForKDB.initializeKomaSetting();
		initializeButtonSetting();
		initializeTextBoxSetting();
		initializeCheckBox();
		initializePlayerIconLabel();
		initializeCastleIcon();
		initializeListBoxSetting();
		initializeCanvasSetting();
		initializeSoundSetting();
		initializeMenuBar();
		initializeNumberRowCol();
	}
	public void contentPaneSetting() {
		getContentPane().setLayout(null);
		getContentPane().setBackground(backGroundColor);
		
		for(int x=0; x<40; x++) {
			getContentPane().add(shogiData.k[x]);
		}
		for(ButtonType b: ButtonType.values()) getContentPane().add(button[b.id]);
		for(LabelType l: LabelType.values()) getContentPane().add(label[l.id]);
		for(TextBoxType t: TextBoxType.values()) getContentPane().add(textBox[t.id]);
		for(ListBoxType lb: ListBoxType.values()) getContentPane().add(scrollPane[lb.id]);
		for(SenteGote sg: SenteGote.values()) getContentPane().add(playerIconLabel[sg.id]);
		for(CheckBoxType cb: CheckBoxType.values()) getContentPane().add(checkBox[cb.id]);
		getContentPane().add(castleIconLabel);
		getContentPane().add(radioButtonSente);
		getContentPane().add(radioButtonGote);
		getContentPane().add(comboBox);
		for(SenteGote sg: SenteGote.values()) {
			for(int x=0; x<8; x++) getContentPane().add(shogiData.labelNumOfKoma[sg.id][x]);
		}
		for(int x=0; x<9; x++) {
			getContentPane().add(labelNumberRow[x]);
			getContentPane().add(labelNumberCol[x]);
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
			String fileName = soundFilePath + "Koma Oto.wav";
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
	public void initializeButtonSetting() {
		for(ButtonType b: ButtonType.values()) {
			button[b.id] = new JButton(b.name());
			button[b.id].addActionListener(this);
			button[b.id].addMouseListener(this);
			button[b.id].setOpaque(true);
			button[b.id].setBackground(buttonColor);
			LineBorder border = new LineBorder(buttonBorderColor, 1, true);
			button[b.id].setBorder(border);
		}
		button[ButtonType.Initialize.id].setBounds(580, 10, 80, 20);
		button[ButtonType.Save.id].setBounds(820, 10, 60, 20);
		button[ButtonType.Load.id].setBounds(880, 10, 60, 20);
		button[ButtonType.Strategy.id].setBounds(580, 30, 80, 20);
		button[ButtonType.Tesuji.id].setBounds(580, 50, 80, 20);
		button[ButtonType.Castle.id].setBounds(580, 70, 80, 20);
		button[ButtonType.Kifu.id].setBounds(580, 90, 80, 20);
	}
	public void initializeTextBoxSetting() {
		for(TextBoxType t: TextBoxType.values()) {
			textBox[t.id] = new JTextField();
		}
		for(LabelType l: LabelType.values()) {
			label[l.id] = new JLabel();
		}
		textBox[TextBoxType.Strategy.id].setBounds(660, 30, 160, 20);
		textBox[TextBoxType.Tesuji.id].setBounds(660, 50, 160, 20);
		textBox[TextBoxType.Castle.id].setBounds(660, 70, 160, 20);
		textBox[TextBoxType.LoadFile.id].setBounds(940, 10, 50, 20);
		textBox[TextBoxType.LoadStep.id].setBounds(990, 10, 50, 20);
		textBox[TextBoxType.LoadYear.id].setBounds(1040, 10, 50, 20);
		label[LabelType.Sente.id].setText("▲Sente");
		label[LabelType.Sente.id].setBounds(840, 60, 100, 20);
		textBox[TextBoxType.Player1.id].setBounds(835, 80, 120, 20);
		textBox[TextBoxType.Player1.id].addActionListener(enterActionListener);
		label[LabelType.Gote.id].setText("△Gote");
		label[LabelType.Gote.id].setBounds(965, 60, 100, 20);
		textBox[TextBoxType.Player2.id].setBounds(965, 80, 120, 20);
		textBox[TextBoxType.Player2.id].addActionListener(enterActionListener);
	}
	public void initializeCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id] = new JCheckBox(cb.name());
		}
		checkBox[CheckBoxType.Edit.id].setBounds(660, 15, 60, 10);
		checkBox[CheckBoxType.Reverse.id].setBounds(720, 15, 80, 10);
		checkBox[CheckBoxType.Reverse.id].addActionListener(checkActionListener);
		checkBox[CheckBoxType.Draw.id].setBounds(820, 35, 80, 10);
		radioButtonSente.setBounds(660, 95, 70, 14);
		radioButtonGote.setBounds(720, 95, 70, 14);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonSente);
		buttonGroup.add(radioButtonGote);
		comboBox = new JComboBox<>();
		comboBox.addItem("");
		comboBox.addItem("2023");
		comboBox.addItem("2022");
		comboBox.addItem("all");
		comboBox.setBounds(1000, 40, 100, 20);
	}
	public void clearCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id].setSelected(false);
		}
		radioButtonSente.setSelected(true);
	}
	public void initializeListBoxSetting() {
		for(ListBoxType lb: ListBoxType.values()) {
			listModel[lb.id] = new DefaultListModel<String>();
			listBox[lb.id] = new JList<String>();
			scrollPane[lb.id] = new JScrollPane();
			listBox[lb.id].setModel(listModel[lb.id]);
			listBox[lb.id].addListSelectionListener(this);
			listBox[lb.id].addMouseListener(this);
			listBox[lb.id].addMouseMotionListener(this);
			listBox[lb.id].addKeyListener(this);
			scrollPane[lb.id].getViewport().setView(listBox[lb.id]);
		}
		
		listModel[ListBoxType.Kifu.id].addElement("--------");
		scrollPane[ListBoxType.Kifu.id].setBounds(590, 260, 165, 90);
		scrollPane[ListBoxType.Info.id].setBounds(590, 350, 165, 90);
		scrollPane[ListBoxType.Strategy.id].setBounds(755, 260, 165, 90);
		scrollPane[ListBoxType.Castle.id].setBounds(755, 350, 165, 90);
		scrollPane[ListBoxType.Player.id].setBounds(920, 260, 165, 90);
		scrollPane[ListBoxType.Tesuji.id].setBounds(920, 350, 165, 90);
	}
	public void initializePlayerIconLabel() {
		for(SenteGote sg: SenteGote.values()) playerIconLabel[sg.id] = new JLabel();
		playerIconLabel[SenteGote.Sente.id].setBounds(840, 75, 100, 200);
		playerIconLabel[SenteGote.Gote.id].setBounds(970, 75, 100, 200);
		castleIconLabel.setBounds(880, 450, 200, 252);
	}
	public void clearTextBox() {
		textBox[TextBoxType.Player1.id].setText("");
		textBox[TextBoxType.Player2.id].setText("");
		textBox[TextBoxType.Strategy.id].setText("");
		textBox[TextBoxType.Castle.id].setText("");
		textBox[TextBoxType.Tesuji.id].setText("");
		textBox[TextBoxType.LoadFile.id].setText("");
		textBox[TextBoxType.LoadStep.id].setText("");
		textBox[TextBoxType.LoadYear.id].setText("");
	}
	public void initializeMenuBar() {
		for(MenuType mt: MenuType.values()) {
			menuItem[mt.id] = new JMenuItem(mt.name());
			menuItem[mt.id].addActionListener(this);
			menu.add(menuItem[mt.id]);
		}
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	public void initializeNumberRowCol() {
		for(int x=0; x<9; x++) {
			labelNumberRow[x] = new JLabel();
			labelNumberCol[x] = new JLabel();
			labelNumberRow[x].setText(String.valueOf(9-x));
			labelNumberRow[x].setBounds(45 + x * (shogiData.iconWidth+10), 5, 10, 10);
			labelNumberCol[x].setText(String.valueOf(x+1));
			labelNumberCol[x].setBounds(565, 50 + x * (shogiData.iconHeight+10), 10, 10);
		}
	}
	public void reverseNumberRowCol() {
		for(int x=0; x<9; x++) {
			if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
				labelNumberRow[x].setText(String.valueOf(x+1));
				labelNumberCol[x].setText(String.valueOf(9-x));
			}
			else {
				labelNumberRow[x].setText(String.valueOf(9-x));
				labelNumberCol[x].setText(String.valueOf(x+1));
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Shogi Board >> -------------------------------
	// -------------------------------------------------------------------------
	ShogiData shogiData = new ShogiData();
	ShogiData shogiDataForKDB = new ShogiData();
	public class ShogiData {
		String[] komaName = {"歩", "香", "桂", "銀", "金", "角", "飛", "王", "と", "成香", "成桂", "成銀", "金", "馬", "龍", "王"};
		String[] senteGote = {"▲", "△"};
		ImageIcon icon[][] = new ImageIcon[10][4];
		int iconWidth;
		int iconHeight;
		Koma k[];
		Koma selectedKoma = null;
		Boolean turnIsSente;
		List<Koma> listKomaOnBoard;
		List<List<Koma>> listKomaOnHand;
		List<Koma> listKomaOnHandForSente;
		List<Koma> listKomaOnHandForGote;
		JLabel[][] labelNumOfKoma = new JLabel[2][8];
		
		ShogiData() {
			k = new Koma[40];
			turnIsSente = true;
			listKomaOnBoard = new ArrayList<Koma>();
			listKomaOnHandForSente = new ArrayList<Koma>();
			listKomaOnHandForGote = new ArrayList<Koma>();
			listKomaOnHand = new ArrayList<>();
			listKomaOnHand.add(listKomaOnHandForSente);
			listKomaOnHand.add(listKomaOnHandForGote);
		}
		
		public void initializeIcon() {
			icon[KomaType.Pawn.id][0] = new ImageIcon("./img/Pawn.png");
			icon[KomaType.Pawn.id][1] = new ImageIcon("./img/Promoted Pawn.png");
			icon[KomaType.Pawn.id][2] = new ImageIcon("./img/Pawn Gote.png");
			icon[KomaType.Pawn.id][3] = new ImageIcon("./img/Promoted Pawn Gote.png");
			icon[KomaType.Rance.id][0] = new ImageIcon("./img/Rance.png");
			icon[KomaType.Rance.id][1] = new ImageIcon("./img/Promoted Rance.png");
			icon[KomaType.Rance.id][2] = new ImageIcon("./img/Rance Gote.png");
			icon[KomaType.Rance.id][3] = new ImageIcon("./img/Promoted Rance Gote.png");
			icon[KomaType.Knight.id][0] = new ImageIcon("./img/Knight.png");
			icon[KomaType.Knight.id][1] = new ImageIcon("./img/Promoted Knight.png");
			icon[KomaType.Knight.id][2] = new ImageIcon("./img/Knight Gote.png");
			icon[KomaType.Knight.id][3] = new ImageIcon("./img/Promoted Knight Gote.png");
			icon[KomaType.Silver.id][0] = new ImageIcon("./img/Silver.png");
			icon[KomaType.Silver.id][1] = new ImageIcon("./img/Promoted Silver.png");
			icon[KomaType.Silver.id][2] = new ImageIcon("./img/Silver Gote.png");
			icon[KomaType.Silver.id][3] = new ImageIcon("./img/Promoted Silver Gote.png");
			icon[KomaType.Gold.id][0] = new ImageIcon("./img/Gold.png");
			icon[KomaType.Gold.id][2] = new ImageIcon("./img/Gold Gote.png");
			icon[KomaType.Rook.id][0] = new ImageIcon("./img/Rook.png");
			icon[KomaType.Rook.id][1] = new ImageIcon("./img/Promoted Rook.png");
			icon[KomaType.Rook.id][2] = new ImageIcon("./img/Rook Gote.png");
			icon[KomaType.Rook.id][3] = new ImageIcon("./img/Promoted Rook Gote.png");
			icon[KomaType.Bishop.id][0] = new ImageIcon("./img/Bishop.png");
			icon[KomaType.Bishop.id][1]= new ImageIcon("./img/Promoted Bishop.png");
			icon[KomaType.Bishop.id][2] = new ImageIcon("./img/Bishop Gote.png");
			icon[KomaType.Bishop.id][3]= new ImageIcon("./img/Promoted Bishop Gote.png");
			icon[KomaType.King.id][0] = new ImageIcon("./img/King.png");
			icon[KomaType.King.id][2] = new ImageIcon("./img/King Gote.png");
			
			iconWidth = icon[KomaType.Pawn.id][0].getIconWidth();
			iconHeight = icon[KomaType.Pawn.id][0].getIconHeight();
		}
		
		public void viewKomaOnBoard() {
			k[0].setLocation(600, 0); // なぜかCanvas枠外で一度描画が必要
			for(Koma k: listKomaOnBoard) {
				int X, Y;
				if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
					X = 10 - k.px;
					Y = 10 - k.py;
				} else {
					X = k.px;
					Y = k.py;
				}
				k.setOpaque(true);
				k.setLocation((9-X)*(iconWidth+10)+25, (Y-1)*(iconHeight+10)+25);
			}
		}
		
		public void viewKomaOnHand() {
			int numOfKoma[][] = new int[2][8];
			
			for(SenteGote sg: SenteGote.values()) {
				for(int x=0; x<8; x++) {
					labelNumOfKoma[sg.id][x].setVisible(false);
					labelNumOfKoma[sg.id][x].setSize(20, 10);
					numOfKoma[sg.id][x] = 0;
				}
			}
			
			//for(Koma k: listKomaOnHandForSente) {
			for(Koma k: listKomaOnHand.get(SenteGote.Sente.id)) {
				k.setOpaque(false);
				k.setLocation((9+(k.type.id%4))*(iconWidth+10)+55, (6+(k.type.id/4))*(iconHeight+10)+5);
				numOfKoma[SenteGote.Sente.id][k.type.id]++;
				labelNumOfKoma[SenteGote.Sente.id][k.type.id].setText(Integer.valueOf(numOfKoma[SenteGote.Sente.id][k.type.id]).toString());
				labelNumOfKoma[SenteGote.Sente.id][k.type.id].setVisible(true);
			}
			
			for(Koma k: listKomaOnHand.get(SenteGote.Gote.id)) {
				k.setOpaque(false);
				k.setLocation((9+(k.type.id%4))*(iconWidth+10)+55, (2-(k.type.id/4))*(iconHeight+10)+47);
				numOfKoma[SenteGote.Gote.id][k.type.id]++;
				labelNumOfKoma[SenteGote.Gote.id][k.type.id].setText(Integer.valueOf(numOfKoma[SenteGote.Gote.id][k.type.id]).toString());
				labelNumOfKoma[SenteGote.Gote.id][k.type.id].setVisible(true);
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
			for(SenteGote sg: SenteGote.values()) listKomaOnHand.get(sg.id).clear();
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
			
			if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
				for(int x=0; x<40; x++) {
					k[x].reverse();
				}
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
		
		public void changeKomaColor(Color color) {
			for(int x=0; x<40; x++) {
				k[x].setBackground(color);
			}
		}
		
		public void initializeLabelSetting() {
			for(int x=0; x<8; x++) {
				labelNumOfKoma[SenteGote.Sente.id][x] = new JLabel("0");
				labelNumOfKoma[SenteGote.Sente.id][x].setBounds((10+(x%4))*(iconWidth+10)+38, (6+(x/4))*(iconHeight+10)+10, 80, 20);
				labelNumOfKoma[SenteGote.Sente.id][x].setVisible(false);
				
				labelNumOfKoma[SenteGote.Gote.id][x] = new JLabel("0");
				labelNumOfKoma[SenteGote.Gote.id][x].setBounds((10+(x%4))*(iconWidth+10)+38, (3-(x/4))*(iconHeight+10)+15, 80, 20);
				labelNumOfKoma[SenteGote.Gote.id][x].setVisible(false);
			}
		}
		
		public void putAllKomaInHand() {
			resetAllKoma();
			listKomaOnBoard.clear();
			for(int x=0; x<20; x++) {
				if(radioButtonSente.isSelected()) {
					listKomaOnHandForSente.add(k[x]);
				} else {
					k[x].reverse();
					listKomaOnHandForGote.add(k[x]);
				}
			}
			for(int x=20; x<40; x++) {
				if(radioButtonSente.isSelected()) {
					k[x].reverse();
					listKomaOnHandForSente.add(k[x]);
				} else {
					listKomaOnHandForGote.add(k[x]);
				}
			}
		}
		
		public void clearAllKomaDrawList() {
			for(int x=0; x<40; x++) {
				k[x].clearDrawList();
				k[x].repaint();
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Koma Action >> -------------------------------
	// -------------------------------------------------------------------------
	public enum KomaType {
		Pawn(0), Rance(1), Knight(2), Silver(3), Gold(4), Bishop(5), Rook(6), King(7), Empty(8);
		private final int id;
		private KomaType(final int id) {
			this.id = id;
		}
	};
	public class Koma extends JLabel {
		int px;
		int py;
		int promoted;
		int sente;
		int index;
		int drop;
		List<Point> drawListBase;
		List<Point> drawListTarget;
		KomaType type;
		Koma(KomaType t, int x, int y, int s, int i) {
			this.setIcon(shogiData.icon[t.id][0+s*2]);
			this.setBounds((shogiData.iconWidth+10)*(9-x)+25, (shogiData.iconHeight+10)*(y-1)+25, shogiData.iconWidth, shogiData.iconHeight);
			this.setBackground(boardColor);
			this.setOpaque(true);
			type = t;
			px = x;
			py = y;
			sente = s;
			promoted = 0;
			index = i;
			drop = 0;
			drawListBase = new ArrayList<Point>();
			drawListTarget = new ArrayList<Point>();
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if(drawListBase.size() >= 1) {
				Graphics2D g2 = (Graphics2D)g;
				BasicStroke stroke = new BasicStroke(8.0f);
				g2.setStroke(stroke);
				g2.setColor(Color.magenta);
				for(Point base: drawListBase) {
					Point target = drawListTarget.get(drawListBase.indexOf(base));
					g2.drawLine(base.x-getX(), base.y-getY(), target.x-getX(), target.y-getY());
				}
			}
		}
		
		public void clearDrawList() {
			drawListBase.clear();
			drawListTarget.clear();
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
				this.setIcon(shogiData.icon[type.id][sente*2+1]);
				promoted = 1;
			} else {
				this.setIcon(shogiData.icon[type.id][sente*2]);
				promoted = 0;
			}
		}
		
		public void reverse() {
			if(sente == 0) {
				sente = 1;
			} else {
				sente = 0;
			}
			this.setIcon(shogiData.icon[type.id][sente*2 + promoted]);
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
			if(sd.listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1 || sd.listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) return true;
			
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
			
			if(x == preX && y == preY) {
				this.setLocation((9-preX)*(shogiData.iconWidth+10)+25, (preY-1)*(shogiData.iconHeight+10)+25);
				return false;
			}
			
			if(promoted == -1) {
				if(!this.confirmPromotion(x, y, preX, preY, sd)) {
					// case of cancel
					this.setLocation((9-preX)*(shogiData.iconWidth+10)+25, (preY-1)*(shogiData.iconHeight+10)+25);
					return false;
				}
			}
			else if(promoted == 1 && this.promoted == 0) this.promote(); // no confirm when kifuData
			
			this.px = x;
			this.py = y;
			this.drop = 0;
			this.setLocation((9-x)*(shogiData.iconWidth+10)+25, (y-1)*(shogiData.iconHeight+10)+25);
			
			if(x>0 && x<10 && y>0 && y<10) {
				if( sd.listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1 ) moveFromStoB(sd);
				else if( sd.listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) moveFromGtoB(sd);
				else moveFromBtoB(sd);
			} else if(y>5) {
				if(sd.listKomaOnBoard.indexOf(this) != -1) moveFromBtoS(sd);
				else if(sd.listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1) moveFromStoS(sd);
				else moveFromGtoS(sd);
				return false;
			} else {
				if(sd.listKomaOnBoard.indexOf(this) != -1) moveFromBtoG(sd);
				else if(sd.listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) moveFromGtoG(sd);
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
					sd.listKomaOnHand.get(SenteGote.Sente.id).add(k);
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
					sd.listKomaOnHand.get(SenteGote.Gote.id).add(k);
				}
			}
		}
		// OnHandForSente -> OnBoard
		public void moveFromStoB(ShogiData sd) {
			this.drop = 1;
			sd.listKomaOnHand.get(SenteGote.Sente.id).remove(this);
			sd.listKomaOnBoard.add(this);
		}
		// OnHandForGote -> OnBoard
		public void moveFromGtoB(ShogiData sd) {
			this.drop = 1;
			sd.listKomaOnHand.get(SenteGote.Gote.id).remove(this);
			sd.listKomaOnBoard.add(this);
		}
		// OnBoard -> OnHandForSente
		public void moveFromBtoS(ShogiData sd) {
			sd.listKomaOnBoard.remove(this);
			
			if(this.sente == 1) this.reverse();
			if(this.promoted == 1) this.promote();
			sd.listKomaOnHand.get(SenteGote.Sente.id).add(this);
		}
		// OnBoard -> OnHandForGote
		public void moveFromBtoG(ShogiData sd) {
			sd.listKomaOnBoard.remove(this);
			
			if(this.sente == 0) this.reverse();
			if(this.promoted == 1) this.promote();	
			sd.listKomaOnHand.get(SenteGote.Gote.id).add(this);
		}
		// OnHandForSente -> OnHandForGote
		public void moveFromStoG(ShogiData sd) {
			sd.listKomaOnHand.get(SenteGote.Sente.id).remove(this);
			this.reverse();
			sd.listKomaOnHand.get(SenteGote.Gote.id).add(this);
		}
		// OnHandForGote -> OnHandForSente
		public void moveFromGtoS(ShogiData sd) {
			sd.listKomaOnHand.get(SenteGote.Gote.id).remove(this);
			this.reverse();
			sd.listKomaOnHand.get(SenteGote.Sente.id).add(this);
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
	CanvasBoard cv = new CanvasBoard();
	public class CanvasBoard extends Canvas {
		int lastPointX;
		int lastPointY;
		Boolean mousePressed;
		Boolean enableLastPoint;
		List<Point> drawList = new ArrayList<Point>();
		List<Point> drawListBase = new ArrayList<Point>();
		List<Point> drawListTargetRightClick = new ArrayList<Point>();
		List<Point> drawListBaseRightClick = new ArrayList<Point>();
		List<Point> drawListLeftClick = new ArrayList<Point>();
		List<Point> drawListEngineBase = new ArrayList<Point>();
		List<Point> drawListEngineTarget = new ArrayList<Point>();
		public CanvasBoard() {
			lastPointX = -1;
			lastPointY = -1;
			mousePressed = false;
			enableLastPoint = false;
		}
		
		public void paint(Graphics g) {
			//System.out.println("repaint()");
			shogiData.clearAllKomaDrawList();
			drawShogiBoard(g);
			drawMovableArea(g);
			drawLeftClickedPoints(g);
			drawArrowsForKifuAnalysis(g);
			drawArrowsForRightClick(g);
			drawArrowForEngine(g);
			drawLastPoint(g);
		}
		
		public void drawShogiBoard(Graphics g) {
			g.setColor(boardColor);
			g.fillRect(20, 20, (shogiData.iconWidth+10)*9, (shogiData.iconHeight+10)*9);
			
			g.setColor(Color.black);
			for(int x=0; x<9; x++)
				for(int y=0; y<9; y++) {
					g.drawRect(x*(shogiData.iconWidth+10)+20, y*(shogiData.iconHeight+10)+20, shogiData.iconWidth+10, shogiData.iconHeight+10);
				}
			
			// draw area for Koma in hand
			Graphics2D g2 = (Graphics2D)g;
			BasicStroke bs2 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {4}, 0);
			g2.setStroke(bs2);
			g2.drawRect(590, 119, 248, 138);
			g2.drawRect(590, 442, 248, 138);
		}
		
		public void drawMovableArea(Graphics g) {
			if(mousePressed) {
				for(int x=1; x<10; x++) for(int y=1; y<10; y++) {
					if(shogiData.selectedKoma.isMovable(x, y)) cv.drawPoint(x, y, Color.pink);
				}
			}
		}
		
		public void drawLastPoint(Graphics g) {
			if(enableLastPoint) {
				int X, Y;
				if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
					X = 10 - lastPointX;
					Y = 10 - lastPointY;
				} else {
					X = lastPointX;
					Y = lastPointY;
				}
				drawPoint(X, Y, Color.orange);
			}
		}
		
		public void drawArrowsForKifuAnalysis(Graphics g) {
			for(Point p: drawList) {
				Point pB = drawListBase.get(drawList.indexOf(p));
				int pBX, pBY, pX, pY;
				if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
					pBX = 10 - pB.x;
					pBY = 10 - pB.y;
					pX = 10 - p.x;
					pY = 10 - p.y;
				} else {
					pBX = pB.x;
					pBY = pB.y;
					pX = p.x;
					pY = p.y;
				}
				
				Point pBase = new Point((9-pBX)*(shogiData.iconWidth+10)+25+shogiData.iconWidth/2, (pBY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
				Point pTarget = new Point((9-pX)*(shogiData.iconWidth+10)+25+shogiData.iconWidth/2, (pY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
				Arrow ar = new Arrow(pBase, pTarget);
				BasicStroke stroke = new BasicStroke(1.0f);
				g.setColor(Color.blue);
				ar.draw((Graphics2D)g, stroke);
			}
		}
		
		public void drawArrowForEngine(Graphics g) {
			if(out == null) return;
			float bs = 10.0f;
			Color c;
			int red = 0;
			int green = 250;
			int blue = 0;
			// for safe access in multi thread
			synchronized(drawListEngineBase) {
				for(Point p: drawListEngineTarget) {
					int pBX, pBY, pX, pY;
					Point pB = drawListEngineBase.get(drawListEngineTarget.indexOf(p));
					pBX = pB.x;
					pBY = pB.y;
					pX = p.x;
					pY = p.y;
					
					Point pBase = new Point((9-pBX)*(shogiData.iconWidth+10)+25+shogiData.iconWidth/2, (pBY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
					Point pTarget = new Point((9-pX)*(shogiData.iconWidth+10)+25+shogiData.iconWidth/2, (pY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
					
					Arrow ar = new Arrow(pBase, pTarget);
					BasicStroke stroke = new BasicStroke(bs);
					c = new Color(red, green, blue);
					g.setColor(c);
					ar.draw((Graphics2D)g, stroke);
					bs -= 3.0f;
					green -= 80;
				}
			}
		}
		
		public void clearDrawListForEngine() {
			// for safe access in multi thread
			synchronized(drawListEngineBase) {
				drawListEngineBase.clear();
				drawListEngineTarget.clear();
			}
		}
		
		public void findKomaOnLine(Point p1, Point p2) {
			for(int x=0; x<40; x++) {
				Boolean isOnline = false;
				Boolean isBaseDetected = false;
				int X = shogiData.k[x].getX();
				int Y = shogiData.k[x].getY();
				Point p[] = new Point[4];
				p[0] = new Point(X, Y);
				p[1] = new Point(X+shogiData.iconWidth, Y);
				p[2] = new Point(X+shogiData.iconWidth, Y+shogiData.iconHeight);
				p[3] = new Point(X, Y+shogiData.iconHeight);
				
				double d[] = new double[4];
				Point v1 = new Point(p2.x-p1.x, p2.y-p1.y);
				Point v2[] = new Point[4];
				for(int k=0; k<4; k++) {
					v2[k] = new Point(p[k].x-p1.x, p[k].y-p1.y);
				}
				for(int k=0; k<4; k++) {
					d[k] = v1.x * v2[k].y - v2[k].x * v1.y;
				}
				if(d[0]*d[1] >= 0 && d[1]*d[2] >= 0 && d[2]*d[3] >= 0 && d[3]*d[0] >= 0) {
					continue;
				}
				
				Point center = new Point(X + shogiData.iconWidth/2, Y + shogiData.iconHeight/2);
				if(isCentralPoint(center, p1, p2)) isOnline = true;
				
				if(!isOnline) continue;
				
				if(d[0]*d[1] < 0) {
					isBaseDetected = addDrawPointOnKoma(isBaseDetected, shogiData.k[x], p1, p2, p[0], p[1]);
				}
				if(d[1]*d[2] < 0) {
					isBaseDetected = addDrawPointOnKoma(isBaseDetected, shogiData.k[x], p1, p2, p[1], p[2]);
				}
				if(d[2]*d[3] < 0) {
					isBaseDetected = addDrawPointOnKoma(isBaseDetected, shogiData.k[x], p1, p2, p[2], p[3]);
				}
				if(d[3]*d[0] < 0) {
					isBaseDetected = addDrawPointOnKoma(isBaseDetected, shogiData.k[x], p1, p2, p[3], p[0]);
				}
				shogiData.k[x].repaint();
			}
		}
		
		public Boolean addDrawPointOnKoma(Boolean isBaseDetected, Koma k, Point p1, Point p2, Point p3, Point p4) {
			Point pCross = calcCrossPoint(p1, p2, p3, p4);
			if(!isBaseDetected) {
				k.drawListBase.add(pCross);
				return true;
			}
			
			Point pBase = k.drawListBase.get(k.drawListBase.size()-1);
			if(isCentralPoint(p1, pBase, pCross) && isCentralPoint(pBase, p1, p2)) {
				k.drawListTarget.add(p1);
			} else if(isCentralPoint(p1, pBase, pCross) && isCentralPoint(pCross, p1, p2)) {
				k.drawListBase.remove(k.drawListBase.size()-1);
				k.drawListBase.add(p1);
				k.drawListTarget.add(pCross);
			} else {
				k.drawListTarget.add(pCross);
			}
			
			return true;
		}
		
		public Boolean isCentralPoint(Point p1, Point base, Point target) {
			Boolean result = false;
			if(p1.x >= base.x && p1.x <= target.x && p1.y >= base.y && p1.y <= target.y) result = true;
			else if(p1.x >= base.x && p1.x <= target.x && p1.y <= base.y && p1.y >= target.y) result = true;
			else if(p1.x <= base.x && p1.x >= target.x && p1.y >= base.y && p1.y <= target.y) result = true;
			else if(p1.x <= base.x && p1.x >= target.x && p1.y <= base.y && p1.y >= target.y) result = true;
			
			return result;
		}
		
		public Point calcCrossPoint(Point p1, Point p2, Point p3, Point p4) {
			Point pCross = new Point();
			double a;
			if(p2.x == p1.x) a = 0;
			else a = (double)(p2.y - p1.y) / (double)(p2.x - p1.x);
			double b = p1.y - a * p1.x;
			double c;
			if(p4.x == p3.x) c = 0;
			else c = (double)(p4.y - p3.y) / (double)(p4.x - p3.x);
			double d;
			if(p2.y == p1.y) d = p1.y;
			else d = p3.y - c * p3.x;
			
			if(p1.x==p2.x) pCross.x = p1.x;
			else if(p1.y==p2.y) pCross.x = p3.x;
			else if(p3.x==p4.x) pCross.x = p3.x;
			else pCross.x = (int)((d-b) / (a-c));
			if(p3.x==p4.x) pCross.y = (int)(a*(double)pCross.x + b);
			else pCross.y = (int)(c * pCross.x) + (int)d;
			
			return pCross;
		}
		
		public void drawArrowsForRightClick(Graphics g) {
			for(Point pTarget: drawListTargetRightClick) {
				Point pBase = drawListBaseRightClick.get(drawListTargetRightClick.indexOf(pTarget));
				Point pBaseConverted = convertMousePointToCentralSquare(pBase);
				Point pTargetConverted = convertMousePointToCentralSquare(pTarget);
				if(pBaseConverted.x == pTargetConverted.x && pBaseConverted.y == pTargetConverted.y) continue;
				findKomaOnLine(pBaseConverted, pTargetConverted);
				Arrow ar = new Arrow(pBaseConverted, pTargetConverted);
				BasicStroke stroke = new BasicStroke(8.0f);
				g.setColor(Color.magenta);
				ar.draw((Graphics2D)g, stroke);
			}
		}
		
		public void drawLeftClickedPoints(Graphics g) {
			for(Point p: drawListLeftClick) {
				Point pShogiXY = convertMousePointToShogiXY(p);
				BasicStroke stroke = new BasicStroke(4.0f);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(stroke);
				g2.setColor(new Color(250,150,162));
				g2.drawOval((9-pShogiXY.x)*(shogiData.iconWidth+10)+25, (pShogiXY.y-1)*(shogiData.iconHeight+10)+32, 50, 50);
			}
		}
		
		public Point convertMousePointToCentralSquare(Point p) {
			Point pCalculated = new Point();
			int x = (p.x-25) / (shogiData.iconWidth+10);
			int y = (p.y-25) / (shogiData.iconHeight+10);
			pCalculated.x = 25+x*(shogiData.iconWidth+10) + shogiData.iconWidth/2;
			pCalculated.y = 25+y*(shogiData.iconHeight+10) + shogiData.iconHeight/2;
			
			return pCalculated;
		}
		
		public Point convertMousePointToShogiXY(Point p) {
			Point pCalculated = new Point();
			pCalculated.x = 9 - (p.x-25) / (shogiData.iconWidth+10);
			pCalculated.y = 1 + (p.y-25) / (shogiData.iconHeight+10);
			//System.out.println(pCalculated);
			
			return pCalculated;
		}
		
		public void drawPoint(int x, int y, Color cl) {
			Graphics g = getGraphics();
			g.setColor(cl);
			g.fillRect((9-x)*(shogiData.iconWidth+10)+22, (y-1)*(shogiData.iconHeight+10)+22, shogiData.iconWidth+6, shogiData.iconHeight+6);
		}
		
		public void setLastPoint(int px, int py, Boolean enable) {
			lastPointX = px;
			lastPointY = py;
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
		
		public void clearDrawPointForRightClick() {
			drawListTargetRightClick.clear();
			drawListBaseRightClick.clear();
			drawListLeftClick.clear();
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
				//double w = size.width * .5;
				double w = size.width * .8;
				path.moveTo(0d, -w);
				path.lineTo(t, 0d);
				path.lineTo(0d, w);
				path.closePath();
				return path;
			}
			
			public void draw(Graphics2D g2, BasicStroke stroke) {
				g2.setStroke(stroke);
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
		clearTextBox();
		clearCheckBox();
		reverseNumberRowCol();
		initializePlayerIcon();
		initializeCastleIcon();
		shogiData.resetAllKoma();	
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		loadStrategyData();
		loadCastleData();
		loadTesujiData();
		actionForDB();
		countStrategy();
		countCastle();
		countTesujiData();
		createPlayerDataBase();
	}
	public void actionForSave() {
		Path path;
		String fileName;
		int index = 1;
		
		while(true) {
			fileName = kifuFilePath + "/" + String.format("kifu%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
			fw.write(textBox[TextBoxType.Player1.id].getText() + "\n");
			fw.write(textBox[TextBoxType.Player2.id].getText() + "\n");
			for(Kifu kf: kifuData) fw.write(kf.k.index + "," + kf.x + "," + kf.y + "," + kf.p + "," + kf.pp + "," + kf.d + "\n");
			if(checkBox[CheckBoxType.Draw.id].isSelected()) fw.write("-1");
			fw.close();
			
			System.out.println(fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void actionForLoad() {
		loadByNumber(textBox[TextBoxType.LoadFile.id].getText(), 
				textBox[TextBoxType.LoadStep.id].getText(),
				textBox[TextBoxType.LoadYear.id].getText());
	}
	public void loadByNumber(String numStrFile, String numStrStep, String numStrYear) {
		String fileName;
		if(numStrFile.equals("")) {
			Path path = Paths.get("").toAbsolutePath();
			FileDialog fd = new FileDialog(this, "Load", FileDialog.LOAD);
			fd.setDirectory(path.toString() + "/kifu/");
			fd.setVisible(true);
			if(fd.getFile() == null) return;
			fileName = kifuFilePath + "/" + fd.getFile();
		} else {
			fileName = kifuFilePath + numStrYear + "/" + "kifu" + numStrFile + ".txt";
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
			textBox[TextBoxType.Player1.id].setText(br.readLine());
			textBox[TextBoxType.Player2.id].setText(br.readLine());
			while((content = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(content,",");
				while(st.hasMoreTokens()) {
					int i = Integer.parseInt(st.nextToken()); // index
					if(i == -1) continue; // Draw game
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
			if(numStrStep.equals("")) {
				listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
				listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
			} else {
				int selectedIndex = Integer.parseInt(numStrStep);
				listBox[ListBoxType.Kifu.id].setSelectedIndex(selectedIndex);
				listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(selectedIndex);
				commonListAction();
			}
			
			shogiData.viewKomaOnBoard();
			shogiData.viewKomaOnHand();
			updatePlayerIcon();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	
	public void actionForDB() {
		kifuDB.clear();
		String selectedYear = (String)comboBox.getSelectedItem();
		if(selectedYear.equals("") || selectedYear.equals("all")) loadKifuDBByYear("");
		if(selectedYear.equals("2022") || selectedYear.equals("all")) loadKifuDBByYear("2022");
		if(selectedYear.equals("2023") || selectedYear.equals("all")) loadKifuDBByYear("2023");
	}
	public void loadKifuDBByYear(String strY) {
		try {
			System.out.println("Load Kifu Data");
			int fileIndex = 1;
			while(true) {
				shogiDataForKDB.resetAllKoma();
				String fileName = kifuFilePath + strY + "/" + "kifu" + String.format("%03d", fileIndex) + ".txt";
				//System.out.println(fileName);
				File file = new File(fileName);
				KifuDataBase kdb = new KifuDataBase();
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				for(SenteGote sg: SenteGote.values()) {
					kdb.playerName[sg.id] = br.readLine();
				}
				kdb.year = strY;
				kdb.index = fileIndex;
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						int i = Integer.parseInt(st.nextToken()); // index
						if(i == -1) { // Draw game
							kdb.isSenteWin = -1;
							continue;
						}
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
						if(kdb.castleName[SenteGote.Sente.id].equals("")) {
							kdb.castleName[SenteGote.Sente.id] = checkCastle(shogiDataForKDB, true);
							//if(kdb.castleName[SenteGote.Sente.id].equals("Yagura")) {
							//	System.out.println("Yagura:" + kdb.index);
							//}
						}
						if(kdb.castleName[SenteGote.Gote.id].equals("")) {
							kdb.castleName[SenteGote.Gote.id] = checkCastle(shogiDataForKDB, false);
						}
					}
				}
				br.close();
				if(kdb.isSenteWin != -1) kdb.isSenteWin = isSenteWin(kdb);
				kifuDB.add(kdb);
				fileIndex++;
			}
		} catch(FileNotFoundException en) {
			System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		commonListAction();
		
		System.out.println("Finish");
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
		
			fw.write(textBox[TextBoxType.Strategy.id].getText() + "\n");
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
			fileName = strategyFilePath + String.format("castle%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBox[TextBoxType.Castle.id].getText() + "\n");
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
	public void actionForTesuji() {
		Path path;
		String fileName;
		int index = 1;
		
		while(true) {
			fileName = kifuFilePath + "/" + String.format("tesuji%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBox[TextBoxType.Tesuji.id].getText() + "\n");
			if(textBox[TextBoxType.LoadFile.id].getText().equals("")) {
				fw.write(String.format("%03d", kifuDB.size()+1) + "\n");
			}
			else {
				fw.write(textBox[TextBoxType.LoadFile.id].getText() + "\n");
			}
			fw.write(listBox[ListBoxType.Kifu.id].getSelectedIndex() + "\n");
			fw.close();
			
			System.out.println(fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForKifu() {
		int index = listBox[ListBoxType.Kifu.id].getSelectedIndex();
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
		listModel[ListBoxType.Info.id].addElement("<Kifus of Same Position>");
		listModel[ListBoxType.Info.id].addElement("-------------");
		
		for(KifuDataBase kdb: kifuDB) {
			int i=0;
			Boolean isSame = true;
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
			if(isSame && index < kdb.db.size()) {
				String str = String.format("kf%03d:000:%s:", kdb.index, kdb.year);
				str += kdb.playerName[SenteGote.Sente.id] + "(" + kdb.castleName[SenteGote.Sente.id] + ")" + " vs " + kdb.playerName[SenteGote.Gote.id] + "(" + kdb.castleName[SenteGote.Gote.id] + ")";
				if(kdb.isSenteWin == 1) str+="(Sente Win)";
				else if(kdb.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				listModel[ListBoxType.Info.id].addElement(str);
			}
		}
		
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == button[ButtonType.Initialize.id].getText()) {
			actionForInitialize();
		}
		if(e.getActionCommand() == button[ButtonType.Save.id].getText()) {
			actionForSave();
		}
		if(e.getActionCommand() == button[ButtonType.Load.id].getText()) {
			actionForLoad();
		}
		if(e.getActionCommand() == button[ButtonType.Strategy.id].getText()) {
			actionForStrategy();
		}
		if(e.getActionCommand() == button[ButtonType.Castle.id].getText()) {
			actionForCastle();
		}
		if(e.getActionCommand() == button[ButtonType.Tesuji.id].getText()) {
			actionForTesuji();
		}
		if(e.getActionCommand() == button[ButtonType.Kifu.id].getText()) {
			actionForKifu();
		}
		if(e.getSource() == menuItem[MenuType.Color.id]) {
			actionForMenuColor();
		}
		if(e.getSource() == menuItem[MenuType.Capture.id]) {
			actionForCaptureBoard();
		}
		if(e.getSource() == menuItem[MenuType.KomaInHand.id]) {
			actionForKomaInHand();
		}
		if(e.getSource() == menuItem[MenuType.StartEngine.id]) {
			actionForStartEngine();
		}
		if(e.getSource() == menuItem[MenuType.StopEngine.id]) {
			actionForStopEngine();
		}
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Strategy Data >> -----------------------------
	// -------------------------------------------------------------------------
	List<StrategyData> strategyDataBase = new ArrayList<StrategyData>();
	List<StringCount> strategyCountData = new ArrayList<StringCount>();
	public class StrategyData {
		Point p[];
		String name;
		StrategyData(String strName) {
			name = strName;
			p = new Point[40];
		}
	}
	public void loadStrategyData() {
		System.out.println("Load Strategy Data");
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
		System.out.println("Finish");
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
				//System.out.println("strategy" + String.format("%03d", strategyDataBase.indexOf(strData)+1) + ".txt matched");
				return strData.name;
			}
		}
		
		return "";
	}
	
	public void countStrategy() {
		listModel[ListBoxType.Strategy.id].clear();
		listBox[ListBoxType.Strategy.id].setModel(listModel[ListBoxType.Strategy.id]);
		strategyCountData.clear();
		
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: strategyCountData) {
				if(sc.str.equals(kdb.strategyName)) {
					sc.cnt++;
					if(kdb.isSenteWin == 1) sc.senteWinCnt++;
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
		String str = "<Total:" + String.format("%2d", totalCnt)+" Games" + "(Sente Winning Rate" + String.format("%.0f", d) + "%)>";
		listModel[ListBoxType.Strategy.id].addElement(str);
		listModel[ListBoxType.Strategy.id].addElement("----------");
		for(StringCount sc: strategyCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			listModel[ListBoxType.Strategy.id].addElement(str);
		}
		listBox[ListBoxType.Strategy.id].setModel(listModel[ListBoxType.Strategy.id]);
	}
	
	public void updateListBox2ByStrategy() {
		int selectedIndex = listBox[ListBoxType.Strategy.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndex4 = listBox[ListBoxType.Player.id].getSelectedIndex();
		String playerName = "";
		if(selectedIndex4 >= 2) {
			playerName = listModel[ListBoxType.Player.id].getElementAt(selectedIndex4);
			//System.out.println(playerName);
		}
		int selectedIndexCastle = listBox[ListBoxType.Castle.id].getSelectedIndex();
		String castleName = "";
		if(selectedIndexCastle >= 2) {
			StringCount sc = castleCountData.get(selectedIndexCastle-2);
			castleName = sc.str;
		}
		
		StringCount sc = strategyCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String strategy = sc.str;
		textBox[TextBoxType.Strategy.id].setText(strategy);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);

		if(playerName.equals("")) listModel[ListBoxType.Info.id].addElement("<"+ strategy + "'s Kifu>");
		else listModel[ListBoxType.Info.id].addElement("<"+ strategy + "(" + playerName + ")"+"'s Kifu>");
		listModel[ListBoxType.Info.id].addElement("-------------");
		for(KifuDataBase kdb: kifuDB) {
			if(kdb.strategyName.equals(strategy)) {
				String str = String.format("kf%03d:000:%s:", kdb.index, kdb.year);
				str += kdb.playerName[SenteGote.Sente.id] + "(" + kdb.castleName[SenteGote.Sente.id] + ")" + " vs " + kdb.playerName[SenteGote.Gote.id] + "(" + kdb.castleName[SenteGote.Gote.id] + ")";
				if(kdb.isSenteWin == 1) str+="(Sente Win)";
				else if(kdb.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				if(playerName.equals("") && castleName.equals("")) {
					listModel[ListBoxType.Info.id].addElement(str);
				} else if(!playerName.equals("") && !castleName.equals("")) {
					if(str.contains(playerName) && (kdb.castleName[SenteGote.Sente.id].equals(castleName) || kdb.castleName[SenteGote.Gote.id].equals(castleName))) {
						listModel[ListBoxType.Info.id].addElement(str);
					}
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) listModel[ListBoxType.Info.id].addElement(str);
				} else if(!castleName.equals("")) {
					if(kdb.castleName[SenteGote.Sente.id].equals(castleName) || kdb.castleName[SenteGote.Gote.id].equals(castleName)) listModel[ListBoxType.Info.id].addElement(str);
				}
			}
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	public void getLoadNumberOnListBox2() {
		int index = listBox[ListBoxType.Info.id].getSelectedIndex();
		if(index < 2) return;
		String str = listModel[ListBoxType.Info.id].getElementAt(listBox[ListBoxType.Info.id].getSelectedIndex());
		String subStrFile = str.substring(2,5);
		String subStrStep = str.substring(6,9);
		String subStrYear = str.substring(10,14);
		textBox[TextBoxType.LoadFile.id].setText("");
		textBox[TextBoxType.LoadStep.id].setText("");
		textBox[TextBoxType.LoadYear.id].setText("");
		if(subStrFile.matches("[+-]?\\d*(\\.\\d+)?")) textBox[TextBoxType.LoadFile.id].setText(subStrFile);
		if(subStrStep.matches("[+-]?\\d*(\\.\\d+)?")) textBox[TextBoxType.LoadStep.id].setText(subStrStep);
		if(subStrYear.matches("[+-]?\\d*(\\.\\d+)?")) textBox[TextBoxType.LoadYear.id].setText(subStrYear);
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Castle Data >> -----------------------------
	// -------------------------------------------------------------------------
	List<CastleData> castleDataBase = new ArrayList<CastleData>();
	List<StringCount> castleCountData = new ArrayList<StringCount>();
	public class CastleData {
		String name;
		int data[][] = new int[25][4];
		CastleData(String castleName) {
			name = castleName;
		}
	}
	public void loadCastleData() {
		System.out.println("Load Castle Data");
		castleDataBase.clear();
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = strategyFilePath + "castle" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				content = br.readLine(); // name
				CastleData castleData = new CastleData(content);
				int i = 0;
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						castleData.data[i][0] =  Integer.parseInt(st.nextToken()); // sente
						castleData.data[i][1] = Integer.parseInt(st.nextToken()); // x
						castleData.data[i][2] = Integer.parseInt(st.nextToken()); // y
						castleData.data[i][3] = Integer.parseInt(st.nextToken()); // type
						i++;
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
		
		System.out.println("Finish");
	}
	
	public void countCastle() {
		listModel[ListBoxType.Castle.id].clear();
		listBox[ListBoxType.Castle.id].setModel(listModel[ListBoxType.Castle.id]);
		castleCountData.clear();
		
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kdb.castleName[SenteGote.Sente.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kdb.castleName[SenteGote.Sente.id], kdb.isSenteWin);
				castleCountData.add(sc);
			}
		}
		for(KifuDataBase kdb: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kdb.castleName[SenteGote.Gote.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kdb.castleName[SenteGote.Gote.id], kdb.isSenteWin);
				castleCountData.add(sc);
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
		for(StringCount sc: castleCountData) {	
			totalCnt += sc.cnt;
		}
		String str = "<Total:" + String.format("%2d", totalCnt)+" Castles>";
		listModel[ListBoxType.Castle.id].addElement(str);
		listModel[ListBoxType.Castle.id].addElement("----------");
		for(StringCount sc: castleCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			listModel[ListBoxType.Castle.id].addElement(str);
		}
		listBox[ListBoxType.Castle.id].setModel(listModel[ListBoxType.Castle.id]);
	}
	
	public void updateListBoxInfoByCastle() {
		int selectedIndex = listBox[ListBoxType.Castle.id].getSelectedIndex()-2;
		if(selectedIndex < 0) {
			initializeCastleIcon();
			return;
		}
		int selectedIndexStrategy = listBox[ListBoxType.Strategy.id].getSelectedIndex();
		String strategyName = "";
		if(selectedIndexStrategy >= 2) {
			StringCount sc = strategyCountData.get(selectedIndexStrategy-2);
			strategyName = sc.str;
		}
		int selectedIndexPlayer = listBox[ListBoxType.Player.id].getSelectedIndex();
		String playerName = "";
		if(selectedIndexPlayer >= 2) {
			playerName = listModel[ListBoxType.Player.id].getElementAt(selectedIndexPlayer);
			//System.out.println(playerName);
		}
		
		StringCount sc = castleCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String castleName = sc.str;
		updateCastleIcon();
		textBox[TextBoxType.Castle.id].setText(castleName);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);

		if(strategyName.equals("")) listModel[ListBoxType.Info.id].addElement("<"+ castleName + "'s Kifu>");
		else listModel[ListBoxType.Info.id].addElement("<"+ strategyName + "(" + castleName + ")"+"'s Kifu>");
		listModel[ListBoxType.Info.id].addElement("-------------");
		for(KifuDataBase kdb: kifuDB) {
			if(kdb.castleName[SenteGote.Sente.id].equals(castleName) || kdb.castleName[SenteGote.Gote.id].equals(castleName)) {
				String str = String.format("kf%03d:000:%s:", kdb.index, kdb.year);
				str += kdb.playerName[SenteGote.Sente.id] + "(" + kdb.castleName[SenteGote.Sente.id] + ")" + " vs " + kdb.playerName[SenteGote.Gote.id] + "(" + kdb.castleName[SenteGote.Gote.id] + ")";
				if(kdb.isSenteWin == 1) str+="(Sente Win)";
				else if(kdb.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				
				if(strategyName.equals("") && playerName.equals("")) {
					listModel[ListBoxType.Info.id].addElement(str);
				} else if(!strategyName.equals("") && !playerName.equals("")) {
					if(kdb.strategyName.equals(strategyName) && str.contains(playerName)) listModel[ListBoxType.Info.id].addElement(str);
				} else if(!strategyName.equals("")) {
					if(kdb.strategyName.equals(strategyName)) listModel[ListBoxType.Info.id].addElement(str);
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) listModel[ListBoxType.Info.id].addElement(str);
				}
			}
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
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
	public void updateCastleIcon() {
		int selectedIndex = listBox[ListBoxType.Castle.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		StringCount sc = castleCountData.get(selectedIndex);
		String castleName = sc.str;
		
		castleIconLabel.setIcon(null);
		ImageIcon castleIcon = new ImageIcon(imgFilePath + castleName + ".jpg");
		Image image = castleIcon.getImage();
		Image newImage = image.getScaledInstance(180, 230, java.awt.Image.SCALE_SMOOTH);
		castleIcon = new ImageIcon(newImage);
		castleIconLabel.setIcon(castleIcon);
	}
	public void initializeCastleIcon() {
		castleIconLabel.setIcon(null);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Tesuji Data >> -----------------------------
	// -------------------------------------------------------------------------
	List<TesujiData> tesujiDataBase = new ArrayList<TesujiData>();
	List<StringCount> tesujiCountData = new ArrayList<StringCount>();
	public class TesujiData {
		String name;
		int fileIndex;
		int stepIndex;
		String year;
		TesujiData(String tesujiName, int file, int step) {
			name = tesujiName;
			fileIndex = file;
			stepIndex = step;
		}
	}
	public void loadTesujiData() {
		tesujiDataBase.clear();
		String selectedYear = (String)comboBox.getSelectedItem();
		if(selectedYear.equals("") || selectedYear.equals("all")) loadTesujiDataByYear("");
		if(selectedYear.equals("2022") || selectedYear.equals("all")) loadTesujiDataByYear("2022");
		if(selectedYear.equals("2023") || selectedYear.equals("all")) loadTesujiDataByYear("2023");
	}
	public void loadTesujiDataByYear(String strY) {
		System.out.println("Load Tesuji Data");
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = kifuFilePath + strY + "/" + "tesuji" + String.format("%03d", fileIndex) + ".txt";
				File file = new File(fileName);
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String name = br.readLine();
				int fileNumber = Integer.parseInt(br.readLine());
				int stepNumber = Integer.parseInt(br.readLine());
				TesujiData tesujiData = new TesujiData(name, fileNumber, stepNumber); 
				tesujiData.year = strY;
				br.close();
				tesujiDataBase.add(tesujiData);
				fileIndex++;
				//System.out.println(castleData.name);
			}
		} catch(FileNotFoundException en) {
			System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		System.out.println("Finish");
	}
	public void countTesujiData() {
		listModel[ListBoxType.Tesuji.id].clear();
		listBox[ListBoxType.Tesuji.id].setModel(listModel[ListBoxType.Tesuji.id]);
		tesujiCountData.clear();
		
		for(TesujiData td: tesujiDataBase) {
			Boolean found = false;
			for(StringCount sc: tesujiCountData) {
				if(sc.str.equals(td.name)) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(td.name, 1);
				tesujiCountData.add(sc);
			}
		}
		
		Collections.sort(
				tesujiCountData,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
		
		int totalCnt = 0;
		for(StringCount sc: tesujiCountData) {	
			totalCnt += sc.cnt;
		}
		String str = "<Total:" + String.format("%2d", totalCnt)+" Tesujis>";
		listModel[ListBoxType.Tesuji.id].addElement(str);
		listModel[ListBoxType.Tesuji.id].addElement("----------");
		for(StringCount sc: tesujiCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" counts";
			listModel[ListBoxType.Tesuji.id].addElement(str);
		}
		listBox[ListBoxType.Tesuji.id].setModel(listModel[ListBoxType.Tesuji.id]);
	}
	public void updateListBoxInfoByTesuji() {
		int selectedIndex = listBox[ListBoxType.Tesuji.id].getSelectedIndex()-2;
		if(selectedIndex < 0) {
			return;
		}
		
		StringCount sc = tesujiCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String tesujiName = sc.str;
		textBox[TextBoxType.Tesuji.id].setText(tesujiName);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);

		listModel[ListBoxType.Info.id].addElement("<"+ tesujiName + "'s Kifu>");
		listModel[ListBoxType.Info.id].addElement("-------------");
		for(TesujiData td: tesujiDataBase) {
			if(td.name.equals(tesujiName)) {
				String str = "kf" + String.format("%03d:%03d:%s", td.fileIndex, td.stepIndex, td.year);
				KifuDataBase kdb = getKDB(td.fileIndex, td.year);
				if(kdb == null) continue;
				str += ":" + kdb.playerName[SenteGote.Sente.id] + " vs " + kdb.playerName[SenteGote.Gote.id];
				listModel[ListBoxType.Info.id].addElement(str);
			}
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Kifu Data >> -----------------------------
	// -------------------------------------------------------------------------
	List<Kifu> kifuData = new ArrayList<Kifu>();
	List<KifuDataBase> kifuDB = new ArrayList<KifuDataBase>();
	public class StringCount {
		String str;
		int cnt;
		Point target;
		Point base;
		int index;
		int senteWinCnt;
		StringCount(String s, int isSenteWin) {
			str = s;
			cnt = 1;
			if(isSenteWin == 1) {
				senteWinCnt = 1;
			}
			else if(isSenteWin == 0) {
				senteWinCnt = 0;
			}
		}
	}
	public class KifuDataBase {
		List<Kifu> db = new ArrayList<Kifu>();
		String playerName[] = new String[2];
		String strategyName;
		String castleName[] = new String[2];
		String year;
		int index;
		//Boolean isSenteWin;
		int isSenteWin;
		
		KifuDataBase() {
			strategyName = "";
			for(SenteGote sg: SenteGote.values()) castleName[sg.id] = "";
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
		String s = shogiData.senteGote[sente] + String.valueOf(x)+String.valueOf(y);
		if(preP == 0 && promoted == 1) {
			s += shogiData.komaName[type.id] + "成";
		} else {
			s += shogiData.komaName[type.id+8*promoted];
		}
		if(drop == 1) s += "打";
		
		return s;
	}
	public KifuDataBase getKDB(int fileIndex, String year) {
		for(KifuDataBase kdb: kifuDB) {
			if(kdb.index == fileIndex && kdb.year.equals(year)) return kdb;
		}
		
		return null;
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
		// count string data if same string
		for(StringCount sc: listSC) {
			if(sc.str.equals(str)) {
				sc.cnt++;
				if(isSenteWin(kdb) == 1) sc.senteWinCnt++;
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
	
	public int isSenteWin(KifuDataBase kdb) {
		int index = kdb.db.size()-1;
		if((index%2) == 0) return 1;
		return 0;
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
	List<PlayerData> playerDataBase = new ArrayList<PlayerData>();
	public class PlayerData {
		String playerName;
		List<GameResult> grList = new ArrayList<GameResult>();
		
		PlayerData(String name) {
			playerName = name;
		}
	}
	public class GameResult {
		String strategy;
		String castle;
		int isPlayerWin;
		Boolean isSente;
		
		GameResult() {}
	}
	public class GameResultCount {
		String str;
		int cnt;
		int senteWinCnt = 0;
		int senteLoseCnt = 0;
		int senteDrawCnt = 0;
		int goteWinCnt = 0;
		int goteLoseCnt = 0;
		int goteDrawCnt = 0;
		GameResultCount(String s, int isPlayerWin, Boolean isSente) {
			str = s;
			cnt = 1;
			if(isPlayerWin == 1) {
				if(isSente) {
					senteWinCnt = 1;
				} else {
					goteWinCnt = 1;
				}
			} else if(isPlayerWin == 0) {
				if(isSente) {
					senteLoseCnt = 1;
				} else {
					goteLoseCnt = 1;
				}
			} else {
				if(isSente) {
					senteDrawCnt = 1;
				} else {
					goteDrawCnt = 1;
				}
			}
		}
	}
	public void createPlayerDataBase() {
		listModel[ListBoxType.Player.id].clear();
		listBox[ListBoxType.Player.id].setModel(listModel[ListBoxType.Player.id]);
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
			if(kdb.isSenteWin == 1) {
				grS.isPlayerWin = 1;
				grG.isPlayerWin = 0;
			}
			else if(kdb.isSenteWin == 0) {
				grS.isPlayerWin = 0;
				grG.isPlayerWin = 1;
			} else { // Draw
				grS.isPlayerWin = -1;
				grG.isPlayerWin = -1;
			}
			grS.castle = kdb.castleName[SenteGote.Sente.id];
			grG.castle = kdb.castleName[SenteGote.Gote.id];
			
			for(PlayerData pd: playerDataBase) {
				if(kdb.playerName[SenteGote.Sente.id].equals(pd.playerName)) {
					foundS = true;
					pd.grList.add(grS);
				} else if(kdb.playerName[SenteGote.Gote.id].equals(pd.playerName)) {
					foundG = true;
					pd.grList.add(grG);
				}
			}
			
			if(!foundS) {
				PlayerData pd = new PlayerData(kdb.playerName[SenteGote.Sente.id]);
				pd.grList.add(grS);
				playerDataBase.add(pd);
			}
			if(!foundG) {
				PlayerData pd = new PlayerData(kdb.playerName[SenteGote.Gote.id]);
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
		
		String str = "<Total:" + playerDataBase.size() + " Players>";
		listModel[ListBoxType.Player.id].addElement(str);
		listModel[ListBoxType.Player.id].addElement("---------");
		
		for(PlayerData pd: playerDataBase) {
			listModel[ListBoxType.Player.id].addElement(pd.playerName);
		}
		listBox[ListBoxType.Player.id].setModel(listModel[ListBoxType.Player.id]);
	}
	public void updateListBox2ByPlayerName() {
		int selectedIndex = listBox[ListBoxType.Player.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		
		PlayerData pd = playerDataBase.get(selectedIndex);
		textBox[TextBoxType.Player1.id].setText(pd.playerName);
		updatePlayerIcon();
		
		// count strategy data
		List<GameResultCount> grcList = new ArrayList<GameResultCount>();
		countStrategyDataByPlayerData(grcList, pd);
		
		// count castle data
		List<StringCount> strList = new ArrayList<StringCount>();
		countCastleDataByPlayerData(strList, pd);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
		
		listModel[ListBoxType.Info.id].addElement("<" + pd.playerName + "'s Winning Rate>");
		
		int totalCnt = 0;
		int totalWinCnt = 0;
		int totalLoseCnt = 0;
		int totalDrawCnt = 0;
		int totalSenteWinCnt = 0;
		int totalGoteWinCnt = 0;
		int totalSenteLoseCnt = 0;
		int totalGoteLoseCnt = 0;
		int totalSenteDrawCnt = 0;
		int totalGoteDrawCnt = 0;
		for(GameResultCount grc: grcList) {
			totalCnt += grc.cnt;
			totalWinCnt += grc.senteWinCnt + grc.goteWinCnt;
			totalLoseCnt += grc.senteLoseCnt + grc.goteLoseCnt;
			totalDrawCnt += grc.senteDrawCnt + grc.goteDrawCnt;
			totalSenteWinCnt += grc.senteWinCnt;
			totalGoteWinCnt += grc.goteWinCnt;
			totalSenteLoseCnt += grc.senteLoseCnt;
			totalGoteLoseCnt += grc.goteLoseCnt;
			totalSenteDrawCnt += grc.senteDrawCnt;
			totalGoteDrawCnt += grc.goteDrawCnt;
		}
		String str = "Total " + String.format("%d games %d Win:%d Lose %d Draw", totalCnt, totalWinCnt, totalLoseCnt, totalDrawCnt);
		Double d = (double)totalWinCnt/(double)(totalWinCnt+totalLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		
		str = String.format("Sente %d games %d Win:%d Lose %d Draw", totalSenteWinCnt+totalSenteLoseCnt+totalSenteDrawCnt, totalSenteWinCnt, totalSenteLoseCnt, totalSenteDrawCnt);
		d = (double)totalSenteWinCnt/(double)(totalSenteWinCnt+totalSenteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		str = String.format("Gote %d games %d Win:%d Lose: %d Draw", totalGoteWinCnt+totalGoteLoseCnt+totalGoteDrawCnt, totalGoteWinCnt, totalGoteLoseCnt, totalGoteDrawCnt);
		d = (double)totalGoteWinCnt/(double)(totalGoteWinCnt+totalGoteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		listModel[ListBoxType.Info.id].addElement(str);
		
		listModel[ListBoxType.Info.id].addElement("---------");
		str = "Total Strategies: " + grcList.size() + " patterns";
		listModel[ListBoxType.Info.id].addElement(str);
		for(GameResultCount grc: grcList) {
			str = grc.str;
			d = (double)(grc.senteWinCnt+grc.goteWinCnt)/(double)(grc.cnt)*100;
			str += ":" + String.format("%d", grc.cnt) + " games";
			str += "(Winning Rate" + String.format("%.0f", d) + "%)";
			listModel[ListBoxType.Info.id].addElement(str);
		}
		
		listModel[ListBoxType.Info.id].addElement("---------");
		for(StringCount sc: strList) {
			str = sc.str;
			str += ":" + String.format("%d", sc.cnt) + " games";
			listModel[ListBoxType.Info.id].addElement(str);
		}
		
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	public void countStrategyDataByPlayerData(List<GameResultCount> grcList, PlayerData pd) {
		for(GameResult gr: pd.grList) {
			Boolean found = false;
			for(GameResultCount grc: grcList) {
				if(grc.str.equals(gr.strategy)) {
					found = true;
					grc.cnt++;
					if(gr.isPlayerWin == 1) {
						if(gr.isSente) grc.senteWinCnt++;
						else grc.goteWinCnt++;
					} else if(gr.isPlayerWin == 0) {
						if(gr.isSente) grc.senteLoseCnt++;
						else grc.goteLoseCnt++;
					} else {
						if(gr.isSente) grc.senteDrawCnt++;
						else grc.goteDrawCnt++;
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
	}
	public void countCastleDataByPlayerData(List<StringCount> strList, PlayerData pd) {
		for(GameResult gr: pd.grList) {
			Boolean found = false;
			for(StringCount sc: strList) {
				if(sc.str.equals(gr.castle)) {
					found = true;
					sc.cnt++;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(gr.castle, 1);
				strList.add(sc);
			}
		}
		Collections.sort(
				strList,
				new Comparator<StringCount>() {
					@Override
					public int compare(StringCount obj1, StringCount obj2) {
						return obj2.cnt - obj1.cnt;
					}
				}
				);
	}
	public void updatePlayerIcon() {
		String playerName[] = new String[2];
		playerName[SenteGote.Sente.id] = new String(textBox[TextBoxType.Player1.id].getText());
		playerName[SenteGote.Gote.id] = new String(textBox[TextBoxType.Player2.id].getText());
		
		for(SenteGote sg: SenteGote.values()) {
			playerIconLabel[sg.id].setIcon(null);
			ImageIcon playerIcon = new ImageIcon(imgFilePath + playerName[sg.id] + ".jpg");
			Image image = playerIcon.getImage();
			Image newImage = image.getScaledInstance(100, 133, java.awt.Image.SCALE_SMOOTH);
			playerIcon = new ImageIcon(newImage);
			playerIconLabel[sg.id].setIcon(playerIcon);
		}
	}
	public void initializePlayerIcon() {
		for(SenteGote sg: SenteGote.values()) playerIconLabel[sg.id].setIcon(null);
	}
	
	private ActionListener enterActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updatePlayerIcon();
        }
    };
    
    private ActionListener checkActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	for(Koma k: shogiData.k) {
        		k.reverse();
        	}
        	shogiData.viewKomaOnBoard();
        	reverseNumberRowCol();
        }
    };

	// -------------------------------------------------------------------------
	// ----------------------- << ListBox Action >> -----------------------------
	// -------------------------------------------------------------------------
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				commonListAction();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				updateListBoxInfoByTesuji();
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		//System.out.println("Key Typed");
	}
	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("Key Released");
		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				commonListAction();
				soundKoma();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				updateListBoxInfoByTesuji();
			}
		}
	}
	public void clearListBox() {
		listModel[ListBoxType.Kifu.id].clear();
		listModel[ListBoxType.Kifu.id].addElement("--------");
		listBox[ListBoxType.Kifu.id].setModel(listModel[ListBoxType.Kifu.id]);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
	}
	public void updateListBox(KomaType type, int x, int y, int sente, int promoted, int preP, int drop) {
		// remove items under selected item 
		int selectedIndex = listBox[ListBoxType.Kifu.id].getSelectedIndex();
		if(selectedIndex != -1 && selectedIndex <= listModel[ListBoxType.Kifu.id].size()-1) {
			int index = listModel[ListBoxType.Kifu.id].size()-1;
			while(index > selectedIndex) {
				listModel[ListBoxType.Kifu.id].remove(index);
				kifuData.remove(index-1);
				index--;
			}
		}
		
		// add new item
		String s = createMoveKomaName(type, sente, x, y, promoted, preP, drop);
		s = listModel[ListBoxType.Kifu.id].size() + ":"+s;
		listModel[ListBoxType.Kifu.id].addElement(s);
		listBox[ListBoxType.Kifu.id].setModel(listModel[ListBoxType.Kifu.id]);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(listModel[ListBoxType.Kifu.id].size()-1);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(listModel[ListBoxType.Kifu.id].size()-1);
		
		cv.clearDrawPointForRightClick();
		shogiData.clearAllKomaDrawList();
	}
	public void commonListAction() {
		int selectedIndex = listBox[ListBoxType.Kifu.id].getSelectedIndex();
		//System.out.println("list selected:" + selectedIndex);
		shogiData.resetAllKoma();
		shogiData.viewKomaOnBoard();
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(shogiData, kf.x, kf.y, kf.p);
				shogiData.turnIsSente = !shogiData.turnIsSente;
				cv.setLastPoint(kf.x, kf.y, true);
				if(textBox[TextBoxType.Strategy.id].getText().equals("")) textBox[TextBoxType.Strategy.id].setText(checkStrategy(shogiData));
				if(textBox[TextBoxType.Castle.id].getText().equals("")) {
					textBox[TextBoxType.Castle.id].setText(checkCastle(shogiData, true));
				}
				if(textBox[TextBoxType.Castle.id].getText().equals("")) {
					textBox[TextBoxType.Castle.id].setText(checkCastle(shogiData, false));
				}
			}
		}
		if(selectedIndex == 0) cv.setLastPoint(-1, -1, false);
		cv.clearDrawPointForRightClick();
		shogiData.clearAllKomaDrawList();
		
		checkKDB(selectedIndex);
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();

		sendCommandToEngine();
	}
	public void updateListBox2(List<StringCount> listSC) {
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
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
			if(checkBox[CheckBoxType.Reverse.id].isSelected() && sc.base.x <= 0) {
				sc.base.x = 10 - sc.base.x;
				sc.base.y = 10 - sc.base.y;
			} 
			cv.addDrawPoint(sc.target,  sc.base);
			listModel[ListBoxType.Info.id].addElement(str);
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Menu Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForMenuColor() {
		Color color = JColorChooser.showDialog(this, "Select color", Color.white);
		if(color != null) {
			boardColor = color;
			shogiData.changeKomaColor(boardColor);
			cv.repaint();
		}
	}
	public void actionForCaptureBoard() {
		try {
			Rectangle bounds = this.getBounds();
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(bounds);
			image = image.getSubimage(20, 70, (shogiData.iconWidth+10)*9, (shogiData.iconHeight+10)*9);
			String dirName = "./img/";
			String fileName = "CaptureBoard.jpg";
			ImageIO.write(image, "jpg", new File(dirName, fileName));
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void actionForKomaInHand() {
		shogiData.putAllKomaInHand();
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
	}

	public void actionForStartEngine() {
		Process process = createEngine();
        if(process == null) {
        	System.out.println("create engine failed");
        	return;
        }
        createReceiverThread(process);
        sendInitialCommandToEngine(process);
        sendCommandToEngine();
	}
	public void actionForStopEngine() {
		sendFinalCommandToEngine();
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Interface for Engine >> ----------------------
	// -------------------------------------------------------------------------
	PrintStream out = null;
	MyThreadReceiver receiver;
	String enginePath = "../../../Shogi Ramen/Suisho5/source/source/YaneuraOu-by-clang";
	int numOfMultiPV = 3;
	public Process createEngine() {
		ProcessBuilder p = new ProcessBuilder(enginePath);
		Process process = null;
		try {
			process = p.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return process;
	}
	public void createReceiverThread(Process process) {
		receiver = new MyThreadReceiver(process);
        receiver.start();
	}
	public void sendInitialCommandToEngine(Process process) {
		out = new PrintStream(process.getOutputStream());
		out.println("usi");
		out.flush();
		out.println("setoption name MultiPV value" + " " + numOfMultiPV);
		out.flush();
		out.println("isready");
		out.flush();
		out.println("usinewgame");
		out.flush();
	}
	public void sendFinalCommandToEngine() {
		if(out == null) return;
		out.println("quit");
		out.flush();
		out = null;
		cv.repaint();
	}
	
	public void sendCommandToEngine() {
		if(out == null) return;
		out.println("stop");
		out.flush();
		out.println(createCommandForEngine());
		out.flush();
		out.println("go infinite");
		out.flush();
	}
	
	public String createCommandForEngine() {
		String cmd = "position sfen ";
		String str[] = new String[9];
		int empty;
		for(int y=0; y<9; y++) {
			empty = 0;
			str[y] = "";
			for(int x=8; x>=0; x--) {
				Koma k = getKomaByPosition(x+1, y+1);
				if(k == null) {
					empty++;
					if(x == 0) str[y] += empty;
					continue;
				}
				if(empty != 0) {
					str[y] += empty;
					empty = 0;
				}
				String s = k.type.name().substring(0, 1);
				if(k.type.name().equals("Knight")) s = "N";
				if(k.type.name().equals("Rance")) s = "L";
				if(k.sente == 1) s = s.toLowerCase();
				str[y] += s;
				if(k.promoted == 1) str[y] += '+';
			}
			if(y != 8) str[y] += '/';
		}
		for(int i=0; i<9; i++) {
			cmd += str[i];
		}
		if(shogiData.turnIsSente) cmd += " b ";
		else cmd += " w ";
		cmd += getStringOfKomaInHand();
		cmd += " 1";
		System.out.println(cmd);
		return cmd;
	}
	public String getStringOfKomaInHand() {
		String str = "";
		str += createStringOfKomaInHand(shogiData.listKomaOnHandForSente, true);
		str += createStringOfKomaInHand(shogiData.listKomaOnHandForGote, false);
		if(str.equals("")) str = "-";
		return str;
	}
	
	public String createStringOfKomaInHand(List<Koma> listKomaOnHand, Boolean isSente) {
		String str = "";
		List<StringCount> listSC = new ArrayList<StringCount>();
		Boolean found;
		for(Koma k: listKomaOnHand) {
			found = false;
			for(StringCount sc: listSC) {
				if(sc.str.equals(k.type.name())) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(k.type.name(), 0);
				listSC.add(sc);
			}
		}
		// order is defined as follows
		str += addStrByKomaName(listSC, "Rook", isSente);
		str += addStrByKomaName(listSC, "Bishop", isSente);
		str += addStrByKomaName(listSC, "Gold", isSente);
		str += addStrByKomaName(listSC, "Silver", isSente);
		str += addStrByKomaName(listSC, "Knight", isSente);
		str += addStrByKomaName(listSC, "Rance", isSente);
		str += addStrByKomaName(listSC, "Pawn", isSente);
		return str;
	}
	
	public String addStrByKomaName(List<StringCount> listSC, String name, Boolean isTurnSente) {
		String str = "";
		for(StringCount sc: listSC) {
			if(!sc.str.equals(name)) continue;
			String s = sc.str.substring(0, 1);
			if(sc.str.equals("Knight")) s = "N";
			if(sc.str.equals("Rance")) s = "L";
			if(!isTurnSente) s = s.toLowerCase();
			if(sc.cnt == 1) {
				str += s;
			} else {
				str += sc.cnt + s;
			}
		}
		return str;
	}
	
	public Koma getKomaByPosition(int x, int y) {
		Koma koma = null;
		for(Koma k: shogiData.k) {
			if(k.px == x && k.py == y) koma = k;
		}
		return koma;
	}
	
	class MyThreadReceiver extends Thread {
		Process process;
		MyThreadReceiver(Process p) {
			process = p;
		}
		@Override
		public void run() {
			super.run();
			System.out.println("thread started");
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
				String line;
				while ((line = r.readLine()) != null) {
					if(line.contains("info depth")) {
						//System.out.println(line);
						getPointFromInfo(line);
						cv.repaint();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("thread stopped");
		}
	}
	public void getPointFromInfo(String info) {
		Point base = new Point();
		Point target = new Point();
		if(info.contains("multipv 1")) cv.clearDrawListForEngine();
		
		String[] names = info.split(" ");
		String str = "";
		for(int i=0; i<names.length; i++) {
			if(names[i].equals("pv")) str = names[i+1];
		}
		convertStringPosToPoints(str, base, target);
		// for safe access in multi thread
		synchronized(cv.drawListEngineBase) {
			cv.drawListEngineBase.add(base);
			cv.drawListEngineTarget.add(target);
		}
	}
	public void convertStringPosToPoints(String strPos, Point base, Point target) {
		char ch[] = strPos.toCharArray();
		// drop a piece
		if(ch[1] == '*') {
			if(ch[0] == 'P' || ch[0] == 'p' || ch[0] == 'G' || ch[0] == 'g') {
				base.x = 0;
			} else if(ch[0] == 'B' || ch[0] == 'b' || ch[0] == 'L' || ch[0] == 'l') {
				base.x = -1;
			} else if(ch[0] == 'N' || ch[0] == 'n' || ch[0] == 'R' || ch[0] == 'r') {
				base.x = -2;
			} else if(ch[0] == 'S' || ch[0] == 's' || ch[0] == 'K' || ch[0] == 'k') {
				base.x = -3;
			}
			if(shogiData.turnIsSente) {
				if(ch[0] == 'P' || ch[0] == 'L' || ch[0] == 'N' || ch[0] == 'S') base.y = 7;
				else base.y = 8;
			} else {
				if(ch[0] == 'P' || ch[0] == 'L' || ch[0] == 'N' || ch[0] == 'S') base.y = 3;
				else base.y = 2;
			}
		}
		else {
			base.x = ch[0] - '0';
			base.y = ch[1] - 96;
		}
		target.x = ch[2] - '0';
		target.y = ch[3] - 96;
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Mouse Action >> -----------------------------
	// -------------------------------------------------------------------------
	Point mousePointDifference = new Point();
	Point mousePointBuf = new Point();
	// 6, @Overrideアノテーションを付ける。
	public void mouseDragged(MouseEvent e) {
		//System.out.println("mouse dragged");
		if(e.getButton() == MouseEvent.BUTTON3) {
			Point mp = e.getPoint();
			if(e.getSource() != cv) mp.y -= 50;
			if(cv.drawListTargetRightClick.size() != 0 ) {
				cv.drawListTargetRightClick.remove(cv.drawListTargetRightClick.size()-1);
			}
			cv.drawListTargetRightClick.add(mp);
			cv.repaint();
		}
		
		if(shogiData.selectedKoma == null) return;
		
		Point mp = e.getPoint();
		//System.out.println(mp);
		if(mp.x < 550) {
			shogiData.selectedKoma.setOpaque(true);
		} else {
			shogiData.selectedKoma.setOpaque(false);
		}
		
		for(ListBoxType lb: ListBoxType.values()) {
			if(e.getSource() == listBox[lb.id]) {
				mp.x += scrollPane[lb.id].getBounds().x;
				mp.y += scrollPane[lb.id].getBounds().y + shogiData.iconHeight/2 + 20;
				break;
			}
		}
		if(e.getSource() != cv) mp.y -= 50;
		shogiData.selectedKoma.setLocation(mp.x - shogiData.iconWidth/2, mp.y - shogiData.iconHeight/2);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			//System.out.println("mouse double clicked");
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				if(!textBox[TextBoxType.LoadFile.id].getText().equals("")) {
					actionForLoad();
				}
			}
			if(e.getButton() == MouseEvent.BUTTON1) {
				//System.out.println("Left Double Clicked");
				Koma k = searchKoma(e);
				if(k != null) {
					if(shogiData.listKomaOnBoard.indexOf(k) != -1) k.reverse();
				}
			}
		}
		if(e.getButton() == MouseEvent.BUTTON1) {
			//System.out.println("Left Clicked");
			if(e.getSource() != cv) return;
			Point mp = e.getPoint();
			cv.drawListLeftClick.add(mp);
			cv.repaint();
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3) { // right click
			//System.out.println("mouse clicked");
			Point mp = e.getPoint();
			if(e.getSource() != cv) mp.y -= 50;
			cv.drawListBaseRightClick.add(mp);
			return;
		}
		if(e.getSource() != this && e.getSource() != cv) {
			return;
		}
		if(shogiData.selectedKoma == null) {
			selectKoma(e);
		} 
	}
	public void selectKoma(MouseEvent e) {
		shogiData.selectedKoma = null;
		Koma selectedKoma = searchKoma(e);
		if(selectedKoma == null) return;
		Boolean isOnBoard = false;
		if(shogiData.listKomaOnBoard.indexOf(selectedKoma) != -1) isOnBoard = true;
		commonMousePressed(selectedKoma, isOnBoard);
	}
	public Koma searchKoma(MouseEvent e) {
		Point mp = e.getPoint();
		Point tp = new Point(mp.x, mp.y);
		if(e.getSource() != cv) {
			tp.y -= 50;
		}
		for(Koma k: shogiData.listKomaOnBoard) {
			Point lp = k.getLocation();
			if(tp.x > lp.x-5 && tp.x < lp.x+shogiData.iconWidth+5 && tp.y > lp.y && tp.y < lp.y+shogiData.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: shogiData.listKomaOnHand.get(SenteGote.Sente.id)) {
			Point lp = k.getLocation();
			if(tp.x > lp.x && tp.x < lp.x+shogiData.iconWidth+10 && tp.y > lp.y && tp.y < lp.y+shogiData.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: shogiData.listKomaOnHand.get(SenteGote.Gote.id)) {
			Point lp = k.getLocation();
			if(tp.x > lp.x && tp.x < lp.x+shogiData.iconWidth+10 && tp.y > lp.y && tp.y < lp.y+shogiData.iconHeight+5) {
				return k;
			}
		}
		return null;
	}
	
	public void commonMousePressed(Koma k, Boolean isOnBoard) {
		if(shogiData.turnIsSente && k.sente == 1 && !checkBox[CheckBoxType.Edit.id].isSelected()) {
			shogiData.selectedKoma = null;
			return;
		}
		if(!shogiData.turnIsSente && k.sente == 0 && !checkBox[CheckBoxType.Edit.id].isSelected()) {
			shogiData.selectedKoma = null;
			return;
		}
		shogiData.selectedKoma = k;
		if(isOnBoard) cv.mousePressed = true;
		cv.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(shogiData.selectedKoma != null) {
			releaseKoma();
		} 
		//System.out.println("mouse released");
		if(e.getButton() != MouseEvent.BUTTON3) return;
		Point mp = e.getPoint();
		if(e.getSource() != cv) mp.y -= 50;
		cv.drawListTargetRightClick.add(mp);
		cv.repaint();
	}
	
	public void releaseKoma() {
		Koma selectedKoma = shogiData.selectedKoma;
		if(selectedKoma == null) return;
		int x = (selectedKoma.getLocation().x - 20 + shogiData.iconWidth/2) / (shogiData.iconWidth+10);
		int y = (selectedKoma.getLocation().y - 20 + shogiData.iconHeight/2) / (shogiData.iconHeight+10) + 1;
		int preX = selectedKoma.px;
		int preY = selectedKoma.py;
		int preP = selectedKoma.promoted;
		
		int X, Y;
		if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
			X = 10 - (9-x);
			Y = 10 - y;
		} else {
			X = (9-x);
			Y = y;
		}
		
		//System.out.println("moveKoma("+X+","+Y+")");
		Boolean result = selectedKoma.moveKoma(shogiData, X, Y, -1);
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
		if(X != preX || Y != preY) {
			if(X>0 && X<10 && Y>0 && Y<10) {
				cv.setLastPoint(X, Y, true);
				updateListBox(type, X, Y, sente, promoted, preP, drop);
			}
			Kifu kf = new Kifu(selectedKoma, X, Y, promoted, preP, drop);
			kifuData.add(kf);
			checkKDB(listModel[ListBoxType.Kifu.id].size()-1);
			shogiData.turnIsSente = !shogiData.turnIsSente;
			sendCommandToEngine();
		}
		
		// check strategy
		if(textBox[TextBoxType.Strategy.id].getText().equals("")) {
			textBox[TextBoxType.Strategy.id].setText(checkStrategy(shogiData));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(checkCastle(shogiData, true));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(checkCastle(shogiData, false));
		}
		
		shogiData.selectedKoma = null;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				//System.out.println("mouse entered " + button[bt.id].getText());
				button[bt.id].setBackground(buttonFocusedColor);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				//System.out.println("mouse exited " + button[bt.id].getText());
				button[bt.id].setBackground(buttonColor);
			}
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
