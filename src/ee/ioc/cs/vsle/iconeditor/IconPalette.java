package ee.ioc.cs.vsle.iconeditor;


import ee.ioc.cs.vsle.editor.State;
import ee.ioc.cs.vsle.graphics.Shape;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


/**
 * Created by IntelliJ IDEA.
 * User: Aulo
 * Date: 14.10.2003
 * Time: 9:19:17
 * To change this template use Options | File Templates.
 */
public class IconPalette {
	public JToolBar toolBar;
	IconEditor editor;
	JLabel lblLineWidth = new JLabel(" Size: ");
	JLabel lblTransparency;
	// JLabel lblRotation = new JLabel(" Rotation: ");

	Spinner spinnerLineWidth = new Spinner(1, 10, 1);
	Spinner spinnerTransparency = new Spinner(0, 100, 1);
	// Spinner spinnerRotation = new Spinner(0,180,1);

	// BUTTONS
	JButton selection;
	JButton boundingbox;
	JButton text;
	JButton line;
	JButton arc;
	JButton filledarc;
	JButton rectangle;
	JButton filledrectangle;
	JButton oval;
	JButton filledoval;
	JButton freehand;
	JButton eraser;
	JButton colors;
	JButton addport;

	public IconPalette(IconMouseOps mListener, IconEditor ed) {
		toolBar = new JToolBar();

		this.editor = ed;

		ImageIcon icon;

		spinnerLineWidth.setPreferredSize(new Dimension(40, 20));
		spinnerLineWidth.setMaximumSize(spinnerLineWidth.getPreferredSize());
		spinnerLineWidth.setBorder(BorderFactory.createEtchedBorder());

		spinnerTransparency.setPreferredSize(new Dimension(45, 20));
		spinnerTransparency.setMaximumSize(spinnerTransparency.getPreferredSize());
		spinnerTransparency.setBorder(BorderFactory.createEtchedBorder());

		// spinnerRotation.setPreferredSize(new Dimension(40, 20));
		// spinnerRotation.setMaximumSize(spinnerRotation.getPreferredSize());
		// spinnerRotation.setBorder(BorderFactory.createEmptyBorder());


		// Action listener added as anonymous class.
		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SpinnerModel source = (SpinnerModel) e.getSource();

				try {
					editor.mListener.changeStrokeWidth(Double.parseDouble(String.valueOf(source.getValue())));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		// Action listener added as anonymous class.
		ChangeListener transpListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SpinnerModel source = (SpinnerModel) e.getSource();

				try {
					editor.mListener.changeTransparency(Double.parseDouble(String.valueOf(source.getValue())));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		// Action listener added as anonymous class.
		ChangeListener rotationListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SpinnerModel source = (SpinnerModel) e.getSource();

				try {
					double degrees = Double.parseDouble(String.valueOf(source.getValue()));

					if (editor.shapeList != null && editor.shapeList.size() > 0) {
						Shape shape;

						for (int i = 0; i < editor.shapeList.size(); i++) {
							shape = (Shape) editor.shapeList.get(i);
							if (shape.isSelected()) {
								shape.setRotation(degrees);
							}
						}

					}
					editor.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		// add action listeners to spinners.
		spinnerLineWidth.getModel().addChangeListener(listener);
		spinnerTransparency.getModel().addChangeListener(transpListener);
		// spinnerRotation.getModel().addChangeListener(rotationListener);

		// add relation and selection tool.
		icon = new ImageIcon("images/mouse.gif");
		selection = new JButton(icon);
		selection.setActionCommand(State.selection);
		selection.addActionListener(mListener);
		selection.setToolTipText("Select");
		toolBar.add(selection);

		icon = new ImageIcon("images/boundingbox.gif");
		boundingbox = new JButton(icon);
		boundingbox.setActionCommand(State.boundingbox);
		boundingbox.addActionListener(mListener);
		boundingbox.setToolTipText("Bounding Box");
		toolBar.add(boundingbox);

		icon = new ImageIcon("images/port.gif");
		addport = new JButton(icon);
		addport.setActionCommand(State.addPort);
		addport.addActionListener(mListener);
		addport.setToolTipText("Add Port");
		toolBar.add(addport);

		icon = new ImageIcon("images/text.gif");
		text = new JButton(icon);
		text.setActionCommand(State.drawText);
		text.addActionListener(mListener);
		text.setToolTipText("Text");
		toolBar.add(text);

		// add line drawing tool
		icon = new ImageIcon("images/line.gif");
		line = new JButton(icon);
		line.setActionCommand(State.drawLine);
		line.addActionListener(mListener);
		line.setToolTipText("Line");
		toolBar.add(line);

		// add arc drawing tool
		icon = new ImageIcon("images/arc.gif");
		arc = new JButton(icon);
		arc.setActionCommand(State.drawArc);
		arc.addActionListener(mListener);
		arc.setToolTipText("Arc");
		toolBar.add(arc);

		// add arc drawing tool
		icon = new ImageIcon("images/fillarc.gif");
		filledarc = new JButton(icon);
		filledarc.setActionCommand(State.drawFilledArc);
		filledarc.addActionListener(mListener);
		filledarc.setToolTipText("Filled arc");
		toolBar.add(filledarc);

		// add rectangle drawing tool
		icon = new ImageIcon("images/rect.gif");
		rectangle = new JButton(icon);
		rectangle.setActionCommand(State.drawRect);
		rectangle.addActionListener(mListener);
		rectangle.setToolTipText("Rectangle");
		toolBar.add(rectangle);

		// add filled rectangle drawing tool
		icon = new ImageIcon("images/fillrect.gif");
		filledrectangle = new JButton(icon);
		filledrectangle.setActionCommand(State.drawFilledRect);
		filledrectangle.addActionListener(mListener);
		filledrectangle.setToolTipText("Filled rectangle");
		toolBar.add(filledrectangle);

		// add oval drawing tool
		icon = new ImageIcon("images/oval.gif");
		oval = new JButton(icon);
		oval.setActionCommand(State.drawOval);
		oval.addActionListener(mListener);
		oval.setToolTipText("Oval");
		toolBar.add(oval);

		// add filled oval drawing tool
		icon = new ImageIcon("images/filloval.gif");
		filledoval = new JButton(icon);
		filledoval.setActionCommand(State.drawFilledOval);
		filledoval.addActionListener(mListener);
		filledoval.setToolTipText("Filled oval");
		toolBar.add(filledoval);

		// add freehand drawing tool
		icon = new ImageIcon("images/freehand.gif");
		freehand = new JButton(icon);
		freehand.setActionCommand(State.freehand);
		freehand.addActionListener(mListener);
		freehand.setToolTipText("Freehand drawing");
		toolBar.add(freehand);

		// add freehand drawing tool
		icon = new ImageIcon("images/eraser.gif");
		eraser = new JButton(icon);
		eraser.setActionCommand(State.eraser);
		eraser.addActionListener(mListener);
		eraser.setToolTipText("Eraser");
		toolBar.add(eraser);

		// add color chooser tool
		icon = new ImageIcon("images/colorchooser.gif");
		colors = new JButton(icon);
		colors.setActionCommand(State.chooseColor);
		colors.addActionListener(mListener);
		colors.setToolTipText("Color chooser");
		toolBar.add(colors);

		// add line width selection spinner
		Font f = new Font("Tahoma", Font.PLAIN, 11);

		lblLineWidth.setFont(f);
		lblLineWidth.setToolTipText("Line width or point size of a selected tool");

		toolBar.add(lblLineWidth);
		toolBar.add(spinnerLineWidth);

		// add transparency spinner
		// lblTransparency.setFont(f);

		icon = new ImageIcon("images/transparency.gif");
		lblTransparency = new JLabel(icon);
		lblTransparency.setToolTipText("Object transparency percentage");

		toolBar.add(lblTransparency);
		toolBar.add(spinnerTransparency);

		// add rotation spinner
		// lblRotation.setFont(f);
		// lblRotation.setToolTipText("Rotation in degrees");

		// toolBar.add(lblRotation);
		// toolBar.add(spinnerRotation);

		editor.mainPanel.add(toolBar, BorderLayout.NORTH);

	}

	void removeToolbar() {
		editor.mainPanel.remove(toolBar);
	}

}
