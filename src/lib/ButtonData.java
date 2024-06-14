package lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import lib.CheckBoxData.CheckBoxType;
import lib.TextBoxData.TextBoxType;

public class ButtonData implements ActionListener, MouseListener{
	public enum ButtonType {
		Initialize(0), Save(1), Strategy(2), Castle(3), Tesuji(4), Kifu(5);
		public final int id;
		private ButtonType(final int id) {
			this.id = id;
		}
	};
	public JButton button[] = new JButton[ButtonType.values().length];
	int baseXPosForItems;
	TextBoxData tbd;
	ListBoxData lbd;
	ColorDataBase cldb;
	CheckBoxData cbd;
	CanvasBoard cv;
	CanvasBoardForEngine cve;
	ShogiData sd;
	AnalysisData ad;
	
	public ButtonData(int bPos, TextBoxData td, ListBoxData ld, ColorDataBase clb,
			CheckBoxData chb, CanvasBoard c, CanvasBoardForEngine ce, ShogiData s, 
			AnalysisData a) {
		baseXPosForItems = bPos;
		tbd = td;
		lbd = ld;
		cldb = clb;
		cbd = chb;
		cv = c;
		cve = ce;
		sd = s;
		ad = a;
	}
	public void update(TextBoxData td, ListBoxData ld, ColorDataBase clb,
			CheckBoxData chb, CanvasBoard c, CanvasBoardForEngine ce, ShogiData s, 
			AnalysisData a) {
		tbd = td;
		lbd = ld;
		cldb = clb;
		cbd = chb;
		cv = c;
		cve = ce;
		sd = s;
		ad = a;
	}
	public void initializeButtonSetting() {
		for(ButtonType b: ButtonType.values()) {
			button[b.id] = new JButton(b.name());
			button[b.id].addActionListener(this);
			button[b.id].addMouseListener(this);
			button[b.id].setOpaque(true);
		}
		button[ButtonType.Initialize.id].setBounds(baseXPosForItems, 10, 80, 20);
		button[ButtonType.Save.id].setBounds(baseXPosForItems, 30, 80, 20);
		button[ButtonType.Kifu.id].setBounds(baseXPosForItems, 50, 80, 20);
		button[ButtonType.Strategy.id].setBounds(baseXPosForItems+280, 10, 80, 20);
		button[ButtonType.Tesuji.id].setBounds(baseXPosForItems+280, 30, 80, 20);
		button[ButtonType.Castle.id].setBounds(baseXPosForItems+280, 50, 80, 20);
	}
	public void actionForInitialize() {
		tbd.clearTextBox();
		cbd.clearCheckBox();
		cv.reverseNumberRowCol(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.resetAllKoma(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());	
		sd.viewKomaOnBoard(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		sd.viewKomaOnHand(cbd.checkBox[CheckBoxType.Reverse.id].isSelected());
		ad.clearListBox();
		clearIcons();
		ad.kifuData.clear();
		cv.setLastPoint(-1, -1, false);
		cve.clearBestPointData();
		ad.loadStrategyData();
		ad.loadCastleData();
		ad.loadTesujiData();
		ad.actionForDB((String)cbd.comboBox.getSelectedItem());
		ad.countStrategy();
		ad.countCastle();
		ad.countTesujiData();
		ad.createPlayerDataBase();
		cv.repaint();
		cve.repaint();
	}
	public void clearIcons() {
		ad.initializePlayerIcon();
		ad.initializeCastleIcon();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		if(e.getActionCommand() == button[ButtonType.Initialize.id].getText()) {
			actionForInitialize();
		}
		if(e.getActionCommand() == button[ButtonType.Save.id].getText()) {
			ad.actionForSave();
		}
		if(e.getActionCommand() == button[ButtonType.Strategy.id].getText()) {
			ad.actionForStrategy(sd, tbd.textBox[TextBoxType.Strategy.id].getText());
		}
		if(e.getActionCommand() == button[ButtonType.Castle.id].getText()) {
			ad.actionForCastle(sd, cbd.radioButtonSente.isSelected());
		}
		if(e.getActionCommand() == button[ButtonType.Tesuji.id].getText()) {
			ad.actionForTesuji(lbd.loadFile);
		}
		if(e.getActionCommand() == button[ButtonType.Kifu.id].getText()) {
			ad.actionForKifu();
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		
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
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonFocusedColor);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		for(ButtonType bt: ButtonType.values()) {
			if(e.getSource() == button[bt.id]) {
				button[bt.id].setBackground(cldb.buttonColor);
			}
		}
	}
}