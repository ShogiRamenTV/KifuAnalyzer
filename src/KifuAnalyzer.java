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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Properties;
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
	ActionListener, ListSelectionListener, KeyListener {
	// -------------------------------------------------------------------------
	// ----------------------- << Global Variables >> --------------------------
	// -------------------------------------------------------------------------
	String imgFilePath = "./img/";
	
	public enum SenteGote {
		Sente(0), Gote(1);
		private final int id;
		private SenteGote(final int id) {
			this.id = id;
		}
	};
	//JLabel playerIconLabel[] = new JLabel[2];
	//JLabel castleIconLabel = new JLabel();
	
	public enum ButtonType {
		Initialize(0), Save(1), Strategy(2), Castle(3), Tesuji(4), Kifu(5);
		private final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	JButton button[] = new JButton[ButtonType.values().length];
	
	public enum TextBoxType {
		Player1(0), Player2(1), Strategy(2), Tesuji(3), Castle(4);
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
		Kifu(0), Info(1), Strategy(2), Player(3), Castle(4), Tesuji(5), Engine(6);
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
	
	public enum MenuTypeSetting {
		SetBoardColor(0), SetColor(1);
		private final int id;
		private MenuTypeSetting(final int id) {
			this.id = id;
		}
	};
	public enum MenuTypeEngine {
		StartEngine(0), StopEngine(1), SetEngine(2), KifuAnalysis(3);
		private final int id;
		private MenuTypeEngine(final int id) {
			this.id = id;
		}
	};
	public enum MenuTypeUtility {
		CaptureBoard(0), KomaInHand(1);
		private final int id;
		private MenuTypeUtility(final int id) {
			this.id = id;
		}
	};
	JMenuBar menuBar = new JMenuBar();
	JMenu menuSetting = new JMenu("Setting");
	JMenu menuEngine = new JMenu("Engine");
	JMenu menuUtility = new JMenu("Utility");
	JMenuItem menuItemSetting[] = new JMenuItem[MenuTypeSetting.values().length];
	JMenuItem menuItemEngine[] = new JMenuItem[MenuTypeEngine.values().length];
	JMenuItem menuItemUtility[] = new JMenuItem[MenuTypeEngine.values().length];
	
	public enum ColorSetType {
		Default(0), Sakura(1), GreenTea(2), BlueSky(3), Lemon(4);
		private final int id;		
		private ColorSetType(final int id) {
			this.id = id;
		}
	};
	Color buttonColor;
	Color buttonFocusedColor;
	ColorSet listColorSet[] = new ColorSet[ColorSetType.values().length];
	
	public enum PropertyType {
		Engine, Color;
	};
	// -------------------------------------------------------------------------
	// ----------------------- << Main >> --------------------------------------
	// -------------------------------------------------------------------------
	public static void main(String[] args) {
		KifuAnalyzer ka = new KifuAnalyzer();
		ka.setTitle("KifuAnalyzer");
		ka.setLocationRelativeTo(null);
		ka.setVisible(true);
		ka.validate();
	}
	// -------------------------------------------------------------------------
	// ----------------------- << GUI Setting >> -------------------------------
	// -------------------------------------------------------------------------
	KifuAnalyzer() {
		System.out.print("Initializing KifuAnalyzer ... ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		shogiData.initializeIcon();
		setSize(shogiData.iconWidth*25, shogiData.iconHeight*12);
		initializeAppIcon();
		initializeGUISetting();
		contentPaneSetting();
		listenerSetting();		
		System.out.println("Completed.");
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
		initializeListBoxSetting();
		initializeCanvasSetting();
		initializeSoundSetting();
		initializeMenuBar();
		initializeNumberRowCol();
		initializeColorSet();
	}
	public void contentPaneSetting() {
		getContentPane().setLayout(null);
		for(ButtonType b: ButtonType.values()) getContentPane().add(button[b.id]);
		for(TextBoxType t: TextBoxType.values()) getContentPane().add(textBox[t.id]);
		for(ListBoxType lb: ListBoxType.values()) getContentPane().add(scrollPane[lb.id]);
		for(CheckBoxType cb: CheckBoxType.values()) getContentPane().add(checkBox[cb.id]);
		getContentPane().add(radioButtonSente);
		getContentPane().add(radioButtonGote);
		getContentPane().add(comboBox);
		for(SenteGote sg: SenteGote.values()) {
			for(int x=0; x<8; x++) getContentPane().add(shogiData.labelNumOfKoma[sg.id][x]);
		}
		getContentPane().add(cve);
		getContentPane().add(cv);
	}
	public void listenerSetting() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		cv.addMouseListener(this);
		cv.addMouseMotionListener(this);
		cv.addKeyListener(this);
	}
	int baseXPosForItems = 720;
	public void initializeCanvasSetting() {
		cv.setBounds(0, 0, this.getSize().width, this.getSize().height);
		cve.setBounds(baseXPosForItems+165, 590,330, 90);
		cve.setBackground(Color.white);
	}
	public void initializeButtonSetting() {
		for(ButtonType b: ButtonType.values()) {
			button[b.id] = new JButton(b.name());
			button[b.id].addActionListener(this);
			button[b.id].addMouseListener(this);
			button[b.id].setOpaque(true);
		}
		button[ButtonType.Initialize.id].setBounds(baseXPosForItems, 10, 80, 20);
		button[ButtonType.Save.id].setBounds(baseXPosForItems, 30, 80, 20);
		button[ButtonType.Kifu.id].setBounds(baseXPosForItems, 50, 80, 20);
		button[ButtonType.Strategy.id].setBounds(baseXPosForItems+280, 10, 80, 20);
		button[ButtonType.Tesuji.id].setBounds(baseXPosForItems+280, 30, 80, 20);
		button[ButtonType.Castle.id].setBounds(baseXPosForItems+280, 50, 80, 20);
	}
	public void initializeTextBoxSetting() {
		for(TextBoxType t: TextBoxType.values()) {
			textBox[t.id] = new JTextField();
		}
		textBox[TextBoxType.Strategy.id].setBounds(baseXPosForItems+360, 10, 140, 20);
		textBox[TextBoxType.Tesuji.id].setBounds(baseXPosForItems+360, 30, 140, 20);
		textBox[TextBoxType.Castle.id].setBounds(baseXPosForItems+360, 50, 140, 20);
		textBox[TextBoxType.Player1.id].setBounds(baseXPosForItems, 100, 120, 20);
		textBox[TextBoxType.Player1.id].addActionListener(enterActionListener);
		textBox[TextBoxType.Player2.id].setBounds(baseXPosForItems+120, 100, 120, 20);
		textBox[TextBoxType.Player2.id].addActionListener(enterActionListener);
	}
	public void initializeCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id] = new JCheckBox(cb.name());
		}
		checkBox[CheckBoxType.Edit.id].setBounds(baseXPosForItems+140, 35, 60, 10);
		checkBox[CheckBoxType.Reverse.id].setBounds(baseXPosForItems+190, 35, 80, 10);
		checkBox[CheckBoxType.Reverse.id].addActionListener(checkActionListener);
		checkBox[CheckBoxType.Draw.id].setBounds(baseXPosForItems+80, 35, 80, 10);
		radioButtonSente.setBounds(baseXPosForItems+360, 75, 70, 14);
		radioButtonGote.setBounds(baseXPosForItems+420, 75, 70, 14);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonSente);
		buttonGroup.add(radioButtonGote);
		comboBox = new JComboBox<>();
		comboBox.addItem("");
		comboBox.addItem("2023");
		comboBox.addItem("2022");
		comboBox.addItem("all");
		comboBox.setBounds(baseXPosForItems+80, 8, 100, 25);
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
		scrollPane[ListBoxType.Kifu.id].setBounds(baseXPosForItems, 280, 165, 140);
		scrollPane[ListBoxType.Info.id].setBounds(baseXPosForItems, 420, 165, 140);
		scrollPane[ListBoxType.Strategy.id].setBounds(baseXPosForItems+165, 280, 165, 140);
		scrollPane[ListBoxType.Castle.id].setBounds(baseXPosForItems+165, 420, 165, 140);
		scrollPane[ListBoxType.Player.id].setBounds(baseXPosForItems+330, 280, 165, 140);
		scrollPane[ListBoxType.Tesuji.id].setBounds(baseXPosForItems+330, 420, 165, 140);
		scrollPane[ListBoxType.Engine.id].setBounds(baseXPosForItems, 590, 165, 90);
		
		for(int index=0; index<numOfMultiPV; index++) {
			listModel[ListBoxType.Engine.id].addElement("");
		}
	}
	public void clearTextBox() {
		textBox[TextBoxType.Player1.id].setText("");
		textBox[TextBoxType.Player2.id].setText("");
		textBox[TextBoxType.Strategy.id].setText("");
		textBox[TextBoxType.Castle.id].setText("");
		textBox[TextBoxType.Tesuji.id].setText("");
		loadFile = "";
		loadStep = "";
		loadYear = "";
	}
	public void clearIcons() {
		initializePlayerIcon();
		initializeCastleIcon();
	}
	public void initializeMenuBar() {
		for(MenuTypeSetting mt: MenuTypeSetting.values()) {
			menuItemSetting[mt.id] = new JMenuItem(mt.name());
			menuItemSetting[mt.id].addActionListener(this);
			menuSetting.add(menuItemSetting[mt.id]);
		}
		menuBar.add(menuSetting);
		for(MenuTypeEngine mt: MenuTypeEngine.values()) {
			menuItemEngine[mt.id] = new JMenuItem(mt.name());
			menuItemEngine[mt.id].addActionListener(this);
			menuEngine.add(menuItemEngine[mt.id]);
		}
		menuBar.add(menuEngine);
		for(MenuTypeUtility mt: MenuTypeUtility.values()) {
			menuItemUtility[mt.id] = new JMenuItem(mt.name());
			menuItemUtility[mt.id].addActionListener(this);
			menuUtility.add(menuItemUtility[mt.id]);
		}
		menuBar.add(menuUtility);
		
		this.setJMenuBar(menuBar);
	}
	JLabel labelNumberRow[] = new JLabel[9];
	JLabel labelNumberCol[] = new JLabel[9];
	public void initializeNumberRowCol() {
		for(int x=0; x<9; x++) {
			labelNumberRow[x] = new JLabel();
			labelNumberCol[x] = new JLabel();
			labelNumberRow[x].setText(String.valueOf(9-x));
			labelNumberRow[x].setBounds(105 + x * (shogiData.iconWidth+10), 5, 10, 10);
			labelNumberCol[x].setText(String.valueOf(x+1));
			labelNumberCol[x].setBounds(625, 50 + x * (shogiData.iconHeight+10), 10, 10);
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
	public void initializeColorSet() {
		listColorSet[ColorSetType.Default.id] = new ColorSet(
				new Color(245, 245, 245), // backGround;
				new Color(235, 235, 235), // button;
				new Color(215, 215, 215), // buttonFocused;
				new Color(0, 0, 0)		 // buttonBorder;
				);
		listColorSet[ColorSetType.Sakura.id] = new ColorSet(
				new Color(254, 244, 244), 
				new Color(230, 205, 227), 
				new Color(229, 171, 190), 
				new Color(201, 117, 134)
				);
		listColorSet[ColorSetType.GreenTea.id] = new ColorSet(
				new Color(244, 254, 244), 
				new Color(205, 230, 227), 
				new Color(171, 229, 190), 
				new Color(117, 201, 134)
				);
		listColorSet[ColorSetType.BlueSky.id] = new ColorSet(
				new Color(213, 248, 253), 
				new Color(160, 216, 239), 
				new Color(171, 190, 229), 
				new Color(117, 134, 201)
				);
		listColorSet[ColorSetType.Lemon.id] = new ColorSet(
				new Color(255, 250, 205), 
				new Color(255, 255, 0), 
				new Color(255, 215, 0), 
				new Color(255, 140, 0)
				);
		setColor();
	}
	public void setColor() {
		String value = loadProperty(PropertyType.Color.name());
		int select;
		if(value == null) select = ColorSetType.Default.id;
		else select = Integer.parseInt(value);
		
		buttonColor = listColorSet[select].button;
		buttonFocusedColor = listColorSet[select].buttonFocused;
		LineBorder border = new LineBorder(listColorSet[select].buttonBorder, 1, true);
		for(ButtonType bt: ButtonType.values()) {
			button[bt.id].setBackground(buttonColor);
			button[bt.id].setBorder(border);
		}
		this.setBackground(listColorSet[select].backGround);
		this.getContentPane().setBackground(listColorSet[select].backGround);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Shogi Board >> -------------------------------
	// -------------------------------------------------------------------------
	Color boardColor = new Color(255, 238, 203);
	ShogiData shogiData = new ShogiData();
	ShogiData shogiDataForKDB = new ShogiData();
	public class ShogiData {
		String[] komaName = {"歩", "香", "桂", "銀", "金", "角", "飛", "王", "と", "成香", "成桂", "成銀", "金", "馬", "龍", "王"};
		String[] senteGote = {"▲", "△"};
		BufferedImage img[][] = new BufferedImage[10][4];
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
			try {
				img[KomaType.Pawn.id][0] = ImageIO.read(new File("./img/Pawn.png"));
				img[KomaType.Pawn.id][1] = ImageIO.read(new File("./img/Promoted Pawn.png"));
				img[KomaType.Pawn.id][2] = ImageIO.read(new File("./img/Pawn Gote.png"));
				img[KomaType.Pawn.id][3] = ImageIO.read(new File("./img/Promoted Pawn Gote.png"));
				img[KomaType.Rance.id][0] = ImageIO.read(new File("./img/Rance.png"));
				img[KomaType.Rance.id][1] = ImageIO.read(new File("./img/Promoted Rance.png"));
				img[KomaType.Rance.id][2] = ImageIO.read(new File("./img/Rance Gote.png"));
				img[KomaType.Rance.id][3] = ImageIO.read(new File("./img/Promoted Rance Gote.png"));
				img[KomaType.Knight.id][0] = ImageIO.read(new File("./img/Knight.png"));
				img[KomaType.Knight.id][1] = ImageIO.read(new File("./img/Promoted Knight.png"));
				img[KomaType.Knight.id][2] = ImageIO.read(new File("./img/Knight Gote.png"));
				img[KomaType.Knight.id][3] = ImageIO.read(new File("./img/Promoted Knight Gote.png"));
				img[KomaType.Silver.id][0] = ImageIO.read(new File("./img/Silver.png"));
				img[KomaType.Silver.id][1] = ImageIO.read(new File("./img/Promoted Silver.png"));
				img[KomaType.Silver.id][2] = ImageIO.read(new File("./img/Silver Gote.png"));
				img[KomaType.Silver.id][3] = ImageIO.read(new File("./img/Promoted Silver Gote.png"));
				img[KomaType.Gold.id][0] = ImageIO.read(new File("./img/Gold.png"));
				img[KomaType.Gold.id][2] = ImageIO.read(new File("./img/Gold Gote.png"));
				img[KomaType.Rook.id][0] = ImageIO.read(new File("./img/Rook.png"));
				img[KomaType.Rook.id][1] = ImageIO.read(new File("./img/Promoted Rook.png"));
				img[KomaType.Rook.id][2] = ImageIO.read(new File("./img/Rook Gote.png"));
				img[KomaType.Rook.id][3] = ImageIO.read(new File("./img/Promoted Rook Gote.png"));
				img[KomaType.Bishop.id][0] = ImageIO.read(new File("./img/Bishop.png"));
				img[KomaType.Bishop.id][1]= ImageIO.read(new File("./img/Promoted Bishop.png"));
				img[KomaType.Bishop.id][2] = ImageIO.read(new File("./img/Bishop Gote.png"));
				img[KomaType.Bishop.id][3]= ImageIO.read(new File("./img/Promoted Bishop Gote.png"));
				img[KomaType.King.id][0] = ImageIO.read(new File("./img/King.png"));
				img[KomaType.King.id][2] = ImageIO.read(new File("./img/King Gote.png"));
				iconWidth = img[KomaType.Pawn.id][0].getWidth();
				iconHeight = img[KomaType.Pawn.id][0].getHeight();
			} catch(IOException e) {
				System.out.println(e);
				return;
			}
		}
		public void viewKomaOnBoard() {
			for(Koma k: listKomaOnBoard) {
				int X, Y;
				if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
					X = 10 - k.px;
					Y = 10 - k.py;
				} else {
					X = k.px;
					Y = k.py;
				}
				k.pos.x = (9-X)*(iconWidth+10)+85;
				k.pos.y = (Y-1)*(iconHeight+10)+25;
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
			
			for(Koma k: listKomaOnHand.get(SenteGote.Sente.id)) {
				if(!checkBox[CheckBoxType.Reverse.id].isSelected()) {
					k.pos.x = (iconWidth+10)*10+50;
					k.pos.y = (iconHeight+10)*(k.type.id+1)+25;
					labelNumOfKoma[SenteGote.Sente.id][k.type.id].setLocation((iconWidth+10)*11+35, (iconHeight+10)*(k.type.id+1)+30);
				} else {
					k.pos.x = 10;
					k.pos.y = (iconHeight+10)*(7-k.type.id)+25;
					labelNumOfKoma[SenteGote.Sente.id][k.type.id].setLocation(55, (iconHeight+10)*(7-k.type.id)+70);
				}
				numOfKoma[SenteGote.Sente.id][k.type.id]++;
				labelNumOfKoma[SenteGote.Sente.id][k.type.id].setText(Integer.valueOf(numOfKoma[SenteGote.Sente.id][k.type.id]).toString());
				labelNumOfKoma[SenteGote.Sente.id][k.type.id].setVisible(true);
			}
			
			for(Koma k: listKomaOnHand.get(SenteGote.Gote.id)) {
				if(!checkBox[CheckBoxType.Reverse.id].isSelected()) {
					k.pos.x = 10;
					k.pos.y = (iconHeight+10)*(7-k.type.id)+25;
					labelNumOfKoma[SenteGote.Gote.id][k.type.id].setLocation(55, (iconHeight+10)*(7-k.type.id)+70);
				} else {
					k.pos.x = (iconWidth+10)*10+50;
					k.pos.y = (iconHeight+10)*(k.type.id+1)+25;
					labelNumOfKoma[SenteGote.Gote.id][k.type.id].setLocation((iconWidth+10)*11+35, (iconHeight+10)*(k.type.id+1)+30);
				}
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
					k[x].reverseForReverseMode();
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
		public Koma findKoma(int x, int y) {
			for(Koma k: listKomaOnBoard) {
				if(k.px == x && k.py == y) return k;
			}
			return null;
		}
		public Koma findKomaInHand(KomaType type, Boolean isSente) {
			List<Koma> listKomaOnHand;
			if(isSente) {
				listKomaOnHand = listKomaOnHandForSente;
			} else {
				listKomaOnHand = listKomaOnHandForGote;
			}
			for(Koma k: listKomaOnHand) {
				if(k.type == type) return k;
			}
			
			return null;
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
				k[x].px = 0;
				k[x].py = k[x].type.id+2;
				if(radioButtonSente.isSelected()) {
					listKomaOnHandForSente.add(k[x]);
				} else {
					k[x].reverse();
					listKomaOnHandForGote.add(k[x]);
				}
			}
			for(int x=20; x<40; x++) {
				k[x].px = 10;
				k[x].py = 8-k[x].type.id;
				if(radioButtonSente.isSelected()) {
					k[x].reverse();
					listKomaOnHandForSente.add(k[x]);
				} else {
					listKomaOnHandForGote.add(k[x]);
				}
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
	public class Koma {
		Point pos;
		BufferedImage imgKoma;
		int px;
		int py;
		int promoted;
		int sente;
		int index;
		int drop;
		Boolean forward;
		List<Point> drawListBase;
		List<Point> drawListTarget;
		KomaType type;
		Koma(KomaType t, int x, int y, int s, int i) {
			pos = new Point();
			pos.x = (shogiData.iconWidth+10)*(9-x)+25;
			pos.y = (shogiData.iconHeight+10)*(y-1)+25;
			imgKoma = shogiData.img[t.id][0+s*2];
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
		public void clearDrawList() {
			drawListBase.clear();
			drawListTarget.clear();
		}
		public void reset(KomaType t, int x, int y, int s) {
			type = t;
			px = x;
			py = y;
			if(s == 0) forward = true;
			else forward = false;
			sente = s;
			promoted = 0;
			imgKoma = shogiData.img[type.id][sente*2 + promoted];
		}
		public void promote() {
			if(type == KomaType.Gold || type == KomaType.King) return;
			if(promoted == 0) {
				if(forward && sente == 0) {
					imgKoma = shogiData.img[type.id][0+1];
				} else if(forward && sente == 1) {
					imgKoma = shogiData.img[type.id][0+1];
				} else if(!forward && sente == 0) {
					imgKoma = shogiData.img[type.id][2+1];
				} else {
					imgKoma = shogiData.img[type.id][2+1];
				}
				promoted = 1;
			} else {
				imgKoma = shogiData.img[type.id][sente*2];
				promoted = 0;
			}
		}
		public void reverse() {
			if(sente == 0) {
				sente = 1;
			} else {
				sente = 0;
			}
			if(forward && sente == 0) {
				imgKoma = shogiData.img[type.id][2 + promoted];
			} else if(forward && sente == 1) {
				imgKoma = shogiData.img[type.id][2 + promoted];
			} else if(!forward && sente == 0) {
				imgKoma = shogiData.img[type.id][0 + promoted];
			} else {
				imgKoma = shogiData.img[type.id][0 + promoted];
			}
			forward = !forward;
		}
		public void reverseForReverseMode() {
			if(forward) {
				imgKoma = shogiData.img[type.id][2 + promoted];
				forward = false;
			} else {
				imgKoma = shogiData.img[type.id][0 + promoted];
				forward = true;
			}
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
				if(result == 0) { // yes
					this.promote();
					return true;
				} else if(result == 1) { // no
					return true;
				} else { // cancel
					return false;
				}
			}
			
			return true;
		}
		public Boolean moveKoma(ShogiData sd, int x, int y, int promoted) {
			int preX = this.px;
			int preY = this.py;
			
			if(x == preX && y == preY) {
				//this.setLocation((9-preX)*(shogiData.iconWidth+10)+85, (preY-1)*(shogiData.iconHeight+10)+25);
				pos.x = (9-preX)*(shogiData.iconWidth+10)+85;
				pos.y = (preY-1)*(shogiData.iconHeight+10)+25;
				return false;
			}
			
			if(promoted == -1) {
				if(!this.confirmPromotion(x, y, preX, preY, sd)) {
					// case of cancel
					pos.x = (9-preX)*(shogiData.iconWidth+10)+85;
					pos.y = (preY-1)*(shogiData.iconHeight+10)+25;
					return false;
				}
			}
			else if(promoted == 1 && this.promoted == 0) this.promote(); // no confirm when kifuData
			
			this.px = x;
			this.py = y;
			this.drop = 0;
			pos.x = (9-x)*(shogiData.iconWidth+10)+85;
			pos.y = (y-1)*(shogiData.iconHeight+10)+25;
			
			if(x>0 && x<10 && y>0 && y<10) {
				if( sd.listKomaOnHand.get(SenteGote.Sente.id).indexOf(this) != -1 ) moveFromStoB(sd);
				else if( sd.listKomaOnHand.get(SenteGote.Gote.id).indexOf(this) != -1) moveFromGtoB(sd);
				else moveFromBtoB(sd);
			} else if(x<=0) {
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
					k.px = 0;
					k.py = k.type.id+2;
					sd.listKomaOnHand.get(SenteGote.Sente.id).add(k);
				} else {
					if(k.sente == 0) k.reverse();
					k.px = 10;
					k.py = 8-k.type.id;
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
	// ----------------------- << Sound >> -------------------------------------
	// -------------------------------------------------------------------------
	String soundFilePath = "./sound/Koma Oto.wav";
	Clip soundKoma;
	public void initializeSoundSetting() {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundFilePath));
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
		List<PointWithScore> drawListForEngine = new ArrayList<PointWithScore>();
		Image imgBoard;
		Image imgBackground;
		Image playerIcon[];
		Image castleIcon;
		public CanvasBoard() {
			lastPointX = -1;
			lastPointY = -1;
			mousePressed = false;
			enableLastPoint = false;
			try {
				BufferedImage boardImage = ImageIO.read(new File("./img/shogi board.png"));
				imgBoard = boardImage.getScaledInstance((50+10)*9, (63+10)*9, java.awt.Image.SCALE_SMOOTH);
				boardImage = ImageIO.read(new File("./img/background.png"));
				imgBackground = boardImage.getScaledInstance(50*25, 63*12, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				imgBoard = null;
				imgBackground = null;
			}
			playerIcon = new Image[2];
			castleIcon = null;
		}
		public void paint(Graphics g) {
			//drawBackground(g);
			//drawShogiBoardBackground(g);
			drawShogiBoard(g);
			drawStrings(g);
			drawIcons(g);
			drawLastPoint(g);
			drawMovableArea(g);
			drawShogiKoma(g);
			drawLeftClickedPoints(g);
			drawArrowsForKifuAnalysis(g);
			drawArrowsForRightClick(g);
			drawArrowForEngine(g);
		}
		public void drawBackground(Graphics g) {
			if(imgBackground != null) g.drawImage(imgBackground, 0, 0, this);
		}
		public void drawShogiBoard(Graphics g) {
			g.setColor(boardColor);
			g.fillRect(80, 20, (shogiData.iconWidth+10)*9, (shogiData.iconHeight+10)*9);
			g.setColor(Color.black);
			for(int x=0; x<9; x++)
				for(int y=0; y<9; y++) {
					g.drawRect(x*(shogiData.iconWidth+10)+80, y*(shogiData.iconHeight+10)+20, shogiData.iconWidth+10, shogiData.iconHeight+10);
				}
		}
		public void drawShogiBoardBackground(Graphics g) {
			if(imgBoard != null) g.drawImage(imgBoard, 80, 20, this);
		}
		public void drawShogiKoma(Graphics g) {
			for(Koma k: shogiData.k) {
				g.drawImage(k.imgKoma, k.pos.x, k.pos.y, this);
			}
		}
		public void drawIcons(Graphics g) {
			if(playerIcon[SenteGote.Sente.id] != null) g.drawImage(playerIcon[SenteGote.Sente.id], baseXPosForItems, 130, this);
			if(playerIcon[SenteGote.Gote.id] != null) g.drawImage(playerIcon[SenteGote.Gote.id], baseXPosForItems+130, 130, this);
			if(castleIcon != null) g.drawImage(castleIcon, baseXPosForItems+300, 110, this);
		}
		public void drawStrings(Graphics g) {
			g.drawString("4000", baseXPosForItems+165, 585);
			g.drawString("-4000", baseXPosForItems+165, 695);
			g.drawString("Top 5 Best Moves", baseXPosForItems, 585);
			for(int x=0; x<9; x++) {
				g.drawString(labelNumberRow[x].getText(), labelNumberRow[x].getBounds().x, labelNumberRow[x].getBounds().y+10);
				g.drawString(labelNumberCol[x].getText(), labelNumberCol[x].getBounds().x, labelNumberCol[x].getBounds().y+10);
			}
			g.drawString("▲Sente", baseXPosForItems, 95);
			g.drawString("△Gote", baseXPosForItems+120, 95);
			// emphasize sente gote
			if(shogiData.turnIsSente) {
				g.drawRect(baseXPosForItems, 80, 100, 18);
			} else {
				g.drawRect(baseXPosForItems+120, 80, 100, 18);
			}
		}
		public void drawMovableArea(Graphics g) {
			if(mousePressed) {
				for(int x=1; x<10; x++) for(int y=1; y<10; y++) {
					if(!checkBox[CheckBoxType.Reverse.id].isSelected()) {
						if(shogiData.selectedKoma.isMovable(x, y)) cv.drawPoint(x, y, Color.pink);
					} else {
						if(shogiData.selectedKoma.isMovable(10-x, 10-y)) cv.drawPoint(x, y, Color.pink);
					}
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
			int index = 0;
			float bs = 10.0f;
			int red = 0;
			int green = 255;
			int blue = 255;
			if(isEngineOn) return;
			for(Point p: drawList) {
				Point pB = drawListBase.get(index);
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
				
				Point pBase = new Point((9-pBX)*(shogiData.iconWidth+10)+85+shogiData.iconWidth/2, (pBY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
				Point pTarget = new Point((9-pX)*(shogiData.iconWidth+10)+85+shogiData.iconWidth/2, (pY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
				Arrow ar = new Arrow(pBase, pTarget);
				BasicStroke stroke = new BasicStroke(bs);
				g.setColor(new Color(red, green, blue));
				ar.draw((Graphics2D)g, stroke);
				index++;
				bs -= 2.0f;
				if(bs < 1.0f) bs = 1.0f;
				blue -= 40;
				green -= 40;
				if(blue < 40) blue = 40;
				if(green < 40) green = 40;
			}
		}
		public void drawArrowForEngine(Graphics g) {
			if(out == null) return;
			float bs = 10.0f;
			Color c;
			int red = 0;
			int green = 250;
			int blue = 0;
			int count = 0;
			// for safe access in multi thread
			synchronized(drawListForEngine) {
				for(PointWithScore ps: drawListForEngine) {
					if(count == 0) {
						bestPointFromEngine.score = ps.score;
					}
					int pBX, pBY, pX, pY;
					if(checkBox[CheckBoxType.Reverse.id].isSelected()) {
						pBX = 10 - ps.base.x;
						pBY = 10 - ps.base.y;
						pX = 10 - ps.target.x;
						pY = 10 - ps.target.y;
					} else {
						pBX = ps.base.x;
						pBY = ps.base.y;
						pX = ps.target.x;
						pY = ps.target.y;
					}
					Point pBase = new Point((9-pBX)*(shogiData.iconWidth+10)+85+shogiData.iconWidth/2, (pBY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
					Point pTarget = new Point((9-pX)*(shogiData.iconWidth+10)+85+shogiData.iconWidth/2, (pY-1)*(shogiData.iconHeight+10)+25+shogiData.iconHeight/2);
					Arrow ar = new Arrow(pBase, pTarget);
					BasicStroke stroke = new BasicStroke(bs);
					c = new Color(red, green, blue);
					g.setColor(c);
					ar.draw((Graphics2D)g, stroke);
					bs -= 2.0f;
					green -= 80;
					count++;
					
					if(count == 3) break; // only top 3 are shown 
				}
			}
		}
		public void clearDrawListForEngine() {
			// for safe access in multi thread
			synchronized(drawListForEngine) {
				drawListForEngine.clear();
			}
		}
		public void drawArrowsForRightClick(Graphics g) {
			for(Point pTarget: drawListTargetRightClick) {
				Point pBase = drawListBaseRightClick.get(drawListTargetRightClick.indexOf(pTarget));
				Point pBaseConverted = convertMousePointToCentralSquare(pBase);
				Point pTargetConverted = convertMousePointToCentralSquare(pTarget);
				if(pBaseConverted.x == pTargetConverted.x && pBaseConverted.y == pTargetConverted.y) continue;
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
			return pCalculated;
		}
		public void drawPoint(int x, int y, Color cl) {
			Graphics g = getGraphics();
			BasicStroke stroke = new BasicStroke(4.0f);
			Graphics2D g2 = (Graphics2D)g;
			g2.setStroke(stroke);
			g2.setColor(cl);
			g2.drawRect((9-x)*(shogiData.iconWidth+10)+82, (y-1)*(shogiData.iconHeight+10)+22, shogiData.iconWidth+6, shogiData.iconHeight+6);
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
	public class PointWithScore {
		Point base;
		Point target;
		int score;
		public PointWithScore(Point b, Point t, int s) {
			base = b;
			target = t;
			score = s;
		}
	}
	
	public static final int maxSizeOfKifu = 200;
	public static final int maxScoreOfEngine = 4000;
	public static final int sizeOfOval = 6;
	BestPointData bestPointFromEngine = new BestPointData();
	List<BestPointData> bestPointData = new ArrayList<BestPointData>();
	CanvasForEngine cve = new CanvasForEngine();
	public class BestPointData {
		int score;
		String[] moveName = new String[numOfMultiPV];
		BestPointData() {
			for(int index=0; index<numOfMultiPV; index++) moveName[index] = new String("");
		}
	}
	public class CanvasForEngine extends Canvas {
		public CanvasForEngine() {
			for(int index=0; index<maxSizeOfKifu; index++) {
				BestPointData bpt = new BestPointData();
				bestPointData.add(bpt);
			}
		}
		public void paint(Graphics g) {
			drawBaseField(g);
			getPointFromEngine();
			drawPointFromEngine(g);
			drawPointOfCurrentPosition(g);
		}
		private void drawBaseField(Graphics g) {
			g.setColor(Color.black);
			g.drawLine(0, this.getHeight()/2, this.getWidth(), this.getHeight()/2);
			g.drawRect(0, 0, this.getWidth(), this.getHeight());
		}
		private void getPointFromEngine() {
			if(!isEngineOn) return;
			int index = listBox[ListBoxType.Kifu.id].getSelectedIndex();
			if(bestPointFromEngine.score == 0) return;
			BestPointData bpd = bestPointData.get(index);
			bpd.score = bestPointFromEngine.score;
			for(index=0; index<numOfMultiPV; index++) {
				bpd.moveName[index] = bestPointFromEngine.moveName[index];
			}
		}
		private void drawPointFromEngine(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			BasicStroke stroke = new BasicStroke(2.0f);
			g2.setStroke(stroke);
			g2.setColor(Color.blue);
			for(int index = 0; index<maxSizeOfKifu-1; index++) {
				BestPointData bpd = bestPointData.get(index);
				BestPointData bpd2 = bestPointData.get(index+1);
				if(bpd.score == 0 || bpd2.score == 0) continue;
				if(bpd.score > maxScoreOfEngine) bpd.score = maxScoreOfEngine;
				if(bpd.score < (-1 * maxScoreOfEngine) ) bpd.score = (-1 * maxScoreOfEngine);
				
				int convertedIndex = (int)((double)this.getWidth() * ((double)index/(double)maxSizeOfKifu));
				int convertedIndex2 = (int)((double)this.getWidth() * ((double)(index+1)/(double)maxSizeOfKifu));
				int convertedHeight = (int)((double)this.getHeight()/2 * (double)bpd.score/(double)maxScoreOfEngine);
				if((index%2) == 0) convertedHeight *= -1; 
				convertedHeight += this.getHeight()/2;
				int convertedHeight2 = (int)((double)this.getHeight()/2 * (double)bpd2.score/(double)maxScoreOfEngine);
				if(((index+1)%2) == 0) convertedHeight2 *= -1; 
				convertedHeight2 += this.getHeight()/2;
				g2.drawLine(convertedIndex, convertedHeight, convertedIndex2, convertedHeight2);
			}
		}
		private void drawPointOfCurrentPosition(Graphics g) {
			if(!isEngineOn) {
				int selectedIndex = listBox[ListBoxType.Kifu.id].getSelectedIndex();
				BestPointData bpd = bestPointData.get(selectedIndex);
				updateListBoxEngine(bpd);
				if(bpd.score == 0) return;
				int convertedIndex = (int)((double)this.getWidth() * ((double)selectedIndex/(double)maxSizeOfKifu));
				int convertedHeight = (int)((double)this.getHeight()/2 * (double)bpd.score/(double)maxScoreOfEngine);
				if((selectedIndex%2) == 0) convertedHeight *= -1; 
				convertedHeight += this.getHeight()/2;
				g.setColor(Color.red);
				g.drawOval(convertedIndex - sizeOfOval/2, convertedHeight-sizeOfOval/2, sizeOfOval, sizeOfOval);
				String s = Integer.toString(bpd.score);
				g.drawString(s, 0, 20);
			}
		}
		public void clearBestPointData() {
			for(int index=0; index<maxSizeOfKifu; index++) {
				BestPointData bpd = bestPointData.get(index);
				bpd.score = 0;
				for(int x=0; x<numOfMultiPV; x++) bpd.moveName[x] = "";
			}
		}
		public void updateListBoxEngine(BestPointData bpd) {
			for(int index=0; index<numOfMultiPV; index++) {
				listModel[ListBoxType.Engine.id].set(index, bpd.moveName[index]);
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
		shogiData.resetAllKoma();	
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
		clearListBox();
		clearIcons();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cve.clearBestPointData();
		loadStrategyData();
		loadCastleData();
		loadTesujiData();
		actionForDB();
		countStrategy();
		countCastle();
		countTesujiData();
		createPlayerDataBase();
		cv.repaint();
		cve.repaint();
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
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	String loadFile = "";
	String loadStep = "";
	String loadYear = "";
	public void actionForLoad() {
		loadByNumber(loadFile, loadStep, loadYear);
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
		initializeShogiBoard();
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
					updateListBox(shogiData.k[i].type, x, y, shogiData.k[i].px, shogiData.k[i].py, shogiData.k[i].sente, p, pp, d);
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
			cv.repaint();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void initializeShogiBoard() {
		shogiData.resetAllKoma();	
		shogiData.viewKomaOnBoard();
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cv.clearDrawPoint();
		actionForStopEngine();
		cve.clearBestPointData();
		clearTextBox();
		cv.repaint();
		cve.repaint();
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
			System.out.print("Loading Kifu Data(" + strY + ") ... ");
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
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		commonListAction();
		
		System.out.println("Completed.");
	}
	public void actionForStrategy() {
		Path path;
		String fileName;
		int index = 1;
		
		if(textBox[TextBoxType.Strategy.id].getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Strategy name is empty");
			return;
		}
		
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
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForCastle() {
		Path path;
		String fileName;
		int index = 1;
		
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Castle name is empty");
			return;
		}
		
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
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForTesuji() {
		Path path;
		String fileName;
		int index = 1;
		
		if(textBox[TextBoxType.Tesuji.id].getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Tesuji name is empty");
			return;
		}
		
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
			if(loadFile.equals("")) {
				fw.write(String.format("%03d", kifuDB.size()+1) + "\n");
			}
			else {
				fw.write(loadFile + "\n");
			}
			fw.write(listBox[ListBoxType.Kifu.id].getSelectedIndex() + "\n");
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
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
		if(e.getSource() == menuItemSetting[MenuTypeSetting.SetBoardColor.id]) {
			actionForSetBoardColor();
		}
		if(e.getSource() == menuItemSetting[MenuTypeSetting.SetColor.id]) {
			actionForSetColor();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StartEngine.id]) {
			actionForStartEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StopEngine.id]) {
			actionForStopEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.SetEngine.id]) {
			actionForSetEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.KifuAnalysis.id]) {
			actionForKifuAnalysis();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.CaptureBoard.id]) {
			actionForCaptureBoard();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.KomaInHand.id]) {
			actionForKomaInHand();
		}
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Strategy Data >> -----------------------------
	// -------------------------------------------------------------------------
	String strategyFilePath = "./strategy/";
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
		System.out.print("Loading Strategy Data ... ");
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
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		System.out.println("Completed.");
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
		loadFile = "";
		loadStep = "";
		loadYear = "";
		if(subStrFile.matches("[+-]?\\d*(\\.\\d+)?")) loadFile = subStrFile;
		if(subStrStep.matches("[+-]?\\d*(\\.\\d+)?")) loadStep = subStrStep;
		if(subStrYear.matches("[+-]?\\d*(\\.\\d+)?")) loadYear = subStrYear;
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
		System.out.print("Loading Castle Data ... ");
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
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		System.out.println("Completed.");
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
		try {
			BufferedImage img = ImageIO.read(new File(imgFilePath + castleName + ".jpg"));
			cv.castleIcon = img.getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH);
		} catch(IOException e) {
			cv.castleIcon = null;
		}
		cv.repaint();
	}
	public void initializeCastleIcon() {
		cv.castleIcon = null;
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
		System.out.print("Loading Tesuji Data(" + strY + ") ... ");
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
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		System.out.println("Completed.");
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
	String kifuFilePath = "./kifu";
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
	public String createMoveKomaName(KomaType type, int sente, int x, int y, int preX, int preY, int promoted, int preP, int drop) {
		String s = shogiData.senteGote[sente] + String.valueOf(x)+String.valueOf(y);
		if(preP == 0 && promoted == 1) {
			s += shogiData.komaName[type.id] + "成";
		} else {
			s += shogiData.komaName[type.id+8*preP];
		}
		if(drop == 1) s += "打";
		else {
			s += "(" + preX + preY + ")";
		}
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
		String str = createMoveKomaName(kf.k.type, kf.k.sente, kf.x, kf.y, kf.k.px, kf.k.py, kf.p, kf.pp, kf.d);
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
	public void importShogiWarsKifu() {
		System.out.print("Importing Kifu ... ");
		String strClipBoard;
		if( (strClipBoard = getClipboardData()) == null ) {
			System.out.println("Failed.");
			return;
		}
		initializeShogiBoard();
		Boolean result = parseShogiWarsKifu(strClipBoard);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		commonListAction();
		updatePlayerIcon();
		if(result) System.out.println("Completed.");
		else System.out.println("Failed.");
	}
	public String getClipboardData() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable object = clipboard.getContents(null);
		String strClipBoard;
		try {
			strClipBoard = (String)object.getTransferData(DataFlavor.stringFlavor);
		} catch(UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		return strClipBoard;
	}
	String endPattern[] = {"中断", "投了", "持将棋", "千日手", "切れ負け", "反則勝ち", "反則負け", "入玉勝ち", "詰み"};
	public Boolean parseShogiWarsKifu(String strClipBoard) {
		String content;
		StringTokenizer stLine = new StringTokenizer(strClipBoard, System.lineSeparator());
		Boolean startKifu = false;
		Boolean isSente = true;
		while(stLine.hasMoreTokens()) {
			content = stLine.nextToken();
			//System.out.println(content);
			if(content.contains("先手：")) {
				textBox[TextBoxType.Player1.id].setText(content.substring(content.indexOf("：")+1));
			}
			if(content.contains("後手：")) {
				textBox[TextBoxType.Player2.id].setText(content.substring(content.indexOf("：")+1));
			}
			if(content.contains("手数----指手---------消費時間--")) {
				startKifu = true;
				continue;
			}
			if(!startKifu) continue;
			else {
				for(String endStr: endPattern) {
					if(content.contains(endStr)) return true;
				}
			}
			StringTokenizer st = new StringTokenizer(content," ");
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				if(!token.matches("[+-]?\\d*(\\.\\d+)?")) continue;					
				token = st.nextToken();
				Kifu kf;
				try {
					kf = convertShogiWarsKifu(token, isSente);
				} catch(Exception e) {
					System.out.println("format error");
					return false;
				}
				if(kf == null) {
					JOptionPane.showMessageDialog(null, "Loading kifu failed");
					return false;
				}
				updateListBox(kf.k.type, kf.x, kf.y, kf.k.px, kf.k.py, kf.k.sente, kf.p, kf.pp, kf.d);
				kifuData.add(kf);
				shogiData.k[kf.k.index].moveKoma(shogiData, kf.x, kf.y, kf.p);
				isSente = !isSente;
			}
		}
		return startKifu;
	}
	public Kifu convertShogiWarsKifu(String token, Boolean isSente) {
		Kifu kf = null;
		String kanjiNum = "一二三四五六七八九";
		Koma k = null;
		KomaType type = null;
		int x = 0;
		int y = 0;
		int p = 0;
		int drop = 0;
		int index = 0;
		while(index<token.length()) {
			String s = token.substring(index, index+1);
			if(index == 0) x = Integer.parseInt(s);
			if(index == 1) {
				y = kanjiNum.indexOf(s)+1;
			}
			if(index == 2) {
				for(KomaType t: KomaType.values()) {
					if(shogiData.komaName[t.id].equals(s)) {
						type = t;
						break;
					}
				}
			}
			if(index == 3) {
				if(s.equals("成")) p = 1;
				if(s.equals("打")) {
					drop = 1;
					k = shogiData.findKomaInHand(type, isSente);
					if(k == null) return null;
				}
			}
			if(s.equals("(")) {
				int preX = Integer.parseInt(token.substring(index+1, index+2));
				int preY = Integer.parseInt(token.substring(index+2, index+3));
				k = shogiData.findKoma(preX, preY);
				if(k == null) return null;
			}
			index++;
		}
		kf = new Kifu(k, x, y, p, k.promoted, drop);
		return kf;
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
			try {
				BufferedImage img = ImageIO.read(new File(imgFilePath + playerName[sg.id] + ".jpg"));
				cv.playerIcon[sg.id] = img.getScaledInstance(100, 133, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				cv.playerIcon[sg.id] = null;
			}
		}
		cv.repaint();
	}
	public void initializePlayerIcon() {
		for(SenteGote sg: SenteGote.values()) cv.playerIcon[sg.id] = null;
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
        		k.reverseForReverseMode();
        	}
        	shogiData.viewKomaOnBoard();
        	shogiData.viewKomaOnHand();
        	reverseNumberRowCol();
        	cv.repaint();
        	cve.repaint();
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
	Boolean commandKeyOn = false;
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_META) {
			commandKeyOn = true;
		}
		if(commandKeyOn && e.getKeyCode() == KeyEvent.VK_V) {
			importShogiWarsKifu();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("Key Released");
		commandKeyOn = false;
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
	public void updateListBox(KomaType type, int x, int y, int preX, int preY, int sente, int promoted, int preP, int drop) {
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
		String s = createMoveKomaName(type, sente, x, y, preX, preY, promoted, preP, drop);
		s = listModel[ListBoxType.Kifu.id].size() + ":"+s;
		listModel[ListBoxType.Kifu.id].addElement(s);
		listBox[ListBoxType.Kifu.id].setModel(listModel[ListBoxType.Kifu.id]);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(listModel[ListBoxType.Kifu.id].size()-1);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(listModel[ListBoxType.Kifu.id].size()-1);
		
		cv.clearDrawPointForRightClick();
	}
	public void commonListAction() {
		int selectedIndex = listBox[ListBoxType.Kifu.id].getSelectedIndex();
		shogiData.resetAllKoma();
		shogiData.viewKomaOnBoard();
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(shogiData, kf.x, kf.y, kf.p);
				if(!checkBox[CheckBoxType.Edit.id].isSelected()) shogiData.turnIsSente = !shogiData.turnIsSente;
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
		
		checkKDB(selectedIndex);
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();

		sendCommandToEngine();
		cv.repaint();
		cve.repaint();
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
			cv.addDrawPoint(sc.target,  sc.base);
			listModel[ListBoxType.Info.id].addElement(str);
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Menu Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForSetBoardColor() {
		Color color = JColorChooser.showDialog(this, "Select color", Color.white);
		if(color != null) {
			boardColor = color;
			cv.repaint();
			cve.repaint();
		}
	}
	public class ColorSet {
		Color backGround;
		Color button;
		Color buttonFocused;
		Color buttonBorder;
		ColorSet(Color bg, Color b, Color bf, Color bb) {
			backGround = bg;
			button = b;
			buttonFocused = bf;
			buttonBorder = bb;
		}
	}
	public void actionForSetColor() {
		String[] selectvalues = new String[ColorSetType.values().length];
		for(ColorSetType cst: ColorSetType.values()) selectvalues[cst.id] = cst.name();
		int select = JOptionPane.showOptionDialog(
				this,
				"Which color style?",
				"Color Style",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				selectvalues,
				selectvalues[0]
				);
		if (select == JOptionPane.CLOSED_OPTION) {
			System.out.println("cancel");
		} else {
			setProperty(PropertyType.Color.name(), Integer.valueOf(select).toString());
			setColor();
		}
	}
	public void actionForCaptureBoard() {
		try {
			Rectangle bounds = this.getBounds();
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(bounds);
			image = image.getSubimage(0, 70, (shogiData.iconWidth+10)*11+55, (shogiData.iconHeight+10)*9);
			String dirName = "./img/";
			String fileName = "CaptureBoard.jpg";
			ImageIO.write(image, "jpg", new File(dirName, fileName));
			
			JOptionPane.showMessageDialog(null, dirName + fileName + " is saved.");
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
		cv.repaint();
		cve.repaint();
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
        isEngineOn = true;
	}
	public void actionForStopEngine() {
		sendFinalCommandToEngine();
		isEngineOn = false;
	}
	public void actionForSetEngine() {
		setPropertyForEngine();
	}
	public void actionForKifuAnalysis() {
		cve.clearBestPointData();
		checkBox[CheckBoxType.Edit.id].setSelected(false);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		commonListAction();
		MyThreadKifuAnalysis thread = new MyThreadKifuAnalysis();
		actionForStartEngine();
		if(!isEngineOn) {
			System.out.println("Failed to start shogi engine");
			return;
		}
		thread.start();
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Interface for Engine >> ----------------------
	// -------------------------------------------------------------------------
	PrintStream out = null;
	MyThreadReceiver receiver;
	String propertyFile = "KifuAnalyzer.properties";
	public static final int numOfMultiPV = 5;
	public static final int calculatingTimeOfEngine = 2000; // ms
	Boolean isEngineOn = false;
	public Process createEngine() {
		String enginePath = loadProperty(PropertyType.Engine.name());
		if(enginePath == null) {
			setPropertyForEngine();
			enginePath = loadProperty(PropertyType.Engine.name());
		}
		if(enginePath == null) return null;
		ProcessBuilder p = new ProcessBuilder(enginePath);
		Process process = null;
		try {
			process = p.start();
		} catch (IOException e) {
			System.out.println("engine is not installed");
			setPropertyForEngine();
		}
		return process;
	}
	public String loadProperty(String key) {
		Properties settings = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			settings.load(in);
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
		return settings.getProperty(key);
	}
	public void setPropertyForEngine() {
		Path path = Paths.get("").toAbsolutePath();
		FileDialog fd = new FileDialog(this, "Load", FileDialog.LOAD);
		fd.setDirectory(path.toString());
		fd.setVisible(true);
		if(fd.getFile() == null) return;
		String fileName = fd.getDirectory() + fd.getFile();
		setProperty(PropertyType.Engine.name(), fileName);
	}
	public void setProperty(String key, String value) {
		Properties properties = new Properties();
		try {
			for(PropertyType pt: PropertyType.values()) {
				String str = loadProperty(pt.name());
				if(pt.name().equals(key)) {
					properties.setProperty(key, value);
				} else {
					if(str == null) continue;
					properties.setProperty(pt.name(), str);
				}
			}
			properties.store(new FileOutputStream(propertyFile), "Comments");
		} catch (IOException e) {
			System.out.println(e);
		}
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
		cve.repaint();
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
				Koma k = shogiData.findKoma(x+1, y+1);
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
				if(k.promoted == 1) str[y] += '+';
				str[y] += s;
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
		//System.out.println(cmd);
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
	class MyThreadReceiver extends Thread {
		Process process;
		MyThreadReceiver(Process p) {
			process = p;
		}
		@Override
		public void run() {
			super.run();
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
				String line;
				while ((line = r.readLine()) != null) {
					//System.out.println(line);
					if(line.contains("info depth")) {
						getPointFromInfo(line);
						cv.repaint();
						cve.repaint();
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			}
			for(int index=0; index<numOfMultiPV; index++) {
				listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
	}
	public class ConvertedData {
		int promote;
		int drop;
		KomaType type;
	}
	public void getPointFromInfo(String info) {
		Point base = new Point();
		Point target = new Point();
		if(info.contains("multipv 1")) {
			cv.clearDrawListForEngine();
			for(int index=0; index<numOfMultiPV; index++) {
				listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
		if(!info.contains("multipv")) { // case of there are only 1 way to move
			cv.clearDrawListForEngine();
			for(int index=0; index<numOfMultiPV; index++) {
				listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
		
		String[] names = info.split(" ");
		String str = "";
		int score = 0;
		int index = 0;
		ConvertedData cd = new ConvertedData();
		//Point p = new Point(); // for promote and drop
		for(int i=0; i<names.length; i++) {
			if(names[i].equals("pv")) str = names[i+1];
			if(names[i].equals("cp")) score = Integer.parseInt(names[i+1]);
			if(names[i].equals("multipv")) index = Integer.parseInt(names[i+1]) - 1;
		}
		convertStringPosToPoints(str, base, target, cd);
		// for safe access in multi thread
		synchronized(cv.drawListForEngine) {
			PointWithScore ps = new PointWithScore(base, target, score);
			cv.drawListForEngine.add(ps);
			Koma k = null;
			if(cd.drop == 0) {
				k = shogiData.findKoma(base.x, base.y);
			} else {
				k = shogiData.findKomaInHand(cd.type, shogiData.turnIsSente);
			}
			if(k != null) {
				String komaMove = createMoveKomaName(k.type, k.sente, target.x, target.y, k.px, k.py, cd.promote, k.promoted, cd.drop);
				komaMove += " " + score;
				listModel[ListBoxType.Engine.id].set(index, komaMove);
				listBox[ListBoxType.Engine.id].setModel(listModel[ListBoxType.Engine.id]);
				bestPointFromEngine.moveName[index] = komaMove;
			}
		}
	}
	public void convertStringPosToPoints(String strPos, Point base, Point target, ConvertedData cd) {
		char ch[] = strPos.toCharArray();
		String str = "PLNSGBRKE";
		// drop a piece
		if(ch[1] == '*') {
			if(ch[0] == 'P' || ch[0] == 'L' || ch[0] == 'N' || ch[0] == 'S' || ch[0] == 'G' || ch[0] == 'B' || ch[0] == 'R') {
				if(shogiData.turnIsSente) {
					base.x = 0;
				} else {
					base.x = 10;
				}
			}
			int index = 0;
			for(KomaType t: KomaType.values()) {
				if(str.charAt(index) == ch[0]) {
					cd.type = t;
					break;
				}
				index++;
			}
			if(shogiData.turnIsSente) {
				if(ch[0] == 'P') base.y = 2;
				else if(ch[0] == 'L') base.y = 3;
				else if(ch[0] == 'N') base.y = 4;
				else if(ch[0] == 'S') base.y = 5;
				else if(ch[0] == 'G') base.y = 6;
				else if(ch[0] == 'B') base.y = 7;
				else if(ch[0] == 'R') base.y = 8;
			} else {
				if(ch[0] == 'P') base.y = 8;
				else if(ch[0] == 'L') base.y = 7;
				else if(ch[0] == 'N') base.y = 6;
				else if(ch[0] == 'S') base.y = 5;
				else if(ch[0] == 'G') base.y = 4;
				else if(ch[0] == 'B') base.y = 3;
				else if(ch[0] == 'R') base.y = 2;
			}
			cd.drop = 1;
		}
		else {
			base.x = ch[0] - '0';
			base.y = ch[1] - 96;
		}
		target.x = ch[2] - '0';
		target.y = ch[3] - 96;
		
		if(ch.length == 5) {
			if(ch[4] == '+') cd.promote = 1;
		}
	}
	class MyThreadKifuAnalysis extends Thread {
		MyThreadKifuAnalysis() {
		}
		@Override
		public void run() {
			System.out.print("Kifu Analysis start ...");
			Boolean isUnderAnalysis = true;
			while(isUnderAnalysis && isEngineOn) {
				int index = listBox[ListBoxType.Kifu.id].getSelectedIndex();
				int size = listModel[ListBoxType.Kifu.id].getSize();
				if(size-1 == index) isUnderAnalysis = false;
				try {
					Thread.sleep(calculatingTimeOfEngine);
				} catch(InterruptedException e) {
					System.out.println(e);
				}
				listBox[ListBoxType.Kifu.id].setSelectedIndex(index+1);
				listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(index+1);
				commonListAction();
			}
			System.out.println("completed.");
			actionForStopEngine();
		}
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
			cve.repaint();
		}
		
		if(shogiData.selectedKoma == null) return;
		
		Point mp = e.getPoint();
		for(ListBoxType lb: ListBoxType.values()) {
			if(e.getSource() == listBox[lb.id]) {
				mp.x += scrollPane[lb.id].getBounds().x;
				mp.y += scrollPane[lb.id].getBounds().y + shogiData.iconHeight/2 + 20;
				break;
			}
		}
		if(e.getSource() != cv) mp.y -= 50;
		shogiData.selectedKoma.pos.x = mp.x - shogiData.iconWidth/2;
		shogiData.selectedKoma.pos.y = mp.y - shogiData.iconHeight/2;
		cv.repaint();
		cve.repaint();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			//System.out.println("mouse double clicked");
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				if(!loadFile.equals("")) {
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
			if(mp.x < 80 || mp.y < 20 || mp.x > (shogiData.iconWidth+10)*9+80 || mp.y > (shogiData.iconHeight+10)*9+20) {
				return;
			}
			Point pShogiXY = cv.convertMousePointToShogiXY(mp);
			for(Point p: cv.drawListLeftClick) {
				Point pXY = cv.convertMousePointToShogiXY(p);
				if(pXY.x == pShogiXY.x && pXY.y == pShogiXY.y) {
					cv.drawListLeftClick.remove(p);
					cv.repaint();
					cve.repaint();
					return;
				}
			}
			cv.drawListLeftClick.add(mp);
			cv.repaint();
			cve.repaint();
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
			Point lp = k.pos;
			if(tp.x > lp.x-5 && tp.x < lp.x+shogiData.iconWidth+5 && tp.y > lp.y && tp.y < lp.y+shogiData.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: shogiData.listKomaOnHand.get(SenteGote.Sente.id)) {
			Point lp = k.pos;
			if(tp.x > lp.x && tp.x < lp.x+shogiData.iconWidth+10 && tp.y > lp.y && tp.y < lp.y+shogiData.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: shogiData.listKomaOnHand.get(SenteGote.Gote.id)) {
			Point lp = k.pos;
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
		cve.repaint();
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
		Point p = cv.drawListBaseRightClick.get(cv.drawListBaseRightClick.size()-1);
		if(p.x == mp.x && p.y == mp.y) cv.drawListBaseRightClick.remove(cv.drawListBaseRightClick.size()-1);
		else {
			cv.drawListTargetRightClick.add(mp);
			cv.repaint();
			cve.repaint();
		}
	}
	public void releaseKoma() {
		Koma selectedKoma = shogiData.selectedKoma;
		if(selectedKoma == null) return;
		double d = (double)(selectedKoma.pos.x - 70 + shogiData.iconWidth/2) / (double)(shogiData.iconWidth+10);
		int x;
		if(d>0) x = (int)d;
		else x = -1;
		int y = (selectedKoma.pos.y - 20 + shogiData.iconHeight/2) / (shogiData.iconHeight+10) + 1;
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
		Boolean result = selectedKoma.moveKoma(shogiData, X, Y, -1);
		soundKoma();
		shogiData.viewKomaOnBoard();
		shogiData.viewKomaOnHand();
		
		cv.mousePressed = false;
		cv.repaint();
		cve.repaint();
		
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
				updateListBox(type, X, Y, preX, preY, sente, promoted, preP, drop);
			}
			Kifu kf = new Kifu(selectedKoma, X, Y, promoted, preP, drop);
			kifuData.add(kf);
			checkKDB(listModel[ListBoxType.Kifu.id].size()-1);
			if(!checkBox[CheckBoxType.Edit.id].isSelected()) shogiData.turnIsSente = !shogiData.turnIsSente;
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
				button[bt.id].setBackground(buttonFocusedColor);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(buttonColor);
			}
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
