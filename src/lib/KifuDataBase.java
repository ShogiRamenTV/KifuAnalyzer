package lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;

import lib.ShogiData.Koma;
import lib.ShogiData.SenteGote;

//-------------------------------------------------------------------------
// ----------------------- << Kifu Data >> -----------------------------
// -------------------------------------------------------------------------
public class KifuDataBase {
	public String kifuFilePath = "./kifu/";
	public List<Kifu> kifuData = new ArrayList<Kifu>();
	public List<KifuData> kifuDB = new ArrayList<KifuData>();
	
	ShogiData sd;
	ShogiData sdForKDB;
	DefaultListModel<String> listModelInfo;
	JList<String> listBoxInfo;
	JList<String> listBoxKifu;
	CanvasBoard cv;
	JCheckBox checkBoxReverse;
	public KifuDataBase(ShogiData s, ShogiData sb, CanvasBoard c, 
			DefaultListModel<String> lmI, 
			JList<String> lbI, JList<String> lbK, JCheckBox r) 
	{
		sd = s;
		sdForKDB = sb;
		cv = c;
		listModelInfo = lmI;
		listBoxInfo = lbI;
		listBoxKifu = lbK;
		checkBoxReverse = r;
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
	public Kifu createKifu(Koma koma, int px, int py, int promote, int preP, int drop) {
		Kifu k = new Kifu(koma, px, py, promote, preP, drop);
		return k;
	}
	public KifuData createKifuData() {
		KifuData kd = new KifuData();
		return kd;
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
}