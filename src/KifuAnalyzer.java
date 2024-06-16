import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import lib.AnalysisData;
import lib.AnalysisData.Kifu;
import lib.CanvasData;
import lib.ColorDataBase;
import lib.ColorDataBase.ButtonType;
import lib.EditProperty;
import lib.GUIData;
import lib.GUIData.CheckBoxType;
import lib.GUIData.ListBoxType;
import lib.GUIData.TextBoxType;
import lib.ShogiData;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
import lib.ShogiEngine;

public class KifuAnalyzer extends JFrame implements MouseListener, MouseMotionListener, KeyListener {
	// -------------------------------------------------------------------------
	// ----------------------- << Global Variables >> --------------------------
	// -------------------------------------------------------------------------
	String imgFilePath = "./img/";
	String imgFilePathKoma = imgFilePath + "koma/";
	
	ShogiData sd = new ShogiData();
	ShogiData sdForKDB = new ShogiData();
	ShogiEngine se;
	CanvasData cd;
	EditProperty ep = new EditProperty();
	AnalysisData ad;
	ColorDataBase cldb;
	GUIData gd;
	private final int baseXPosForItems = 720;
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
		gd.actionForInitialize();
	}
	public void initializeAppIcon() {
		ImageIcon icon = new ImageIcon(imgFilePath + "Shogi Ramen TV.jpg");
		setIconImage(icon.getImage());
	}
	public void initializeGUISetting() {
		sd.initializeKomaSetting();
		sdForKDB.initializeKomaSetting();
		se = new ShogiEngine(this, sd, ep, gd, cd, ad);
		gd = new GUIData(baseXPosForItems, this, sd, se, ep, cd, ad, cldb);
		cd = new CanvasData(baseXPosForItems, this.getWidth(), this.getHeight(), sd, se, gd);
		cldb = new ColorDataBase(ep, cd, gd.button);
		ad = new AnalysisData(this, sd, sdForKDB, se, cd, gd);
		gd.update(cd, ad, cldb);	// mutual referense
		se.update(gd, cd, ad);		// mutual referense
		gd.initialize();
		cd.initialize();
		cldb.initializeColorSet();
	}
	public void contentPaneSetting() {
		getContentPane().setLayout(null);
		for(ButtonType b: ButtonType.values()) getContentPane().add(gd.button[b.id]);
		for(TextBoxType t: TextBoxType.values()) getContentPane().add(gd.textBox[t.id]);
		for(ListBoxType lb: ListBoxType.values()) getContentPane().add(gd.scrollPane[lb.id]);
		for(CheckBoxType cb: CheckBoxType.values()) getContentPane().add(gd.checkBox[cb.id]);
		getContentPane().add(gd.radioButtonSente);
		getContentPane().add(gd.radioButtonGote);
		getContentPane().add(gd.comboBox);
		getContentPane().add(cd.cve);
		getContentPane().add(cd.cv);
	}
	public void listenerSetting() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		cd.cv.addMouseListener(this);
		cd.cv.addMouseMotionListener(this);
		cd.cv.addKeyListener(this);
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Ctrl+v >> ------------------------------------
	// -------------------------------------------------------------------------
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
			ad.importShogiWarsKifu();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.out.println("Key Released");
		commandKeyOn = false;
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
			if(e.getSource() != cd.cv) mp.y -= 50;
			if(cd.cv.drawListTargetRightClick.size() != 0 ) {
				cd.cv.drawListTargetRightClick.remove(cd.cv.drawListTargetRightClick.size()-1);
			}
			cd.cv.drawListTargetRightClick.add(mp);
			cd.cv.repaint();
			cd.cve.repaint();
		}
		
		if(sd.selectedKoma == null) return;
		
		Point mp = e.getPoint();
		for(ListBoxType lb: ListBoxType.values()) {
			if(e.getSource() == gd.listBox[lb.id]) {
				mp.x += gd.scrollPane[lb.id].getBounds().x;
				mp.y += gd.scrollPane[lb.id].getBounds().y + sd.iconHeight/2 + 20;
				break;
			}
		}
		if(e.getSource() != cd.cv) mp.y -= 50;
		sd.selectedKoma.pos.x = mp.x - sd.iconWidth/2;
		sd.selectedKoma.pos.y = mp.y - sd.iconHeight/2;
		cd.cv.repaint();
		cd.cve.repaint();
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
			if(e.getSource() != cd.cv) return;
			Point mp = e.getPoint();
			if(mp.x < 80 || mp.y < 20 || mp.x > (sd.iconWidth+10)*9+80 || mp.y > (sd.iconHeight+10)*9+20) {
				return;
			}
			Point pShogiXY = cd.cv.convertMousePointToShogiXY(mp);
			for(Point p: cd.cv.drawListLeftClick) {
				Point pXY = cd.cv.convertMousePointToShogiXY(p);
				if(pXY.x == pShogiXY.x && pXY.y == pShogiXY.y) {
					cd.cv.drawListLeftClick.remove(p);
					cd.cv.repaint();
					cd.cve.repaint();
					return;
				}
			}
			cd.cv.drawListLeftClick.add(mp);
			cd.cv.repaint();
			cd.cve.repaint();
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3) { // right click
			//System.out.println("mouse clicked");
			Point mp = e.getPoint();
			if(e.getSource() != cd.cv) mp.y -= 50;
			cd.cv.drawListBaseRightClick.add(mp);
			return;
		}
		if(e.getSource() != this && e.getSource() != cd.cv) {
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
		if(e.getSource() != cd.cv) {
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
		if(sd.turnIsSente && k.sente == 1 && !gd.checkBox[CheckBoxType.Edit.id].isSelected()) {
			sd.selectedKoma = null;
			return;
		}
		if(!sd.turnIsSente && k.sente == 0 && !gd.checkBox[CheckBoxType.Edit.id].isSelected()) {
			sd.selectedKoma = null;
			return;
		}
		sd.selectedKoma = k;
		if(isOnBoard) cd.cv.mousePressed = true;
		cd.cv.repaint();
		cd.cve.repaint();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(sd.selectedKoma != null) {
			releaseKoma();
		} 
		//System.out.println("mouse released");
		if(e.getButton() != MouseEvent.BUTTON3) return;
		Point mp = e.getPoint();
		if(e.getSource() != cd.cv) mp.y -= 50;
		Point p = cd.cv.drawListBaseRightClick.get(cd.cv.drawListBaseRightClick.size()-1);
		if(p.x == mp.x && p.y == mp.y) cd.cv.drawListBaseRightClick.remove(cd.cv.drawListBaseRightClick.size()-1);
		else {
			cd.cv.drawListTargetRightClick.add(mp);
			cd.cv.repaint();
			cd.cve.repaint();
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
		if(gd.checkBox[CheckBoxType.Reverse.id].isSelected()) {
			X = 10 - (9-x);
			Y = 10 - y;
		} else {
			X = (9-x);
			Y = y;
		}
		Boolean result = selectedKoma.moveKoma(X, Y, -1);
		gd.soundKoma();
		sd.viewKomaOnBoard(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		
		cd.cv.mousePressed = false;
		cd.cv.repaint();
		cd.cve.repaint();
		
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
				cd.cv.setLastPoint(X, Y, true);
				ad.updateListBox(type, X, Y, preX, preY, sente, promoted, preP, drop);
			}
			Kifu kf = ad.createKifu(selectedKoma, X, Y, promoted, preP, drop);
			ad.kifuData.add(kf);
			ad.checkKDB(gd.listModel[ListBoxType.Kifu.id].size()-1);
			if(!gd.checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
			se.sendCommandToEngine();
		}
		
		// check strategy
		if(gd.textBox[TextBoxType.Strategy.id].getText().equals("")) {
			gd.textBox[TextBoxType.Strategy.id].setText(ad.checkStrategy(sd));
		}
		if(gd.textBox[TextBoxType.Castle.id].getText().equals("")) {
			gd.textBox[TextBoxType.Castle.id].setText(ad.checkCastle(sd, true));
		}
		if(gd.textBox[TextBoxType.Castle.id].getText().equals("")) {
			gd.textBox[TextBoxType.Castle.id].setText(ad.checkCastle(sd, false));
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
