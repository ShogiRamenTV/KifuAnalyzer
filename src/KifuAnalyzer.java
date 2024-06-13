import java.awt.AWTException;
import java.awt.Color;
import java.awt.FileDialog;
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
import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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

import lib.CanvasBoard;
import lib.CanvasBoardForEngine;
import lib.EditProperty;
import lib.EditProperty.PropertyType;
import lib.KomaSound;
import lib.ShogiData;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine;
import lib.StringCount;

public class KifuAnalyzer extends JFrame implements MouseListener, MouseMotionListener, 
	ActionListener, ListSelectionListener, KeyListener {
	// -------------------------------------------------------------------------
	// ----------------------- << Global Variables >> --------------------------
	// -------------------------------------------------------------------------
	String imgFilePath = "./img/";
	String imgFilePathPlayerIcon = imgFilePath + "playerIcon/";
	String imgFilePathCastleIcon = imgFilePath + "castleIcon/";
	String imgFilePathKoma = imgFilePath + "koma/";
	
	
	KomaSound ks = new KomaSound();
	ShogiData sd = new ShogiData();
	ShogiData sdForKDB = new ShogiData();
	ShogiEngine se = new ShogiEngine();
	CanvasBoard cv;
	CanvasBoardForEngine cve;
	EditProperty ep = new EditProperty();
	
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
		SetColor(0);
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
		Default(0), Sakura(1), GreenTea(2), BlueSky(3), Evening(4), Universe(5);
		private final int id;		
		private ColorSetType(final int id) {
			this.id = id;
		}
	};
	Color buttonColor;
	Color buttonFocusedColor;
	ColorSet listColorSet[] = new ColorSet[ColorSetType.values().length];
	
	
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
		sd.initializeIcon();
		setSize(sd.iconWidth*25, sd.iconHeight*12);
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
		sd.initializeKomaSetting();
		sdForKDB.initializeKomaSetting();
		initializeButtonSetting();
		initializeTextBoxSetting();
		initializeCheckBox();
		initializeListBoxSetting();
		cve = new CanvasBoardForEngine(se, listModel[ListBoxType.Engine.id], listBox[ListBoxType.Kifu.id]);
		cv = new CanvasBoard(sd, checkBox[CheckBoxType.Reverse.id], se, cve);
		initializeCanvasSetting();
		ks.initializeSoundSetting();
		initializeMenuBar();
		cv.initializeNumberRowCol();
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
		checkBox[CheckBoxType.Edit.id].setBounds(baseXPosForItems+140, 35, 60, 12);
		checkBox[CheckBoxType.Reverse.id].setBounds(baseXPosForItems+190, 35, 80, 12);
		checkBox[CheckBoxType.Reverse.id].addActionListener(checkActionListener);
		checkBox[CheckBoxType.Draw.id].setBounds(baseXPosForItems+80, 35, 80, 12);
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
		
		for(int index=0; index<se.getNumOfMultiPV(); index++) {
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
	
	// -------------------------------------------------------------------------
	// ---------------------------- << Color >> -------------------------------
	// -------------------------------------------------------------------------
	public class ColorSet {
		Color button;
		Color buttonFocused;
		Color buttonBorder;
		Color font;
		Image board;
		Image background;
		ColorSet(Color b, Color bf, Color bb, Color f) {
			button = b;
			buttonFocused = bf;
			buttonBorder = bb;
			font = f;
			board = null;
			background = null;
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
			ep.setProperty(PropertyType.Color.name(), Integer.valueOf(select).toString());
			setColor();
		}
	}
	public void initializeColorSet() {
		listColorSet[ColorSetType.Default.id] = new ColorSet(
				new Color(235, 235, 235), // button;
				new Color(215, 215, 215), // buttonFocused;
				new Color(0, 0, 0),		 // buttonBorder;
				new Color(0, 0, 0)		 // font;
				);
		listColorSet[ColorSetType.Sakura.id] = new ColorSet(
				new Color(230, 205, 227), 
				new Color(229, 171, 190), 
				new Color(201, 117, 134),
				new Color(201, 67, 84)
				);
		listColorSet[ColorSetType.GreenTea.id] = new ColorSet(
				new Color(205, 230, 227), 
				new Color(171, 229, 190), 
				new Color(117, 201, 134),
				new Color(255, 255, 255)
				);
		listColorSet[ColorSetType.BlueSky.id] = new ColorSet(
				new Color(160, 216, 239), 
				new Color(171, 190, 229), 
				new Color(117, 134, 201),
				new Color(0, 0, 0)	
				);
		listColorSet[ColorSetType.Evening.id] = new ColorSet(
				new Color(255, 255, 0), 
				new Color(255, 215, 0), 
				new Color(255, 140, 0),
				new Color(255, 255, 255)
				);
		listColorSet[ColorSetType.Universe.id] = new ColorSet(
				new Color(255, 255, 0), 
				new Color(255, 215, 0), 
				new Color(255, 140, 0),
				new Color(255, 255, 255)
				);
		initializeImageSet();
		setColor();
	}
	public void initializeImageSet() {
		for(ColorSetType cst: ColorSetType.values()) {
			try {
				String fileName = cv.imgFilePathBoard + "shogi board" + cst.id + ".png";
				BufferedImage img = ImageIO.read(new File(fileName));
				listColorSet[cst.id].board = img.getScaledInstance((50+10)*9, (63+10)*9, java.awt.Image.SCALE_SMOOTH);
				fileName = cv.imgFilePathBackground + "background" + cst.id + ".png";
				img = ImageIO.read(new File(fileName));
				listColorSet[cst.id].background = img.getScaledInstance(50*25, 63*12, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				listColorSet[cst.id].board = null;
				listColorSet[cst.id].background = null;
			}
		}
	}
	public void setColor() {
		String value = ep.loadProperty(PropertyType.Color.name());
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
		cv.imgBoard = listColorSet[select].board;
		cv.imgBackground = listColorSet[select].background;
		cv.clrFont =  listColorSet[select].font;
		cv.repaint();
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Button Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForInitialize() {
		clearTextBox();
		clearCheckBox();
		cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
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
			fileName = kifuFilePath + String.format("kifu%03d.txt", index);
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
			fileName = kifuFilePath + fd.getFile();
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
					updateListBox(sd.k[i].type, x, y, sd.k[i].px, sd.k[i].py, sd.k[i].sente, p, pp, d);
					Kifu kf = new Kifu(sd.k[i], x, y, p, pp, d);
					kifuData.add(kf);
					sd.k[i].moveKoma(x, y, p);
				}
			}
			br.close();
			
			sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());
			if(numStrStep.equals("")) {
				listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
				listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
			} else {
				int selectedIndex = Integer.parseInt(numStrStep);
				listBox[ListBoxType.Kifu.id].setSelectedIndex(selectedIndex);
				listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(selectedIndex);
				commonListAction();
			}
			
			sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
			sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
			updatePlayerIcon();
			cv.repaint();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void initializeShogiBoard() {
		sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cv.clearDrawPoint();
		actionForStopEngine();
		cve.clearBestPointData();
		//clearTextBox();
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
				sdForKDB.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());
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
						Kifu kf = new Kifu(sd.k[i], x, y, p, pp, d);
						kdb.db.add(kf);
						sdForKDB.k[kf.k.index].moveKoma(kf.x, kf.y, kf.p);
						if(kdb.strategyName.equals("")) {
							kdb.strategyName = checkStrategy(sdForKDB);
						}
						if(kdb.castleName[SenteGote.Sente.id].equals("")) {
							kdb.castleName[SenteGote.Sente.id] = checkCastle(sdForKDB, true);
							//if(kdb.castleName[SenteGote.Sente.id].equals("Yagura")) {
							//	System.out.println("Yagura:" + kdb.index);
							//}
						}
						if(kdb.castleName[SenteGote.Gote.id].equals("")) {
							kdb.castleName[SenteGote.Gote.id] = checkCastle(sdForKDB, false);
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
			fileName = strategyFilePathBase + strategyFilePath + String.format("strategy%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBox[TextBoxType.Strategy.id].getText() + "\n");
			for(int i=0; i<40; i++) {
				fw.write(sd.k[i].px + "," + sd.k[i].py + "\n");
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
			fileName = strategyFilePathBase + castleFilePath + String.format("castle%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBox[TextBoxType.Castle.id].getText() + "\n");
			for(Koma k: sd.k) {
				if((k.type == KomaType.King) && radioButtonSente.isSelected() && k.sente == 0) {
					saveListKomaAroundKing(sd, k, fw);
				}
				if((k.type == KomaType.King) && radioButtonGote.isSelected() && k.sente == 1) {
					saveListKomaAroundKing(sd, k, fw);
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
		if(loadFile.equals("")) return;
		
		while(true) {
			fileName = kifuFilePath + String.format("tesuji%03d.txt", index);
			path = Paths.get(fileName);
			if(!Files.exists(path)) break;
			index++;
		}
		
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
		
			fw.write(textBox[TextBoxType.Tesuji.id].getText() + "\n");
			fw.write(loadFile + "\n");
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
	String strategyFilePathBase = "./strategy/";
	String strategyFilePath = "strategy/";
	String castleFilePath = "castle/";
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
				String fileName = strategyFilePathBase + strategyFilePath + "strategy" + String.format("%03d", fileIndex) + ".txt";
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
				String fileName = strategyFilePathBase + castleFilePath + "castle" + String.format("%03d", fileIndex) + ".txt";
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
			BufferedImage img = ImageIO.read(new File(imgFilePathCastleIcon + castleName + ".jpg"));
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
	String kifuFilePath = "./kifu/";
	List<Kifu> kifuData = new ArrayList<Kifu>();
	List<KifuDataBase> kifuDB = new ArrayList<KifuDataBase>();
	
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
		String str = sd.createMoveKomaName(kf.k.type, kf.k.sente, kf.x, kf.y, kf.k.px, kf.k.py, kf.p, kf.pp, kf.d);
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
			sc.base = new Point(sd.k[kf.k.index].px, sd.k[kf.k.index].py);
			listSC.add(sc);
		}
	}
	public int isSenteWin(KifuDataBase kdb) {
		int index = kdb.db.size()-1;
		if((index%2) == 0) return 1;
		return 0;
	}
	public Boolean checkSamePositionKDB(int index, KifuDataBase kdb) {
		sdForKDB.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());
		
		int i = 0;
		while(i<index && index<kdb.db.size()) {
			Kifu kf = kdb.db.get(i);
			sdForKDB.k[kf.k.index].moveKoma(kf.x, kf.y, kf.p);
			i++;
		}
		Boolean isSame = true;
		for(int x=0; x<40; x++) {
			if(sd.k[x].type != sdForKDB.k[x].type || 
					sd.k[x].px != sdForKDB.k[x].px || 
					sd.k[x].py != sdForKDB.k[x].py ||
					sd.k[x].promoted != sdForKDB.k[x].promoted) {
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
				sd.k[kf.k.index].moveKoma(kf.x, kf.y, kf.p);
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
					if(sd.komaName[t.id].equals(s)) {
						type = t;
						break;
					}
				}
			}
			if(index == 3) {
				if(s.equals("成")) p = 1;
				if(s.equals("打")) {
					drop = 1;
					k = sd.findKomaInHand(type, isSente);
					if(k == null) return null;
				}
			}
			if(s.equals("(")) {
				int preX = Integer.parseInt(token.substring(index+1, index+2));
				int preY = Integer.parseInt(token.substring(index+2, index+3));
				k = sd.findKoma(preX, preY);
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
				BufferedImage img = ImageIO.read(new File(imgFilePathPlayerIcon + playerName[sg.id] + ".jpg"));
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
        	for(Koma k: sd.k) {
        		k.reverseForReverseMode();
        	}
        	sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
        	sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cv.repaint();
        	cve.repaint();
        }
    };
    class MyThreadKifuAnalysis extends Thread {
		MyThreadKifuAnalysis() {
		}
		@Override
		public void run() {
			System.out.print("Kifu Analysis start ...");
			Boolean isUnderAnalysis = true;
			int calcTimeMs = se.getCalculatingTimeOfEngine();
			while(isUnderAnalysis && se.isEngineOn) {
				int index = listBox[ListBoxType.Kifu.id].getSelectedIndex();
				int size = listModel[ListBoxType.Kifu.id].getSize();
				if(size-1 == index) isUnderAnalysis = false;
				try {
					Thread.sleep(calcTimeMs);
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
				ks.soundKoma();
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
		String s = sd.createMoveKomaName(type, sente, x, y, preX, preY, promoted, preP, drop);
		s = listModel[ListBoxType.Kifu.id].size() + ":"+s;
		listModel[ListBoxType.Kifu.id].addElement(s);
		listBox[ListBoxType.Kifu.id].setModel(listModel[ListBoxType.Kifu.id]);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(listModel[ListBoxType.Kifu.id].size()-1);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(listModel[ListBoxType.Kifu.id].size()-1);
		
		cv.clearDrawPointForRightClick();
	}
	public void commonListAction() {
		int selectedIndex = listBox[ListBoxType.Kifu.id].getSelectedIndex();
		sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(kf.x, kf.y, kf.p);
				if(!checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
				cv.setLastPoint(kf.x, kf.y, true);
				if(textBox[TextBoxType.Strategy.id].getText().equals("")) textBox[TextBoxType.Strategy.id].setText(checkStrategy(sd));
				if(textBox[TextBoxType.Castle.id].getText().equals("")) {
					textBox[TextBoxType.Castle.id].setText(checkCastle(sd, true));
				}
				if(textBox[TextBoxType.Castle.id].getText().equals("")) {
					textBox[TextBoxType.Castle.id].setText(checkCastle(sd, false));
				}
			}
		}
		if(selectedIndex == 0) cv.setLastPoint(-1, -1, false);
		cv.clearDrawPointForRightClick();
		
		checkKDB(selectedIndex);
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());

		se.sendCommandToEngine();
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
	
	public void actionForCaptureBoard() {
		try {
			Rectangle bounds = this.getBounds();
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(bounds);
			image = image.getSubimage(0, 70, (sd.iconWidth+10)*11+55, (sd.iconHeight+10)*9);
			String dirName = imgFilePath;
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
		sd.putAllKomaInHand(checkBox[CheckBoxType.Reverse.id].isSelected(),radioButtonSente.isSelected());
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
		cv.repaint();
		cve.repaint();
	}
	public void actionForStartEngine() {
		//(JFrame fr, ShogiData s, CanvasBoard c, CanvasBoardForEngine ce, 
		//		DefaultListModel<String> lmE, JList<String> lbE, 
		//		DefaultListModel<String> lmK, JList<String> lbK) 
		Process process = se.createEngine(this, sd, cv, cve, 
				listModel[ListBoxType.Engine.id], listBox[ListBoxType.Engine.id],
				listModel[ListBoxType.Kifu.id], listBox[ListBoxType.Kifu.id]);
        if(process == null) {
        	System.out.println("create engine failed");
        	return;
        }
        se.createReceiverThread(process);
        se.sendInitialCommandToEngine(process);
        se.sendCommandToEngine();
        se.isEngineOn = true;
	}
	public void actionForStopEngine() {
		se.sendFinalCommandToEngine();
		se.isEngineOn = false;
	}
	public void actionForSetEngine() {
		ep.setPropertyForEngine(this);
	}
	public void actionForKifuAnalysis() {
		cve.clearBestPointData();
		checkBox[CheckBoxType.Edit.id].setSelected(false);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		commonListAction();
		MyThreadKifuAnalysis thread = new MyThreadKifuAnalysis();
		actionForStartEngine();
		if(!se.isEngineOn) {
			System.out.println("Failed to start shogi engine");
			return;
		}
		thread.start();
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
		
		if(sd.selectedKoma == null) return;
		
		Point mp = e.getPoint();
		for(ListBoxType lb: ListBoxType.values()) {
			if(e.getSource() == listBox[lb.id]) {
				mp.x += scrollPane[lb.id].getBounds().x;
				mp.y += scrollPane[lb.id].getBounds().y + sd.iconHeight/2 + 20;
				break;
			}
		}
		if(e.getSource() != cv) mp.y -= 50;
		sd.selectedKoma.pos.x = mp.x - sd.iconWidth/2;
		sd.selectedKoma.pos.y = mp.y - sd.iconHeight/2;
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
					if(sd.listKomaOnBoard.indexOf(k) != -1) k.reverse();
				}
			}
		}
		if(e.getButton() == MouseEvent.BUTTON1) {
			//System.out.println("Left Clicked");
			if(e.getSource() != cv) return;
			Point mp = e.getPoint();
			if(mp.x < 80 || mp.y < 20 || mp.x > (sd.iconWidth+10)*9+80 || mp.y > (sd.iconHeight+10)*9+20) {
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
		if(sd.selectedKoma == null) {
			selectKoma(e);
		} 
	}
	public void selectKoma(MouseEvent e) {
		sd.selectedKoma = null;
		Koma selectedKoma = searchKoma(e);
		if(selectedKoma == null) return;
		Boolean isOnBoard = false;
		if(sd.listKomaOnBoard.indexOf(selectedKoma) != -1) isOnBoard = true;
		commonMousePressed(selectedKoma, isOnBoard);
	}
	public Koma searchKoma(MouseEvent e) {
		Point mp = e.getPoint();
		Point tp = new Point(mp.x, mp.y);
		if(e.getSource() != cv) {
			tp.y -= 50;
		}
		for(Koma k: sd.listKomaOnBoard) {
			Point lp = k.pos;
			if(tp.x > lp.x-5 && tp.x < lp.x+sd.iconWidth+5 && tp.y > lp.y && tp.y < lp.y+sd.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: sd.listKomaOnHand.get(SenteGote.Sente.id)) {
			Point lp = k.pos;
			if(tp.x > lp.x && tp.x < lp.x+sd.iconWidth+10 && tp.y > lp.y && tp.y < lp.y+sd.iconHeight+5) {
				return k;
			}
		}
		for(Koma k: sd.listKomaOnHand.get(SenteGote.Gote.id)) {
			Point lp = k.pos;
			if(tp.x > lp.x && tp.x < lp.x+sd.iconWidth+10 && tp.y > lp.y && tp.y < lp.y+sd.iconHeight+5) {
				return k;
			}
		}
		return null;
	}
	public void commonMousePressed(Koma k, Boolean isOnBoard) {
		if(sd.turnIsSente && k.sente == 1 && !checkBox[CheckBoxType.Edit.id].isSelected()) {
			sd.selectedKoma = null;
			return;
		}
		if(!sd.turnIsSente && k.sente == 0 && !checkBox[CheckBoxType.Edit.id].isSelected()) {
			sd.selectedKoma = null;
			return;
		}
		sd.selectedKoma = k;
		if(isOnBoard) cv.mousePressed = true;
		cv.repaint();
		cve.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(sd.selectedKoma != null) {
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
		Koma selectedKoma = sd.selectedKoma;
		if(selectedKoma == null) return;
		double d = (double)(selectedKoma.pos.x - 70 + sd.iconWidth/2) / (double)(sd.iconWidth+10);
		int x;
		if(d>0) x = (int)d;
		else x = -1;
		int y = (selectedKoma.pos.y - 20 + sd.iconHeight/2) / (sd.iconHeight+10) + 1;
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
		Boolean result = selectedKoma.moveKoma(X, Y, -1);
		ks.soundKoma();
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
		
		cv.mousePressed = false;
		cv.repaint();
		cve.repaint();
		
		if(result == false) {
			sd.selectedKoma = null;
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
			if(!checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
			se.sendCommandToEngine();
		}
		
		// check strategy
		if(textBox[TextBoxType.Strategy.id].getText().equals("")) {
			textBox[TextBoxType.Strategy.id].setText(checkStrategy(sd));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(checkCastle(sd, true));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(checkCastle(sd, false));
		}
		
		sd.selectedKoma = null;
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
