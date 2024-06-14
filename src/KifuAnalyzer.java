import java.awt.AWTException;
import java.awt.Color;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lib.CanvasBoard;
import lib.CanvasBoardForEngine;
import lib.CastleDataBase;
import lib.ColorDataBase;
import lib.EditProperty;
import lib.KifuDataBase;
import lib.KifuDataBase.Kifu;
import lib.KifuDataBase.KifuData;
import lib.KomaSound;
import lib.PlayerDataBase;
import lib.ShogiData;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine;
import lib.StrategyDataBase;
import lib.StringCount;
import lib.TesujiDataBase;

public class KifuAnalyzer extends JFrame implements MouseListener, MouseMotionListener, 
	ActionListener, ListSelectionListener, KeyListener {
	// -------------------------------------------------------------------------
	// ----------------------- << Global Variables >> --------------------------
	// -------------------------------------------------------------------------
	String imgFilePath = "./img/";
	String imgFilePathKoma = imgFilePath + "koma/";
	
	KomaSound ks = new KomaSound();
	ShogiData sd = new ShogiData();
	ShogiData sdForKDB = new ShogiData();
	ShogiEngine se = new ShogiEngine();
	CanvasBoard cv;
	CanvasBoardForEngine cve;
	EditProperty ep = new EditProperty();
	KifuDataBase kdb;
	StrategyDataBase sdb;
	CastleDataBase cdb;
	TesujiDataBase tdb;
	PlayerDataBase pdb;
	ColorDataBase cldb;
	
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
		kdb = new KifuDataBase(this, sd, sdForKDB, cv, 
				listModel[ListBoxType.Info.id], listBox[ListBoxType.Info.id],
				listModel[ListBoxType.Kifu.id], listBox[ListBoxType.Kifu.id], 
				checkBox[CheckBoxType.Reverse.id], checkBox[CheckBoxType.Draw.id], checkBox[CheckBoxType.Edit.id],
				textBox[TextBoxType.Player1.id], textBox[TextBoxType.Player2.id], 
				textBox[TextBoxType.Strategy.id], textBox[TextBoxType.Castle.id],
				pdb, cdb, sdb, se, cve);
		sdb = new StrategyDataBase(listModel[ListBoxType.Strategy.id], listBox[ListBoxType.Strategy.id],
				listModel[ListBoxType.Player.id], listBox[ListBoxType.Player.id],
				listModel[ListBoxType.Info.id], listBox[ListBoxType.Info.id],
				listBox[ListBoxType.Castle.id], textBox[TextBoxType.Castle.id],
				kdb, cdb);
		cdb = new CastleDataBase(listModel[ListBoxType.Castle.id], listBox[ListBoxType.Castle.id],
				listModel[ListBoxType.Player.id], listBox[ListBoxType.Player.id],
				listModel[ListBoxType.Info.id], listBox[ListBoxType.Info.id],
				listBox[ListBoxType.Strategy.id], textBox[TextBoxType.Castle.id], 
				cv, kdb, sdb);
		tdb = new TesujiDataBase(listModel[ListBoxType.Tesuji.id], listBox[ListBoxType.Tesuji.id],
				listModel[ListBoxType.Info.id], listBox[ListBoxType.Info.id],
				textBox[TextBoxType.Tesuji.id], comboBox, kdb
				);
		pdb = new PlayerDataBase(listModel[ListBoxType.Player.id], listBox[ListBoxType.Player.id],
				listModel[ListBoxType.Info.id], listBox[ListBoxType.Info.id],
				textBox[TextBoxType.Player1.id], textBox[TextBoxType.Player2.id], kdb, cv);
		cldb = new ColorDataBase(ep, cv, button);
		
		kdb.update(pdb, cdb, sdb, se, cve);
		sdb.update(kdb, cdb);
		
		initializeCanvasSetting();
		ks.initializeSoundSetting();
		initializeMenuBar();
		cv.initializeNumberRowCol();
		cldb.initializeColorSet();
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
		pdb.initializePlayerIcon();
		cdb.initializeCastleIcon();
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
	// ----------------------- << Button Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForInitialize() {
		clearTextBox();
		clearCheckBox();
		cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
		kdb.clearListBox();
		clearIcons();
		kdb.kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cve.clearBestPointData();
		sdb.loadStrategyData();
		cdb.loadCastleData();
		tdb.loadTesujiData();
		actionForDB();
		sdb.countStrategy();
		cdb.countCastle();
		tdb.countTesujiData();
		pdb.createPlayerDataBase();
		cv.repaint();
		cve.repaint();
	}
	public void actionForSave() {
		kdb.actionForSave();
	}
	String loadFile = "";
	String loadStep = "";
	String loadYear = "";
	public void actionForLoad() {
		kdb.loadByNumber(loadFile, loadStep, loadYear);
	}
	
	public void actionForDB() {
		kdb.kifuDB.clear();
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
				String fileName = kdb.kifuFilePath + strY + "/" + "kifu" + String.format("%03d", fileIndex) + ".txt";
				//System.out.println(fileName);
				File file = new File(fileName);
				KifuData kd = kdb.createKifuData();
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String content;
				for(SenteGote sg: SenteGote.values()) {
					kd.playerName[sg.id] = br.readLine();
				}
				kd.year = strY;
				kd.index = fileIndex;
				while((content = br.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(content,",");
					while(st.hasMoreTokens()) {
						int i = Integer.parseInt(st.nextToken()); // index
						if(i == -1) { // Draw game
							kd.isSenteWin = -1;
							continue;
						}
						int x = Integer.parseInt(st.nextToken()); // x
						int y = Integer.parseInt(st.nextToken()); // y
						int p = Integer.parseInt(st.nextToken()); // promote
						int pp = Integer.parseInt(st.nextToken()); // preP
						int d = Integer.parseInt(st.nextToken()); // drop
						Kifu kf = kdb.createKifu(sd.k[i], x, y, p, pp, d);
						kd.db.add(kf);
						sdForKDB.k[kf.k.index].moveKoma(kf.x, kf.y, kf.p);
						if(kd.strategyName.equals("")) {
							kd.strategyName = sdb.checkStrategy(sdForKDB);
						}
						if(kd.castleName[SenteGote.Sente.id].equals("")) {
							kd.castleName[SenteGote.Sente.id] = cdb.checkCastle(sdForKDB, true);
						}
						if(kd.castleName[SenteGote.Gote.id].equals("")) {
							kd.castleName[SenteGote.Gote.id] = cdb.checkCastle(sdForKDB, false);
						}
					}
				}
				br.close();
				if(kd.isSenteWin != -1) kd.isSenteWin = kdb.isSenteWin(kd);
				kdb.kifuDB.add(kd);
				fileIndex++;
			}
		} catch(FileNotFoundException en) {
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		kdb.commonListAction();
		
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
			fileName = sdb.strategyFilePathBase + sdb.strategyFilePath + String.format("strategy%03d.txt", index);
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
			fileName = sdb.strategyFilePathBase + cdb.castleFilePath + String.format("castle%03d.txt", index);
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
					cdb.saveListKomaAroundKing(sd, k, fw);
				}
				if((k.type == KomaType.King) && radioButtonGote.isSelected() && k.sente == 1) {
					cdb.saveListKomaAroundKing(sd, k, fw);
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
			fileName = kdb.kifuFilePath + String.format("tesuji%03d.txt", index);
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
		
		for(KifuData kd: kdb.kifuDB) {
			int i=0;
			Boolean isSame = true;
			// check same moves
			while(i<index && i<kd.db.size()) {
				if( kdb.kifuData.get(i).k.type != kd.db.get(i).k.type ||
					kdb.kifuData.get(i).x != kd.db.get(i).x || 
					kdb.kifuData.get(i).y != kd.db.get(i).y || 
					kdb.kifuData.get(i).p != kd.db.get(i).p ) {
					// check same position if moves were different
					isSame = kdb.checkSamePositionKDB(index, kd);
					break;
				}
				i++;
			}
			if(isSame && index < kd.db.size()) {
				String str = String.format("kf%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
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
			cldb.actionForSetColor(this);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StartEngine.id]) {
			se.actionForStartEngine(this, sd, cv, cve, 
					listModel[ListBoxType.Engine.id], listBox[ListBoxType.Engine.id], 
					listModel[ListBoxType.Kifu.id], listBox[ListBoxType.Kifu.id]);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StopEngine.id]) {
			se.actionForStopEngine();
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
	private ActionListener enterActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            pdb.updatePlayerIcon();
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
	// ----------------------- << ListBox Action >> -----------------------------
	// -------------------------------------------------------------------------
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				kdb.commonListAction();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				sdb.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				pdb.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				cdb.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				tdb.updateListBoxInfoByTesuji();
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
			kdb.importShogiWarsKifu();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("Key Released");
		commandKeyOn = false;
		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				kdb.commonListAction();
				ks.soundKoma();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				sdb.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				pdb.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				cdb.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				tdb.updateListBoxInfoByTesuji();
			}
		}
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
	public void actionForSetEngine() {
		ep.setPropertyForEngine(this);
	}
	public void actionForKifuAnalysis() {
		cve.clearBestPointData();
		checkBox[CheckBoxType.Edit.id].setSelected(false);
		listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		kdb.commonListAction();
		MyThreadKifuAnalysis thread = new MyThreadKifuAnalysis();
		se.actionForStartEngine(this, sd, cv, cve, listModel[ListBoxType.Engine.id], listBox[ListBoxType.Engine.id], 
				listModel[ListBoxType.Kifu.id], listBox[ListBoxType.Kifu.id]);
		if(!se.isEngineOn) {
			System.out.println("Failed to start shogi engine");
			return;
		}
		thread.start();
	}
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
				kdb.commonListAction();
			}
			System.out.println("completed.");
			se.actionForStopEngine();
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
				kdb.updateListBox(type, X, Y, preX, preY, sente, promoted, preP, drop);
			}
			Kifu kf = kdb.createKifu(selectedKoma, X, Y, promoted, preP, drop);
			kdb.kifuData.add(kf);
			kdb.checkKDB(listModel[ListBoxType.Kifu.id].size()-1);
			if(!checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
			se.sendCommandToEngine();
		}
		
		// check strategy
		if(textBox[TextBoxType.Strategy.id].getText().equals("")) {
			textBox[TextBoxType.Strategy.id].setText(sdb.checkStrategy(sd));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(cdb.checkCastle(sd, true));
		}
		if(textBox[TextBoxType.Castle.id].getText().equals("")) {
			textBox[TextBoxType.Castle.id].setText(cdb.checkCastle(sd, false));
		}
		
		sd.selectedKoma = null;
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonFocusedColor);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonColor);
			}
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
