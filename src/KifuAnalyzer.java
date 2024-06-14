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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lib.CanvasBoard;
import lib.CanvasBoardForEngine;
import lib.CastleDataBase;
import lib.ColorDataBase;
import lib.EditProperty;
import lib.KifuDataBase;
import lib.KifuDataBase.Kifu;
import lib.KomaSound;
import lib.ListBoxData;
import lib.ListBoxData.ListBoxType;
import lib.PlayerDataBase;
import lib.ShogiData;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine;
import lib.StrategyDataBase;
import lib.StringCount;
import lib.TesujiDataBase;
import lib.TextBoxData;
import lib.TextBoxData.TextBoxType;

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
	ListBoxData lbd;
	TextBoxData tbd;
	
	public enum ButtonType {
		Initialize(0), Save(1), Strategy(2), Castle(3), Tesuji(4), Kifu(5);
		private final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	JButton button[] = new JButton[ButtonType.values().length];
	
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
		tbd = new TextBoxData(baseXPosForItems, pdb, lbd);
		tbd.initializeTextBoxSetting();
		initializeCheckBox();
		lbd = new ListBoxData(baseXPosForItems, se.getNumOfMultiPV(), kdb, pdb, sdb, cdb, tdb, ks);
		lbd.initializeListBoxSetting();
		cve = new CanvasBoardForEngine(se, lbd.listModel[ListBoxType.Engine.id], lbd.listBox[ListBoxType.Kifu.id]);
		cv = new CanvasBoard(sd, checkBox[CheckBoxType.Reverse.id], se, cve);
		kdb = new KifuDataBase(this, sd, sdForKDB, cv, lbd.listModel, lbd.listBox,
				checkBox[CheckBoxType.Reverse.id], checkBox[CheckBoxType.Draw.id], checkBox[CheckBoxType.Edit.id],
				tbd.textBox,
				pdb, cdb, sdb, se, cve);
		sdb = new StrategyDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox,
				kdb, cdb);
		cdb = new CastleDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, 
				cv, kdb, sdb);
		tdb = new TesujiDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, comboBox, kdb
				);
		pdb = new PlayerDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, kdb, cv);
		cldb = new ColorDataBase(ep, cv, button);
		
		kdb.update(pdb, cdb, sdb, se, cve);
		sdb.update(kdb, cdb);
		lbd.update(kdb, pdb, sdb, cdb, tdb);
		tbd.update(pdb, lbd);
		
		
		initializeCanvasSetting();
		ks.initializeSoundSetting();
		initializeMenuBar();
		cv.initializeNumberRowCol();
		cldb.initializeColorSet();
	}
	public void contentPaneSetting() {
		getContentPane().setLayout(null);
		for(ButtonType b: ButtonType.values()) getContentPane().add(button[b.id]);
		for(TextBoxType t: TextBoxType.values()) getContentPane().add(tbd.textBox[t.id]);
		for(ListBoxType lb: ListBoxType.values()) getContentPane().add(lbd.scrollPane[lb.id]);
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
	private final int baseXPosForItems = 720;
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
		tbd.clearTextBox();
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
		kdb.actionForDB((String)comboBox.getSelectedItem());
		sdb.countStrategy();
		cdb.countCastle();
		tdb.countTesujiData();
		pdb.createPlayerDataBase();
		cv.repaint();
		cve.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == button[ButtonType.Initialize.id].getText()) {
			actionForInitialize();
		}
		if(e.getActionCommand() == button[ButtonType.Save.id].getText()) {
			kdb.actionForSave();
		}
		if(e.getActionCommand() == button[ButtonType.Strategy.id].getText()) {
			sdb.actionForStrategy(sd, tbd.textBox[TextBoxType.Strategy.id].getText());
		}
		if(e.getActionCommand() == button[ButtonType.Castle.id].getText()) {
			cdb.actionForCastle(sd, radioButtonSente.isSelected());
		}
		if(e.getActionCommand() == button[ButtonType.Tesuji.id].getText()) {
			tdb.actionForTesuji(lbd.loadFile);
		}
		if(e.getActionCommand() == button[ButtonType.Kifu.id].getText()) {
			kdb.actionForKifu();
		}
		if(e.getSource() == menuItemSetting[MenuTypeSetting.SetColor.id]) {
			cldb.actionForSetColor(this);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StartEngine.id]) {
			se.actionForStartEngine(this, sd, cv, cve, 
					lbd.listModel[ListBoxType.Engine.id], lbd.listBox[ListBoxType.Engine.id], 
					lbd.listModel[ListBoxType.Kifu.id], lbd.listBox[ListBoxType.Kifu.id]);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StopEngine.id]) {
			se.actionForStopEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.SetEngine.id]) {
			ep.setPropertyForEngine(this);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.KifuAnalysis.id]) {
			kdb.actionForKifuAnalysis();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.CaptureBoard.id]) {
			actionForCaptureBoard();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.KomaInHand.id]) {
			actionForKomaInHand();
		}
	}
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
    
 
	// -------------------------------------------------------------------------
	// ----------------------- << lbd.listBox Action >> -----------------------------
	// -------------------------------------------------------------------------
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
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
	}
	public void updateListBox2(List<StringCount> listSC) {
		lbd.listModel[ListBoxType.Info.id].clear();
		lbd.listBox[ListBoxType.Info.id].setModel(lbd.listModel[ListBoxType.Info.id]);
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
			lbd.listModel[ListBoxType.Info.id].addElement(str);
		}
		lbd.listBox[ListBoxType.Info.id].setModel(lbd.listModel[ListBoxType.Info.id]);
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
			if(e.getSource() == lbd.listBox[lb.id]) {
				mp.x += lbd.scrollPane[lb.id].getBounds().x;
				mp.y += lbd.scrollPane[lb.id].getBounds().y + sd.iconHeight/2 + 20;
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
		
		// update kifu lbd.listBox and kifuData
		if(X != preX || Y != preY) {
			if(X>0 && X<10 && Y>0 && Y<10) {
				cv.setLastPoint(X, Y, true);
				kdb.updateListBox(type, X, Y, preX, preY, sente, promoted, preP, drop);
			}
			Kifu kf = kdb.createKifu(selectedKoma, X, Y, promoted, preP, drop);
			kdb.kifuData.add(kf);
			kdb.checkKDB(lbd.listModel[ListBoxType.Kifu.id].size()-1);
			if(!checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
			se.sendCommandToEngine();
		}
		
		// check strategy
		if(tbd.textBox[TextBoxType.Strategy.id].getText().equals("")) {
			tbd.textBox[TextBoxType.Strategy.id].setText(sdb.checkStrategy(sd));
		}
		if(tbd.textBox[TextBoxType.Castle.id].getText().equals("")) {
			tbd.textBox[TextBoxType.Castle.id].setText(cdb.checkCastle(sd, true));
		}
		if(tbd.textBox[TextBoxType.Castle.id].getText().equals("")) {
			tbd.textBox[TextBoxType.Castle.id].setText(cdb.checkCastle(sd, false));
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
