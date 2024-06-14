package lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListBoxData implements ListSelectionListener, MouseListener, MouseMotionListener, KeyListener{
	public String loadFile = "";
	public String loadStep = "";
	public String loadYear = "";
	int baseXPosForItems;
	int numOfMultiPV;
	KifuDataBase kdb;
	PlayerDataBase pdb;
	StrategyDataBase sdb;
	CastleDataBase cdb;
	TesujiDataBase tdb;
	KomaSound ks;
	public ListBoxData(int baseXPos, int numOfMPV,
			KifuDataBase kb, PlayerDataBase pb, StrategyDataBase sb,
			CastleDataBase cb, TesujiDataBase tb, 
			KomaSound s) {
		baseXPosForItems = baseXPos;
		numOfMultiPV = numOfMPV;
		kdb = kb;
		pdb = pb;
		sdb = sb;
		cdb = cb;
		tdb = tb;
		ks = s;
	}

	public enum ListBoxType {
		Kifu(0), Info(1), Strategy(2), Player(3), Castle(4), Tesuji(5), Engine(6);
		public final int id;		
		private ListBoxType(final int id) {
			this.id = id;
		}
	};
	public JScrollPane scrollPane[] = new JScrollPane[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	public DefaultListModel<String> listModel[] = new DefaultListModel[ListBoxType.values().length];
	@SuppressWarnings("unchecked")
	public JList<String> listBox[] = new JList[ListBoxType.values().length];
	
	public void update(KifuDataBase kb, PlayerDataBase pb, StrategyDataBase sb,
			CastleDataBase cb, TesujiDataBase tb) {
		kdb = kb;
		pdb = pb;
		sdb = sb;
		cdb = cb;
		tdb = tb;
	}
	public void initializeListBoxSetting() {
		for(ListBoxType lb: ListBoxType.values()) {
			listModel[lb.id] = new DefaultListModel<String>();
			listBox[lb.id] = new JList<String>();
			scrollPane[lb.id] = new JScrollPane();
			listBox[lb.id].setModel(listModel[lb.id]);
			listBox[lb.id].addListSelectionListener(this);
			listBox[lb.id].addMouseListener(this);
			listBox[lb.id].addMouseMotionListener(this);
			listBox[lb.id].addKeyListener(this);
			scrollPane[lb.id].getViewport().setView(listBox[lb.id]);
		}
		
		listModel[ListBoxType.Kifu.id].addElement("--------");
		scrollPane[ListBoxType.Kifu.id].setBounds(baseXPosForItems, 280, 165, 140);
		scrollPane[ListBoxType.Info.id].setBounds(baseXPosForItems, 420, 165, 140);
		scrollPane[ListBoxType.Strategy.id].setBounds(baseXPosForItems+165, 280, 165, 140);
		scrollPane[ListBoxType.Castle.id].setBounds(baseXPosForItems+165, 420, 165, 140);
		scrollPane[ListBoxType.Player.id].setBounds(baseXPosForItems+330, 280, 165, 140);
		scrollPane[ListBoxType.Tesuji.id].setBounds(baseXPosForItems+330, 420, 165, 140);
		scrollPane[ListBoxType.Engine.id].setBounds(baseXPosForItems, 590, 165, 90);
		
		for(int index=0; index<numOfMultiPV; index++) {
			listModel[ListBoxType.Engine.id].addElement("");
		}
	}
	public void getLoadNumberOnListBox2() {
		int index = listBox[ListBoxType.Info.id].getSelectedIndex();
		if(index < 2) return;
		String str = listModel[ListBoxType.Info.id].getElementAt(listBox[ListBoxType.Info.id].getSelectedIndex());
		String subStrFile = str.substring(2,5);
		String subStrStep = str.substring(6,9);
		String subStrYear = str.substring(10,14);
		loadFile = "";
		loadStep = "";
		loadYear = "";
		if(subStrFile.matches("[+-]?\\d*(\\.\\d+)?")) loadFile = subStrFile;
		if(subStrStep.matches("[+-]?\\d*(\\.\\d+)?")) loadStep = subStrStep;
		if(subStrYear.matches("[+-]?\\d*(\\.\\d+)?")) loadYear = subStrYear;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getValueIsAdjusting()) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				kdb.commonListAction();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				sdb.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				pdb.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				cdb.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				tdb.updateListBoxInfoByTesuji();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getClickCount() == 2) {
			//System.out.println("mouse double clicked");
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				if(!loadFile.equals("")) {
					kdb.loadByNumber(loadFile, loadStep, loadYear);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(e.getSource() == listBox[ListBoxType.Kifu.id]) {
				kdb.commonListAction();
				ks.soundKoma();
			}
			if(e.getSource() == listBox[ListBoxType.Info.id]) {
				getLoadNumberOnListBox2();
			}
			if(e.getSource() == listBox[ListBoxType.Strategy.id]) {
				sdb.updateListBox2ByStrategy();
			}
			if(e.getSource() == listBox[ListBoxType.Player.id]) {
				pdb.updateListBox2ByPlayerName();
			}
			if(e.getSource() == listBox[ListBoxType.Castle.id]) {
				cdb.updateListBoxInfoByCastle();
			}
			if(e.getSource() == listBox[ListBoxType.Tesuji.id]) {
				tdb.updateListBoxInfoByTesuji();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
}