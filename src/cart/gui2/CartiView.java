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
import java.util.Map;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


public class CartiView {
	
	final static String CLUSTER = "CartiView.Cluster";
	final static String MINE = "CartiView.Mine";

	private JFrame theFrame;
	private CartiController controller;
	
	private JSlider orderSlider;
	private JSlider kSlider;
	private JButton cluster;
	private JButton mine;
	private CartiPanel cartiPanel;
	private SelOptions selectionOptions;
	private FilterOptions filteringOptions;
	private Stats selectedsStats;
	private JDialog selectedsStatsDialog;
	private ClusterInfo clusterInfo;
	private JDialog clusterInfoDialog;
	
	private boolean selOptionsListenerShouldListen; // prevents the selection options list listener from listening while updating
	private boolean clusterInfoListenerShouldListen; // prevents cluster info table listener from listening while updating
	
	public CartiView() {
		theFrame = new JFrame("Carti");
		theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		theFrame.setLayout(new BorderLayout(10,10));
	}
	
	public void setController(CartiController controller) {
		this.controller = controller;
	}
	
	public void init(List<Integer> orderedObjs, Set<Integer> dims, int maxK, int[][] matrixToShow) {
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
		// listener for all the buttons
		ActionListener buttonsListener = createButtonsListener();
		
		// add the selection options panel
		selectionOptions = new SelOptions();
		selectionOptions.init(orderedObjs, buttonsListener, createSelOptionsListListener());
			
		controlsPanel.add(selectionOptions.getPanel());
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
		
		// add the filtering options panel
		filteringOptions = new FilterOptions();
		filteringOptions.init(buttonsListener);
			
		controlsPanel.add(filteringOptions.getPanel());
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// add button for clustering
		cluster = new JButton("Cluster selected");
		cluster.setActionCommand(CLUSTER);
		cluster.setAlignmentX(Component.CENTER_ALIGNMENT);
		cluster.addActionListener(buttonsListener);
		controlsPanel.add(cluster);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// add button for mining
		mine = new JButton("Mine");
		mine.setActionCommand(MINE);
		mine.setAlignmentX(Component.CENTER_ALIGNMENT);
		mine.addActionListener(buttonsListener);
		controlsPanel.add(mine);
		controlsPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
		
        // add slider for k
     	JLabel kSliderLabel = new JLabel("k");
     	kSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(kSliderLabel);
        kSlider = createSlider(1,maxK);
        controlsPanel.add(kSlider);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // add slider for order_1
     	JLabel order_1SliderLabel = new JLabel("order_1");
     	order_1SliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     	controlsPanel.add(order_1SliderLabel);
        orderSlider = createSlider(0, dims.size()-1);
        controlsPanel.add(orderSlider);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // add visual and controls panel to the frame
        theFrame.add(visualPanel, BorderLayout.CENTER);
        theFrame.add(controlsPanel, BorderLayout.LINE_END);
        
        // initialise selecteds stats dialog
        selectedsStats = new Stats();
        selectedsStats.init(dims);
        selectedsStatsDialog = new JDialog(theFrame, "Selecteds stats");
        selectedsStatsDialog.add(selectedsStats.getStatsPanel());
        
		// initialise cluster info dialog
        clusterInfo = new ClusterInfo();
        clusterInfo.init(buttonsListener, createClusterTableModelListener());
        clusterInfoDialog = new JDialog(theFrame, "Clusters info");
        clusterInfoDialog.add(clusterInfo.getInfoPanel());
        
        // make sure the listSelectionListener is listening
        selOptionsListenerShouldListen = true;
        clusterInfoListenerShouldListen = true;
	}
	
	// listens to all the buttons
	private ActionListener createButtonsListener() {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand() == SelOptions.CLEAR) {
					controller.manSelectedsClear();
				} else if (e.getActionCommand() == FilterOptions.CLEAR) {
					controller.manFilteredsClear();
				} else if (e.getActionCommand() == FilterOptions.UNDO) {
					controller.undoFiltering();
				} else if (e.getActionCommand() == FilterOptions.FILTER) {
					controller.filterSelecteds();
				} else if (e.getActionCommand() == CartiView.MINE) {
					controller.mine();
				} else if (e.getActionCommand() == CartiView.CLUSTER) {
					controller.clusterSelected();
				} 
				
				// only get here if one of the ClusterInfo buttons was pressed
				Set<Integer> clusterIds = clusterInfo.getSelectedRowsClusterIds();
				
				// if the user has not selected a cluster
				if (clusterIds.isEmpty()) {
					return;
				}
				
				if (e.getActionCommand() == ClusterInfo.ADD) {
					controller.addSelectedToClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.REMOVE) {
					controller.removeSelectedFromClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.DELETE) {
					controller.deleteClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.SELECT) {
					controller.selectClusters(clusterIds);
				}
			}
		};
		
		return listener;
	}
	
	// listens for changes in selection in the SelOptions list
	private ListSelectionListener createSelOptionsListListener() {
		ListSelectionListener listener = new ListSelectionListener () {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false && (selOptionsListenerShouldListen)) {
					controller.manSelectedsChange(selectionOptions.getSelecteds());
				}
			}
		};
		
		return listener;
	}
	
	// listens for mouse clicks in the cartiPanel
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
	
	// listens for changes in the ClusterInfo table (whether a cluster is visible/not visible)
	private TableModelListener createClusterTableModelListener() {
		TableModelListener listener = new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				if (clusterInfoListenerShouldListen) {
					int row = e.getFirstRow();
					ClusterInfo.ClusterTable table = (ClusterInfo.ClusterTable)e.getSource();
					boolean isVisible = (boolean)table.getValueAt(row, 0);
					int clusterId = (int)table.getValueAt(row,1);
					
					if (isVisible) {
						controller.showCluster(clusterId);
					} else {
						controller.hideCluster(clusterId);
					}
				}
			}
		};
		
		return listener;
	}
	
	// creates a slider with given minimum/maximum values
	private JSlider createSlider(int min, int max) {
		JSlider slider = new JSlider(min, max);
		slider.setValue(min);
		//slider.setMajorTickSpacing(((max / 10) / 10) * 10);
		slider.setMajorTickSpacing(max/5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(createSliderListener(min));
		return slider;
	}
	
	// listens for changes in the sliders and notifies the controller if a value has changed
	private ChangeListener createSliderListener(final int initValue) {
		ChangeListener listener = new ChangeListener() {
			
			private int previousVal = initValue;
			
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				
				// notify controller when slider has stopped moving on a different value than before
				if ((!slider.getValueIsAdjusting()) && (slider.getValue() != previousVal)) {
					previousVal = slider.getValue();
					
					if (slider == kSlider) {
						controller.kSliderChanged();
					} else if (slider == orderSlider) {
						controller.orderSliderChanged();
					}
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
    
    // this just clears the saved locations, it does not actually decolour them in the figure
    // this is used to counter a bug when calling updateFigure after a filtering
    public void clearFigureSavedLocs() {
    	cartiPanel.clearSavedLocs();
    }
    
    public void updateFigureSelected(Set<Integer> selectedLocs) {
    	cartiPanel.updateSelected(selectedLocs);
    	theFrame.validate();
    	theFrame.repaint();
    }
    
    public void updateFigureClustered(Set<Integer> clusteredLocs) {
    	cartiPanel.updateClustered(clusteredLocs);
    	theFrame.validate();
    	theFrame.repaint();
    }
    
    public void updateSelOptions(List<Integer> orderedObjs, Set<Integer> selecteds) {
    	selOptionsListenerShouldListen = false;
    	selectionOptions.updateSelected(orderedObjs, selecteds);
    	selOptionsListenerShouldListen = true;
    }
    
    public void updateSelStats(Set<Integer> selecteds, int[] dimSupports, double[] standardDevs, int[] measures, int[] medAbsDevs) {
    	if (selecteds.size() == 0) {
    		selectedsStatsDialog.setVisible(false);
    	} else {
    		selectedsStats.updateStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
    		selectedsStatsDialog.pack();
    		selectedsStatsDialog.setVisible(true);
    	}
    }
    
    public void updateClusterInfo(Map<Integer, Cluster> clustersMap, Set<Integer> clustersToShow) {
    	clusterInfoListenerShouldListen = false;
    	if (clustersMap.size() == 0) {
    		clusterInfoDialog.setVisible(false);
    	} else {
    		clusterInfo.updateClusterInfo(clustersMap, clustersToShow);
    		clusterInfoDialog.pack();
    		clusterInfoDialog.setVisible(true);
    	}
    	clusterInfoListenerShouldListen = true;
    }
    
    public JFrame getFrame() {
    	return theFrame;
    }
    
	public int getOrderSliderVal() {
		return orderSlider.getValue();
	}
	
	public int getKSliderVal() {
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
	
}
