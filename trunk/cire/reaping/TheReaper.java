package reaping;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import java.awt.List;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;
import java.awt.Image;
import edu.stanford.ejalbert.BrowserLauncher;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import del.icio.us.Delicious;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import java.awt.geom.Rectangle2D;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import java.util.jar.Attributes;
import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.mem.GraphImpl;
import javax.swing.JColorChooser;
import org.jgraph.JGraph;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.DefaultEdge;
import static org.jrdf.graph.AnyObjectNode.ANY_OBJECT_NODE;
import static org.jrdf.graph.AnyPredicateNode.ANY_PREDICATE_NODE;
import static org.jrdf.graph.AnySubjectNode.ANY_SUBJECT_NODE;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Triple;
import org.jrdf.graph.TripleFactory;
import org.jrdf.graph.TripleFactoryException;
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;
import org.jrdf.JRDFFactory;
import org.jrdf.SortedMemoryJRDFFactoryImpl;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import javax.swing.ImageIcon;
import java.net.URL;
import java.net.MalformedURLException;

public class TheReaper extends JPanel implements ActionListener,
HyperlinkListener,MouseListener,ProcessorMessageListener,
ProcessorThreadStateChangedListener,FocusListener{

    private	JTextField	usernameTextField;
    private	JPasswordField	passwordTextField;
    private	JTextField	processingBookmark;
    static final long serialVersionUID=1L;
    private List userBookmarksInCommon;
    private List genres;
    private List yourBookmarksList;
    private List recommendedBookmarksList;
    private List yourMapsList;
    private List recommendedMapsList;
    private	JTextField	currentUsersBookmarksField;
    private	JTextField	minUsersPerGroupTextField;
    private	JTextField	mindmapTitleField;
    private	JTextField	mindmapNodeTitleField;
    private BrowserLauncher launcher;
    private Processor currentProcessor;
    private JLabel userListLabel;
    private JLabel genresLabel;
    private JLabel userBookmarksInCommonLabel;
    private JLabel todaysBookmarksLabel;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JLabel minUsersPerGroupLabel;
    private JLabel mapTitleLabel;
    private JLabel minNodeTitleLabel;
    private JLabel yourBookmarksLabel;
    private JLabel yourMapsLabel;
    private JLabel recommendedMapsLabel;
    private JLabel recommendedBookmarksLabel;
    private JTable usernameTabel;
    private JScrollPane usernamescroller;
    private JScrollPane mindmapScroller;
    private JScrollPane mindmapNodeScroller;
    private JTextArea mindmapNodeText;
    private JScrollPane userBookmarksScroller;
    private JScrollPane genresScroller;
    DefaultTableModel usernameTableModel;
    private Delicious del;
    private String[][] userNameTableData;
    private ProcessorThread processorThread;
    private JGraph jgraph;
    private Attributes mindmapNotes;
    private static final JRDFFactory JRDF_FACTORY = SortedMemoryJRDFFactoryImpl.getFactory();
    private Graph currentGraph;
    private JPopupMenu popup;
    private GraphModel model;
    private GraphLayoutCache view;
    private JScrollPane yourBookmarksScroller;
    private JScrollPane recommendedBookmarksScroller;
    private JScrollPane yourMapsScroller;
    private JScrollPane recommendedMapsScroller;
    Connection conn;
    private java.util.List<URLSet> sets;
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FFFFFF");
    private static final Color DEFAULT_CELL_COLOR = Color.decode("#CC00FF");
    private static final Dimension DEFAULT_SIZE = new Dimension(500, 500);
    private JColorChooser colorChooser;
    private GraphUndoManager undoManager;
    //private WebBrowser webBrowser;

    private ProcessorThread.ProcessorThreadState currentProcessingState=ProcessorThread.ProcessorThreadState.STOPPED;

    public TheReaper() {

    	currentProcessor=new Processor(this,this);
    	this.setBackground(Color.WHITE);

    	try{
			Class.forName("org.hsqldb.jdbcDriver");
        	conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	mindmapNotes=new Attributes();
    	
        popup = new JPopupMenu();

        JMenuItem deleteThisPopupItem = new JMenuItem("Delete");
        deleteThisPopupItem.setActionCommand("RemoveMindMapNode");
        deleteThisPopupItem.addActionListener(this);
        popup.add(deleteThisPopupItem);
    	
        JMenuItem addNodePopupItem = new JMenuItem("Add node");
        addNodePopupItem.setActionCommand("AddMindMapNode");
        addNodePopupItem.addActionListener(this);
        popup.add(addNodePopupItem);
        
        JMenuItem connectNodesPopupItem = new JMenuItem("Connect nodes");
        connectNodesPopupItem.setActionCommand("ConnectNodes");
        connectNodesPopupItem.addActionListener(this);
        popup.add(connectNodesPopupItem);
        
        JMenuItem editLabelPopupItem = new JMenuItem("Edit Label");
        editLabelPopupItem.setActionCommand("EditLabel");
        editLabelPopupItem.addActionListener(this);
        popup.add(editLabelPopupItem);
        
        colorChooser=new JColorChooser();
        
        JMenuItem changeColorPopupItem = new JMenuItem("Change Color");
        changeColorPopupItem.setActionCommand("ChangeColor");
        changeColorPopupItem.addActionListener(this);
        popup.add(changeColorPopupItem);
        
		//-----------------------------------------------------------------------------------

		JTabbedPane tabbedPane = new JTabbedPane();
		//tabbedPane.setLayout(new GridBagLayout());
		
		
		JMenuBar menuBar = new JMenuBar();

		
		//--------------------------------------------------------------------------
		JMenu mapMenu = new JMenu("Maps");

		mapMenu.setToolTipText(
		        "Map related actions");
		menuBar.add(mapMenu);

		JMenuItem undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setToolTipText("Undo recent changes");
		undoMenuItem.setActionCommand("UndoMindmap");
		undoMenuItem.addActionListener(this);
		mapMenu.add(undoMenuItem);
		
		JMenuItem redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setToolTipText("Redo recent changes");
		redoMenuItem.setActionCommand("RedoMindmap");
		redoMenuItem.addActionListener(this);
		mapMenu.add(redoMenuItem);
		
		JMenuItem zoomInMenuItem = new JMenuItem("Zoom in");
		zoomInMenuItem.setToolTipText("Zoom in");
		zoomInMenuItem.setActionCommand("ZoomInMindmap");
		zoomInMenuItem.addActionListener(this);
		mapMenu.add(zoomInMenuItem);
		
		JMenuItem zoomOutMenuItem = new JMenuItem("Zoom out");
		zoomOutMenuItem.setToolTipText("Zoom out");
		zoomOutMenuItem.setActionCommand("ZoomOutMindmap");
		zoomOutMenuItem.addActionListener(this);
		mapMenu.add(zoomOutMenuItem);
		
		JMenuItem saveMapMenuItem = new JMenuItem("Save");
		saveMapMenuItem.setToolTipText("Save the current map");
		saveMapMenuItem.setActionCommand("SaveCurrentMindmap");
		saveMapMenuItem.addActionListener(this);
		mapMenu.add(saveMapMenuItem);
		
		JMenuItem loadSelectedMapMenuItem = new JMenuItem("Load");
		loadSelectedMapMenuItem.setToolTipText(
		        "Load the currently selected map");
		loadSelectedMapMenuItem.setActionCommand("LoadSelectedMindmap");
		loadSelectedMapMenuItem.addActionListener(this);
		mapMenu.add(loadSelectedMapMenuItem);
		
		JMenuItem mergeMapMenuItem = new JMenuItem("Merge");
		mergeMapMenuItem.setToolTipText(
		        "Merg the current map with the selected map.");
		mergeMapMenuItem.setActionCommand("MergeSelectedMindmap");
		mergeMapMenuItem.addActionListener(this);
		mapMenu.add(mergeMapMenuItem);
		
		JMenuItem generateMapMenuItem = new JMenuItem("Autogenerate Mind Maps");
		generateMapMenuItem.setToolTipText(
		        "Generate a mind map of your bookmarks using the relationships between them and other users.");
		generateMapMenuItem.setActionCommand("Autogenerate Mind Maps");
		generateMapMenuItem.addActionListener(this);
		mapMenu.add(generateMapMenuItem);
		
		
		//--------------------------------------------------------------------
		JMenu nodeMenu = new JMenu("Nodes");

		nodeMenu.setToolTipText(
		        "Map node related actions");
		menuBar.add(nodeMenu);
		
		JMenuItem addSelectedBookmarkMenuItem = new JMenuItem("Add Bookmark");
		addSelectedBookmarkMenuItem.setToolTipText(
        "Add the selected bookmark to this map node.");
		addSelectedBookmarkMenuItem.setActionCommand("AddBookmarkToNode");
		addSelectedBookmarkMenuItem.addActionListener(this);
		nodeMenu.add(addSelectedBookmarkMenuItem);
		
		JMenuItem addSelectedFileMenuItem = new JMenuItem("Add File");
		addSelectedFileMenuItem.setToolTipText(
        "Select a file from your filesystem to add to this node.");
		addSelectedFileMenuItem.setActionCommand("AddFileToNode");
		addSelectedFileMenuItem.addActionListener(this);
		nodeMenu.add(addSelectedFileMenuItem);
		
		
		//-------------------------------------------------------------------
		
		JMenu bookmarksMenu = new JMenu("Bookmarks");

		bookmarksMenu.setToolTipText(
		        "Bookmark related actions");
		menuBar.add(bookmarksMenu);
		
		JMenuItem getBookmarksMenuItem = new JMenuItem("Get similar users");
		getBookmarksMenuItem.setToolTipText("Find people with similar sets of bookmarks.");
		getBookmarksMenuItem.setActionCommand("Get Bookmarks");
		getBookmarksMenuItem.addActionListener(this);
		bookmarksMenu.add(getBookmarksMenuItem);
		
		JMenuItem recommendBookmarkMenuItem = new JMenuItem("Get recommended bookmarks");
		recommendBookmarkMenuItem.setToolTipText("Recommend bookmarks that similar users liked.");
		recommendBookmarkMenuItem.setActionCommand("RecommendBookmark");
		recommendBookmarkMenuItem.addActionListener(this);
		bookmarksMenu.add(recommendBookmarkMenuItem);
		
		JMenuItem allUsersButtonMenuItem = new JMenuItem("Show All Users");
		allUsersButtonMenuItem.setToolTipText("Show all the users in all the categories.");
		allUsersButtonMenuItem.setActionCommand("ShowAllUsers");
		allUsersButtonMenuItem.addActionListener(this);
		bookmarksMenu.add(allUsersButtonMenuItem);
		
		JMenuItem openCurrentBookmarkButtonMenuItem = new JMenuItem("Open selected bookmark");
		openCurrentBookmarkButtonMenuItem.setToolTipText("Open the currently selected bookmark in a web browser.");
		openCurrentBookmarkButtonMenuItem.setActionCommand("OpenBookmark");
		openCurrentBookmarkButtonMenuItem.addActionListener(this);
		bookmarksMenu.add(openCurrentBookmarkButtonMenuItem);
		
		JMenuItem openCurrentUsersBookmarksMenuItem = new JMenuItem("Open Users Bookmarks");
		openCurrentUsersBookmarksMenuItem.setToolTipText("Open the currently selected users bookmars in a web browser.");
		openCurrentUsersBookmarksMenuItem.setActionCommand("OpenThisUsersBookmarks");
		openCurrentUsersBookmarksMenuItem.addActionListener(this);
		bookmarksMenu.add(openCurrentUsersBookmarksMenuItem);
		
		JMenuItem categoryUsersButtonMenuItem = new JMenuItem("Show Users For Category");
		categoryUsersButtonMenuItem.setToolTipText("Show all the users in the selected category.");
		categoryUsersButtonMenuItem.setActionCommand("ShowCategoryUsers");
		categoryUsersButtonMenuItem.addActionListener(this);
		bookmarksMenu.add(categoryUsersButtonMenuItem);
		

		//-------------------------------------------------------------------------------
		
		JMenu helpMenu = new JMenu("Help");

		helpMenu.setToolTipText(
		        "Usage instructions");
		menuBar.add(helpMenu);
		
		JMenuItem usageInstructionsMenuItem = new JMenuItem("Usage instructions");
		usageInstructionsMenuItem.setToolTipText("Click here for general usage instructions.");
		usageInstructionsMenuItem.setActionCommand("help");
		usageInstructionsMenuItem.addActionListener(this);
		helpMenu.add(usageInstructionsMenuItem);
		//---------------------------------------------------------------------------
		
		
		javax.swing.JPanel mainPanel= new javax.swing.JPanel();
    	mainPanel.setLayout(new GridBagLayout());

		javax.swing.JPanel browserPanel= new javax.swing.JPanel();
    	browserPanel.setLayout(new GridBagLayout());

    	javax.swing.JPanel userPanel= new javax.swing.JPanel();
    	userPanel.setLayout(new GridBagLayout());

    	javax.swing.JPanel bookmarksPanel= new javax.swing.JPanel();
    	bookmarksPanel.setLayout(new GridBagLayout());

    	javax.swing.JPanel urlPanel= new javax.swing.JPanel();
    	urlPanel.setLayout(new GridBagLayout());

    	javax.swing.JPanel genresPanel= new javax.swing.JPanel();
    	genresPanel.setLayout(new GridBagLayout());
    	
    	javax.swing.JPanel mindmapPanel= new javax.swing.JPanel();
    	mindmapPanel.setLayout(new GridBagLayout());

    	javax.swing.JPanel mindmapNodePanel= new javax.swing.JPanel();
    	mindmapNodePanel.setLayout(new GridBagLayout());

	    try{
	    	launcher= new BrowserLauncher();
	    }
	    catch(Exception e) {
	    }

		/* -----------------------START: User Panel -----------------------------------------------------------*/


    	userNameLabel= new JLabel("Delicious Username",JLabel.LEFT);
    	GridBagConstraints userNameLabelConstraints = new GridBagConstraints();
    	userNameLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	userNameLabelConstraints.gridx=0;
    	userNameLabelConstraints.gridy=0;
        userPanel.add(userNameLabel,userNameLabelConstraints);
        

        usernameTextField=new JTextField(currentProcessor.getLastUser(conn));
        usernameTextField.setColumns(25);
        usernameTextField.setVisible(true);
        usernameTextField.addActionListener(this);
        GridBagConstraints usernameTextFieldConstraints = new GridBagConstraints();
        //usernameTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        usernameTextFieldConstraints.gridx=1;
        usernameTextFieldConstraints.gridy=0;
        userPanel.add(usernameTextField,usernameTextFieldConstraints);

        
        passwordLabel= new JLabel("Delicious Password",JLabel.LEFT);
    	GridBagConstraints passwordLabelConstraints = new GridBagConstraints();
    	passwordLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	passwordLabelConstraints.gridx=0;
    	passwordLabelConstraints.gridy=1;
        userPanel.add(passwordLabel,passwordLabelConstraints);
        

    	passwordTextField=new JPasswordField("");
    	passwordTextField.setColumns(25);
    	passwordTextField.setVisible(true);
    	passwordTextField.addActionListener(this);
    	GridBagConstraints passwordTextFieldConstraints = new GridBagConstraints();
    	//passwordTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    	passwordTextFieldConstraints.gridx=1;
    	passwordTextFieldConstraints.gridy=1;
        userPanel.add(passwordTextField,passwordTextFieldConstraints);
        
        
        
    	minUsersPerGroupLabel= new JLabel("Minimum number of users per group.",JLabel.LEFT);
    	GridBagConstraints minUsersPerGroupLabelConstraints = new GridBagConstraints();
    	minUsersPerGroupLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	minUsersPerGroupLabelConstraints.gridx=0;
    	minUsersPerGroupLabelConstraints.gridy=2;
        userPanel.add(minUsersPerGroupLabel,minUsersPerGroupLabelConstraints);
        
        
        minUsersPerGroupTextField=new JTextField("2");
        minUsersPerGroupTextField.setColumns(25);
        minUsersPerGroupTextField.setVisible(true);
    	GridBagConstraints minUsersPerGroupTextFieldConstraints = new GridBagConstraints();
    	//minUsersPerGroupTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    	minUsersPerGroupTextFieldConstraints.gridx=1;
    	minUsersPerGroupTextFieldConstraints.gridy=2;
        userPanel.add(minUsersPerGroupTextField,minUsersPerGroupTextFieldConstraints); 
        
        

        
		/*---------------------------------------- END: User Panel---------------------------------- */

		/* ---------------------------------------START: Bookmark Panel ---------------------------------*/
        
        
        
        yourBookmarksLabel= new JLabel("Your bookmarks",JLabel.CENTER);
        GridBagConstraints yourBookmarksLabelConstraints = new GridBagConstraints();
        yourBookmarksLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        yourBookmarksLabelConstraints.gridx=0;
        yourBookmarksLabelConstraints.gridy=0;
        yourBookmarksLabelConstraints.gridwidth = 2;
        bookmarksPanel.add(yourBookmarksLabel,yourBookmarksLabelConstraints);
        

        
        Dimension yourBookmarksListSize= new Dimension();
        //yourBookmarksListSize.setSize(750, 150);
        yourBookmarksList= new List(10, false);
        yourBookmarksList.setBackground(Color.WHITE);
        yourBookmarksList.addActionListener(this);
        yourBookmarksScroller= new javax.swing.JScrollPane(yourBookmarksList);
        yourBookmarksScroller.setMaximumSize(yourBookmarksListSize);
        GridBagConstraints yourBookmarksScrollerConstraints = new GridBagConstraints();
        yourBookmarksScrollerConstraints.fill = GridBagConstraints.BOTH;
        yourBookmarksScrollerConstraints.gridx=0;
        yourBookmarksScrollerConstraints.gridy=1;
        yourBookmarksScrollerConstraints.gridwidth = 2;
        bookmarksPanel.add(yourBookmarksScroller,yourBookmarksScrollerConstraints);
        


    	userListLabel= new JLabel("Delicious Users Similar To You",JLabel.LEFT);
    	GridBagConstraints userListLabelConstraints = new GridBagConstraints();
    	userListLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	userListLabelConstraints.gridx=0;
    	userListLabelConstraints.gridy=2;
        bookmarksPanel.add(userListLabel,userListLabelConstraints);

        
    	userBookmarksInCommonLabel= new JLabel("Bookmarks you share with the current user.",JLabel.LEFT);
    	GridBagConstraints userBookmarksInCommonLabelConstraints = new GridBagConstraints();
    	userBookmarksInCommonLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	userBookmarksInCommonLabelConstraints.gridx=1;
    	userBookmarksInCommonLabelConstraints.gridy=2;
        bookmarksPanel.add(userBookmarksInCommonLabel,userBookmarksInCommonLabelConstraints);

        
	    userNameTableData=new String[20][2];
	    String[] userNameTableColumnNames= {"Users with the same bookmarks",
	    		"Number of shared bookmarks"};
	    usernameTableModel= new DefaultTableModel(userNameTableColumnNames,10);
	    usernameTabel = new JTable();
	    usernameTabel.setModel(usernameTableModel);
	    //usernameTabel.setPreferredScrollableViewportSize(new Dimension(300, 150));
	    usernameTabel.setRowSelectionAllowed(true);
	    usernameTabel.setColumnSelectionAllowed(false);
	    usernameTabel.setBackground(Color.WHITE);
	    //usernameTabel.setSize(new Dimension(300, 150));
	    //usernameTabel.setMaximumSize(new Dimension(300, 150));
	    usernameTabel.addMouseListener(this);
	    usernamescroller = new javax.swing.JScrollPane(usernameTabel);
	    GridBagConstraints usernamescrollerConstraints = new GridBagConstraints();
	    usernamescrollerConstraints.fill = GridBagConstraints.BOTH;
	    usernamescrollerConstraints.gridx=0;
	    usernamescrollerConstraints.gridy=3;
        bookmarksPanel.add(usernamescroller,usernamescrollerConstraints);
        
        userBookmarksInCommon= new List(10, false);
        userBookmarksInCommon.setBackground(Color.WHITE);
        userBookmarksInCommon.addActionListener(this);
        //userBookmarksInCommon.setPreferredSize(new Dimension(600, 50));
        userBookmarksScroller= new javax.swing.JScrollPane(userBookmarksInCommon);
        GridBagConstraints userBookmarksScrollerConstraints = new GridBagConstraints();
        userBookmarksScrollerConstraints.fill = GridBagConstraints.BOTH;
        userBookmarksScrollerConstraints.gridx=1;
        userBookmarksScrollerConstraints.gridy=3;
        bookmarksPanel.add(userBookmarksScroller,userBookmarksScrollerConstraints);

		
        
        recommendedBookmarksLabel= new JLabel("Recommended Bookmarks",JLabel.CENTER);
        GridBagConstraints recommendedBookmarksLabelConstraints = new GridBagConstraints();
        recommendedBookmarksLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        recommendedBookmarksLabelConstraints.gridx=0;
        recommendedBookmarksLabelConstraints.gridy=4;
        recommendedBookmarksLabelConstraints.gridwidth = 2;
        bookmarksPanel.add(recommendedBookmarksLabel,recommendedBookmarksLabelConstraints);
        
        Dimension recommendedBookmarksListSize= new Dimension();
        recommendedBookmarksListSize.setSize(750, 150);
        recommendedBookmarksList= new List(10, false);
        recommendedBookmarksList.setBackground(Color.WHITE);
        recommendedBookmarksList.addActionListener(this);
        recommendedBookmarksScroller= new javax.swing.JScrollPane(recommendedBookmarksList);
        recommendedBookmarksScroller.setMaximumSize(recommendedBookmarksListSize);
        GridBagConstraints recommendedBookmarksScrollerConstraints = new GridBagConstraints();
        recommendedBookmarksScrollerConstraints.fill = GridBagConstraints.BOTH;
        recommendedBookmarksScrollerConstraints.gridx=0;
        recommendedBookmarksScrollerConstraints.gridy=5;
        recommendedBookmarksScrollerConstraints.gridwidth = 2;
        bookmarksPanel.add(recommendedBookmarksScroller,recommendedBookmarksScrollerConstraints);
        



    	todaysBookmarksLabel= new JLabel("The currently selected users bookmarks.",JLabel.LEFT);
    	GridBagConstraints todaysBookmarksLabelConstraints = new GridBagConstraints();
    	todaysBookmarksLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
    	todaysBookmarksLabelConstraints.gridx=0;
    	todaysBookmarksLabelConstraints.gridy=6;
        bookmarksPanel.add(todaysBookmarksLabel,todaysBookmarksLabelConstraints);

        currentUsersBookmarksField=new JTextField("");
        currentUsersBookmarksField.setColumns(30);
        currentUsersBookmarksField.setVisible(true);
        currentUsersBookmarksField.addActionListener(this);
        GridBagConstraints currentUsersBookmarksFieldConstraints = new GridBagConstraints();
        currentUsersBookmarksFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        currentUsersBookmarksFieldConstraints.gridx=1;
        currentUsersBookmarksFieldConstraints.gridy=6;
        bookmarksPanel.add(currentUsersBookmarksField,currentUsersBookmarksFieldConstraints);



        /*----------------------------------- END: Url Panel ------------------------------------*/


        /* ------------------------------------------START: Genres Panel ---------------------------------*/
        yourMapsLabel= new JLabel("Your maps",JLabel.CENTER);
        GridBagConstraints yourMapsLabelConstraints = new GridBagConstraints();
        yourMapsLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        yourMapsLabelConstraints.gridx=0;
        yourMapsLabelConstraints.gridy=0;
        genresPanel.add(yourMapsLabel,yourMapsLabelConstraints);
        
        
        recommendedMapsLabel= new JLabel("Recommended Maps",JLabel.CENTER);
        GridBagConstraints recommendedMapsLabelConstraints = new GridBagConstraints();
        recommendedMapsLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        recommendedMapsLabelConstraints.gridx=1;
        recommendedMapsLabelConstraints.gridy=0;
        genresPanel.add(recommendedMapsLabel,recommendedMapsLabelConstraints);
        
        Dimension yourMapsListSize= new Dimension();
        //yourMapsListSize.setSize(350, 150);
        yourMapsList= new List(10, false);
        yourMapsList.setBackground(Color.WHITE);
        yourMapsList.addActionListener(this);
        yourMapsScroller= new javax.swing.JScrollPane(yourMapsList);
        yourMapsScroller.setMaximumSize(yourMapsListSize);
        GridBagConstraints yourMapsListSizeConstraints = new GridBagConstraints();
        yourMapsListSizeConstraints.fill = GridBagConstraints.BOTH;
        yourMapsListSizeConstraints.gridx=0;
        yourMapsListSizeConstraints.gridy=1;
        yourMapsListSizeConstraints.weightx=1;
        yourMapsListSizeConstraints.weighty=1;
        genresPanel.add(yourMapsScroller,yourMapsListSizeConstraints);
        

        
        Dimension recommendedMapsListSize= new Dimension();
        //recommendedMapsListSize.setSize(750, 150);
        recommendedMapsList= new List(10, false);
        recommendedMapsList.setBackground(Color.WHITE);
        recommendedMapsList.addActionListener(this);
        recommendedMapsScroller= new javax.swing.JScrollPane(recommendedMapsList);
        recommendedMapsScroller.setMaximumSize(recommendedMapsListSize);
        GridBagConstraints recommendedMapsListConstraints = new GridBagConstraints();
        recommendedMapsListConstraints.fill = GridBagConstraints.BOTH;
        recommendedMapsListConstraints.gridx=1;
        recommendedMapsListConstraints.gridy=1;
        recommendedMapsListConstraints.weightx=1;
        recommendedMapsListConstraints.weighty=1;
        genresPanel.add(recommendedMapsScroller,recommendedMapsListConstraints);
        
        
		genresLabel= new JLabel("Automatically determined categories your bookmarks fall into",JLabel.LEFT);
		GridBagConstraints genresLabelConstraints = new GridBagConstraints();
		genresLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		genresLabelConstraints.gridx=0;
		genresLabelConstraints.gridy=2;
		genresLabelConstraints.gridwidth = 2;
        genresPanel.add(genresLabel,genresLabelConstraints);

        Dimension genresListSize= new Dimension();
        //genresListSize.setSize(750, 150);
        genres= new List(10, false);
        genres.setBackground(Color.WHITE);
        genres.addActionListener(this);
        genresScroller= new javax.swing.JScrollPane(genres);
        genresScroller.setMaximumSize(genresListSize);
        GridBagConstraints genresConstraints = new GridBagConstraints();
        genresConstraints.fill = GridBagConstraints.BOTH;
        genresConstraints.gridx=0;
        genresConstraints.gridy=3;
        genresConstraints.gridwidth = 2;
        genresConstraints.weightx=1;
        genresConstraints.weighty=1;
        genresPanel.add(genresScroller,genresConstraints);
        



		/* END: Genres Panel */
        
        /*---------------------------------------------------Start mindmap panel ----------------------------------*/
        

        mapTitleLabel= new JLabel("Title",JLabel.RIGHT);
        GridBagConstraints mapTitleLabelConstraints = new GridBagConstraints();
        //mapTitleLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        mapTitleLabelConstraints.gridx=0;
        mapTitleLabelConstraints.gridy=0;
        mindmapPanel.add(mapTitleLabel,mapTitleLabelConstraints);
        
        mindmapTitleField=new JTextField("");
        mindmapTitleField.setColumns(25);
        mindmapTitleField.setVisible(true);
        GridBagConstraints mindmapTitleFieldConstraints = new GridBagConstraints();
        //mindmapTitleFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        mindmapTitleFieldConstraints.gridx=1;
        mindmapTitleFieldConstraints.gridy=0;
        mindmapTitleFieldConstraints.weightx=1;
        mindmapPanel.add(mindmapTitleField,mindmapTitleFieldConstraints);
        

        
        
        model = new DefaultGraphModel();
        view = new GraphLayoutCache(model, new DefaultCellViewFactory());
        
        undoManager = new GraphUndoManager();
        

        
        jgraph = new JGraph(model, view);
        jgraph.getModel().addUndoableEditListener(undoManager);
        
        jgraph.setEditable(true);
        jgraph.setEditClickCount(2);
        jgraph.setSizeable(true); 
        jgraph.setSelectionEnabled(true); 
        jgraph.setMoveBelowZero(true);
        jgraph.setPortsVisible(true); 
        jgraph.setMoveable(true); 
        jgraph.setEdgeLabelsMovable(true); 
        jgraph.setDropEnabled(true); 
        jgraph.setDragEnabled(true); 
        //jgraph.setDisconnectable(true); 
        jgraph.setConnectable(true); 
        //jgraph.setCloneable(true); 
        jgraph.setBendable(true); 
        jgraph.addMouseListener(this);
        jgraph.setAutoResizeGraph(true);
        
        jgraph.setPreferredSize( DEFAULT_SIZE );
        
        Color  mindmapBackground = DEFAULT_BG_COLOR;

        jgraph.setBackground( mindmapBackground );
        jgraph.addFocusListener(this);
		DefaultGraphCell cell = new DefaultGraphCell("first");

		
		GraphConstants.setBounds(cell.getAttributes(),
				new Rectangle2D.Double(DEFAULT_SIZE.width/2, 
						DEFAULT_SIZE.height/2, 50, 50));

		GraphConstants.setBackground(
				cell.getAttributes(), DEFAULT_CELL_COLOR);
			GraphConstants.setOpaque(
				cell.getAttributes(), true);
			
			//GraphConstants.setAutoSize(cell.getAttributes(), true);

			GraphConstants.setBorderColor(
					cell.getAttributes(), Color.black);
			
		// Add a Port
		DefaultPort port = new DefaultPort();
		cell.addPort();
		//cell.add(port);
		//port.setParent(cell);

		jgraph.getGraphLayoutCache().insert(cell);
		//jgraph.getGraphLayoutCache().setAutoSizeOnValueChange(true);
		int nodeIndex =jgraph.getModel().getIndexOfRoot(cell);
		mindmapNotes.putValue(Integer.toString(nodeIndex), "");
        
        
        //jgraph.add(minUsersPerGroupTextField);
        mindmapScroller = new javax.swing.JScrollPane(jgraph);
        //mindmapScroller.setSize(500,500);
        GridBagConstraints mindmapScrollerConstraints = new GridBagConstraints();
        mindmapScrollerConstraints.fill = GridBagConstraints.BOTH;
        mindmapScrollerConstraints.gridx=0;
        mindmapScrollerConstraints.gridy=1;
        mindmapScrollerConstraints.gridwidth = 2;
        mindmapScrollerConstraints.weightx=1;
        mindmapScrollerConstraints.weighty=1;
        
        mindmapPanel.add(mindmapScroller,mindmapScrollerConstraints);
        /*--------------------------END Mindmap Panel-------------------------------------*/
        
        
        
        
        /*-----------------------------START Mindmap Node Panel----------------------*/
        
        minNodeTitleLabel= new JLabel("Title",JLabel.RIGHT);
        GridBagConstraints minNodeTitleLabelConstraints = new GridBagConstraints();
        //minNodeTitleLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        minNodeTitleLabelConstraints.gridx=0;
        minNodeTitleLabelConstraints.gridy=0;
        mindmapNodePanel.add(minNodeTitleLabel,minNodeTitleLabelConstraints);
        
        mindmapNodeTitleField=new JTextField("");
        mindmapNodeTitleField.setColumns(25);
        mindmapNodeTitleField.setVisible(true);
        mindmapNodeTitleField.addFocusListener(this);
        GridBagConstraints mindmapNodeTitleFieldConstraints = new GridBagConstraints();
        //mindmapNodeTitleFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        mindmapNodeTitleFieldConstraints.gridx=1;
        mindmapNodeTitleFieldConstraints.gridy=0;
        mindmapNodeTitleFieldConstraints.weightx=1;
        mindmapNodePanel.add(mindmapNodeTitleField,mindmapNodeTitleFieldConstraints);
        
        mindmapNodeText =new JTextArea(30,60);
        mindmapNodeText.addFocusListener(this);
        //mindmapNodeText.setSize(DEFAULT_SIZE);
        mindmapNodeScroller= new javax.swing.JScrollPane(mindmapNodeText);
        GridBagConstraints mindmapNodeScrollerConstraints = new GridBagConstraints();
        mindmapNodeScrollerConstraints.fill = GridBagConstraints.BOTH;
        mindmapNodeScrollerConstraints.gridx=0;
        mindmapNodeScrollerConstraints.gridy=1;
        mindmapNodeScrollerConstraints.gridwidth=2;
        mindmapNodeScrollerConstraints.weightx=1;
        mindmapNodeScrollerConstraints.weighty=1;
        mindmapNodePanel.add(mindmapNodeScroller,mindmapNodeScrollerConstraints);
        

        /*------------------------End mindmap Node Panel------------------------------*/
        
        
        
		
		processingBookmark=new JTextField("");
    	processingBookmark.setVisible(true);

    	mindmapPanel.setToolTipText("Right click on the panel below to create a mind map.");
    	mindmapNodePanel.setToolTipText("Type in the text box below to add content to the currently selected node in your mind map.");
    	bookmarksPanel.setToolTipText("Use this menu to find interesting web sites.");
    	genresPanel.setToolTipText("This panel has tools for analysing the relationships between your bookmarks.");
    	userPanel.setToolTipText("You can adjust your settings here.");
        tabbedPane.addTab("Mindmap", null, mindmapPanel);
        tabbedPane.addTab("Current Mindmap Node", null, mindmapNodePanel);
        tabbedPane.addTab("Bookmarks", null, bookmarksPanel);
        tabbedPane.addTab("Sets", null, genresPanel);
        tabbedPane.addTab("Profile", null, userPanel);
        
        
    	GridBagConstraints menubarConstraints = new GridBagConstraints();
    	menubarConstraints.fill = GridBagConstraints.HORIZONTAL;
    	menubarConstraints.gridx=0;
    	menubarConstraints.gridy=0;
    	menubarConstraints.weighty=1;
        mainPanel.add(menuBar,menubarConstraints);
        
    	GridBagConstraints tabbedPaneConstraints = new GridBagConstraints();
    	tabbedPaneConstraints.fill = GridBagConstraints.BOTH;
    	tabbedPaneConstraints.gridx=0;
    	tabbedPaneConstraints.gridy=1;
    	tabbedPaneConstraints.weightx=1;
    	tabbedPaneConstraints.weighty=1;
        mainPanel.add(tabbedPane,tabbedPaneConstraints);
        
    	GridBagConstraints processingBookmarkConstraints = new GridBagConstraints();
    	processingBookmarkConstraints.fill = GridBagConstraints.HORIZONTAL;
    	processingBookmarkConstraints.gridx=0;
    	processingBookmarkConstraints.gridy=2;
    	processingBookmarkConstraints.weightx=1;
    	processingBookmarkConstraints.weighty=1;
        mainPanel.add(processingBookmark,processingBookmarkConstraints);


        this.add(mainPanel);

        validate();

        loadUsers(conn);

        loadSets(conn);
        
        loadBookmarks(conn);

        try{
        conn.close();
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
    

    

    public void loadUsers(Connection conn)
    {
    	if(!currentProcessor.getLastUser(conn).equalsIgnoreCase(""))
    	{
	    	try{
	    		HashMap<Integer,User> users= currentProcessor.getUsers(conn);
	    		Collection<User> tempUsers=users.values();
	    		Iterator<User> tempUsersIterator=tempUsers.iterator();

	    		((DefaultTableModel)usernameTabel.getModel()).getDataVector().setSize(tempUsers.size());
	    		((DefaultTableModel)usernameTabel.getModel()).setNumRows(tempUsers.size());
	    		for(int i=0;i<tempUsers.size();i++)
	    		{
	    			User currentUser=tempUsersIterator.next();

					Vector tempVector= new Vector(2);
					tempVector.add("");
					tempVector.add("");
					((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector()).set(i, tempVector);

	    			if(currentUser.getUsername().equals(usernameTextField.getText()))
	    			{
	    				continue;
	    			}

	    			((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector().elementAt(i)).set(0, currentUser.getUsername());
	    			((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector().elementAt(i)).set(1, currentUser.getCount());

	    		}
	        	}catch(Exception e)
	        	{
	        		e.printStackTrace();
	        	}
	        // Sort all the rows in descending order based on the
	        // values in the second column of the model
        	if(((DefaultTableModel)usernameTabel.getModel()).getDataVector().size()>0)
        	{
        		sortAllRowsBy((DefaultTableModel)usernameTabel.getModel(), 1, false);
        	}

    	}
    }
    
    public void loadBookmarks(Connection conn)
    {
    	HashMap<Integer,Url> urls= currentProcessor.getURLs(conn);
    	for(int i=0;i<urls.size();i++)
    	{
    		yourBookmarksList.add(urls.get(i).getUrl());
    	}
    }

    public void loadBookmarkSetUsers(int id)
    {
    	HashMap<Integer,User> users= currentProcessor.getUsers(conn);

    	URLSet set= sets.get(id);

    	try{

    		((DefaultTableModel)usernameTabel.getModel()).getDataVector().setSize(set.getUserIDs().size());
    		((DefaultTableModel)usernameTabel.getModel()).setNumRows(set.getUserIDs().size());

    		for(int i=0;i<set.getUserIDs().size();i++)
    		{

				Vector tempVector= new Vector(2);
				tempVector.add("");
				tempVector.add("");
				((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector()).set(i, tempVector);

    			if(users.get(set.getUserIDs().get(i)).getUsername().equals(usernameTextField.getText()))
    			{
    				continue;
    			}

    			((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector().elementAt(i)).set(0, users.get(set.getUserIDs().get(i)).getUsername());
    			((Vector)((DefaultTableModel)usernameTabel.getModel()).getDataVector().elementAt(i)).set(1, users.get(set.getUserIDs().get(i)).getUrls().size());

    		}
        	}catch(Exception e)
        	{
        		e.printStackTrace();
        	}

        	if(((DefaultTableModel)usernameTabel.getModel()).getDataVector().size()>0)
        	{
        		sortAllRowsBy((DefaultTableModel)usernameTabel.getModel(), 1, false);
        	}

        	userBookmarksInCommon.removeAll();
    		for(int i=0;i<set.getURLCount();i++)
    		{

    			userBookmarksInCommon.add(currentProcessor.getURLs(conn).get(set.getUrlIDs().get(i)).getUrl());
    		}

    }

    public void loadSets(Connection conn)
    {

    	if(!currentProcessor.getLastUser(conn).equalsIgnoreCase(""))
    	{
    		HashMap<Integer,URLSet> setsHashMap= currentProcessor.getSets(conn);
    		Collection<URLSet> tempSets=setsHashMap.values();
    		Iterator<URLSet> tempSetsIterator=tempSets.iterator();

    		this.sets= new ArrayList<URLSet>();
    		while(tempSetsIterator.hasNext())
    		{
    			this.sets.add(tempSetsIterator.next());
    		}

    		//sortSetsByRank();

    		HashMap<Integer,Url> urls = currentProcessor.getURLs(conn);
        	try{

	    		genres.removeAll();

	    		for(int i=0;i<sets.size();i++)
	    		{
	    			String currentGenreBookmarks="";
	    			int minUsersPerGroup=0;
	    			boolean showThisGroup=false;
	    			try{
	    				minUsersPerGroup=Integer.parseInt(minUsersPerGroupTextField.getText());
	    				
	    			}catch(NumberFormatException ne)
	    			{
	    				
	    			}

	    			if(sets.get(i).getUrlIDs().size()>=minUsersPerGroup){
		    			for(int j=0;j<sets.get(i).getUrlIDs().size();j++)
		    			{
		    				currentGenreBookmarks+=urls.get(sets.get(i).getUrlIDs().get(j)).getUrl();
		    				if(j<sets.get(i).getUrlIDs().size()-1)
		    				{
		    					currentGenreBookmarks+=" | ";
		    				}
		    			}
		    			genres.add(currentGenreBookmarks);
	    			}
	    		}

            	}catch(Exception e)
            	{
            		e.printStackTrace();
            	}
    	}

    }

    public void sortSetsByRank()
    {
    	URLSetComparator compare= new URLSetComparator();
    	Collections.sort(sets, compare);
    }

    // Regardless of sort order (ascending or descending), null values always appear last.
    // colIndex specifies a column in model.
    public void sortAllRowsBy(DefaultTableModel model, int colIndex, boolean ascending) {
        Vector data = model.getDataVector();
        Collections.sort(data, new ColumnSorter(colIndex, ascending));
        model.fireTableStructureChanged();
    }

    
	public static DefaultGraphCell createVertex(String name, double x,
			double y, double w, double h, Color bg, boolean raised) {

			// Create vertex with the given name
			DefaultGraphCell cell = new DefaultGraphCell(name);

			// Set bounds
			GraphConstants.setBounds(cell.getAttributes(),
					new Rectangle2D.Double(x, y, w, h));


			GraphConstants.setBorderColor(
					cell.getAttributes(), Color.black);


			// Add a Port
			DefaultPort port = new DefaultPort();
			cell.add(port);
			port.setParent(cell);

			return cell;
		}

    
    public void actionPerformed(ActionEvent e) {

    	if(e.getActionCommand().equals("AddMindMapNode"))
    	{
    		
    		Object[] selection=jgraph.getSelectionModel().getSelectionCells();
    		
    		if(jgraph.getSelectionCount()==1)
    		{

        		DefaultGraphCell[] cellsToInsert= new DefaultGraphCell[3];

        		
    			DefaultGraphCell cell = new DefaultGraphCell();
    			
    			Rectangle2D selectedCellBounds=GraphConstants
    			.getBounds(jgraph.getModel().getAttributes(selection[0]));
    			double selectedCellBoundsX=selectedCellBounds.getX();
    			double selectedCellBoundsY=selectedCellBounds.getY();
    			double selectedCellBoundsH=selectedCellBounds.getHeight();
    			double selectedCellBoundsW=selectedCellBounds.getWidth();
    			
    			GraphConstants.setBounds(cell.getAttributes(),
    					new Rectangle2D.Double(selectedCellBoundsX+5, 
    							selectedCellBoundsY+5, selectedCellBoundsW, selectedCellBoundsH));
    			
    			//GraphConstants.setAutoSize(cell.getAttributes(), true);

    			GraphConstants.setBackground(
    					cell.getAttributes(), GraphConstants.getBackground(
    	    					jgraph.getModel().getAttributes(selection[0])));
    				GraphConstants.setOpaque(
    					cell.getAttributes(), true);

    			GraphConstants.setBorderColor(
    					cell.getAttributes(), Color.black);


    			// Add a Port
    			DefaultPort port = new DefaultPort();
    			cell.add(port);
    			port.setParent(cell);
    			jgraph.getSelectionModel().clearSelection();
    			jgraph.setSelectionCell(cell);
    			cellsToInsert[0]=(DefaultGraphCell)selection[0];
    			cellsToInsert[1] =cell;
    			
    			

        		DefaultEdge edge = new DefaultEdge("");
        		
        		edge.setTarget(cellsToInsert[1].getChildAt(0));
        		edge.setSource(jgraph.getModel().getChild(selection[0],0));
        		
        		int arrow = GraphConstants.ARROW_CLASSIC;
        		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
        		GraphConstants.setEndFill(edge.getAttributes(), true);
        		GraphConstants.setLabelAlongEdge(edge.getAttributes(), true) ;
        		
        		cellsToInsert[2] = edge;
        		
        		jgraph.getGraphLayoutCache().insert(cellsToInsert);
        		
        		int nodeIndex =jgraph.getModel().getIndexOfRoot(cell);
        		mindmapNotes.putValue(Integer.toString(nodeIndex), "");

    		}


    	}
    	
    	
    	if(e.getActionCommand().equals("RemoveMindMapNode"))
    	{
    		Object[] selection=jgraph.getSelectionCells();

    			for(int i=0;i<selection.length;i++)
    			{
        			if(!jgraph.getModel().isEdge(selection[i]))
        			{
            			int selectedNode =jgraph.getModel().getIndexOfRoot(selection[i]);
            			mindmapNotes.remove(new Attributes.Name(Integer.toString(selectedNode)));
        			}

        			jgraph.getGraphLayoutCache().remove(selection);
    			}

    		
    	}
    	
    	

    	
    	if(e.getActionCommand().equals("SaveCurrentMindmap"))
    	{
    		try{
    		currentGraph= JRDF_FACTORY.getNewGraph();
    		GraphElementFactory elementFactory = currentGraph.getElementFactory();
            TripleFactory tripleFactory = currentGraph.getTripleFactory();
    		//get every edge and the cells to which it is attached
    		//for each of these triples add them to the rdf graph
    		//then write the graph to a file and add it to the database
    		 
    		for(int i=0;i<jgraph.getModel().getRootCount();i++)
    		{
    			Object currentRoot=jgraph.getModel().getRootAt(i);
    			if(jgraph.getModel().isEdge(currentRoot) )
    			{
    				//get source and target
    				Object sourcePort=jgraph.getModel().getSource(currentRoot);
    				Object source=jgraph.getModel().getParent(sourcePort);
    				Object targetPort=jgraph.getModel().getTarget(currentRoot);
    				Object target=jgraph.getModel().getParent(targetPort);
    				//you could consider the position of the cell to be an attribute of the cell
    				//and store it as a triple cell has x position value and same for y
    				//this could be used for label, and notes or url
    				Literal resource=elementFactory.createLiteral(
    						Integer.toString(jgraph.getModel().getIndexOfRoot(target)));
    				
    			}
    			
    		}
    		//print
    		}catch(Exception saveMapException)
    		{
    			saveMapException.printStackTrace();
    		}
    	}
    	
    	if(e.getActionCommand().equals("LoadSelectedMindmap"))
    	{
    	
    	}
    	
    	if(e.getActionCommand().equals("DeleteSelectedMindmap"))
    	{

    	}
    	
    	
    	
    	if(e.getActionCommand().equals("UndoMindmap"))
    	{
    		undoManager.undo();
    	}
    	
    	if(e.getActionCommand().equals("RedoMindmap"))
    	{
    		undoManager.redo();
    	}
    	
    	
    	if(e.getActionCommand().equals("ZoomInMindmap"))
    	{
    		double scale =jgraph.getScale(); 
    		jgraph.setScale(scale*1.1);
    	}
    	
    	if(e.getActionCommand().equals("ZoomOutMindmap"))
    	{
    		double scale =jgraph.getScale(); 
    		jgraph.setScale(scale*0.9);
    	}
    	
    	
    	if(e.getActionCommand().equals("ConnectNodes"))
    	{
    		Object[] selection=jgraph.getSelectionModel().getSelectionCells();
    		
    		if(jgraph.getSelectionCount()==2)
    		{

        		DefaultGraphCell[] cellsToInsert= new DefaultGraphCell[3];

        		DefaultEdge edge = new DefaultEdge("");
        		
        		int arrow = GraphConstants.ARROW_CLASSIC;
        		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
        		GraphConstants.setEndFill(edge.getAttributes(), true);
        		GraphConstants.setLabelAlongEdge(edge.getAttributes(), true) ;
        		
        		edge.setSource(jgraph.getModel().getChild(selection[0],0));
        		edge.setTarget(jgraph.getModel().getChild(selection[1],0));
        		
        		cellsToInsert[0] = edge;
        		cellsToInsert[1]=(DefaultGraphCell)selection[0];
        		cellsToInsert[2]=(DefaultGraphCell)selection[1];
        		
        		jgraph.getGraphLayoutCache().insert(cellsToInsert);

    		}
    		else if(jgraph.getSelectionCount()==1)
    		{
        		DefaultGraphCell[] cellsToInsert= new DefaultGraphCell[2];

        		DefaultEdge edge = new DefaultEdge("");
        		
        		int arrow = GraphConstants.ARROW_CLASSIC;
        		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
        		GraphConstants.setEndFill(edge.getAttributes(), true);
        		GraphConstants.setLabelAlongEdge(edge.getAttributes(), true) ;
        		
        		edge.setSource(jgraph.getModel().getChild(selection[0],0));
        		edge.setTarget(jgraph.getModel().getChild(selection[0],0));
        		
        		cellsToInsert[0] = edge;
        		cellsToInsert[1]=(DefaultGraphCell)selection[0];
        		
        		jgraph.getGraphLayoutCache().insert(cellsToInsert);
    			
    		}
    	}
    	
    	if(e.getActionCommand().equals("EditLabel"))
    	{
    		
    		Object[] selection=jgraph.getSelectionModel().getSelectionCells();
    		
    		if(jgraph.getSelectionCount()==1)
    		{

    			jgraph.startEditingAtCell(selection[0]) ;

    		}
    	} 
    	
    	
    	if(e.getActionCommand().equals("ChangeColor"))
    	{
    		
    		Object[] selection=jgraph.getSelectionModel().getSelectionCells();
    		DefaultGraphCell[] cellToInsert= new DefaultGraphCell[1];
    		if(jgraph.getSelectionCount()==1)
    		{
    			Color newColor = JColorChooser.showDialog(
                        this,
                        "Choose a Color",
                        GraphConstants.getBackground(
            					jgraph.getModel().getAttributes(selection[0])));
    			
    			GraphConstants.setBackground(
    					jgraph.getModel().getAttributes(selection[0]), newColor);
    			
    			
    			cellToInsert[0]=(DefaultGraphCell)selection[0];
    			jgraph.getGraphLayoutCache().insert(cellToInsert);

    		}
    		
    		
    	}
    	
    	if(e.getActionCommand().equals("AddBookmarkToNode"))
    	{

    		Object[] selection=jgraph.getSelectionCells();
    		if(selection.length==1)
    		{
    			int selectedNode =jgraph.getModel().getIndexOfRoot(selection[0]);
    			
    			mindmapNotes.put(new Attributes.Name(Integer.toString(selectedNode)),yourBookmarksList.getSelectedItem());
    			mindmapNodeText.setText(mindmapNotes.getValue(Integer.toString(selectedNode)));
    			
    			mindmapNodeTitleField.setText(yourBookmarksList.getSelectedItem());
    			((DefaultGraphCell)selection[0]).setUserObject(yourBookmarksList.getSelectedItem());
    			jgraph.getGraphLayoutCache().setVisible(selection[0], true);
    			//GraphConstants.setResize(jgraph.getModel().getAttributes(selection[0]), true);
    			//jgraph.getModel().edit(jgraph.getModel().getAttributes(selection[0]), null, null, null);
    		}
    		
    		
    	}
    	
    	

    	if(e.getSource().equals(userBookmarksInCommon))
    	{
    		launcher.openURLinBrowser(e.getActionCommand());
    	}

    	if(e.getActionCommand().equals("ShowAllUsers"))
    	{
        	try{
    		Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
        	}
        	catch(Exception ex)
        	{
        		ex.printStackTrace();
        	}

            	loadUsers(conn);

			try{
				conn.close();
            }catch(Exception ex)
            {
            	ex.printStackTrace();
            }

    	}

    	if(e.getActionCommand().equals("ShowCategoryBookmarks"))
    	{
    		if(currentProcessingState==ProcessorThread.ProcessorThreadState.STOPPED)
    		{
            	try
            	{

            		processorThread = new ProcessorThread(currentProcessor,usernameTextField.getText(),genres.getSelectedIndex(),false);
            	}
            	catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}

    		}
    		else
    		{
    			processingBookmark.setText("Don't push that button!");
    		}
    	}


    	if(genres.getSelectedIndex()!=-1&&e.getSource().equals(genres))
    	{
    		try
    		{
    			loadBookmarkSetUsers(genres.getSelectedIndex());
    		}
    		catch(NumberFormatException numE)
    		{
    			numE.printStackTrace();
    		}

    	}

    	if(e.getActionCommand().equals("OpenThisUsersBookmarks")&& !currentUsersBookmarksField.getText().equals(""))
    	{

    		//launcher.openURLinBrowser(currentUsersBookmarksField.getText());
    		launcher.openURLinBrowser("http://www.apphive.com/Redirect.php?forward_to="+currentUsersBookmarksField.getText());
    	}

    	if(genres.getSelectedIndex()!=-1&&e.getActionCommand().equals("ShowCategoryUsers"))
    	{
    		loadBookmarkSetUsers(genres.getSelectedIndex());
    	}

    	if(userBookmarksInCommon.getSelectedItem()!=null&&e.getActionCommand().equals("OpenBookmark"))
    	{

    		//launcher.openURLinBrowser(userBookmarksInCommon.getSelectedItem());
    		launcher.openURLinBrowser("http://www.apphive.com/Redirect.php?forward_to="+userBookmarksInCommon.getSelectedItem());
    	}

    	if((e.getSource().equals(usernameTextField)&& !usernameTextField.getText().equals(""))||
    			("Get Bookmarks".equals(e.getActionCommand()) && !usernameTextField.getText().equals("")))
    	{
    		if(currentProcessingState==ProcessorThread.ProcessorThreadState.STOPPED)
    		{
            	try
            	{
            		//Processor newProcessor= new Processor(this,this);
            		processorThread = new ProcessorThread(currentProcessor,usernameTextField.getText(),0,true);
            	}
            	catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}

    		}
    		else
    		{
    			processingBookmark.setText("Don't push that button!");
    		}

    	}


    }

    public void focusGained(FocusEvent e) 
    {

    	
    }
    
    public void focusLost(FocusEvent e)
    {
    	if(e.getSource().equals(mindmapNodeText))
    	{
    		Object[] selection=jgraph.getSelectionCells();
    		if(selection.length==1)
    		{
    			int selectedNode =jgraph.getModel().getIndexOfRoot(selection[0]);
    			
    			mindmapNotes.put(new Attributes.Name(Integer.toString(selectedNode)),mindmapNodeText.getText());

    			jgraph.getGraphLayoutCache().setVisible(selection[0], true);
    		}

    	}
    	
    	if(e.getSource().equals(mindmapNodeTitleField))
    	{
    		Object[] selection=jgraph.getSelectionCells();
    		if(selection.length==1)
    		{

    			((DefaultGraphCell)selection[0]).setUserObject(mindmapNodeTitleField.getText());
    			jgraph.getGraphLayoutCache().setVisible(selection[0], true);
    		}
    	}
    	
    	if(e.getSource().equals(jgraph))
    	{
    		Object[] selection=jgraph.getSelectionCells();
    		if(selection.length==1)
    		{
    			int selectedNode =jgraph.getModel().getIndexOfRoot(selection[0]);
    			mindmapNodeText.setText(mindmapNotes.getValue(Integer.toString(selectedNode)));
    			
    			mindmapNodeTitleField.setText((String)jgraph.getModel().getValue(selection[0]));
    		}
    		
    	}
    	
    }    
    
     
    
    
    public void mouseClicked(MouseEvent e){
    	try{
			Class.forName("org.hsqldb.jdbcDriver");
	        conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        if (!e.getComponent().equals(jgraph)&&(e.getClickCount() == 2 || e.getClickCount() == 1)){
           int selectedRow=usernameTabel.getSelectedRow();
           String currentUsername=(String)usernameTabel.getValueAt(selectedRow, 0);
		        try
		    	{
		        	HashMap<Integer,User> users= currentProcessor.getUsers(conn);
		        	HashMap<Integer,Url> urls= currentProcessor.getURLs(conn);
		        	User user=currentProcessor.getUser(currentUsername);

	    			userBookmarksInCommon.removeAll();

		    		for(int i=0;i<user.getUrls().size();i++)
		    		{
		    			userBookmarksInCommon.add(urls.get(user.getUrls().get(i)).getUrl());
		    		}

		    		currentUsersBookmarksField.setText("http://del.icio.us/"+currentUsername);

		    	}
		    	catch(Exception ex)
		    	{

		    	}
           }

		try{
			conn.close();
        }catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }

    public void mouseEntered(MouseEvent e){

        }

    public void mouseExited(MouseEvent e){

        }

    public void mousePressed(MouseEvent e){
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                       e.getX(), e.getY());
        }
        }

    public void mouseReleased(MouseEvent e){
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                       e.getX(), e.getY());
        }
        }

    public void hyperlinkUpdate(HyperlinkEvent he)
    {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(he.getEventType())  )
        {

        	try
        	{
        		launcher.openURLinBrowser(he.getURL().toURI().toString());

        	}
        	catch(Exception ex)
        	{

        	}

        }

    }

	public void messageSent(ProcessorMessageEvent e)
	{
		processingBookmark.setText(e.message);
	}



	public void processorThreadstateChanged(ProcessorThreadStateChangedEvent e)
	{
		currentProcessingState=e.state;
    	try{
    		Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:Processor","sa","");
        	}
        	catch(Exception ex)
        	{
        		ex.printStackTrace();
        	}

			if(e.state==ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGUSERS)
			{
				loadUsers(conn);
				processingBookmark.setText("");

			}
			else if(e.state==ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGUSER)
			{
				loadUsers(conn);
			}
			else if(e.state==ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGSET)
			{
				loadSets(conn);
			}
			else if(e.state==ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGSETS)
			{
				loadSets(conn);
				processingBookmark.setText("");
			}
			/**
			else if(e.state==ProcessorThread.ProcessorThreadState.FINISHEDPROCESSINGRECOMMENDATIONS)
			{
				java.util.List<String> recommendation=currentProcessor.getRecommedation();
    			userBookmarksInCommon.removeAll();

	    		for(int i=0;i<recommendation.size();i++)
	    		{
	    			userBookmarksInCommon.add(recommendation.get(i));
	    		}

			}
			**/

			try{
				conn.close();
            }catch(Exception ex)
            {
            	ex.printStackTrace();
            }

	}

    private static void createGUI() {

        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("The Reaper");
        frame.setLayout(new GridBagLayout());
        File icon=new File("TheReaperIcon.png");
        Image iconImage=null;
        try{
        	iconImage=ImageIO.read(icon);
        }
        catch(Exception e)
        {
        	System.out.print(e.getMessage());
        }
        frame.setIconImage(iconImage);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TheReaper theReaper = new TheReaper();
        theReaper.setOpaque(true);
        frame.setContentPane(theReaper);
        frame.pack();
        frame.setVisible(true);
        //frame.setSize(750, 700);
        frame.setResizable(true);
    }


    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }
}


