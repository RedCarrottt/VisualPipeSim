package sim.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SourceEditor implements WindowListener {
	SimFrame mSimFrame;
	String mFileName;
	
	JDialog mMainDialog;
	JPanel mMainPanel;
	JScrollPane mSourceEditorScrollPane;
	JTextArea mSourceEditorArea;
	
	public SourceEditor(SimFrame simFrame, String fileName) {
		this.mSimFrame = simFrame;
		this.mFileName = fileName;
		initializeUI(this.mSimFrame.getJFrame());
	}
	
	private void initializeUI(JFrame ownerFrame) {
		mMainDialog = new JDialog(ownerFrame);
		mMainDialog.setTitle("Edit Source");
		mMainDialog.setSize(500, 450);
		mMainDialog.setLocation(100, 100);
		mMainDialog.addWindowListener(this);
		
		mMainPanel = new JPanel(new FlowLayout());
		mMainDialog.getContentPane().add(mMainPanel);
		
		mSourceEditorArea = new JTextArea();
		mSourceEditorArea.setPreferredSize(new Dimension(480, 410));
		mSourceEditorArea.setEditable(true);
		mSourceEditorArea.setLineWrap(true);
		mSourceEditorArea.setBorder(BorderFactory.createEtchedBorder());
		
		mSourceEditorScrollPane = new JScrollPane(mSourceEditorArea);
		mMainPanel.add(mSourceEditorScrollPane);
		
		loadFile(this.mFileName);
		
		mMainDialog.setModal(true);
		mMainDialog.setVisible(true);
	}
	private void loadFile(String fileName) {
		try {
			String line = null;

			BufferedReader in = new BufferedReader(new FileReader(fileName));

			while ((line = in.readLine()) != null) {
				mSourceEditorArea.append(line + "\n");
			}
			
			in.close();
		} catch (FileNotFoundException fnfE) {
			// Not implemented
		} catch (IOException ioE) {
			// Not implemented
		}
	}
	
	private void saveFile(String fileName) {
		try {			
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			String str = mSourceEditorArea.getText();
			out.write(str);
			out.flush();
			
			out.close();
		} catch (FileNotFoundException fnfE) {
			// Not implemented
		} catch (IOException ioE) {
			// Not implemented
		}
		return;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// Not implemented
	}

	@Override
	public void windowClosing(WindowEvent e) {
		saveFile(this.mFileName);
		this.mSimFrame.reloadFile(this.mFileName);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// Not implemented
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// Not implemented
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// Not implemented
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// Not implemented
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// Not implemented		
	}
}
