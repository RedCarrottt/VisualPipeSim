// Microprocessor Simulator
// A.Greensted - University of York
// ajg112@ohm.york.ac.uk
// November 2008

package sim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import sim.Instruction;
import sim.Processor;
import sim.Sim;

public class SimFrame
{
	public enum State {RUNNING, NOT_RUNNING};

	private JFrame thisFrame = null;
	private RegisterFilePanel regFilePanel;
	private ControlPanel controlPanel;
	private ProgCounterPanel pcPanel;
	private DataMemoryPanel dataMemPanel;
	private VariablesPanel signalsPanel;
	private VariablesPanel controlsPanel;
	private SourceViewer sourceViewer;
	private PipelineDisplay pipelineDisplay;
//	private InstructionDisplay insnDisplay;
	private ProcessorControls procControls;

	private GUIListener listener;
	
	private Processor mProcessor;
	private Sim mSim;
	
	public void reloadFile(String asmFilename) {
		this.thisFrame.dispose();
		this.mSim.reloadFile(asmFilename);
	}
	
	public JFrame getJFrame() {
		return this.thisFrame;
	}

	public SimFrame(Sim sim, Instruction[] program, Processor processor, GUIListener listener, File asmFile)
	{
		this.listener = listener;
		
		this.mSim = sim;
		this.mProcessor = processor;

		JFrame frame = this.thisFrame
				= new JFrame("Microprocessor Simulator (" + asmFile.getName() + ") - A.Greensted v0.5 (Enhanced by RedCarrottt)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ArrayList<Image> imageList = new ArrayList<Image>();
		imageList.add(new ImageIcon("images/icon.png").getImage());
		frame.setIconImages(imageList);

		JPanel cP = new JPanel(new BorderLayout());
		frame.getContentPane().add(cP);
		cP.setLayout(new BorderLayout(0,4));
		cP.setBorder(new EmptyBorder(4,4,4,4));
		cP.setBackground(Color.DARK_GRAY);

		pcPanel = new ProgCounterPanel();
		regFilePanel = new RegisterFilePanel();
		dataMemPanel = new DataMemoryPanel();
		controlPanel = new ControlPanel(this, asmFile.getAbsolutePath(), listener);
		signalsPanel = new VariablesPanel("sim.Processor$Signals", "Signals");
		controlsPanel = new VariablesPanel("sim.Processor$Controls", "Controls");
		sourceViewer = new SourceViewer(asmFile, program);
		pipelineDisplay = new PipelineDisplay(program);
//		insnDisplay = new InstructionDisplay(program);
		procControls = new ProcessorControls(processor);

		JPanel panel;

		// Info Panel
		JPanel infoPanel = new JPanel();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(pcPanel);
		panel.add(procControls);

		infoPanel.add(panel);
		infoPanel.add(regFilePanel);
		infoPanel.add(dataMemPanel);

		// Variables Panel
		JPanel varPanel = new JPanel();
		varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.X_AXIS));
		varPanel.add(signalsPanel);
		varPanel.add(controlsPanel);


		// Tabbed Pane
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addTab("Info", infoPanel);
		tabbedPane.addTab("Internal Signals", varPanel);
//		tabbedPane.addTab("Display", new JLabel("XXX Pipeline Display Options"));
//		tabbedPane.addTab("Branch Prediction", new JLabel("XXX BP"));

		// Left Panel
		JPanel leftPanel = new JPanel(new BorderLayout(0,4));
		leftPanel.add(sourceViewer, BorderLayout.CENTER);
		leftPanel.add(controlPanel, BorderLayout.SOUTH);
		leftPanel.setBackground(Color.DARK_GRAY);

		// Bottom Panel
		JPanel bottomPanel = new JPanel(new BorderLayout(4,0));
		bottomPanel.add(leftPanel, BorderLayout.WEST);
		bottomPanel.add(tabbedPane, BorderLayout.CENTER);
		bottomPanel.setBackground(Color.DARK_GRAY);

		cP.add(pipelineDisplay, BorderLayout.NORTH);
		cP.add(bottomPanel, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);
	}

	public void update(Processor processor, boolean all)
	{
		regFilePanel.update(processor);
		dataMemPanel.update(processor);
		pcPanel.update(processor);
		pipelineDisplay.update(processor);

		if (all)
		{
			signalsPanel.update(processor.signals);
			controlsPanel.update(processor.controls);
			sourceViewer.update(processor);
//			insnDisplay.update(processor);
		}
	}

	public void setState(State state)
	{
		switch (state)
		{
			case RUNNING :
				controlPanel.setRunning(true);
				break;

			case NOT_RUNNING :
				controlPanel.setRunning(false);
				break;
		}
	}
}
