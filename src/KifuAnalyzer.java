import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lib.ButtonData;
import lib.ButtonData.ButtonType;
import lib.CanvasBoard;
import lib.CanvasBoardForEngine;
import lib.CastleDataBase;
import lib.CheckBoxData;
import lib.CheckBoxData.CheckBoxType;
import lib.ColorDataBase;
import lib.EditProperty;
import lib.KifuDataBase;
import lib.KifuDataBase.Kifu;
import lib.KomaSound;
import lib.ListBoxData;
import lib.ListBoxData.ListBoxType;
import lib.MenuData;
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
	ButtonData bd;
	CheckBoxData cbd;
	MenuData md;
	
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
		bd.actionForInitialize();
	}
	public void initializeAppIcon() {
		ImageIcon icon = new ImageIcon(imgFilePath + "Shogi Ramen TV.jpg");
		setIconImage(icon.getImage());
	}
	public void initializeGUISetting() {
		sd.initializeKomaSetting();
		sdForKDB.initializeKomaSetting();
		tbd = new TextBoxData(baseXPosForItems, pdb, lbd);
		tbd.initializeTextBoxSetting();
		cbd = new CheckBoxData(baseXPosForItems, sd, cv, cve);
		cbd.initializeCheckBox();
		lbd = new ListBoxData(baseXPosForItems, se.getNumOfMultiPV(), kdb, pdb, sdb, cdb, tdb, ks);
		lbd.initializeListBoxSetting();
		bd = new ButtonData(baseXPosForItems, tbd, lbd, cldb, cbd, cv, cve, sd, kdb, sdb, cdb, tdb, pdb);
		bd.initializeButtonSetting();
		
		cve = new CanvasBoardForEngine(baseXPosForItems, se, 
				lbd.listModel[ListBoxType.Engine.id], lbd.listBox[ListBoxType.Kifu.id]);
		cv = new CanvasBoard(sd, cbd.checkBox[CheckBoxType.Reverse.id], se, cve);
		kdb = new KifuDataBase(this, sd, sdForKDB, cv, lbd.listModel, lbd.listBox,
				cbd.checkBox[CheckBoxType.Reverse.id], cbd.checkBox[CheckBoxType.Draw.id], cbd.checkBox[CheckBoxType.Edit.id],
				tbd.textBox,
				pdb, cdb, sdb, se, cve);
		sdb = new StrategyDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox,
				kdb, cdb);
		cdb = new CastleDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, 
				cv, kdb, sdb);
		tdb = new TesujiDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, cbd.comboBox, kdb
				);
		pdb = new PlayerDataBase(lbd.listModel, lbd.listBox,
				tbd.textBox, kdb, cv);
		cldb = new ColorDataBase(ep, cv, bd.button);
		md = new MenuData(this, cldb, se, sd, cv, cve, lbd, cbd, ep, kdb);
		
		kdb.update(pdb, cdb, sdb, se, cve);
		sdb.update(kdb, cdb);
		lbd.update(kdb, pdb, sdb, cdb, tdb);
		tbd.update(pdb, lbd);
		bd.update(tbd, lbd, cldb, cbd, cv, cve, sd, kdb, sdb, cdb, tdb, pdb);
		cbd.update(cv, cve);

		cv.initializeSettings(this.getWidth(), this.getHeight());
		cve.initializeSetting();
		ks.initializeSoundSetting();
		md.initializeMenuBar();
		cv.initializeNumberRowCol();
		cldb.initializeColorSet();
	}
	public void contentPaneSetting() {
		getContentPane().setLayout(null);
		for(ButtonType b: ButtonType.values()) getContentPane().add(bd.button[b.id]);
		for(TextBoxType t: TextBoxType.values()) getContentPane().add(tbd.textBox[t.id]);
		for(ListBoxType lb: ListBoxType.values()) getContentPane().add(lbd.scrollPane[lb.id]);
		for(CheckBoxType cb: CheckBoxType.values()) getContentPane().add(cbd.checkBox[cb.id]);
		getContentPane().add(cbd.radioButtonSente);
		getContentPane().add(cbd.radioButtonGote);
		getContentPane().add(cbd.comboBox);
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

	// -------------------------------------------------------------------------
	// ----------------------- << Button Action >> -----------------------------
	// -------------------------------------------------------------------------
	
	@Override
	public void actionPerformed(ActionEvent e) {
	
	}
    
    
 
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
		if(sd.turnIsSente && k.sente == 1 && !cbd.checkBox[CheckBoxType.Edit.id].isSelected()) {
			sd.selectedKoma = null;
			return;
		}
		if(!sd.turnIsSente && k.sente == 0 && !cbd.checkBox[CheckBoxType.Edit.id].isSelected()) {
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
		if(cbd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
			X = 10 - (9-x);
			Y = 10 - y;
		} else {
			X = (9-x);
			Y = y;
		}
		Boolean result = selectedKoma.moveKoma(X, Y, -1);
		ks.soundKoma();
		sd.viewKomaOnBoard(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		
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
			if(!cbd.checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
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
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
