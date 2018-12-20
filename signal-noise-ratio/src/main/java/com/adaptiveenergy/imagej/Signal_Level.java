package com.adaptiveenergy.imagej;

import ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JRadioButton;

public class Signal_Level implements PlugInFilter{
	public Signal_Level() {
	}
	
	protected ImagePlus image;
	
	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}		
		
		image = imp;				
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		
		width = ip.getWidth();
		height = ip.getHeight();
		
		new SNRWindow(image,ip);
	}

	public void showAbout() {
		IJ.showMessage("Signal level and SNR wizard"
		);
	}
}

class SNRWindow extends JFrame implements RoiListener{
	private boolean FLAG = true;
	
	//panels
	private JPanel contentPanel;
	private JPanel contenPanel_addition;
	private JPanel contenPanel_save;
	
	//above panel
	private JFormattedTextField textField_xstart;
	private JFormattedTextField textField_width;
	private JTextField textField_xend;
	private JFormattedTextField textField_ystart;
	private JFormattedTextField textField_height;
	private JTextField textField_yend;
	private JLabel lblSsampleNumber;
	private JLabel lblMedianSignal;
	private JLabel lblMinSignal;
	private JLabel lblMaxSingal;
	private JLabel lblMeanSingal;
	private JLabel lblStdevValue;
	private JLabel lblMeanSNR;
	
	private JTextField textField_filepath;	
	private JCheckBox chckbxNewCheckBox;
	
	//additional panel
	private JFormattedTextField textField_xstart_addition;
	private JFormattedTextField textField_width_addition;
	private JTextField textField_xend_addition;
	private JFormattedTextField textField_ystart_addition;
	private JFormattedTextField textField_height_addition;
	private JTextField textField_yend_addition;
	private JLabel lblSsampleNumber_addition;
	private JLabel lblMedianSignal_addition;
	private JLabel lblMinSignal_addition;
	private JLabel lblMaxSingal_addition;
	private JLabel lblMeanSingal_addition;
	private JLabel lblStdevValue_addition;
	private JLabel lblMeanSNR_addition;
	private JLabel lblAverageSNRNumber;
		
	private JRadioButton rdbtnEditAboveRoi;
	private JRadioButton rdbtnEditBelowRoi;
	
	private int width;
	private int height;
	
	ImagePlus imp;
	private ImageProcessor ip;
	//RoiManager roimanager;
	Roi roi;
	Roi AboveRoi = null;
	Roi BelowRoi = null;
	Rectangle rect;
	Rectangle drawrect;
	ImagePlus roiImp;
	ImageProcessor roiIp;
	ImageStatistics measure;
	double AboveSNR;
	double BelowSNR;
	NumberFormatter XFieldFormatter;
	NumberFormatter YFieldFormatter;
	NumberFormatter WFieldFormatter;
	NumberFormatter HFieldFormatter;
	NumberFormatter WFieldFormatter_addition;
	NumberFormatter HFieldFormatter_addition;
		
	SNRWindow(ImagePlus imp, ImageProcessor ip) {
		this.ip = ip;
		this.imp = imp;		
		Roi.addRoiListener(this);
		//roimanager = RoiManager.getInstance();
		//roimanager.setVisible(false);
		
		setTitle("Statistics in Window");
    	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    	setBounds(100, 100, 382, 390);   
    	setLayout(null);
    	
    	//rdb to choose the roi
    	rdbtnEditAboveRoi = new JRadioButton("edit above ROI");
		rdbtnEditAboveRoi.setBounds(130, 244, 109, 23);	
		add(rdbtnEditAboveRoi);
		rdbtnEditAboveRoi.setVisible(false);	
		
		rdbtnEditAboveRoi.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(rdbtnEditAboveRoi.isSelected()) {
					textField_xstart_addition.setEditable(false);
					textField_width_addition.setEditable(false);
					textField_ystart_addition.setEditable(false);
					textField_height_addition.setEditable(false);
					textField_xstart.setEditable(true);
					textField_width.setEditable(true);
					textField_ystart.setEditable(true);
					textField_height.setEditable(true);
					
					//roimanager.setRoi();
					//roi=roimanager.getRoi(0);
					BelowRoi = roi;
					roi = AboveRoi;
					imp.setRoi(roi);
					rdbtnEditBelowRoi.setSelected(false);
				}
			}
		});
		
		rdbtnEditBelowRoi = new JRadioButton("edit below ROI");
		rdbtnEditBelowRoi.setBounds(250, 244, 109, 23);		
		rdbtnEditBelowRoi.setSelected(true);
		add(rdbtnEditBelowRoi);
		rdbtnEditBelowRoi.setVisible(false);
		
		rdbtnEditBelowRoi.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(rdbtnEditBelowRoi.isSelected()) {
					textField_xstart.setEditable(false);
					textField_width.setEditable(false);
					textField_ystart.setEditable(false);
					textField_height.setEditable(false);
					textField_xstart_addition.setEditable(true);
					textField_width_addition.setEditable(true);
					textField_ystart_addition.setEditable(true);
					textField_height_addition.setEditable(true);

					//roi=roimanager.getRoi(1);
					AboveRoi = roi;
					roi = BelowRoi;
					imp.setRoi(roi);
					rdbtnEditAboveRoi.setSelected(false);
				}
			}
		});
		
		if(imp.getRoi()!=null) {
			roi = imp.getRoi();
			rect = roi.getBounds();
			roiImp = imp.duplicate();
			roiIp = roiImp.getProcessor();
			measure = roiIp.getStatistics();
			updateAboveROI();
		}
		
		//set textfield format
		width = ip.getWidth();
		height = ip.getHeight();
		
		NumberFormat FieldFormat = NumberFormat.getIntegerInstance();
		XFieldFormatter = new NumberFormatter(FieldFormat);
		XFieldFormatter.setMinimum(0);
		XFieldFormatter.setMaximum(width);
		YFieldFormatter = new NumberFormatter(FieldFormat);
		YFieldFormatter.setMinimum(0);
		YFieldFormatter.setMaximum(height);
		WFieldFormatter = new NumberFormatter(FieldFormat);
		WFieldFormatter.setMinimum(0);
		WFieldFormatter.setMaximum(height);
		HFieldFormatter = new NumberFormatter(FieldFormat);
		HFieldFormatter.setMinimum(0);
		HFieldFormatter.setMaximum(height);
		WFieldFormatter_addition = new NumberFormatter(FieldFormat);
		WFieldFormatter_addition.setMinimum(0);
		WFieldFormatter.setMaximum(height);
		HFieldFormatter_addition = new NumberFormatter(FieldFormat);
		HFieldFormatter_addition.setMinimum(0);
		HFieldFormatter_addition.setMaximum(height);
		
		contentPanel = new JPanel();
		contenPanel_save = new JPanel();
		contenPanel_save.setBounds(0, 270, 382, 300);
		contenPanel_addition = new JPanel();
		addPanel();
		addSavePanel();
		addAdditionPanel();
		add(contentPanel);	
		add(contenPanel_save);
		add(contenPanel_addition);
		contentPanel.setVisible(true);
		contenPanel_save.setVisible(true);
		contenPanel_addition.setVisible(false);
		setVisible(true);	
	}
	
    private void addAdditionPanel() {
    	contenPanel_addition.setBounds(0, 270, 382, 270);
    	contenPanel_addition.setBorder(new EmptyBorder(5, 5, 5, 5));  
    	contenPanel_addition.setLayout(null);
    	JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 2, 345, 2);
		contenPanel_addition.add(separator_3);
    	JLabel lblHorizontal = new JLabel("horizontal");
    	lblHorizontal.setHorizontalAlignment(SwingConstants.RIGHT);
    	lblHorizontal.setBounds(0, 31, 62, 14);
		contenPanel_addition.add(lblHorizontal);		
		JLabel lblVertical = new JLabel("vertical");
		lblVertical.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVertical.setBounds(10, 58, 52, 14);
		contenPanel_addition.add(lblVertical);		
		JLabel lblStart = new JLabel("start");
		lblStart.setHorizontalAlignment(SwingConstants.CENTER);
		lblStart.setBounds(92, 11, 46, 14);
		contenPanel_addition.add(lblStart);		
		JLabel lblSize = new JLabel("size");
		lblSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblSize.setBounds(186, 11, 46, 14);
		contenPanel_addition.add(lblSize);		
		JLabel lblEnd = new JLabel("end");
		lblEnd.setHorizontalAlignment(SwingConstants.CENTER);
		lblEnd.setBounds(282, 11, 46, 14);
		contenPanel_addition.add(lblEnd);		
		JLabel lblSamplesInWindows = new JLabel("samples in windows : ");
		lblSamplesInWindows.setBounds(10, 83, 128, 14);
		contenPanel_addition.add(lblSamplesInWindows);		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 107, 345, 2);
		contenPanel_addition.add(separator);		
		JLabel lblMin = new JLabel("min");
		lblMin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMin.setBounds(55, 120, 46, 14);
		contenPanel_addition.add(lblMin);		
		JLabel lblMedian = new JLabel("median");
		lblMedian.setHorizontalAlignment(SwingConstants.CENTER);
		lblMedian.setBounds(160, 120, 46, 14);
		contenPanel_addition.add(lblMedian);		
		JLabel lblMax = new JLabel("max");
		lblMax.setHorizontalAlignment(SwingConstants.CENTER);
		lblMax.setBounds(264, 120, 46, 14);
		contenPanel_addition.add(lblMax);	
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 172, 345, 2);
		contenPanel_addition.add(separator_1);		
		JLabel lblMean = new JLabel("mean");
		lblMean.setHorizontalAlignment(SwingConstants.CENTER);
		lblMean.setBounds(55, 185, 46, 14);
		contenPanel_addition.add(lblMean);		
		JLabel lblStdev = new JLabel("stdev");
		lblStdev.setHorizontalAlignment(SwingConstants.CENTER);
		lblStdev.setBounds(160, 185, 46, 14);
		contenPanel_addition.add(lblStdev);		
		JLabel lblMeanstdev = new JLabel("SNR");
		lblMeanstdev.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanstdev.setBounds(256, 185, 69, 14);
		contenPanel_addition.add(lblMeanstdev);		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(10, 235, 345, 2);
		contenPanel_addition.add(separator_2);

		textField_xstart_addition = new JFormattedTextField(XFieldFormatter);
		textField_xstart_addition.setBounds(72, 28, 86, 20);
		
		//keyborad input update to roi
		textField_xstart_addition.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {	
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
		        	roi.setLocation((int)textField_xstart_addition.getValue(),temprect.y);
		        	imp.setRoi(roi);
		        	roiModified(imp,3);
	        	}
	        	WFieldFormatter_addition.setMaximum(width-(int)textField_xstart_addition.getValue());
	        }
	    });
		contenPanel_addition.add(textField_xstart_addition);
		textField_xstart_addition.setColumns(10);		
		textField_width_addition = new JFormattedTextField(WFieldFormatter_addition);
		textField_width_addition.setBounds(168, 28, 86, 20);
		textField_width_addition.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
		        	Roi temproi = new Roi(temprect.x,temprect.y,(int)textField_width_addition.getValue(),temprect.height);
		        	roi = temproi;
		        	imp.setRoi(roi); 
		        	roiModified(imp,3);
	        	}   
	        }
	    });
		contenPanel_addition.add(textField_width_addition);
		textField_width_addition.setColumns(10);		
		textField_xend_addition = new JTextField();
		textField_xend_addition.setEditable(false);
		textField_xend_addition.setBounds(264, 28, 86, 20);
		contenPanel_addition.add(textField_xend_addition);
		textField_xend_addition.setColumns(10);		
		textField_ystart_addition = new JFormattedTextField(YFieldFormatter);
		textField_ystart_addition.setBounds(72, 55, 86, 20);
		textField_ystart_addition.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {	
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
	        		roi.setLocation(temprect.x,(int)textField_ystart_addition.getValue());
	        		imp.setRoi(roi);
	        		roiModified(imp,3);
	        	}
	        	HFieldFormatter_addition.setMaximum(height-(int)textField_ystart_addition.getValue());
	        }
	    });
		contenPanel_addition.add(textField_ystart_addition);
		textField_ystart_addition.setColumns(10);		
		textField_height_addition = new JFormattedTextField(HFieldFormatter_addition);
		textField_height_addition.setBounds(168, 55, 86, 20);
		textField_height_addition.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
		        	Roi temproi = new Roi(temprect.x,temprect.y,temprect.width,(int)textField_height_addition.getValue());
		        	roi = temproi;
		        	imp.setRoi(roi); 
		        	roiModified(imp,3);
	        	}   
	        }
	    });
		contenPanel_addition.add(textField_height_addition);
		textField_height_addition.setColumns(10);		
		textField_yend_addition = new JTextField();
		textField_yend_addition.setEditable(false);
		textField_yend_addition.setBounds(264, 55, 86, 20);
		contenPanel_addition.add(textField_yend_addition);
		textField_yend_addition.setColumns(10);
		lblSsampleNumber_addition = new JLabel("");
		lblSsampleNumber_addition.setBounds(180, 83, 52, 14);
		contenPanel_addition.add(lblSsampleNumber_addition);		
		lblMinSignal_addition = new JLabel("");
		lblMinSignal_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinSignal_addition.setBounds(55, 145, 46, 14);
		contenPanel_addition.add(lblMinSignal_addition);		
		lblMedianSignal_addition = new JLabel("");
		lblMedianSignal_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblMedianSignal_addition.setBounds(160, 145, 46, 14);
		contenPanel_addition.add(lblMedianSignal_addition);		
		lblMaxSingal_addition = new JLabel("");
		lblMaxSingal_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblMaxSingal_addition.setBounds(264, 145, 46, 14);
		contenPanel_addition.add(lblMaxSingal_addition);		
		lblMeanSingal_addition = new JLabel("");
		lblMeanSingal_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanSingal_addition.setBounds(39, 210, 76, 14);
		contenPanel_addition.add(lblMeanSingal_addition);		
		lblStdevValue_addition = new JLabel("");
		lblStdevValue_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblStdevValue_addition.setBounds(144, 210, 76, 14);
		contenPanel_addition.add(lblStdevValue_addition);		
		lblMeanSNR_addition = new JLabel();
		lblMeanSNR_addition.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanSNR_addition.setBounds(241, 210, 107, 14);
		contenPanel_addition.add(lblMeanSNR_addition);	
		
		JLabel lblAverageSNR = new JLabel("average SNR : ");
		lblAverageSNR.setBounds(10, 245, 128, 14);
		contenPanel_addition.add(lblAverageSNR);		
		lblAverageSNRNumber = new JLabel("");
		lblAverageSNRNumber.setBounds(180, 245, 52, 14);
		contenPanel_addition.add(lblAverageSNRNumber);
		
	}

	private void addSavePanel() {		
    	contenPanel_save.setBorder(new EmptyBorder(5, 5, 5, 5));  
    	contenPanel_save.setLayout(null);
    	JSeparator separator_4 = new JSeparator();
		separator_4.setBounds(10, 2, 345, 2);
		contenPanel_save.add(separator_4);		
    	JButton btnMem = new JButton("Mem");
    	btnMem.setBounds(10, 47, 69, 23);
    	contenPanel_save.add(btnMem);
    	JButton btnRecall = new JButton("Recall");
    	btnRecall.setBounds(89, 47, 69, 23);
    	contenPanel_save.add(btnRecall);
    	JButton btnRecord = new JButton("Record");
    	btnRecord.setBounds(256, 47, 99, 23);
    	contenPanel_save.add(btnRecord);
    	textField_filepath = new JTextField();
		textField_filepath.setBounds(10, 15, 262, 20);
		contenPanel_save.add(textField_filepath);
		textField_filepath.setColumns(10);
    	JButton button = new JButton("...");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		button.setBounds(287, 13, 68, 23);
		contenPanel_save.add(button);
	}

	private void addPanel() { 	
    	contentPanel.setBounds(0, 0, 382, 270);    	
    	contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));   	
    	contentPanel.setLayout(null);    	
    	JLabel lblhorizontal = new JLabel("horizontal");
		lblhorizontal.setHorizontalAlignment(SwingConstants.RIGHT);
		lblhorizontal.setBounds(0, 31, 62, 14);
		contentPanel.add(lblhorizontal);		
		JLabel lblVertical = new JLabel("vertical");
		lblVertical.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVertical.setBounds(10, 58, 52, 14);
		contentPanel.add(lblVertical);		
		JLabel lblStart = new JLabel("start");
		lblStart.setHorizontalAlignment(SwingConstants.CENTER);
		lblStart.setBounds(92, 11, 46, 14);
		contentPanel.add(lblStart);		
		JLabel lblSize = new JLabel("size");
		lblSize.setHorizontalAlignment(SwingConstants.CENTER);
		lblSize.setBounds(186, 11, 46, 14);
		contentPanel.add(lblSize);		
		JLabel lblEnd = new JLabel("end");
		lblEnd.setHorizontalAlignment(SwingConstants.CENTER);
		lblEnd.setBounds(282, 11, 46, 14);
		contentPanel.add(lblEnd);		
		JLabel lblSamplesInWindows = new JLabel("samples in windows : ");
		lblSamplesInWindows.setBounds(10, 83, 128, 14);
		contentPanel.add(lblSamplesInWindows);		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 107, 345, 2);
		contentPanel.add(separator);		
		JLabel lblMin = new JLabel("min");
		lblMin.setHorizontalAlignment(SwingConstants.CENTER);
		lblMin.setBounds(55, 120, 46, 14);
		contentPanel.add(lblMin);		
		JLabel lblMedian = new JLabel("median");
		lblMedian.setHorizontalAlignment(SwingConstants.CENTER);
		lblMedian.setBounds(160, 120, 46, 14);
		contentPanel.add(lblMedian);		
		JLabel lblMax = new JLabel("max");
		lblMax.setHorizontalAlignment(SwingConstants.CENTER);
		lblMax.setBounds(264, 120, 46, 14);
		contentPanel.add(lblMax);	
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 172, 345, 2);
		contentPanel.add(separator_1);		
		JLabel lblMean = new JLabel("mean");
		lblMean.setHorizontalAlignment(SwingConstants.CENTER);
		lblMean.setBounds(55, 185, 46, 14);
		contentPanel.add(lblMean);		
		JLabel lblStdev = new JLabel("stdev");
		lblStdev.setHorizontalAlignment(SwingConstants.CENTER);
		lblStdev.setBounds(160, 185, 46, 14);
		contentPanel.add(lblStdev);		
		JLabel lblMeanstdev = new JLabel("SNR");
		lblMeanstdev.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanstdev.setBounds(256, 185, 69, 14);
		contentPanel.add(lblMeanstdev);		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(10, 235, 345, 2);
		contentPanel.add(separator_2);		
		
		textField_xstart = new JFormattedTextField(XFieldFormatter);
		textField_xstart.setBounds(72, 28, 86, 20);
		textField_xstart.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
	        		roi.setLocation((int)textField_xstart.getValue(),temprect.y);
	        		imp.setRoi(roi);
	        		roiModified(imp,3);
	        	}
	        	WFieldFormatter.setMaximum(width-(int)textField_xstart.getValue());
	        }
	    });
		contentPanel.add(textField_xstart);
		textField_xstart.setColumns(10);		
		textField_width = new JFormattedTextField(WFieldFormatter);
		textField_width.setBounds(168, 28, 86, 20);
		textField_width.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
		        	Roi temproi = new Roi(temprect.x,temprect.y,(int)textField_width.getValue(),temprect.height);
		        	roi = temproi;
		        	imp.setRoi(roi); 
		        	roiModified(imp,3);
	        	}        	       	
	        }
	    });
		contentPanel.add(textField_width);
		textField_width.setColumns(10);		
		textField_xend = new JTextField();
		textField_xend.setEditable(false);
		textField_xend.setBounds(264, 28, 86, 20);
		contentPanel.add(textField_xend);
		textField_xend.setColumns(10);		
		textField_ystart = new JFormattedTextField(YFieldFormatter);
		textField_ystart.setBounds(72, 55, 86, 20);
		textField_ystart.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
		        	roi.setLocation(temprect.x,(int)textField_ystart.getValue());
		        	imp.setRoi(roi);
		        	roiModified(imp,3);
	        	}  
	        	HFieldFormatter.setMaximum(height-(int)textField_ystart.getValue());
	        }
	    });
		contentPanel.add(textField_ystart);
		textField_ystart.setColumns(10);		
		textField_height = new JFormattedTextField(HFieldFormatter);
		textField_height.setBounds(168, 55, 86, 20);
		textField_height.addPropertyChangeListener("value", new PropertyChangeListener()
	    {
	        @Override
	        public void propertyChange(PropertyChangeEvent evt)
	        {	
	        	if(FLAG == false) {
	        		Rectangle temprect = roi.getBounds();
	        		Roi temproi = new Roi(temprect.x,temprect.y,temprect.width,(int)textField_height.getValue());
	        		roi = temproi;
	        		imp.setRoi(roi);
	        		roiModified(imp,3);
	        	}        	
	        }
	    });
		contentPanel.add(textField_height);
		textField_height.setColumns(10);		
		textField_yend = new JTextField();
		textField_yend.setEditable(false);
		textField_yend.setBounds(264, 55, 86, 20);
		contentPanel.add(textField_yend);
		textField_yend.setColumns(10);
		lblSsampleNumber = new JLabel("");
		lblSsampleNumber.setBounds(180, 83, 52, 14);
		contentPanel.add(lblSsampleNumber);		
		lblMinSignal = new JLabel("");
		lblMinSignal.setHorizontalAlignment(SwingConstants.CENTER);
		lblMinSignal.setBounds(55, 145, 46, 14);
		contentPanel.add(lblMinSignal);		
		lblMedianSignal = new JLabel("");
		lblMedianSignal.setHorizontalAlignment(SwingConstants.CENTER);
		lblMedianSignal.setBounds(160, 145, 46, 14);
		contentPanel.add(lblMedianSignal);		
		lblMaxSingal = new JLabel("");
		lblMaxSingal.setHorizontalAlignment(SwingConstants.CENTER);
		lblMaxSingal.setBounds(264, 145, 46, 14);
		contentPanel.add(lblMaxSingal);		
		lblMeanSingal = new JLabel("");
		lblMeanSingal.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanSingal.setBounds(39, 210, 76, 14);
		contentPanel.add(lblMeanSingal);		
		lblStdevValue = new JLabel("");
		lblStdevValue.setHorizontalAlignment(SwingConstants.CENTER);
		lblStdevValue.setBounds(144, 210, 76, 14);
		contentPanel.add(lblStdevValue);		
		lblMeanSNR = new JLabel();
		lblMeanSNR.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeanSNR.setBounds(241, 210, 107, 14);
		contentPanel.add(lblMeanSNR);	
		chckbxNewCheckBox = new JCheckBox("additional ROI");
		chckbxNewCheckBox.setBounds(10, 244, 115, 23);
		contentPanel.add(chckbxNewCheckBox);
		//lblhorizontal.setText(String.valueOf(width));
		//lblVertical.setText(String.valueOf(height));
		
		chckbxNewCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(chckbxNewCheckBox.isSelected()) {
					setBounds(100, 100, 382, 662);		
					contenPanel_save.setBounds(0, 540, 382, 300);
					contenPanel_addition.setVisible(true);
					rdbtnEditBelowRoi.setVisible(true);
					rdbtnEditAboveRoi.setVisible(true);
					textField_xstart.setEditable(false);
					textField_width.setEditable(false);
					textField_ystart.setEditable(false);
					textField_height.setEditable(false);
					textField_xstart_addition.setEditable(true);
					textField_width_addition.setEditable(true);
					textField_ystart_addition.setEditable(true);
					textField_height_addition.setEditable(true);
					
					//roi = roimanager.getRoi(1);
					AboveRoi = roi;
					roi = BelowRoi;
					imp.setRoi(roi);								
				}
				else{
					setBounds(100, 100, 382, 390);   
					contenPanel_save.setBounds(0, 270, 382, 300);
					contenPanel_addition.setVisible(false);
					rdbtnEditBelowRoi.setVisible(false);
					rdbtnEditAboveRoi.setVisible(false);
					textField_xstart.setEditable(true);
					textField_width.setEditable(true);
					textField_ystart.setEditable(true);
					textField_height.setEditable(true);
					
					//roi = roimanager.getRoi(0);
					if(rdbtnEditBelowRoi.isSelected()) {
						BelowRoi = roi;
						roi = AboveRoi;
						imp.setRoi(roi);
					}
					else {
						rdbtnEditBelowRoi.setSelected(true);
						roi = AboveRoi;
						imp.setRoi(roi);
					}
				}
			}
		});
	}
    int i=1;
	@Override
	public void roiModified(ImagePlus imp, int id) {
		FLAG = true;
		if(id==DELETED) {
			if(!chckbxNewCheckBox.isSelected()) {
				deleteAboveROI();
			}
			else {
				if(rdbtnEditAboveRoi.isSelected()) {
					deleteAboveROI();
				}
				else {
					deleteBelowROI();
				}
			}
		}
		else{
			roi = imp.getRoi();
			rect = roi.getBounds();
			roiImp = imp.duplicate();
			roiIp = roiImp.getProcessor();
			measure = roiIp.getStatistics();
			if(!chckbxNewCheckBox.isSelected()) {
				updateAboveROI();
			}
			else {
				if(rdbtnEditAboveRoi.isSelected()) {
					updateAboveROI();
				}
				else {
					updateBelowROI();
				}
			}
			if((lblMeanSNR.getText()!=null) && (lblMeanSNR_addition.getText()!=null)) {
				lblAverageSNRNumber.setText(String.valueOf(round((BelowSNR+AboveSNR)/2, 3)));
			}
			else {
				lblAverageSNRNumber.setText(null);
			}
		}
		FLAG = false;
	}

	private void deleteBelowROI() {
		textField_xstart_addition.setText(null);
		textField_width_addition.setText(null);
		textField_xend_addition.setText(null);
		textField_ystart_addition.setText(null);
		textField_height_addition.setText(null);
		textField_yend_addition.setText(null);	
		lblSsampleNumber_addition.setText(null);
		lblMinSignal_addition.setText(null);
		lblMedianSignal_addition.setText(null);
		lblMaxSingal_addition.setText(null);
		lblMeanSingal_addition.setText(null);
		lblStdevValue_addition.setText(null);
		lblMeanSNR_addition.setText(null);			
		/*if(roimanager.getRoi(1) != null) {
			roimanager.add(null,1);
		}
		roi=null;*/
		BelowRoi = null;
	}

	private void deleteAboveROI() {
		textField_xstart.setText(null);
		textField_width.setText(null);
		textField_xend.setText(null);
		textField_ystart.setText(null);
		textField_height.setText(null);
		textField_yend.setText(null);	
		lblSsampleNumber.setText(null);
		lblMinSignal.setText(null);
		lblMedianSignal.setText(null);
		lblMaxSingal.setText(null);
		lblMeanSingal.setText(null);
		lblStdevValue.setText(null);
		lblMeanSNR.setText(null);	
		/*if(roimanager.getRoi(0) != null) {
			roimanager.add(null,0);
		}
		roi=null;*/
		AboveRoi = null;
	}

	private void updateBelowROI() {
		textField_xstart_addition.setValue(rect.x);
		textField_width_addition.setValue(rect.width);
		textField_xend_addition.setText(String.valueOf(rect.x+rect.width));
		textField_ystart_addition.setValue(rect.y);
		textField_height_addition.setValue(rect.height);
		textField_yend_addition.setText(String.valueOf(rect.height+rect.y));	
		lblSsampleNumber_addition.setText(String.valueOf(roiIp.getPixelCount()));
		lblMinSignal_addition.setText(String.valueOf((int)measure.min));
		lblMedianSignal_addition.setText(String.valueOf((int)measure.median));
		lblMaxSingal_addition.setText(String.valueOf((int)measure.max));
		lblMeanSingal_addition.setText(String.valueOf(round(measure.mean,2)));
		lblStdevValue_addition.setText(String.valueOf(round(measure.stdDev,2)));
		lblMeanSNR_addition.setText(String.valueOf(round(measure.mean/measure.stdDev,3)));	
		BelowSNR = measure.mean/measure.stdDev;
	}

	private void updateAboveROI() {
		textField_xstart.setValue(rect.x);
		textField_width.setValue(rect.width);
		textField_xend.setText(String.valueOf(rect.x+rect.width));
		textField_ystart.setValue(rect.y);
		textField_height.setValue(rect.height);
		textField_yend.setText(String.valueOf(rect.height+rect.y));	
		lblSsampleNumber.setText(String.valueOf(roiIp.getPixelCount()));
		lblMinSignal.setText(String.valueOf((int)measure.min));
		lblMedianSignal.setText(String.valueOf((int)measure.median));
		lblMaxSingal.setText(String.valueOf((int)measure.max));
		lblMeanSingal.setText(String.valueOf(round(measure.mean,2)));
		lblStdevValue.setText(String.valueOf(round(measure.stdDev,2)));
		lblMeanSNR.setText(String.valueOf(round(measure.mean/measure.stdDev,3)));	
		AboveSNR = measure.mean/measure.stdDev;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
