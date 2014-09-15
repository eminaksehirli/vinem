package cart.gui2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class CartiView {

	private JFrame mainFrame;
	private CartiController controller;
	private JList<String> dimList;
	private JTextField dimDropArea;
	private JSlider orderSlider_1;
	private JSlider orderSlider_2;
	private JSlider kSlider;
	private CartiPanel cartiPanel;
	
	public CartiView() {
		mainFrame = new JFrame("Carti");
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.setLayout(new BorderLayout(10,10));
	}
	
	public void setController(CartiController controller) {
		this.controller = controller;
	}
	
	public void init(List<String> allDims, int maxK, int[][] matrixToShow) {
		// visualPanel contains the visual representation
		JPanel visualPanel = createVerticalBoxPanel(700,700);
		// controlsPanel contains the buttons/sliders/...
		JPanel controlsPanel = createVerticalBoxPanel(300,700);
		
		// VISUAL GOES HERE
		// add cartPane
		// TODO add selection
		cartiPanel = new CartiPanel(matrixToShow);
		visualPanel.add(cartiPanel);
		
		// CONTROLS GO HERE
		// add list of dimensions
		dimList = createDimList(allDims, createDimListTransferHandler());
        JScrollPane listView = new JScrollPane(dimList);
        listView.setMaximumSize(new Dimension(200, 200));
        listView.setBorder(BorderFactory.createTitledBorder("Dimensions"));
		controlsPanel.add(listView);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
	
		// add dimension drop area
		JLabel dimDropAreaLabel = new JLabel("Dimension drop area");
		dimDropAreaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(dimDropAreaLabel);
		dimDropArea = createDimDropArea(createDimDropAreaListener("0"), createDimDropAreaTransferHandler());
		controlsPanel.add(dimDropArea);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		
        // add slider for k
     	JLabel kSliderLabel = new JLabel("k");
     	kSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(kSliderLabel);
        kSlider = createSlider(1,maxK, createKSliderListener(1));
        controlsPanel.add(kSlider);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // add slider for order_1
     	JLabel order_1SliderLabel = new JLabel("order_1");
     	order_1SliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(order_1SliderLabel);
        orderSlider_1 = createSlider(0, allDims.size()-1, createOrderSliderListener(0));
        controlsPanel.add(orderSlider_1);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // add slider for order_2
        JLabel order_2SliderLabel = new JLabel("order_2");
     	order_2SliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(order_2SliderLabel);
        orderSlider_2 = createSlider(0, allDims.size()-1, createOrderSliderListener(0));
        controlsPanel.add(orderSlider_2);
		
        
        // add visual and controls panel to the frame
        mainFrame.add(visualPanel, BorderLayout.CENTER);
        mainFrame.add(controlsPanel, BorderLayout.LINE_END);
	}
	
	private JList<String> createDimList(List<String> allDims, TransferHandler handler) {
		DefaultListModel<String> listModel = new DefaultListModel<String>();	
		for (int i = 0; i < allDims.size(); i++) {
			listModel.addElement(allDims.get(i));
		}   
		
        JList<String> list = new JList<String>(listModel);
        list.setVisibleRowCount(-1);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDragEnabled(true);
        list.setTransferHandler(handler);
        
        return list;
	}
	
	private TransferHandler createDimListTransferHandler() {
		TransferHandler handler = new TransferHandler() {
 
            public boolean canImport(TransferHandler.TransferSupport info) {
            	// we don't want to import to this list
                return false;
            }
             
            public int getSourceActions(JComponent c) {
                return COPY;
            }
             
            protected Transferable createTransferable(JComponent c) {
                JList list = (JList)c;
                Object[] values = list.getSelectedValues();
         
                StringBuffer buff = new StringBuffer();
 
                for (int i = 0; i < values.length; i++) {
                    Object val = values[i];
                    buff.append(val == null ? "" : val.toString());
                    if (i != values.length - 1) {
                        buff.append("\n");
                    }
                }
                return new StringSelection(buff.toString());
            }
        };
		
		return handler;
	}

	private JTextField createDimDropArea(DocumentListener listener, TransferHandler handler) {
		JTextField area = new JTextField(100);
		area.setText("0");
        area.setEditable(false);
        area.setMaximumSize(new Dimension(100,30));
        area.setHorizontalAlignment(JTextField.CENTER);
        area.setTransferHandler(handler);
        area.getDocument().addDocumentListener(listener); 
        
        return area;
	} 
	
	private DocumentListener createDimDropAreaListener(final String initValue) {
		DocumentListener listener = new DocumentListener() {
    	
			private String previousVal = initValue;
			
	    	public void insertUpdate(DocumentEvent e) {
	    		String newVal = "";
	    		try {
					newVal = e.getDocument().getText(0, e.getDocument().getLength());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
	    		
	    		// notify controller the dimensions to show has changed
	    		if (!previousVal.equals(newVal)) {
	    			previousVal = newVal;
	    			controller.dimsToShowChanged();
	    		}
			}
	
	    	// do nothing for these
			public void changedUpdate(DocumentEvent e) { }
			public void removeUpdate(DocumentEvent e) { }
		};
    	
		return listener;
	}
	
	private TransferHandler createDimDropAreaTransferHandler() {
		TransferHandler handler = new TransferHandler() {
    	 
        public boolean canImport(TransferHandler.TransferSupport info) {
            // we only import Strings
            if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
            
            // Get the String that is being dropped.
            String data;
            try {
                data = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            }
            catch (Exception e) { return false; }
            
            // we only import Strings which are in the dimList
            for (int i = 0; i < dimList.getModel().getSize(); i++) {
            	if (data.equals(dimList.getModel().getElementAt(i))) {
            		return true;
            	}
            }
            
            // the String was not in the dimList
            return false;
        }
    
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }
             
            // Check for String flavor
            if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            // Get the string that is being dropped.
            String data;
            try {
                data = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            }
            catch (Exception e) { return false; }
            
            // TODO make the content of the textfield reflect what we want
            JTextField dim = (JTextField)info.getComponent();
            String previous = dim.getText();
            dim.setText(previous + ", " + data);
                         
            return true;
        }
         
        public int getSourceActions(JComponent c) {
            return NONE;
        }
    };
    
	return handler;
}
	
	private JSlider createSlider(int min, int max, ChangeListener listener) {
		JSlider slider = new JSlider(min, max);
		slider.setValue(min);
		//slider.setMajorTickSpacing(((max / 10) / 10) * 10);
		slider.setMajorTickSpacing(max/5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(listener);
		return slider;
	}
	
	private ChangeListener createKSliderListener(final int initValue) {
		ChangeListener listener = new ChangeListener() {
			
			private int previousVal = initValue;
			
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				
				// notify controller when slider has stopped moving on a different value than before
				if ((!slider.getValueIsAdjusting()) && (slider.getValue() != previousVal)) {
					previousVal = slider.getValue();
					controller.kSliderChanged();
				}
			}
		};
				
		return listener;
	}
	
	private ChangeListener createOrderSliderListener(final int initValue) {
		ChangeListener listener = new ChangeListener() {
			
			private int previousVal = initValue;
			
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				
				// notify controller when slider has stopped moving on a different value than before
				if ((!slider.getValueIsAdjusting()) && (slider.getValue() != previousVal)) {
					previousVal = slider.getValue();
					controller.orderSliderChanged();
				}
			}
		};
				
		return listener;
	}
	
    private JPanel createVerticalBoxPanel(int prefWidth, int prefHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.setPreferredSize(new Dimension(prefWidth,prefHeight));
        return p;
    }
    
    private JPanel createHorizontalBoxPanel(int prefWidth, int prefHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.setPreferredSize(new Dimension(prefWidth,prefHeight));
        return p;
    }
    
    public void updateCartiPanel(int[][] matrixToShow) {
    	cartiPanel.updateMatrix(matrixToShow);
    	mainFrame.validate();
    	mainFrame.repaint();
    }
    
    public JFrame getMainFrame() {
    	return mainFrame;
    }
    
	public int getOrderSlider_1() {
		return orderSlider_1.getValue();
	}
	
	public int getOrderSlider_2() {
		return orderSlider_2.getValue();
	}
	
	public int getK() {
		return kSlider.getValue();
	}
	
	// TODO parse dimDropArea and return the correct list
	public List<Integer> getDimsToShow() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(0);
		
		return list;
	}
}
