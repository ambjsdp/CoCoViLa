package ee.ioc.cs.vsle.editor;

import ee.ioc.cs.vsle.vclass.GObj;
import ee.ioc.cs.vsle.vclass.ClassField;
import ee.ioc.cs.vsle.util.db;
import ee.ioc.cs.vsle.util.FileFuncs;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Ando
 * Date: 29.03.2005
 * Time: 19:44:21
 * To change this template use Options | File Templates.
 */
public class ClassSaveDialog extends JFrame implements ActionListener {


	JTextField textField;
	JButton cancel, ok;
	String text;
	Canvas canvas;

	public ClassSaveDialog(String text, Canvas canvas) {
		super();
		this.text = text;
		this.canvas = canvas;
		JPanel specText = new JPanel();
		textField = new JTextField(".java");
		textField.setCaretPosition(0);

		JPanel buttonPane = new JPanel();

		ok = new JButton("Save");
		ok.addActionListener(this);
		buttonPane.add(ok);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		buttonPane.add(cancel);

		specText.setLayout(new BorderLayout());

		specText.setLayout(new BorderLayout());
		specText.add(textField, BorderLayout.CENTER);
		specText.add(buttonPane, BorderLayout.SOUTH);

		getContentPane().add(specText);
		validate();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			FileFuncs ff = new FileFuncs();
			if (textField.getText().length() != 0) {
				ff.writeFile(RuntimeProperties.packageDir + textField.getText(), text);
				this.dispose();
				canvas.repaint();
			}

		}
		if (e.getSource() == cancel) {
			this.dispose();
			canvas.repaint();
		}
	}
}
