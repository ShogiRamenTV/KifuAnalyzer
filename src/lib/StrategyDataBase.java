package lib;

import java.awt.Point;
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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import lib.KifuDataBase.KifuData;
import lib.ListBoxData.ListBoxType;
import lib.ShogiData.SenteGote;
import lib.TextBoxData.TextBoxType;

// -------------------------------------------------------------------------
// ----------------------- << Strategy Data >> -----------------------------
// -------------------------------------------------------------------------

public class StrategyDataBase {
	public String strategyFilePathBase = "./strategy/";
	public String strategyFilePath = "strategy/";
	//String castleFilePath = "castle/";
	List<StrategyData> strategyDataBase = new ArrayList<StrategyData>();
	List<StringCount> strategyCountData = new ArrayList<StringCount>();
	DefaultListModel<String> listModel[];
	JList<String> listBox[];
	JTextField textBox[];
	KifuDataBase kifuDataBase;
	CastleDataBase castleDataBase;
	public StrategyDataBase(DefaultListModel<String> lm[], JList<String> lb[],
			JTextField tb[],
			KifuDataBase kdb, CastleDataBase cdb) {
		listModel = lm;
		listBox = lb;
		textBox = tb;
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
	public void update(KifuDataBase kdb, CastleDataBase cdb) {
		kifuDataBase = kdb;
		castleDataBase = cdb;
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
		listModel[ListBoxType.Strategy.id].clear();
		listBox[ListBoxType.Strategy.id].setModel(listModel[ListBoxType.Strategy.id]);
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
		listModel[ListBoxType.Strategy.id].addElement(str);
		listModel[ListBoxType.Strategy.id].addElement("----------");
		for(StringCount sc: strategyCountData) {
			str = sc.str;
			d = (double)sc.senteWinCnt/(double)(sc.cnt)*100;
			str += ":" + String.format("%2d", sc.cnt)+" games";
			str += "(Sente Winning Rate" + String.format("%.0f", d) + "%)";
			listModel[ListBoxType.Strategy.id].addElement(str);
		}
		listBox[ListBoxType.Strategy.id].setModel(listModel[ListBoxType.Strategy.id]);
	}
	public void updateListBox2ByStrategy() {
		int selectedIndex = listBox[ListBoxType.Strategy.id].getSelectedIndex()-2;
		if(selectedIndex < 0) return;
		int selectedIndex4 = listBox[ListBoxType.Player.id].getSelectedIndex();
		String playerName = "";
		if(selectedIndex4 >= 2) {
			playerName = listModel[ListBoxType.Player.id].getElementAt(selectedIndex4);
			//System.out.println(playerName);
		}
		int selectedIndexCastle = listBox[ListBoxType.Castle.id].getSelectedIndex();
		String castleName = "";
		if(selectedIndexCastle >= 2) {
			StringCount sc = castleDataBase.castleCountData.get(selectedIndexCastle-2);
			castleName = sc.str;
		}
		
		StringCount sc = strategyCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String strategy = sc.str;
		textBox[TextBoxType.Castle.id].setText(strategy);
		
		listModel[ListBoxType.Info.id].clear();
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	
		if(playerName.equals("")) listModel[ListBoxType.Info.id].addElement("<"+ strategy + "'s Kifu>");
		else listModel[ListBoxType.Info.id].addElement("<"+ strategy + "(" + playerName + ")"+"'s Kifu>");
		listModel[ListBoxType.Info.id].addElement("-------------");
		for(KifuData kd: kifuDataBase.kifuDB) {
			if(kd.strategyName.equals(strategy)) {
				String str = String.format("kf%03d:000:%s:", kd.index, kd.year);
				str += kd.playerName[SenteGote.Sente.id] + "(" + kd.castleName[SenteGote.Sente.id] + ")" + " vs " + kd.playerName[SenteGote.Gote.id] + "(" + kd.castleName[SenteGote.Gote.id] + ")";
				if(kd.isSenteWin == 1) str+="(Sente Win)";
				else if(kd.isSenteWin == 0) str+="(Gote Win)";
				else str+="(Draw)";
				if(playerName.equals("") && castleName.equals("")) {
					listModel[ListBoxType.Info.id].addElement(str);
				} else if(!playerName.equals("") && !castleName.equals("")) {
					if(str.contains(playerName) && (kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName))) {
						listModel[ListBoxType.Info.id].addElement(str);
					}
				} else if(!playerName.equals("")) {
					if(str.contains(playerName)) listModel[ListBoxType.Info.id].addElement(str);
				} else if(!castleName.equals("")) {
					if(kd.castleName[SenteGote.Sente.id].equals(castleName) || kd.castleName[SenteGote.Gote.id].equals(castleName)) listModel[ListBoxType.Info.id].addElement(str);
				}
			}
		}
		listBox[ListBoxType.Info.id].setModel(listModel[ListBoxType.Info.id]);
	}	
}
