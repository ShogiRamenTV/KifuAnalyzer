package lib;

import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;

//-------------------------------------------------------------------------
// ----------------------- << Kifu Data >> -----------------------------
// -------------------------------------------------------------------------
public class KifuDataBase {
	public String kifuFilePath = "./kifu/";
	public List<Kifu> kifuData = new ArrayList<Kifu>();
	public List<KifuData> kifuDB = new ArrayList<KifuData>();
	
	JFrame fr;
	ShogiData sd;
	ShogiData sdForKDB;
	DefaultListModel<String> listModelInfo;
	JList<String> listBoxInfo;
	DefaultListModel<String> listModelKifu;
	JList<String> listBoxKifu;
	DefaultListModel<String> listModelEngine;
	JList<String> listBoxEngine;
	CanvasBoard cv;
	JCheckBox checkBoxReverse;
	JCheckBox checkBoxDraw;
	JCheckBox checkBoxEdit;
	JTextField textBoxPlayer1;
	JTextField textBoxPlayer2;
	JTextField textBoxStrategy;
	JTextField textBoxCastle;
	PlayerDataBase pdb;
	CastleDataBase cdb;
	StrategyDataBase sdb;
	ShogiEngine engine;
	CanvasBoardForEngine cve;
	public KifuDataBase(JFrame f, ShogiData s, ShogiData sbb, CanvasBoard c, 
			DefaultListModel<String> lmI, JList<String> lbI, 
			DefaultListModel<String> lmK, JList<String> lbK,
			DefaultListModel<String> lmE, JList<String> lbE,
			JCheckBox cR, JCheckBox cD, JCheckBox cE,
			JTextField tP1, JTextField tP2, JTextField tS, JTextField tC, 
			PlayerDataBase pb, CastleDataBase cb, StrategyDataBase sb,
			ShogiEngine se, CanvasBoardForEngine ce) 
	{
		fr = f;
		sd = s;
		sdForKDB = sbb;
		cv = c;
		listModelInfo = lmI;
		listBoxInfo = lbI;
		listModelKifu = lmK;
		listBoxKifu = lbK;
		listModelEngine = lmE;
		listBoxEngine = lbE;
		checkBoxReverse = cR;
		checkBoxDraw = cD;
		checkBoxEdit = cE;
		textBoxPlayer1 = tP1;
		textBoxPlayer2 = tP2;
		textBoxStrategy = tS;
		textBoxCastle = tC;
		pdb = pb;
		cdb = cb;
		sdb = sb;
		engine = se;
		cve = ce;
	}
	public class KifuData {
		public List<Kifu> db = new ArrayList<Kifu>();
		public String playerName[] = new String[2];
		public String strategyName;
		public String castleName[] = new String[2];
		public String year;
		public int index;
		public int isSenteWin;
		
		public KifuData() {
			strategyName = "";
			for(SenteGote sg: SenteGote.values()) castleName[sg.id] = "";
		}
	}

	public class Kifu {
		public Koma k;
		public int x;
		public int y;
		public int p;
		public int pp;
		public int d;
		
		public Kifu(Koma koma, int px, int py, int promote, int preP, int drop) {
			k = koma;
			x = px;
			y = py;
			p = promote;
			pp = preP;
			d = drop;
		}
	}
	public void update(PlayerDataBase pb, CastleDataBase cb, StrategyDataBase sb,
			ShogiEngine se, CanvasBoardForEngine ce) {
		pdb = pb;
		cdb = cb;
		sdb = sb;
		engine = se;
		cve = ce;
	}
	public void actionForKifu() {
		int index = listBoxKifu.getSelectedIndex();
		listModelInfo.clear();
		listBoxInfo.setModel(listModelInfo);
		listModelInfo.addElement("<Kifus of Same Position>");
		listModelInfo.addElement("-------------");
		
		for(KifuData kd: kifuDB) {
			int i=0;
			Boolean isSame = true;
			// check same moves
			while(i<index && i<kd.db.size()) {
				if( kifuData.get(i).k.type != kd.db.get(i).k.type ||
					kifuData.get(i).x != kd.db.get(i).x || 
					kifuData.get(i).y != kd.db.get(i).y || 
					kifuData.get(i).p != kd.db.get(i).p ) {
					// check same position if moves were different
					isSame = checkSamePositionKDB(index, kd);
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
				listModelInfo.addElement(str);
			}
		}
		
		listBoxInfo.setModel(listModelInfo);
	}
	public void actionForKifuAnalysis() {
		cve.clearBestPointData();
		checkBoxEdit.setSelected(false);
		listBoxKifu.setSelectedIndex(0);
		listBoxKifu.ensureIndexIsVisible(0);
		commonListAction();
		MyThreadKifuAnalysis thread = new MyThreadKifuAnalysis();
		engine.actionForStartEngine(fr, sd, cv, cve, listModelEngine, listBoxEngine, 
				listModelKifu, listBoxKifu);
		if(!engine.isEngineOn) {
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
			int calcTimeMs = engine.getCalculatingTimeOfEngine();
			while(isUnderAnalysis && engine.isEngineOn) {
				int index = listBoxKifu.getSelectedIndex();
				int size = listModelKifu.getSize();
				if(size-1 == index) isUnderAnalysis = false;
				try {
					Thread.sleep(calcTimeMs);
				} catch(InterruptedException e) {
					System.out.println(e);
				}
				listBoxKifu.setSelectedIndex(index+1);
				listBoxKifu.ensureIndexIsVisible(index+1);
				commonListAction();
			}
			System.out.println("completed.");
			engine.actionForStopEngine();
		}
	}
	public Kifu createKifu(Koma koma, int px, int py, int promote, int preP, int drop) {
		Kifu k = new Kifu(koma, px, py, promote, preP, drop);
		return k;
	}
	public KifuData createKifuData() {
		KifuData kd = new KifuData();
		return kd;
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
			fw.write(textBoxPlayer1.getText() + "\n");
			fw.write(textBoxPlayer2.getText() + "\n");
			for(Kifu kf: kifuData) fw.write(kf.k.index + "," + kf.x + "," + kf.y + "," + kf.p + "," + kf.pp + "," + kf.d + "\n");
			if(checkBoxDraw.isSelected()) fw.write("-1");
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void clearListBox() {
		listModelKifu.clear();
		listModelKifu.addElement("--------");
		listBoxKifu.setModel(listModelKifu);
		listBoxKifu.setSelectedIndex(0);
	}
	public void updateListBox(KomaType type, int x, int y, int preX, int preY, int sente, int promoted, int preP, int drop) {
		// remove items under selected item 
		int selectedIndex = listBoxKifu.getSelectedIndex();
		if(selectedIndex != -1 && selectedIndex <= listModelKifu.size()-1) {
			int index = listModelKifu.size()-1;
			while(index > selectedIndex) {
				listModelKifu.remove(index);
				kifuData.remove(index-1);
				index--;
			}
		}
		
		// add new item
		String s = sd.createMoveKomaName(type, sente, x, y, preX, preY, promoted, preP, drop);
		s = listModelKifu.size() + ":"+s;
		listModelKifu.addElement(s);
		listBoxKifu.setModel(listModelKifu);
		listBoxKifu.ensureIndexIsVisible(listModelKifu.size()-1);
		listBoxKifu.setSelectedIndex(listModelKifu.size()-1);
		
		cv.clearDrawPointForRightClick();
	}
	public void commonListAction() {
		int selectedIndex = listBoxKifu.getSelectedIndex();
		sd.resetAllKoma(checkBoxReverse.isSelected());
		sd.viewKomaOnBoard(checkBoxReverse.isSelected());
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(kf.x, kf.y, kf.p);
				if(!checkBoxEdit.isSelected()) sd.turnIsSente = !sd.turnIsSente;
				cv.setLastPoint(kf.x, kf.y, true);
				if(textBoxStrategy.getText().equals("")) textBoxStrategy.setText(sdb.checkStrategy(sd));
				if(textBoxCastle.getText().equals("")) {
					textBoxCastle.setText(cdb.checkCastle(sd, true));
				}
				if(textBoxCastle.getText().equals("")) {
					textBoxCastle.setText(cdb.checkCastle(sd, false));
				}
			}
		}
		if(selectedIndex == 0) cv.setLastPoint(-1, -1, false);
		cv.clearDrawPointForRightClick();
		
		checkKDB(selectedIndex);
		sd.viewKomaOnBoard(checkBoxReverse.isSelected());
		sd.viewKomaOnHand(checkBoxReverse.isSelected());

		engine.sendCommandToEngine();
		cv.repaint();
		cve.repaint();
	}
	public void loadByNumber(String numStrFile, String numStrStep, String numStrYear) {
		String fileName;
		if(numStrFile.equals("")) {
			Path path = Paths.get("").toAbsolutePath();
			FileDialog fd = new FileDialog(fr, "Load", FileDialog.LOAD);
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
			textBoxPlayer1.setText(br.readLine());
			textBoxPlayer2.setText(br.readLine());
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
					Kifu kf = createKifu(sd.k[i], x, y, p, pp, d);
					kifuData.add(kf);
					sd.k[i].moveKoma(x, y, p);
				}
			}
			br.close();
			
			sd.resetAllKoma(checkBoxReverse.isSelected());
			if(numStrStep.equals("")) {
				listBoxKifu.setSelectedIndex(0);
				listBoxKifu.ensureIndexIsVisible(0);
			} else {
				int selectedIndex = Integer.parseInt(numStrStep);
				listBoxKifu.setSelectedIndex(selectedIndex);
				listBoxKifu.ensureIndexIsVisible(selectedIndex);
				commonListAction();
			}
			
			sd.viewKomaOnBoard(checkBoxReverse.isSelected());
			sd.viewKomaOnHand(checkBoxReverse.isSelected());
			pdb.updatePlayerIcon();
			cv.repaint();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForDB(String comboBoxStr) {
		kifuDB.clear();
		String selectedYear = comboBoxStr;
		if(selectedYear.equals("") || selectedYear.equals("all")) loadKifuDBByYear("");
		if(selectedYear.equals("2022") || selectedYear.equals("all")) loadKifuDBByYear("2022");
		if(selectedYear.equals("2023") || selectedYear.equals("all")) loadKifuDBByYear("2023");
	}
	public void loadKifuDBByYear(String strY) {
		try {
			System.out.print("Loading Kifu Data(" + strY + ") ... ");
			int fileIndex = 1;
			while(true) {
				sdForKDB.resetAllKoma(checkBoxReverse.isSelected());
				String fileName = kifuFilePath + strY + "/" + "kifu" + String.format("%03d", fileIndex) + ".txt";
				//System.out.println(fileName);
				File file = new File(fileName);
				KifuData kd = createKifuData();
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
						Kifu kf = createKifu(sd.k[i], x, y, p, pp, d);
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
				if(kd.isSenteWin != -1) kd.isSenteWin = isSenteWin(kd);
				kifuDB.add(kd);
				fileIndex++;
			}
		} catch(FileNotFoundException en) {
			//System.out.println(en);
		} catch(IOException er) {
			System.out.println(er);
		}
		
		listBoxKifu.setSelectedIndex(0);
		commonListAction();
		
		System.out.println("Completed.");
	}
	public void initializeShogiBoard() {
		sd.resetAllKoma(checkBoxReverse.isSelected());	
		sd.viewKomaOnBoard(checkBoxReverse.isSelected());
		clearListBox();
		kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cv.clearDrawPoint();
		engine.actionForStopEngine();
		cve.clearBestPointData();
		cv.repaint();
		cve.repaint();
	}
	public KifuData getKDB(int fileIndex, String year) {
		for(KifuData kd: kifuDB) {
			if(kd.index == fileIndex && kd.year.equals(year)) return kd;
		}
		
		return null;
	}
	public void checkKDB(int index) {
		int i;
		Boolean isSame;
		List<StringCount> listSC = new ArrayList<StringCount>();
		
		for(KifuData kd: kifuDB) {
			i=0;
			isSame = true;
			// check same moves
			while(i<index && i<kd.db.size()) {
				if( kifuData.get(i).k.type != kd.db.get(i).k.type ||
						kifuData.get(i).x != kd.db.get(i).x || 
						kifuData.get(i).y != kd.db.get(i).y || 
						kifuData.get(i).p != kd.db.get(i).p ) {
					// check same position if moves were different
					isSame = checkSamePositionKDB(index, kd);
					break;
				}
				i++;
			}
			
			// count next move on kdb
			if(isSame && index < kd.db.size()) {
				countNextMoveOnKDB(listSC, kd, index);
			}
		}
		
		updateListBox2(listSC);
	}
	public void countNextMoveOnKDB(List<StringCount> listSC, KifuData kd, int index) {
		Kifu kf = kd.db.get(index);
		String str = sd.createMoveKomaName(kf.k.type, kf.k.sente, kf.x, kf.y, kf.k.px, kf.k.py, kf.p, kf.pp, kf.d);
		Boolean found = false;
		// count string data if same string
		for(StringCount sc: listSC) {
			if(sc.str.equals(str)) {
				sc.cnt++;
				if(isSenteWin(kd) == 1) sc.senteWinCnt++;
				found = true;
			}
		}
		// add new string data
		if(!found) {
			StringCount sc = new StringCount(str, isSenteWin(kd));
			sc.target = new Point(kf.x, kf.y);
			sc.base = new Point(sd.k[kf.k.index].px, sd.k[kf.k.index].py);
			listSC.add(sc);
		}
	}
	public int isSenteWin(KifuData kd) {
		int index = kd.db.size()-1;
		if((index%2) == 0) return 1;
		return 0;
	}
	public Boolean checkSamePositionKDB(int index, KifuData kd) {
		sdForKDB.resetAllKoma(checkBoxReverse.isSelected());
		
		int i = 0;
		while(i<index && index<kd.db.size()) {
			Kifu kf = kd.db.get(i);
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
	
	
	public void updateListBox2(List<StringCount> listSC) {
		listModelInfo.clear();
		listBoxInfo.setModel(listModelInfo);
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
			listModelInfo.addElement(str);
		}
		listBoxInfo.setModel(listModelInfo);
	}
	
	// -------------------------------------------------------------------------
	// ----------------------- << Shogi Wars Interface >> ----------------------
	// -------------------------------------------------------------------------
    public void importShogiWarsKifu() {
		System.out.print("Importing Kifu ... ");
		String strClipBoard;
		if( (strClipBoard = getClipboardData()) == null ) {
			System.out.println("Failed.");
			return;
		}
		initializeShogiBoard();
		Boolean result = parseShogiWarsKifu(strClipBoard);
		listBoxKifu.setSelectedIndex(0);
		listBoxKifu.ensureIndexIsVisible(0);
		commonListAction();
		pdb.updatePlayerIcon();
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
				textBoxPlayer1.setText(content.substring(content.indexOf("：")+1));
			}
			if(content.contains("後手：")) {
				textBoxPlayer2.setText(content.substring(content.indexOf("：")+1));
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
		kf = createKifu(k, x, y, p, k.promoted, drop);
		return kf;
	}
}