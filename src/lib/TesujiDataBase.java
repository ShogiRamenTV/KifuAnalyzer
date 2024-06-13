package lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

import lib.KifuDataBase.KifuData;
import lib.ShogiData.SenteGote;

// -------------------------------------------------------------------------
// ----------------------- << Tesuji Data >> -----------------------------
// -------------------------------------------------------------------------
public class TesujiDataBase {
	List<TesujiData> tesujiDataBase = new ArrayList<TesujiData>();
	List<StringCount> tesujiCountData = new ArrayList<StringCount>();
	DefaultListModel<String> listModelTesuji;
	JList<String> listBoxTesuji;
	DefaultListModel<String> listModelInfo;
	JList<String> listBoxInfo;
	JTextField textBoxTesuji;
	JComboBox<String> comboBox;
	KifuDataBase kdb;
	public TesujiDataBase (DefaultListModel<String> lmT, JList<String> lbT,
			DefaultListModel<String> lmI, JList<String> lbI,
			JTextField tT,
			JComboBox<String> cb, KifuDataBase kb) {
		listModelTesuji = lmT;
		listBoxTesuji = lbT;
		listModelInfo = lmI;
		listBoxInfo = lbI;
		textBoxTesuji = tT;
		comboBox = cb;
		kdb = kb;
	}
	
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
	public void loadTesujiData() {
		tesujiDataBase.clear();
		String selectedYear = (String)comboBox.getSelectedItem();
		if(selectedYear.equals("") || selectedYear.equals("all")) loadTesujiDataByYear("");
		if(selectedYear.equals("2022") || selectedYear.equals("all")) loadTesujiDataByYear("2022");
		if(selectedYear.equals("2023") || selectedYear.equals("all")) loadTesujiDataByYear("2023");
	}
	public void loadTesujiDataByYear(String strY) {
		System.out.print("Loading Tesuji Data(" + strY + ") ... ");
		try {
			int fileIndex = 1;
			while(true) {
				String fileName = kdb.kifuFilePath + strY + "/" + "tesuji" + String.format("%03d", fileIndex) + ".txt";
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
		listModelTesuji.clear();
		listBoxTesuji.setModel(listModelTesuji);
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
		listModelTesuji.addElement(str);
		listModelTesuji.addElement("----------");
		for(StringCount sc: tesujiCountData) {
			str = sc.str;
			str += ":" + String.format("%2d", sc.cnt)+" counts";
			listModelTesuji.addElement(str);
		}
		listBoxTesuji.setModel(listModelTesuji);
	}
	public void updateListBoxInfoByTesuji() {
		int selectedIndex = listBoxTesuji.getSelectedIndex()-2;
		if(selectedIndex < 0) {
			return;
		}
		
		StringCount sc = tesujiCountData.get(selectedIndex);
		//System.out.println(sc.str);
		String tesujiName = sc.str;
		textBoxTesuji.setText(tesujiName);
		
		listModelInfo.clear();
		listBoxInfo.setModel(listModelInfo);
	
		listModelInfo.addElement("<"+ tesujiName + "'s Kifu>");
		listModelInfo.addElement("-------------");
		for(TesujiData td: tesujiDataBase) {
			if(td.name.equals(tesujiName)) {
				String str = "kf" + String.format("%03d:%03d:%s", td.fileIndex, td.stepIndex, td.year);
				KifuData kd = kdb.getKDB(td.fileIndex, td.year);
				if(kd == null) continue;
				str += ":" + kd.playerName[SenteGote.Sente.id] + " vs " + kd.playerName[SenteGote.Gote.id];
				listModelInfo.addElement(str);
			}
		}
		listBoxInfo.setModel(listModelInfo);
	}
}
