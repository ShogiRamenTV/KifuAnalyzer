package lib;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import lib.AnalysisData.StringCount;
import lib.EditProperty.PropertyType;
import lib.GUIData.ListBoxType;
import lib.ShogiData.Koma;
import lib.ShogiData.KomaType;

// -------------------------------------------------------------------------
// ----------------------- << Interface for Engine >> ----------------------
// -------------------------------------------------------------------------
public class ShogiEngine {
	public PrintStream out = null;
	MyThreadReceiver receiver;
	public static final int numOfMultiPV = 5;
	public static final int calculatingTimeOfEngine = 2000; // ms
	public Boolean isEngineOn = false;
	JFrame fr;
	EditProperty ep;
	ShogiData sd;
	CanvasData cd;
	GUIData gd;
	AnalysisData ad;
	
	public ShogiEngine(JFrame f, ShogiData s, EditProperty e,
			GUIData g, CanvasData c, AnalysisData a) {
		fr = f;
		ep = e;
		sd = s;
		cd = c;
		gd = g;
		ad = a;
	}
	public void update(GUIData g, CanvasData c, AnalysisData a) {
		gd = g;
		cd = c;
		ad = a;
	}
	public Process createEngine() {
		String enginePath = ep.loadProperty(PropertyType.Engine.name());
		if(enginePath == null) {
			ep.setPropertyForEngine(fr);
			enginePath = ep.loadProperty(PropertyType.Engine.name());
		}
		if(enginePath == null) return null;
		String engineFolder = enginePath.substring(0, enginePath.lastIndexOf("/"));
		ProcessBuilder p = new ProcessBuilder(enginePath);
		p.directory(new File(engineFolder));
		Process process = null;
		try {
			process = p.start();
		} catch (IOException e) {
			System.out.println("engine is not installed");
			ep.setPropertyForEngine(fr);
		}
		
		return process;
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
		cd.cv.repaint();
		cd.cve.repaint();
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
	public int getNumOfMultiPV() {
		return numOfMultiPV;
	}
	public int getCalculatingTimeOfEngine() {
		return calculatingTimeOfEngine;
	}
	public String createCommandForEngine() {
		String cmd = "position sfen ";
		String str[] = new String[9];
		int empty;
		for(int y=0; y<9; y++) {
			empty = 0;
			str[y] = "";
			for(int x=8; x>=0; x--) {
				Koma k = sd.findKoma(x+1, y+1);
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
		if(sd.turnIsSente) cmd += " b ";
		else cmd += " w ";
		cmd += getStringOfKomaInHand();
		cmd += " 1";
		//System.out.println(cmd);
		return cmd;
	}
	public String getStringOfKomaInHand() {
		String str = "";
		str += createStringOfKomaInHand(sd.listKomaOnHandForSente, true);
		str += createStringOfKomaInHand(sd.listKomaOnHandForGote, false);
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
				StringCount sc = ad.createStringCount(k.type.name(), 0);
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
						cd.cv.repaint();
						cd.cve.repaint();
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			}
			for(int index=0; index<numOfMultiPV; index++) {
				gd.listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
	}
	public class ConvertedData {
		int promote;
		int drop;
		KomaType type;
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
	public void getPointFromInfo(String info) {
		Point base = new Point();
		Point target = new Point();
		if(info.contains("multipv 1")) {
			cd.cv.clearDrawListForEngine();
			for(int index=0; index<numOfMultiPV; index++) {
				gd.listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
		if(!info.contains("multipv")) { // case of there are only 1 way to move
			cd.cv.clearDrawListForEngine();
			for(int index=0; index<numOfMultiPV; index++) {
				gd.listModel[ListBoxType.Engine.id].set(index, "");
			}
		}
		
		String[] names = info.split(" ");
		String str = "";
		int score = 0;
		int index = 0;
		ConvertedData cond = new ConvertedData();
		for(int i=0; i<names.length; i++) {
			if(names[i].equals("pv")) str = names[i+1];
			if(names[i].equals("cp")) score = Integer.parseInt(names[i+1]);
			if(names[i].equals("multipv")) index = Integer.parseInt(names[i+1]) - 1;
		}
		convertStringPosToPoints(str, base, target, cond);
		// for safe access in multi thread
		synchronized(cd.cv.drawListForEngine) {
			PointWithScore ps = new PointWithScore(base, target, score);
			cd.cv.drawListForEngine.add(ps);
			Koma k = null;
			if(cond.drop == 0) {
				k = sd.findKoma(base.x, base.y);
			} else {
				k = sd.findKomaInHand(cond.type, sd.turnIsSente);
			}
			if(k != null) {
				String komaMove = sd.createMoveKomaName(k.type, k.sente, target.x, target.y, k.px, k.py, cond.promote, k.promoted, cond.drop);
				komaMove += " " + score;
				gd.listModel[ListBoxType.Engine.id].set(index, komaMove);
				gd.listBox[ListBoxType.Engine.id].setModel(gd.listModel[ListBoxType.Engine.id]);
				cd.cve.bestPointFromEngine.moveName[index] = komaMove;
			}
		}
	}
	public void convertStringPosToPoints(String strPos, Point base, Point target, ConvertedData cd) {
		char ch[] = strPos.toCharArray();
		String str = "PLNSGBRKE";
		// drop a piece
		if(ch[1] == '*') {
			if(ch[0] == 'P' || ch[0] == 'L' || ch[0] == 'N' || ch[0] == 'S' || ch[0] == 'G' || ch[0] == 'B' || ch[0] == 'R') {
				if(sd.turnIsSente) {
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
			if(sd.turnIsSente) {
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
}