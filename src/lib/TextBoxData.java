package lib;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

public class TextBoxData {
	int baseXPosForItems;
	AnalysisData ad;
	ListBoxData lbd;
	public TextBoxData(int baseXPos, AnalysisData a, ListBoxData ld) {
		baseXPosForItems = baseXPos;
		ad = a;
		lbd = ld;
	}
	public enum TextBoxType {
		Player1(0), Player2(1), Strategy(2), Tesuji(3), Castle(4);
		public final int id;
		private TextBoxType(final int id) {
			this.id = id;
		}
	};
	public JTextField textBox[] = new JTextField[TextBoxType.values().length];
	public void update(AnalysisData a, ListBoxData ld) {
		ad = a;
		lbd = ld;
	}
	public void initializeTextBoxSetting() {
		for(TextBoxType t: TextBoxType.values()) {
			textBox[t.id] = new JTextField();
		}
		textBox[TextBoxType.Strategy.id].setBounds(baseXPosForItems+360, 10, 140, 20);
		textBox[TextBoxType.Tesuji.id].setBounds(baseXPosForItems+360, 30, 140, 20);
		textBox[TextBoxType.Castle.id].setBounds(baseXPosForItems+360, 50, 140, 20);
		textBox[TextBoxType.Player1.id].setBounds(baseXPosForItems, 100, 120, 20);
		textBox[TextBoxType.Player1.id].addActionListener(enterActionListener);
		textBox[TextBoxType.Player2.id].setBounds(baseXPosForItems+120, 100, 120, 20);
		textBox[TextBoxType.Player2.id].addActionListener(enterActionListener);
	}
	public void clearTextBox() {
		textBox[TextBoxType.Player1.id].setText("");
		textBox[TextBoxType.Player2.id].setText("");
		textBox[TextBoxType.Strategy.id].setText("");
		textBox[TextBoxType.Castle.id].setText("");
		textBox[TextBoxType.Tesuji.id].setText("");
		lbd.loadFile = "";
		lbd.loadStep = "";
		lbd.loadYear = "";
	}
	private ActionListener enterActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ad.updatePlayerIcon();
        }
    };
}