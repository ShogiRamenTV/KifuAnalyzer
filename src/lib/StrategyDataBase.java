package lib;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;

import lib.KifuDataBase.KifuData;
import lib.ShogiData.SenteGote;

// -------------------------------------------------------------------------
// ----------------------- << Strategy Data >> -----------------------------
// -------------------------------------------------------------------------

public class StrategyDataBase {
	public String strategyFilePathBase = "./strategy/";
	public String strategyFilePath = "strategy/";
	//String castleFilePath = "castle/";
	List<StrategyData> strategyDataBase = new ArrayList<StrategyData>();
	List<StringCount> strategyCountData = new ArrayList<StringCount>();
	DefaultListModel<String> listModelStrategy;
	JList<String> listBoxStrategy;
	DefaultListModel<String> listModelPlayer;
	JList<String> listBoxPlayer;
	DefaultListModel<String> listModelInfo;
	JList<String> listBoxInfo;
	JList<String> listBoxCastle;
	JTextField textBoxCastle;
	KifuDataBase kifuDataBase;
	CastleDataBase castleDataBase;
	public StrategyDataBase(DefaultListModel<String> lmS, JList<String> lbS,
			DefaultListModel<String> lmP, JList<String> lbP,
			DefaultListModel<String> lmI, JList<String> lbI, JList<String> lbC,
			JTextField tC,
			KifuDataBase kdb, CastleDataBase cdb) {
		listModelStrategy = lmS;	
		listBoxStrategy = lbS;
		listModelPlayer = lmP;
		listBoxPlayer = lbP;
		listModelInfo = lmI;
		listBoxInfo = lbI;
		listBoxCastle = lbC;
		textBoxCastle = tC;
		kifuDataBase = kdb;
		castleDataBase = cdb;
	}
	
	public class StrategyData {
		Point p[];
		String name;
		StrategyData(String strName) {
			name = strName;
			p = new Point[40];
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
		listModelStrategy.clear();
		listBoxStrategy.setModel(listModelStrategy);
		strategyCountData.clear();
		
		for(KifuData kd: kifuDataBase.kifuDB) {
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
		listModelStrategy.addElement(str);
		listModelStrategy.addElement("----------");
		for(StringCount sc: strategyCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			listModelStrategy.addElement(str);
		}
		listBoxStrategy.setModel(listModelStrategy);
	}
	public void updateListBox2ByStrategy() {
		int selectedIndex = listBoxStrategy.getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndex4 = listBoxPlayer.getSelectedIndex();
		String playerName = "";
		if(selectedIndex4 >= 2) {
			playerName = listModelPlayer.getElementAt(selectedIndex4);
			//System.out.println(playerName);
		}
		int selectedIndexCastle = listBoxCastle.getSelectedIndex();
		String castleName = "";
		if(selectedIndexCastle >= 2) {
			StringCount sc = castleDataBase.castleCountData.get(selectedIndexCastle-2);
			castleName = sc.str;
		}
		
		StringCount sc = strategyCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String strategy = sc.str;
		textBoxCastle.setText(strategy);
		
		listModelInfo.clear();
		listBoxInfo.setModel(listModelInfo);
	
		if(playerName.equals("")) listModelInfo.addElement("<"+ strategy + "'s Kifu>");
		else listModelInfo.addElement("<"+ strategy + "(" + playerName + ")"+"'s Kifu>");
		listModelInfo.addElement("-------------");
		for(KifuData kd: kifuDataBase.kifuDB) {
			if(kd.strategyName.equals(strategy)) {
				String str = String.format("kf%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				if(playerName.equals("") && castleName.equals("")) {
					listModelInfo.addElement(str);
				} else if(!playerName.equals("") && !castleName.equals("")) {
					if(str.contains(playerName) && (kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName))) {
						listModelInfo.addElement(str);
					}
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) listModelInfo.addElement(str);
				} else if(!castleName.equals("")) {
					if(kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName)) listModelInfo.addElement(str);
				}
			}
		}
		listBoxInfo.setModel(listModelInfo);
	}	
}
