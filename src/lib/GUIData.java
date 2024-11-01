package lib;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
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

import lib.ShogiData.Koma;
// -------------------------------------------------------------------------
// ---------- << Button, ListBox, TextBox, CheckBox, Menu >> ---------------
// -------------------------------------------------------------------------
public class GUIData implements ActionListener, MouseListener, ListSelectionListener, KeyListener{
	int baseXPosForItems;
	JFrame fr;
	ShogiData sd;
	ShogiEngine se;
	CanvasData cd;
	AnalysisData ad;
	ColorDataBase cldb;
	EditProperty ep;
	public GUIData(int bPosX, JFrame f, ShogiData s, ShogiEngine sen, EditProperty e,
			CanvasData c, AnalysisData a, 
			ColorDataBase cdb) {
		baseXPosForItems = bPosX;
		fr = f;
		sd = s;
		se = sen;
		ep = e;
		cd = c;
		ad = a;
		cldb = cdb;
	}
	public void update(CanvasData c, AnalysisData a, 
			ColorDataBase cdb) {
		cd = c;
		ad = a;
		cldb = cdb;
	}
	public void initialize() {
		initializeTextBoxSetting();
		initializeCheckBox();
		initializeListBoxSetting();
		initializeButtonSetting();
		initializeSoundSetting();
		initializeMenuBar();
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Button >> ------------------------------------
	// -------------------------------------------------------------------------
	public enum ButtonType {
		Initialize(0), Save(1), Strategy(2), Castle(3), Tesuji(4), Kifu(5);
		public final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	public JButton button[] = new JButton[ButtonType.values().length];
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
	public void actionForInitialize() {
		clearTextBox();
		clearCheckBox();
		se.actionForStopEngine();
		cd.cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.resetAllKoma(checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
		ad.clearListBox();
		clearIcons();
		ad.kifuData.clear();
		cd.cv.setLastPoint(-1, -1, false);
		cd.cve.clearBestPointData();
		ad.loadStrategyData();
		ad.loadCastleData();
		ad.loadTesujiData();
		ad.actionForDB((String)comboBox.getSelectedItem());
		ad.countStrategy();
		ad.countCastle();
		ad.countTesujiData();
		ad.createPlayerDataBase();
		cd.cv.repaint();
		cd.cve.repaint();
	}
	public void clearIcons() {
		ad.initializePlayerIcon();
		ad.initializeCastleIcon();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getClickCount() == 2) {
			//System.out.println("mouse double clicked");
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				if(!loadFile.equals("")) {
					ad.loadByNumber(loadFile, loadStep, loadYear);
				}
			}
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonFocusedColor);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonColor);
			}
		}
	}
	// -------------------------------------------------------------------------
	// ----------------------- << ListBox >> ------------------------------------
	// -------------------------------------------------------------------------
	public String loadFile = "";
	public String loadStep = "";
	public String loadYear = "";
	public enum ListBoxType {
		Kifu(0), Info(1), Strategy(2), Player(3), Castle(4), Tesuji(5), Engine(6);
		public final int id;		
		private ListBoxType(final int id) {
			this.id = id;
		}
	};
	public JScrollPane scrollPane[] = new JScrollPane[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	public DefaultListModel<String> listModel[] = new DefaultListModel[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	public JList<String> listBox[] = new JList[ListBoxType.values().length];
	
	public void initializeListBoxSetting() {
		for(ListBoxType lb: ListBoxType.values()) {
			listModel[lb.id] = new DefaultListModel<String>();
			listBox[lb.id] = new JList<String>();
			scrollPane[lb.id] = new JScrollPane();
			listBox[lb.id].setModel(listModel[lb.id]);
			listBox[lb.id].addListSelectionListener(this);
			listBox[lb.id].addMouseListener(this);
			//listBox[lb.id].addMouseMotionListener(this);
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
	public void getLoadNumberOnListBox2() {
		int index = listBox[ListBoxType.Info.id].getSelectedIndex();
		if(index < 2) return;
		String str = listModel[ListBoxType.Info.id].getElementAt(listBox[ListBoxType.Info.id].getSelectedIndex());
		String subStrFile = str.substring(2,5);
		String subStrStep = str.substring(6,9);
		String subStrYear = str.substring(10,14);
		loadFile = subStrFile;
		loadStep = subStrStep;
		loadYear = subStrYear;
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				ad.commonListAction();
				soundKoma();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				ad.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				ad.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				ad.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				ad.updateListBoxInfoByTesuji();
			}
		}
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getValueIsAdjusting()) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				ad.commonListAction();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				ad.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				ad.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				ad.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				ad.updateListBoxInfoByTesuji();
			}
		}
	}
	// -------------------------------------------------------------------------
	// ----------------------- << TextBox >> -----------------------------------
	// -------------------------------------------------------------------------
	public enum TextBoxType {
		Player1(0), Player2(1), Strategy(2), Tesuji(3), Castle(4);
		public final int id;
		private TextBoxType(final int id) {
			this.id = id;
		}
	};
	public JTextField textBox[] = new JTextField[TextBoxType.values().length];
	
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
	private ActionListener enterActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ad.updatePlayerIcon();
        }
    };
	// -------------------------------------------------------------------------
	// ----------------------- << CheckBox >> ------------------------------------
	// -------------------------------------------------------------------------
    public enum CheckBoxType {
		Edit(0), Reverse(1), Draw(2), Arrow(3), Engine(4);
		public final int id;
		private CheckBoxType(final int id) {
			this.id = id;
		}
	};
	public JCheckBox checkBox[] = new JCheckBox[CheckBoxType.values().length];
	public JRadioButton radioButtonSente = new JRadioButton("Sente", true);
	public JRadioButton radioButtonGote = new JRadioButton("Gote");
	public JComboBox<String> comboBox;
	
	public void clearCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id].setSelected(false);
		}
		radioButtonSente.setSelected(true);
	}
	public void initializeCheckBox() {
		for(CheckBoxType cb: CheckBoxType.values()) {
			checkBox[cb.id] = new JCheckBox(cb.name());
		}
		checkBox[CheckBoxType.Draw.id].setBounds(baseXPosForItems+80, 35, 80, 12);
		checkBox[CheckBoxType.Edit.id].setBounds(baseXPosForItems+140, 35, 60, 12);
		checkBox[CheckBoxType.Reverse.id].setBounds(baseXPosForItems+190, 35, 80, 12);
		checkBox[CheckBoxType.Reverse.id].addActionListener(checkActionListenerForReverse);
		checkBox[CheckBoxType.Arrow.id].setBounds(baseXPosForItems+190, 50, 80, 12);
		checkBox[CheckBoxType.Arrow.id].addActionListener(checkActionListenerForArrow);
		checkBox[CheckBoxType.Engine.id].setBounds(baseXPosForItems+80, 50, 80, 12);
		checkBox[CheckBoxType.Engine.id].addActionListener(checkActionListenerForEngine);
		radioButtonSente.setBounds(baseXPosForItems+360, 75, 70, 14);
		radioButtonGote.setBounds(baseXPosForItems+420, 75, 70, 14);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonSente);
		buttonGroup.add(radioButtonGote);
		comboBox = new JComboBox<>();
		createCheckBoxList();
		comboBox.setBounds(baseXPosForItems+80, 8, 100, 25);
	}
	public void createCheckBoxList() {
		File dir = new File("./kifu/");
		comboBox.addItem("");
		File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        File file = files[i];
	        String[] names = file.toString().split("/");
	        if(file.isDirectory()) comboBox.addItem(names[names.length-1]);
	    }
	    comboBox.addItem("all");
	}
	
	private ActionListener checkActionListenerForReverse = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	for(Koma k: sd.k) {
        		k.reverseForReverseMode();
        	}
        	sd.viewKomaOnBoard(checkBox[CheckBoxType.Reverse.id].isSelected());
        	sd.viewKomaOnHand(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cd.cv.reverseNumberRowCol(checkBox[CheckBoxType.Reverse.id].isSelected());
        	cd.cv.repaint();
        	cd.cve.repaint();
        }
    };
    private ActionListener checkActionListenerForArrow = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	cd.cv.repaint();
        }
    };
    private ActionListener checkActionListenerForEngine = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        	if(checkBox[CheckBoxType.Engine.id].isSelected()) {
        		se.actionForStartEngine();
        	} else {
        		se.actionForStopEngine();
        	}
        }
    };
    // -------------------------------------------------------------------------
    // ----------------------- << Sound >> -----------------------------------
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
    // ----------------------- << Menu >> -----------------------------------
    // -------------------------------------------------------------------------
	String imgFilePath = "./img/";
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
		CaptureBoard(0), KomaInHand(1), ImportKIF(2);
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
		
		fr.setJMenuBar(menuBar);
	}
	public void actionForCaptureBoard() {
		try {
			Rectangle bounds = fr.getBounds();
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(bounds);
			image = image.getSubimage(0, 50, (sd.iconWidth+10)*11+55, (sd.iconHeight+10)*9+40);
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
		cd.cv.repaint();
		cd.cve.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getActionCommand() == button[ButtonType.Initialize.id].getText()) {
			actionForInitialize();
		}
		if(e.getActionCommand() == button[ButtonType.Save.id].getText()) {
			ad.actionForSave();
		}
		if(e.getActionCommand() == button[ButtonType.Strategy.id].getText()) {
			ad.actionForStrategy(sd, textBox[TextBoxType.Strategy.id].getText());
		}
		if(e.getActionCommand() == button[ButtonType.Castle.id].getText()) {
			ad.actionForCastle(sd, radioButtonSente.isSelected());
		}
		if(e.getActionCommand() == button[ButtonType.Tesuji.id].getText()) {
			ad.actionForTesuji(loadFile);
		}
		if(e.getActionCommand() == button[ButtonType.Kifu.id].getText()) {
			ad.actionForKifu();
		}
		if(e.getSource() == menuItemSetting[MenuTypeSetting.SetColor.id]) {
			cldb.actionForSetColor(fr);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StartEngine.id]) {
			se.actionForStartEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StopEngine.id]) {
			se.actionForStopEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.SetEngine.id]) {
			ep.setPropertyForEngine(fr);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.KifuAnalysis.id]) {
			ad.actionForKifuAnalysis();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.CaptureBoard.id]) {
			actionForCaptureBoard();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.KomaInHand.id]) {
			actionForKomaInHand();
		}
		if(e.getSource() == menuItemUtility[MenuTypeUtility.ImportKIF.id]) {
			ad.importKIF();
		}
	}
}