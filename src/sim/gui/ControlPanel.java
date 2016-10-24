// Microprocessor Simulator
// A.Greensted - University of York
// ajg112@ohm.york.ac.uk
// November 2008

package sim.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

public class ControlPanel extends JPanel implements ActionListener
{
	private JButton editButton;
	private JButton stepButton;
	private JButton resetButton;
	private JButton runButton;
	private JSpinner stepsSpinner;
	private JCheckBox runSelect;
	private JCheckBox updateSelect;

	private SpinnerNumberModel numberModel;

	private GUIListener listener;
	
	private SimFrame mSimFrame;
	private String mFilePath;

	public ControlPanel(SimFrame simFrame, String filePath, GUIListener listener)
	{
		this.mSimFrame = simFrame;
		this.mFilePath = filePath;
		
		this.listener = listener;

//		setLayout(new BorderLayout());
		setBorder(new TitledBorder("Simulation Controls"));

		Dimension buttonSize = new Dimension(70,20);
		
		editButton = new JButton("Edit Code");
		editButton.addActionListener(this);
		editButton.setPreferredSize(buttonSize);
		add(editButton);

		stepButton = new JButton("Step");
		stepButton.addActionListener(this);
		stepButton.setPreferredSize(buttonSize);
		add(stepButton);

		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.setPreferredSize(buttonSize);
		add(resetButton);

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		runButton.setPreferredSize(buttonSize);
		add(runButton);

		runSelect = new JCheckBox("Non-stop");
		runSelect.addActionListener(this);
		runSelect.setSelected(true);
		add(runSelect);

		updateSelect = new JCheckBox("Update");
		updateSelect.addActionListener(this);
		updateSelect.setSelected(true);
		add(updateSelect);

		numberModel = new SpinnerNumberModel(1,0,100000,1);
		stepsSpinner = new JSpinner(numberModel);
		stepsSpinner.setValue(100);
		add(stepsSpinner);
	}

	public void actionPerformed(ActionEvent aE)
	{
		Object o = aE.getSource();

		if (o == stepButton) listener.stepPressed();
		else if (o == resetButton) listener.resetPressed();
		else if (o == runButton)
		{
			int steps = numberModel.getNumber().intValue();
			listener.runPressed(steps, runSelect.isSelected(), updateSelect.isSelected());
		}
		else if (o == runSelect)
		{
			if (runSelect.isSelected()) stepsSpinner.setEnabled(false);
			else stepsSpinner.setEnabled(true);
		}
		else if (o == editButton) {
			new SourceEditor(this.mSimFrame, this.mFilePath);
		}
	}

	public void setRunning(boolean running)
	{
		if (running)
		{
			stepButton.setEnabled(false);
			resetButton.setEnabled(false);
			stepsSpinner.setEnabled(false);
			runSelect.setEnabled(false);
			updateSelect.setEnabled(false);

			runButton.setText("Stop");
		}
		else
		{
			stepButton.setEnabled(true);
			resetButton.setEnabled(true);
			if (runSelect.isSelected()) stepsSpinner.setEnabled(false);
			else stepsSpinner.setEnabled(true);
			runSelect.setEnabled(true);
			updateSelect.setEnabled(true);

			runButton.setText("Run");
		}
	}
}
