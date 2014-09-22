package cart.gui2;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;




public class CartiView {

	private JFrame theFrame;
	private CartiController controller;
	
	//private JList<String> dimList;
	//private JTextField dimDropArea;
	private JSlider orderSlider_1;
	// private JSlider orderSlider_2;
	private JSlider kSlider;
	private CartiPanel cartiPanel;
	private SelOptions selectionOptions;
	private FilterOptions filteringOptions;
	private Stats selectedsStats;
	private JDialog selectedsStatsDialog;
	
	private boolean shouldListen; // makes it so the selOptions ListSelectionListener only listens when necessary
								  // this should fix a bug where the listener would keep calling the controller whilst
								  // we were updating the lists
	
	public CartiView() {
		theFrame = new JFrame("Carti");
		theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		theFrame.setLayout(new BorderLayout(10,10));
	}
	
	public void setController(CartiController controller) {
		this.controller = controller;
	}
	
	public void init(List<Integer> orderedObjs, List<String> allDims, int maxK, int[][] matrixToShow) {
		// visualPanel contains the visual representation
		JPanel visualPanel = createVerticalBoxPanel(700,700);
		// controlsPanel contains the buttons/sliders/...
		JPanel controlsPanel = createVerticalBoxPanel(300,700);
		
		// VISUAL GOES HERE
		// add cartiPanel
		cartiPanel = new CartiPanel(matrixToShow);
		cartiPanel.addMouseListener(createCartiPanelListener());
		JScrollPane sPane = new JScrollPane(cartiPanel);
		sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		visualPanel.add(sPane);
		
		// CONTROLS GO HERE
		// add the selection options panel
		selectionOptions = new SelOptions();
		selectionOptions.init(orderedObjs, createSelOptionsButtonListener(), createSelOptionsListListener());
			
		controlsPanel.add(selectionOptions.getPanel());
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		
		// add the filtering options panel
		filteringOptions = new FilterOptions();
		filteringOptions.init(createFilterOptionsListener());
			
		controlsPanel.add(filteringOptions.getPanel());
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		
		// add list of dimensions
		//dimList = createDimList(allDims, createDimListTransferHandler());
        //JScrollPane listView = new JScrollPane(dimList);
        //listView.setMaximumSize(new Dimension(200, 200));
        //listView.setBorder(BorderFactory.createTitledBorder("Dimensions"));
		//controlsPanel.add(listView);
		//controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
	
		// add dimension drop area
		//JLabel dimDropAreaLabel = new JLabel("Dimension drop area");
		//dimDropAreaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	// controlsPanel.add(dimDropAreaLabel);
		//dimDropArea = createDimDropArea(createDimDropAreaListener("0"), createDimDropAreaTransferHandler());
		//controlsPanel.add(dimDropArea);
		//controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		
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
        //JLabel order_2SliderLabel = new JLabel("order_2");
     	//order_2SliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	//controlsPanel.add(order_2SliderLabel);
        // orderSlider_2 = createSlider(0, allDims.size()-1, createOrderSliderListener(0));
        // controlsPanel.add(orderSlider_2);
		
        
        // add visual and controls panel to the frame
        theFrame.add(visualPanel, BorderLayout.CENTER);
        theFrame.add(controlsPanel, BorderLayout.LINE_END);
        
        // initialise selecteds stats
        selectedsStats = new Stats();
        selectedsStats.init(new HashSet<Integer>(), new int[0]);
        selectedsStatsDialog = new JDialog(theFrame, "Selecteds stats");
        selectedsStatsDialog.add(selectedsStats.getStatsPanel());
        
        // make the listSelectionListener is listening
        shouldListen = true;
	}
	
	private ActionListener createSelOptionsButtonListener() {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controller.manSelectedsClear();
			}
		};
		
		return listener;
	}
	
	private ListSelectionListener createSelOptionsListListener() {
		ListSelectionListener listener = new ListSelectionListener () {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false && (shouldListen == true)) {
					controller.manSelectedsChange(selectionOptions.getSelecteds());
				}
			}
		};
		
		return listener;
	}
	
	private ActionListener createFilterOptionsListener() {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand() == FilterOptions.CLEAR) {
					controller.manFilteredsClear();
				} else if (e.getActionCommand() == FilterOptions.UNDO) {
					controller.undoFiltering();
				} else if (e.getActionCommand() == FilterOptions.FILTER) {
					controller.filterSelecteds();
				}
			}
		};
		
		return listener;
	}
	
	private MouseListener createCartiPanelListener() {
		MouseListener listener = new MouseListener() {
			
			private int startX;

			public void mousePressed(MouseEvent e) {
				// only left click
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				
				startX = e.getX();
			}

			public void mouseReleased(MouseEvent e) {
				// only left click
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				
				int endX = e.getX();
				int x1 = min(startX, endX);
				int x2 = max(startX, endX);

				// get which cell the click started in and ended at, make sure it is between 0 and cellCount
				int cellCount = cartiPanel.getCellCount();
				int[] cells = cartiPanel.getCells(x1, x2);
				cells[0] = max(0, cells[0]);
				cells[0] = min(cellCount, cells[0]);
				cells[1] = max(0, cells[1]);
				cells[1] = min(cellCount, cells[1]);

				Set<Integer> selectedLocs = new HashSet<>(cells[1] - cells[0]);
				for (int i = cells[0]; i < cells[1]; i++) {
					selectedLocs.add(i);
				}
				
				controller.figureSelectedsChange(selectedLocs);
			}

			// do nothing for these
			public void mouseEntered(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }
			public void mouseClicked(MouseEvent e) { }
		};
		
		return listener;
	}
	
	/*
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
	            dim.setText(data);
	                         
	            return true;
			}
		         
			public int getSourceActions(JComponent c) {
				return NONE;
			}
    	};
    
		return handler;
	} */
	
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
	
    public static JPanel createVerticalBoxPanel(int prefWidth, int prefHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.setPreferredSize(new Dimension(prefWidth,prefHeight));
        return p;
    }
    
    public static JPanel createHorizontalBoxPanel(int prefWidth, int prefHeight) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.setPreferredSize(new Dimension(prefWidth,prefHeight));
        return p;
    }
    
    public void updateFigure(int[][] matrixToShow) {
    	cartiPanel.updateMatrix(matrixToShow);
    	theFrame.validate();
    	theFrame.repaint();
    }
    
    // this just clears the set of selected locations, it does not actually decolour them in the figure
    // this is used to counter a bug when calling updateFigure after a filtering
    public void clearFigureSelectedLocs() {
    	cartiPanel.clearSelectedLocs();
    }
    
    public void updateFigureSelected(List<Integer> orderedObjs, Set<Integer> selecteds) {
    	cartiPanel.updateSelected(orderedObjs, selecteds);
    	theFrame.validate();
    	theFrame.repaint();
    }
    
    public void updateSelOptions(List<Integer> orderedObjs, Set<Integer> selecteds) {
    	shouldListen = false;
    	selectionOptions.updateSelected(orderedObjs, selecteds);
    	shouldListen = true;
    }
    
    public void updateSelStats(Set<Integer> selecteds, int[] dimSupports) {
    	if (selecteds.size() == 0) {
    		hideSelectedsStatsDialog();
    	} else {
    		selectedsStats.updateStats(selecteds, dimSupports);
    		showSelectedsStatsDialog();
    	}
    }
    
    public JFrame getFrame() {
    	return theFrame;
    }
    
	public int getOrderSlider_1() {
		return orderSlider_1.getValue();
	}
	
	/*
	public int getOrderSlider_2() {
		return orderSlider_2.getValue();
	} */
	
	public int getK() {
		return kSlider.getValue();
	}
	
	public boolean selModeIsSelect() {
		return selectionOptions.selModeIsSelect();
	}
	
	public boolean selModeIsAnd() {
		return selectionOptions.selModeIsAnd();
	}
	
	public boolean selModeIsOr() {
		return selectionOptions.selModeIsOr();
	}
	
	
	/*
	// TODO parse dimDropArea and return the correct list
	public List<Integer> getDimsToShow() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(Integer.parseInt(dimDropArea.getText()));
		
		return list;
	} */
	
	private void showSelectedsStatsDialog() {
		selectedsStatsDialog.pack();
		selectedsStatsDialog.setVisible(true);
	}
	
	private void hideSelectedsStatsDialog() {
		selectedsStatsDialog.setVisible(false);
	}
}
