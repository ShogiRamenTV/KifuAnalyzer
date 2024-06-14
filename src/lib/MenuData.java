package lib;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import lib.CheckBoxData.CheckBoxType;

public class MenuData implements ActionListener{
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
	JFrame fr;
	ColorDataBase cldb;
	ShogiEngine se;
	ShogiData sd;
	CanvasBoard cv;
	CanvasBoardForEngine cve;
	ListBoxData lbd;
	CheckBoxData cbd;
	EditProperty ep;
	KifuDataBase kdb;
	
	public MenuData(JFrame f, ColorDataBase clb, ShogiEngine sen, ShogiData s,
			CanvasBoard c, CanvasBoardForEngine ce, ListBoxData ld, CheckBoxData cd,
			EditProperty e, KifuDataBase kb) {
		fr = f;
		cldb = clb;
		se = sen;
		sd = s;
		cv = c;
		cve = ce;
		lbd = ld;
		cbd = cd;
		ep = e;
		kdb = kb;
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
		
		fr.setJMenuBar(menuBar);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Menu Action >> -----------------------------
	// -------------------------------------------------------------------------
	public void actionForCaptureBoard() {
		try {
			Rectangle bounds = fr.getBounds();
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
		sd.putAllKomaInHand(cbd.checkBox[CheckBoxType.Reverse.id].isSelected(),cbd.radioButtonSente.isSelected());
		sd.viewKomaOnBoard(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		cv.repaint();
		cve.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getSource() == menuItemSetting[MenuTypeSetting.SetColor.id]) {
			cldb.actionForSetColor(fr);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StartEngine.id]) {
			se.actionForStartEngine(fr, sd, cv, cve, lbd.listModel, lbd.listBox);
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.StopEngine.id]) {
			se.actionForStopEngine();
		}
		if(e.getSource() == menuItemEngine[MenuTypeEngine.SetEngine.id]) {
			ep.setPropertyForEngine(fr);
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
}