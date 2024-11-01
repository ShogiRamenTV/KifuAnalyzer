package lib;

import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import lib.GUIData.CheckBoxType;
import lib.GUIData.ListBoxType;
import lib.GUIData.TextBoxType;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;
import lib.ShogiData.SenteGote;
// -------------------------------------------------------------------------
// ------------ << Kifu, Strategy, Castle, Player, Tesuji >> ---------------
// -------------------------------------------------------------------------
public class AnalysisData {
	JFrame fr;
	ShogiData sd;
	ShogiData sdForKDB;
	ShogiEngine se;
	CanvasData cd;
	GUIData gd;
	
	public AnalysisData(JFrame f, ShogiData s, ShogiData sdKDB, ShogiEngine sen, CanvasData c, GUIData g) {
		fr = f;
		sd = s;
		sdForKDB = sdKDB;
		se = sen;
		cd = c;
		gd = g;
	}
	public class StringCount {
		public String str;
		public int cnt;
		public Point target;
		public Point base;
		int index;
		public int senteWinCnt;
		public StringCount(String s, int isSenteWin) {
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
	public StringCount createStringCount(String s, int senteWin) {
		StringCount sc = new StringCount(s, senteWin);
		return sc;
	}
	// -------------------------------------------------------------------------
	// ----------------------- << Kifu Data >> -------------------------------
	// -------------------------------------------------------------------------
	public String kifuFilePath = "./kifu/";
	public List<Kifu> kifuData = new ArrayList<Kifu>();
	public List<KifuData> kifuDB = new ArrayList<KifuData>();
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
	public void actionForKifu() {
		int index = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
		gd.listModel[ListBoxType.Info.id].addElement("<Kifus of Same Position>");
		gd.listModel[ListBoxType.Info.id].addElement("-------------");
		
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
				String str = String.format("%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				gd.listModel[ListBoxType.Info.id].addElement(str);
			}
		}
		
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
	}
	public void actionForKifuAnalysis() {
		cd.cve.clearBestPointData();
		gd.checkBox[CheckBoxType.Edit.id].setSelected(false);
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		commonListAction();
		MyThreadKifuAnalysis thread = new MyThreadKifuAnalysis();
		se.actionForStartEngine();
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
				int index = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
				int size = gd.listModel[ListBoxType.Kifu.id].getSize();
				if(size-1 == index) isUnderAnalysis = false;
				try {
					Thread.sleep(calcTimeMs);
				} catch(InterruptedException e) {
					System.out.println(e);
				}
				gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(index+1);
				gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(index+1);
				commonListAction();
			}
			System.out.println("completed.");
			se.actionForStopEngine();
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
			fw.write(gd.textBox[TextBoxType.Player1.id].getText() + "\n");
			fw.write(gd.textBox[TextBoxType.Player2.id].getText() + "\n");
			for(Kifu kf: kifuData) fw.write(kf.k.index + "," + kf.x + "," + kf.y + "," + kf.p + "," + kf.pp + "," + kf.d + "\n");
			if(gd.checkBox[CheckBoxType.Draw.id].isSelected()) fw.write("-1");
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void clearListBox() {
		gd.listModel[ListBoxType.Kifu.id].clear();
		gd.listModel[ListBoxType.Kifu.id].addElement("--------");
		gd.listBox[ListBoxType.Kifu.id].setModel(gd.listModel[ListBoxType.Kifu.id]);
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
	}
	public void updateListBox(KomaType type, int x, int y, int preX, int preY, int sente, int promoted, int preP, int drop) {
		// remove items under selected item 
		int selectedIndex = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
		if(selectedIndex != -1 && selectedIndex <= gd.listModel[ListBoxType.Kifu.id].size()-1) {
			int index = gd.listModel[ListBoxType.Kifu.id].size()-1;
			while(index > selectedIndex) {
				gd.listModel[ListBoxType.Kifu.id].remove(index);
				kifuData.remove(index-1);
				index--;
			}
		}
		
		// add new item
		String s = sd.createMoveKomaName(type, sente, x, y, preX, preY, promoted, preP, drop);
		s = gd.listModel[ListBoxType.Kifu.id].size() + ":"+s;
		gd.listModel[ListBoxType.Kifu.id].addElement(s);
		gd.listBox[ListBoxType.Kifu.id].setModel(gd.listModel[ListBoxType.Kifu.id]);
		gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(gd.listModel[ListBoxType.Kifu.id].size()-1);
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(gd.listModel[ListBoxType.Kifu.id].size()-1);
		
		cd.cv.clearDrawPointForRightClick();
	}
	public void commonListAction() {
		int selectedIndex = gd.listBox[ListBoxType.Kifu.id].getSelectedIndex();
		sd.resetAllKoma(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnBoard(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		
		for(Kifu kf: kifuData) {
			if(kifuData.indexOf(kf) < selectedIndex) {
				kf.k.moveKoma(kf.x, kf.y, kf.p);
				if(!gd.checkBox[CheckBoxType.Edit.id].isSelected()) sd.turnIsSente = !sd.turnIsSente;
				cd.cv.setLastPoint(kf.x, kf.y, true);
				if(gd.textBox[TextBoxType.Strategy.id].getText().equals("")) gd.textBox[TextBoxType.Strategy.id].setText(checkStrategy(sd));
				if(gd.textBox[TextBoxType.Castle.id].getText().equals("")) {
					gd.textBox[TextBoxType.Castle.id].setText(checkCastle(sd, true));
				}
				if(gd.textBox[TextBoxType.Castle.id].getText().equals("")) {
					gd.textBox[TextBoxType.Castle.id].setText(checkCastle(sd, false));
				}
			}
		}
		if(selectedIndex == 0) cd.cv.setLastPoint(-1, -1, false);
		cd.cv.clearDrawPointForRightClick();
		
		checkKDB(selectedIndex);
		sd.viewKomaOnBoard(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(gd.checkBox[CheckBoxType.Reverse.id].isSelected());

		se.sendCommandToEngine();
		cd.cv.repaint();
		cd.cve.repaint();
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
			gd.textBox[TextBoxType.Player1.id].setText(br.readLine());
			gd.textBox[TextBoxType.Player2.id].setText(br.readLine());
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
			
			sd.resetAllKoma(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
			if(numStrStep.equals("")) {
				gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
				gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
			} else {
				int selectedIndex = Integer.parseInt(numStrStep);
				gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(selectedIndex);
				gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(selectedIndex);
				commonListAction();
			}
			
			sd.viewKomaOnBoard(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
			sd.viewKomaOnHand(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
			updatePlayerIcon();
			cd.cv.repaint();
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void actionForDB(String comboBoxStr) {
		kifuDB.clear();
		String selectedYear = comboBoxStr;
		if(selectedYear.equals("all")) actionForDBAll();
		else loadKifuDBByYear(comboBoxStr);
	}
	public void actionForDBAll() {
		loadKifuDBByYear("");
		File dir = new File("./kifu/");
		File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        File file = files[i];
	        String[] names = file.toString().split("/");
	        if(file.isDirectory()) loadKifuDBByYear(names[names.length-1]);
	    }
	}
	public void loadKifuDBByYear(String strY) {
		try {
			System.out.print("Loading Kifu Data(" + strY + ") ... ");
			int fileIndex = 1;
			while(true) {
				sdForKDB.resetAllKoma(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
				String fileName = kifuFilePath + strY + "/" + "kifu" + String.format("%03d", fileIndex) + ".txt";
				// System.out.println(fileName);
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
							kd.strategyName = checkStrategy(sdForKDB);
						}
						if(kd.castleName[SenteGote.Sente.id].equals("")) {
							kd.castleName[SenteGote.Sente.id] = checkCastle(sdForKDB, true);
						}
						if(kd.castleName[SenteGote.Gote.id].equals("")) {
							kd.castleName[SenteGote.Gote.id] = checkCastle(sdForKDB, false);
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
		
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		commonListAction();
		
		System.out.println("Completed.");
	}
	public void initializeShogiBoard() {
		sd.resetAllKoma(gd.checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		clearListBox();
		kifuData.clear();
		cd.cv.setLastPoint(-1, -1, false);
		cd.cv.clearDrawPoint();
		se.actionForStopEngine();
		cd.cve.clearBestPointData();
		cd.cv.repaint();
		cd.cve.repaint();
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
		sdForKDB.resetAllKoma(gd.checkBox[CheckBoxType.Reverse.id].isSelected());
		
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
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
		cd.cv.clearDrawPoint();
		
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
			cd.cv.addDrawPoint(sc.target,  sc.base);
			gd.listModel[ListBoxType.Info.id].addElement(str);
		}
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
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
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
		commonListAction();
		updatePlayerIcon();
		if(result) System.out.println("Completed.");
		else System.out.println("Failed.");
	}
    public void importKIF() {
    	System.out.print("Importing Kifu ... ");
		FileDialog fd = new FileDialog(fr, "Load", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd.getFile() == null) return;
		String fileName = fd.getDirectory() + fd.getFile();
		Path path = Paths.get(fileName);
		String strData = "";
		try {
			strData = Files.readString(path);
		} catch(IOException ex) {
			System.out.println("Import (" + fileName + ") error.");
			return;
		}
		initializeShogiBoard();
		Boolean result = parseShogiWarsKifu(strData);
		gd.listBox[ListBoxType.Kifu.id].setSelectedIndex(0);
		gd.listBox[ListBoxType.Kifu.id].ensureIndexIsVisible(0);
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
				gd.textBox[TextBoxType.Player1.id].setText(content.substring(content.indexOf("：")+1));
			}
			if(content.contains("後手：")) {
				gd.textBox[TextBoxType.Player2.id].setText(content.substring(content.indexOf("：")+1));
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
	// -------------------------------------------------------------------------
	// ----------------------- << StrategyData >> ------------------------------
	// -------------------------------------------------------------------------
	public String strategyFilePathBase = "./strategy/";
	public String strategyFilePath = "strategy/";
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
	public void actionForStrategy(ShogiData sd, String strStrategy) {
		Path path;
		String fileName;
		int index = 1;
		
		if(strStrategy.equals("")) {
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
		
			fw.write(strStrategy + "\n");
			for(int i=0; i<40; i++) {
				fw.write(sd.k[i].px + "," + sd.k[i].py + "\n");
			}
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
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
		gd.listModel[ListBoxType.Strategy.id].clear();
		gd.listBox[ListBoxType.Strategy.id].setModel(gd.listModel[ListBoxType.Strategy.id]);
		strategyCountData.clear();
		
		for(KifuData kd: kifuDB) {
			Boolean found = false;
			for(StringCount sc: strategyCountData) {
				if(sc.str.equals(kd.strategyName)) {
					sc.cnt++;
					if(kd.isSenteWin == 1) sc.senteWinCnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kd.strategyName, kd.isSenteWin);
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
		gd.listModel[ListBoxType.Strategy.id].addElement(str);
		gd.listModel[ListBoxType.Strategy.id].addElement("----------");
		for(StringCount sc: strategyCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			gd.listModel[ListBoxType.Strategy.id].addElement(str);
		}
		gd.listBox[ListBoxType.Strategy.id].setModel(gd.listModel[ListBoxType.Strategy.id]);
	}
	public void updateListBox2ByStrategy() {
		int selectedIndex = gd.listBox[ListBoxType.Strategy.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndex4 = gd.listBox[ListBoxType.Player.id].getSelectedIndex();
		String playerName = "";
		if(selectedIndex4 >= 2) {
			playerName = gd.listModel[ListBoxType.Player.id].getElementAt(selectedIndex4);
			//System.out.println(playerName);
		}
		int selectedIndexCastle = gd.listBox[ListBoxType.Castle.id].getSelectedIndex();
		String castleName = "";
		if(selectedIndexCastle >= 2) {
			StringCount sc = castleCountData.get(selectedIndexCastle-2);
			castleName = sc.str;
		}
		
		StringCount sc = strategyCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String strategy = sc.str;
		gd.textBox[TextBoxType.Strategy.id].setText(strategy);
		
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
	
		if(playerName.equals("")) gd.listModel[ListBoxType.Info.id].addElement("<"+ strategy + "'s Kifu>");
		else gd.listModel[ListBoxType.Info.id].addElement("<"+ strategy + "(" + playerName + ")"+"'s Kifu>");
		gd.listModel[ListBoxType.Info.id].addElement("-------------");
		for(KifuData kd: kifuDB) {
			if(kd.strategyName.equals(strategy)) {
				String str = String.format("%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				if(playerName.equals("") && castleName.equals("")) {
					gd.listModel[ListBoxType.Info.id].addElement(str);
				} else if(!playerName.equals("") && !castleName.equals("")) {
					if(str.contains(playerName) && (kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName))) {
						gd.listModel[ListBoxType.Info.id].addElement(str);
					}
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) gd.listModel[ListBoxType.Info.id].addElement(str);
				} else if(!castleName.equals("")) {
					if(kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName)) gd.listModel[ListBoxType.Info.id].addElement(str);
				}
			}
		}
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
	}	
	// -------------------------------------------------------------------------
	// ----------------------- << Castle Data >> -------------------------------
	// -------------------------------------------------------------------------
	public String castleFilePath = "castle/";
	String imgFilePath = "./img/";
	String imgFilePathCastleIcon = imgFilePath + "castleIcon/";
	List<CastleData> castleDataBase = new ArrayList<CastleData>();
	public List<StringCount> castleCountData = new ArrayList<StringCount>();
	public class CastleData {	
		String name;
		int data[][] = new int[25][4];
		public CastleData(String castleName) {
			name = castleName;
		}
	}
	public void actionForCastle(ShogiData sd, Boolean isRadioButtonSente) {
		Path path;
		String fileName;
		int index = 1;
		
		if(gd.textBox[TextBoxType.Castle.id].getText().equals("")) {
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
		
			fw.write(gd.textBox[TextBoxType.Castle.id].getText() + "\n");
			for(Koma k: sd.k) {
				if((k.type == KomaType.King) && isRadioButtonSente && k.sente == 0) {
					saveListKomaAroundKing(sd, k, fw);
				}
				if((k.type == KomaType.King) && !isRadioButtonSente && k.sente == 1) {
					saveListKomaAroundKing(sd, k, fw);
				}
			}
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
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
		gd.listModel[ListBoxType.Castle.id].clear();
		gd.listBox[ListBoxType.Castle.id].setModel(gd.listModel[ListBoxType.Castle.id]);
		castleCountData.clear();
		
		for(KifuData kd: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kd.castleName[SenteGote.Sente.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kd.castleName[SenteGote.Sente.id], kd.isSenteWin);
				castleCountData.add(sc);
			}
		}
		for(KifuData kd: kifuDB) {
			Boolean found = false;
			for(StringCount sc: castleCountData) {
				if(sc.str.equals(kd.castleName[SenteGote.Gote.id])) {
					sc.cnt++;
					found = true;
				}
			}
			if(!found) {
				StringCount sc = new StringCount(kd.castleName[SenteGote.Gote.id], kd.isSenteWin);
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
		gd.listModel[ListBoxType.Castle.id].addElement(str);
		gd.listModel[ListBoxType.Castle.id].addElement("----------");
		for(StringCount sc: castleCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			gd.listModel[ListBoxType.Castle.id].addElement(str);
		}
		gd.listBox[ListBoxType.Castle.id].setModel(gd.listModel[ListBoxType.Castle.id]);
	}
	public void updateListBoxInfoByCastle() {
		int selectedIndex = gd.listBox[ListBoxType.Castle.id].getSelectedIndex()-2;
		if(selectedIndex < 0) {
			initializeCastleIcon();
			return;
		}
		int selectedIndexStrategy = gd.listBox[ListBoxType.Strategy.id].getSelectedIndex();
		String strategyName = "";
		if(selectedIndexStrategy >= 2) {
			StringCount sc = strategyCountData.get(selectedIndexStrategy-2);
			strategyName = sc.str;
		}
		int selectedIndexPlayer = gd.listBox[ListBoxType.Player.id].getSelectedIndex();
		String playerName = "";
		if(selectedIndexPlayer >= 2) {
			playerName = gd.listModel[ListBoxType.Player.id].getElementAt(selectedIndexPlayer);
			//System.out.println(playerName);
		}
		
		StringCount sc = castleCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String castleName = sc.str;
		updateCastleIcon();
		gd.textBox[TextBoxType.Castle.id].setText(castleName);
		
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);

		if(strategyName.equals("")) gd.listModel[ListBoxType.Info.id].addElement("<"+ castleName + "'s Kifu>");
		else gd.listModel[ListBoxType.Info.id].addElement("<"+ strategyName + "(" + castleName + ")"+"'s Kifu>");
		gd.listModel[ListBoxType.Info.id].addElement("-------------");
		for(KifuData kd: kifuDB) {
			if(kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName)) {
				String str = String.format("%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				
				if(strategyName.equals("") && playerName.equals("")) {
					gd.listModel[ListBoxType.Info.id].addElement(str);
				} else if(!strategyName.equals("") && !playerName.equals("")) {
					if(kd.strategyName.equals(strategyName) && str.contains(playerName)) gd.listModel[ListBoxType.Info.id].addElement(str);
				} else if(!strategyName.equals("")) {
					if(kd.strategyName.equals(strategyName)) gd.listModel[ListBoxType.Info.id].addElement(str);
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) gd.listModel[ListBoxType.Info.id].addElement(str);
				}
			}
		}
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
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
			//Koma k = getKomaByPosition(sd, data[1], data[2], data[0]);
			Koma k = sd.findKoma(data[1], data[2], data[0]);
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
					//k = getKomaByPosition(sd, king.px+x, king.py+y, king.sente);
					k = sd.findKoma(king.px+x, king.py+y, king.sente);
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
	public void updateCastleIcon() {
		int selectedIndex = gd.listBox[ListBoxType.Castle.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		StringCount sc = castleCountData.get(selectedIndex);
		String castleName = sc.str;
		try {
			BufferedImage img = ImageIO.read(new File(imgFilePathCastleIcon + castleName + ".jpg"));
			cd.cv.castleIcon = img.getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH);
		} catch(IOException e) {
			cd.cv.castleIcon = null;
		}
		cd.cv.repaint();
	}
	public void initializeCastleIcon() {
		cd.cv.castleIcon = null;
	}
	
// -------------------------------------------------------------------------
// ----------------------- << Player Data >> -------------------------------
// -------------------------------------------------------------------------
	String imgFilePathPlayerIcon = imgFilePath + "playerIcon/";
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
		gd.listModel[ListBoxType.Player.id].clear();
		gd.listBox[ListBoxType.Player.id].setModel(gd.listModel[ListBoxType.Player.id]);
		playerDataBase.clear();
		
		for(KifuData kd: kifuDB) {
			Boolean foundS = false;
			Boolean foundG = false;
			GameResult grS = new GameResult();
			GameResult grG = new GameResult();
			grS.strategy = kd.strategyName;
			grG.strategy = kd.strategyName;
			grS.isSente = true;
			grG.isSente = false;
			if(kd.isSenteWin == 1) {
				grS.isPlayerWin = 1;
				grG.isPlayerWin = 0;
			}
			else if(kd.isSenteWin == 0) {
				grS.isPlayerWin = 0;
				grG.isPlayerWin = 1;
			} else { // Draw
				grS.isPlayerWin = -1;
				grG.isPlayerWin = -1;
			}
			grS.castle = kd.castleName[SenteGote.Sente.id];
			grG.castle = kd.castleName[SenteGote.Gote.id];
			
			for(PlayerData pd: playerDataBase) {
				if(kd.playerName[SenteGote.Sente.id].equals(pd.playerName)) {
					foundS = true;
					pd.grList.add(grS);
				} else if(kd.playerName[SenteGote.Gote.id].equals(pd.playerName)) {
					foundG = true;
					pd.grList.add(grG);
				}
			}
			
			if(!foundS) {
				PlayerData pd = new PlayerData(kd.playerName[SenteGote.Sente.id]);
				pd.grList.add(grS);
				playerDataBase.add(pd);
			}
			if(!foundG) {
				PlayerData pd = new PlayerData(kd.playerName[SenteGote.Gote.id]);
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
		gd.listModel[ListBoxType.Player.id].addElement(str);
		gd.listModel[ListBoxType.Player.id].addElement("---------");
		
		for(PlayerData pd: playerDataBase) {
			gd.listModel[ListBoxType.Player.id].addElement(pd.playerName);
		}
		gd.listBox[ListBoxType.Player.id].setModel(gd.listModel[ListBoxType.Player.id]);
	}
	public void updateListBox2ByPlayerName() {
		int selectedIndex = gd.listBox[ListBoxType.Player.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		
		PlayerData pd = playerDataBase.get(selectedIndex);
		gd.textBox[TextBoxType.Player1.id].setText(pd.playerName);
		updatePlayerIcon();
		
		// count strategy data
		List<GameResultCount> grcList = new ArrayList<GameResultCount>();
		countStrategyDataByPlayerData(grcList, pd);
		
		// count castle data
		List<StringCount> strList = new ArrayList<StringCount>();
		countCastleDataByPlayerData(strList, pd);
		
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
		
		gd.listModel[ListBoxType.Info.id].addElement("<" + pd.playerName + "'s Winning Rate>");
		
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
		gd.listModel[ListBoxType.Info.id].addElement(str);
		
		str = String.format("Sente %d games %d Win:%d Lose %d Draw", totalSenteWinCnt+totalSenteLoseCnt+totalSenteDrawCnt, totalSenteWinCnt, totalSenteLoseCnt, totalSenteDrawCnt);
		d = (double)totalSenteWinCnt/(double)(totalSenteWinCnt+totalSenteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		gd.listModel[ListBoxType.Info.id].addElement(str);
		str = String.format("Gote %d games %d Win:%d Lose: %d Draw", totalGoteWinCnt+totalGoteLoseCnt+totalGoteDrawCnt, totalGoteWinCnt, totalGoteLoseCnt, totalGoteDrawCnt);
		d = (double)totalGoteWinCnt/(double)(totalGoteWinCnt+totalGoteLoseCnt)*100;
		str += "(Winning Rate" + String.format("%.0f", d) + "%)";
		gd.listModel[ListBoxType.Info.id].addElement(str);
		
		gd.listModel[ListBoxType.Info.id].addElement("---------");
		str = "Total Strategies: " + grcList.size() + " patterns";
		gd.listModel[ListBoxType.Info.id].addElement(str);
		for(GameResultCount grc: grcList) {
			str = grc.str;
			d = (double)(grc.senteWinCnt+grc.goteWinCnt)/(double)(grc.cnt)*100;
			str += ":" + String.format("%d", grc.cnt) + " games";
			str += "(Winning Rate" + String.format("%.0f", d) + "%)";
			gd.listModel[ListBoxType.Info.id].addElement(str);
		}
		
		gd.listModel[ListBoxType.Info.id].addElement("---------");
		for(StringCount sc: strList) {
			str = sc.str;
			str += ":" + String.format("%d", sc.cnt) + " games";
			gd.listModel[ListBoxType.Info.id].addElement(str);
		}
		
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
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
		playerName[SenteGote.Sente.id] = new String(gd.textBox[TextBoxType.Player1.id].getText());
		playerName[SenteGote.Gote.id] = new String(gd.textBox[TextBoxType.Player2.id].getText());
		
		for(SenteGote sg: SenteGote.values()) {
			try {
				BufferedImage img = ImageIO.read(new File(imgFilePathPlayerIcon + playerName[sg.id] + ".jpg"));
				cd.cv.playerIcon[sg.id] = img.getScaledInstance(100, 133, java.awt.Image.SCALE_SMOOTH);
			} catch(IOException e) {
				cd.cv.playerIcon[sg.id] = null;
			}
		}
		cd.cv.repaint();
	}
	public void initializePlayerIcon() {
		for(SenteGote sg: SenteGote.values()) cd.cv.playerIcon[sg.id] = null;
	}

	// -------------------------------------------------------------------------
	// ----------------------- << Tesuji Data >> -------------------------------
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
	public void actionForTesuji(String loadFile) {
		Path path;
		String fileName;
		int index = 1;
		
		if(gd.textBox[TextBoxType.Tesuji.id].getText().equals("")) {
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
		
			fw.write(gd.textBox[TextBoxType.Tesuji.id].getText() + "\n");
			fw.write(loadFile + "\n");
			fw.write(gd.listBox[ListBoxType.Kifu.id].getSelectedIndex() + "\n");
			fw.close();
			
			JOptionPane.showMessageDialog(null, fileName + " is saved.");
		} catch(IOException er) {
			System.out.println(er);
		}
	}
	public void loadTesujiData() {
		tesujiDataBase.clear();
		String selectedYear = (String)gd.comboBox.getSelectedItem();
		if(selectedYear.equals("all")) loadTesujiDataAll();
		else loadTesujiDataByYear(selectedYear);
	}
	public void loadTesujiDataAll() {
		loadTesujiDataByYear("");
		File dir = new File("./kifu/");
		File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        File file = files[i];
	        String[] names = file.toString().split("/");
	        if(file.isDirectory()) loadTesujiDataByYear(names[names.length-1]);
	    }
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
		gd.listModel[ListBoxType.Tesuji.id].clear();
		gd.listBox[ListBoxType.Tesuji.id].setModel(gd.listModel[ListBoxType.Tesuji.id]);
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
		gd.listModel[ListBoxType.Tesuji.id].addElement(str);
		gd.listModel[ListBoxType.Tesuji.id].addElement("----------");
		for(StringCount sc: tesujiCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" counts";
			gd.listModel[ListBoxType.Tesuji.id].addElement(str);
		}
		gd.listBox[ListBoxType.Tesuji.id].setModel(gd.listModel[ListBoxType.Tesuji.id]);
	}
	public void updateListBoxInfoByTesuji() {
		int selectedIndex = gd.listBox[ListBoxType.Tesuji.id].getSelectedIndex()-2;
		if(selectedIndex < 0) {
			return;
		}
		
		StringCount sc = tesujiCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String tesujiName = sc.str;
		gd.textBox[TextBoxType.Tesuji.id].setText(tesujiName);
		
		gd.listModel[ListBoxType.Info.id].clear();
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
	
		gd.listModel[ListBoxType.Info.id].addElement("<"+ tesujiName + "'s Kifu>");
		gd.listModel[ListBoxType.Info.id].addElement("-------------");
		for(TesujiData td: tesujiDataBase) {
			if(td.name.equals(tesujiName)) {
				String str = String.format("%03d:%03d:%s", td.fileIndex, td.stepIndex, td.year);
				KifuData kd = getKDB(td.fileIndex, td.year);
				if(kd == null) continue;
				str += ":" + kd.playerName[SenteGote.Sente.id] + " vs " + kd.playerName[SenteGote.Gote.id];
				gd.listModel[ListBoxType.Info.id].addElement(str);
			}
		}
		gd.listBox[ListBoxType.Info.id].setModel(gd.listModel[ListBoxType.Info.id]);
	}
}