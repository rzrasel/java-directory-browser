// File: DirectoryBrowser.java

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
/**
 DirectoryBrowser - main GUI class showing a checkbox tree and handling combine/write operation.
 Save dialog defaults to the selected folder location.
 */
public class DirectoryBrowser {private static JFrame frame;                         // main application window
    private static JTextField pathField;                 // shows selected folder path (left)
    private static JButton selectButton;                 // "Select Folder" button (right)
    private static JCheckBoxTree tree;                   // checkbox-enabled tree
    private static DefaultTreeModel treeModel;           // tree model
    private static JButton writeButton;                  // "Write to File" button (right panel)
    private static JTextArea statusArea;                 // status log area (right panel)
    private static final FileSystemView fsv = FileSystemView.getFileSystemView(); // system icons/names// keep track of the currently selected root folder to use as default save location
    private static File selectedRootDir = null;// View menu constants
    public static final int VIEW_LARGE_ICONS = 0;
    public static final int VIEW_MEDIUM_ICONS = 1;
    public static final int VIEW_SMALL_ICONS = 2;
    public static final int VIEW_LIST = 3;
    public static final int VIEW_DETAILS = 4;
    private static int currentViewMode = VIEW_LARGE_ICONS;// Refurbished Material Design Colors - Professional Modern Scheme
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);      // Indigo 600
    private static final Color PRIMARY_DARK = new Color(48, 63, 159);       // Indigo 800
    private static final Color PRIMARY_LIGHT = new Color(197, 202, 233);    // Indigo 100
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);       // Deep Orange 500
    private static final Color ACCENT_DARK = new Color(216, 67, 21);        // Deep Orange 700
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);      // Green 600
    private static final Color WARNING_COLOR = new Color(255, 152, 0);      // Orange 500
    private static final Color ERROR_COLOR = new Color(244, 67, 54);        // Red 500
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light Gray Blue
    private static final Color CARD_COLOR = new Color(255, 255, 255);       // White
    private static final Color SURFACE_COLOR = new Color(250, 250, 250);    // Surface White
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);        // Gray 900
    private static final Color TEXT_SECONDARY = new Color(97, 97, 97);      // Gray 700
    private static final Color TEXT_HINT = new Color(158, 158, 158);        // Gray 500
    private static final Color DIVIDER_COLOR = new Color(224, 224, 224);    // Gray 300
    private static final Color HOVER_COLOR = new Color(245, 245, 245);      // Gray 100
    private static final Color SELECTION_BG = new Color(232, 234, 246);     // Indigo 50
    private static final Color SELECTION_FG = new Color(63, 81, 181);       // Indigo 600// Button specific colors
    private static final Color BUTTON_PRIMARY_BG = PRIMARY_COLOR;
    private static final Color BUTTON_PRIMARY_FG = Color.WHITE;
    private static final Color BUTTON_PRIMARY_HOVER = new Color(57, 73, 171);  // Indigo 700
    private static final Color BUTTON_ACCENT_BG = ACCENT_COLOR;
    private static final Color BUTTON_ACCENT_FG = Color.WHITE;
    private static final Color BUTTON_ACCENT_HOVER = ACCENT_DARK;
    private static final Color BUTTON_DISABLED_BG = new Color(189, 189, 189);  // Gray 400
    private static final Color BUTTON_DISABLED_FG = new Color(117, 117, 117);  // Gray 600// Dialog button colors
    private static final Color DIALOG_BUTTON_BG = new Color(250, 250, 250);    // Gray 50
    private static final Color DIALOG_BUTTON_FG = Color.BLACK;                 // Black as requested
    private static final Color DIALOG_BUTTON_HOVER = new Color(240, 240, 240); // Gray 100
    private static final Color DIALOG_BUTTON_BORDER = new Color(218, 220, 224);// Gray 300// Launch UI (called by Main)
    public static void launch() {
        SwingUtilities.invokeLater(DirectoryBrowser::createAndShowGUI);
    }// Build and show GUI
    private static void createAndShowGUI() {
        try {
// Use system look and feel for better native integration
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());// Customize UI for Material Design
            setupMaterialDesignUI();} catch (Exception e) {
            e.printStackTrace();
        }frame = new JFrame("Directory Browser - Combine Selected Files");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setLayout(new BorderLayout(0, 0));// Create menu bar
        createMenuBar();// Top panel with Material Design styling
        JPanel topPanel = createTopPanel();
        frame.add(topPanel, BorderLayout.NORTH);// Initial tree placeholder
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("No folder selected");
        treeModel = new DefaultTreeModel(root);
        tree = new JCheckBoxTree(root);
        tree.setIconSize(48); // Default large icon size
        tree.setRowHeight(64);  // Increased for larger icons and text
        tree.setBackground(CARD_COLOR);
        tree.setForeground(TEXT_PRIMARY);JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, DIVIDER_COLOR));
        treeScroll.getViewport().setBackground(CARD_COLOR);
        treeScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));// Right panel with Material Design styling
        JPanel rightPanel = createRightPanel();// Split pane: tree (left) and rightPanel (right)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, rightPanel);
        split.setResizeWeight(0.5);
        split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setDividerSize(7);
        split.setDividerLocation(650);
        split.setContinuousLayout(true);
        split.setBackground(BACKGROUND_COLOR);
        frame.add(split, BorderLayout.CENTER);frame.setSize(1000, 700);
        centerFrame(frame);
        frame.setVisible(true);
    }// Setup Material Design UI properties
    private static void setupMaterialDesignUI() {
// Enhanced Material Design styling
        UIManager.put("nimbusBase", PRIMARY_LIGHT);
        UIManager.put("nimbusBlueGrey", new Color(96, 125, 139));
        UIManager.put("control", CARD_COLOR);
        UIManager.put("info", new Color(232, 244, 255));
        UIManager.put("nimbusFocus", PRIMARY_COLOR);
        UIManager.put("nimbusLightBackground", CARD_COLOR);
        UIManager.put("nimbusSelectionBackground", SELECTION_BG);// Button styling
        UIManager.put("Button.background", BUTTON_PRIMARY_BG);
        UIManager.put("Button.foreground", BUTTON_PRIMARY_FG);
        UIManager.put("Button.disabledText", BUTTON_DISABLED_FG);
        UIManager.put("Button.select", BUTTON_PRIMARY_HOVER);
        UIManager.put("Button.focus", PRIMARY_COLOR);
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(12, 24, 12, 24));// Text field styling
        UIManager.put("TextField.background", CARD_COLOR);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
        UIManager.put("TextField.selectionBackground", new Color(63, 81, 181, 100));
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.inactiveForeground", TEXT_HINT);// Text area styling
        UIManager.put("TextArea.background", CARD_COLOR);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.selectionBackground", new Color(63, 81, 181, 100));
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.inactiveForeground", TEXT_HINT);// Scroll pane styling
        UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(DIVIDER_COLOR, 1));
        UIManager.put("ScrollPane.foreground", TEXT_PRIMARY);// Panel styling
        UIManager.put("Panel.background", BACKGROUND_COLOR);// Label styling
        UIManager.put("Label.foreground", TEXT_PRIMARY);// Tree styling
        UIManager.put("Tree.background", CARD_COLOR);
        UIManager.put("Tree.foreground", TEXT_PRIMARY);
        UIManager.put("Tree.selectionBackground", SELECTION_BG);
        UIManager.put("Tree.selectionForeground", SELECTION_FG);
        UIManager.put("Tree.selectionBorderColor", PRIMARY_COLOR);// OptionPane (Dialog) styling
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.buttonBackground", DIALOG_BUTTON_BG);
        UIManager.put("OptionPane.buttonForeground", DIALOG_BUTTON_FG);
        UIManager.put("OptionPane.buttonBorder", BorderFactory.createLineBorder(DIALOG_BUTTON_BORDER, 1));// FileChooser styling
        UIManager.put("FileChooser.background", BACKGROUND_COLOR);
        UIManager.put("FileChooser.foreground", TEXT_PRIMARY);
    }
    private static void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_DARK);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JMenu fileMenu = createMenu("File", Color.WHITE);
        JMenuItem exitItem = createMenuItem("Exit", e -> System.exit(0));
        fileMenu.add(exitItem);// View menu
        JMenu viewMenu = createMenu("View", Color.WHITE);
        ButtonGroup viewGroup = new ButtonGroup();JRadioButtonMenuItem largeIconItem = createRadioMenuItem("Large Icons", VIEW_LARGE_ICONS, viewGroup);
        JRadioButtonMenuItem mediumIconItem = createRadioMenuItem("Medium Icons", VIEW_MEDIUM_ICONS, viewGroup);
        JRadioButtonMenuItem smallIconItem = createRadioMenuItem("Small Icons", VIEW_SMALL_ICONS, viewGroup);
        JRadioButtonMenuItem listItem = createRadioMenuItem("List", VIEW_LIST, viewGroup);
        JRadioButtonMenuItem detailsItem = createRadioMenuItem("Details", VIEW_DETAILS, viewGroup);
        largeIconItem.setSelected(true);
        viewMenu.add(largeIconItem);
        viewMenu.add(mediumIconItem);
        viewMenu.add(smallIconItem);
        viewMenu.addSeparator();
        viewMenu.add(listItem);
        viewMenu.add(detailsItem);
        JMenu helpMenu = createMenu("Help", Color.WHITE);
        JMenuItem aboutItem = createMenuItem("About", e -> showAboutDialog());
        helpMenu.add(aboutItem);// Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);frame.setJMenuBar(menuBar);
    }// Helper method to create menu
    private static JMenu createMenu(String text, Color foreground) {
        JMenu menu = new JMenu(text);
        menu.setForeground(foreground);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return menu;
    }// Helper method to create menu item
    private static JMenuItem createMenuItem(String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.addActionListener(action);
        return item;
    }// Helper method to create radio menu item for view options
    private static JRadioButtonMenuItem createRadioMenuItem(String text, int viewMode, ButtonGroup group) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.addActionListener(e -> {
            currentViewMode = viewMode;
            applyViewMode();
        });
        group.add(item);
        return item;
    }// Apply the selected view mode to the tree
    private static void applyViewMode() {
        if (tree != null) {
            switch (currentViewMode) {
                case VIEW_LARGE_ICONS:
                    tree.setIconSize(48);
                    tree.setRowHeight(64);
                    tree.setFontSize(16);
                    appendStatus("View mode: Large Icons (48x48)");
                    break;
                case VIEW_MEDIUM_ICONS:
                    tree.setIconSize(36);
                    tree.setRowHeight(56);
                    tree.setFontSize(14);
                    appendStatus("View mode: Medium Icons (36x36)");
                    break;
                case VIEW_SMALL_ICONS:
                    tree.setIconSize(24);
                    tree.setRowHeight(40);
                    tree.setFontSize(12);
                    appendStatus("View mode: Small Icons (24x24)");
                    break;
                case VIEW_LIST:
                    tree.setIconSize(20);
                    tree.setRowHeight(32);
                    tree.setFontSize(12);
                    appendStatus("View mode: List (minimal icons)");
                    break;
                case VIEW_DETAILS:
                    tree.setIconSize(24);
                    tree.setRowHeight(48);
                    tree.setFontSize(13);
                    appendStatus("View mode: Details (with additional information)");
                    break;
            }
            tree.repaint();
        }
    }
    private static void showAboutDialog() {
        JDialog aboutDialog = new JDialog(frame, "About Directory Browser", true);
        aboutDialog.setLayout(new BorderLayout(20, 20));
        aboutDialog.setSize(400, 300);
        aboutDialog.setBackground(BACKGROUND_COLOR);
        aboutDialog.setLocationRelativeTo(frame);JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));// Title
        JLabel titleLabel = new JLabel("Directory Browser", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_DARK);
        contentPanel.add(titleLabel, BorderLayout.NORTH);// Version info
        JLabel versionLabel = new JLabel("Version 2.0", JLabel.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setForeground(TEXT_SECONDARY);
        contentPanel.add(versionLabel, BorderLayout.CENTER);// Description
        JTextArea descArea = new JTextArea();
        descArea.setText("A Material Design file browser with checkbox selection.\n\n" +
                "Features:\n" +
                "• Browse directories with checkboxes\n" +
                "• Multiple view modes (Icons, List, Details)\n" +
                "• Combine selected files into one output\n" +
                "• Modern Material Design UI");
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setForeground(TEXT_PRIMARY);
        descArea.setBackground(BACKGROUND_COLOR);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        contentPanel.add(new JScrollPane(descArea), BorderLayout.SOUTH);// OK button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton okButton = createMaterialButton("OK", BUTTON_PRIMARY_BG, BUTTON_PRIMARY_FG, BUTTON_PRIMARY_HOVER);
        okButton.setPreferredSize(new Dimension(100, 36));
        okButton.addActionListener(e -> aboutDialog.dispose());
        buttonPanel.add(okButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);aboutDialog.add(contentPanel);
        aboutDialog.setVisible(true);
    }// Create top panel with Material Design
    private static JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 229, 229)),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));// Title label with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);// Create a simple folder icon
        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));JLabel titleLabel = new JLabel("Directory Browser");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_DARK);titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);// Path field and select button container
        JPanel pathContainer = new JPanel(new BorderLayout(12, 12));
        pathContainer.setOpaque(false);// Path field with Material Design style
        pathField = new JTextField();
        pathField.setEditable(false);
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pathField.setBackground(new Color(255, 255, 255));
        pathField.setForeground(TEXT_PRIMARY);
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(DIVIDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        pathContainer.add(pathField, BorderLayout.CENTER);// Select button with refined Material Design style
        selectButton = createMaterialButton("Select Folder", BUTTON_PRIMARY_BG, BUTTON_PRIMARY_FG, BUTTON_PRIMARY_HOVER);
        selectButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        selectButton.setPreferredSize(new Dimension(150, 42));
        selectButton.addActionListener(DirectoryBrowser::onSelectFolder);
        pathContainer.add(selectButton, BorderLayout.EAST);topPanel.add(pathContainer, BorderLayout.CENTER);return topPanel;
    }// Create right panel with Material Design
    private static JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(20, 20));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));// Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);JLabel headerLabel = new JLabel("Combine Files");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(TEXT_PRIMARY);JLabel subHeaderLabel = new JLabel("Select files and combine into one output");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(TEXT_SECONDARY);headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(subHeaderLabel, BorderLayout.SOUTH);
        rightPanel.add(headerPanel, BorderLayout.NORTH);// Center panel for write button and status
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(BACKGROUND_COLOR);// Write button with refined Material Design style
        writeButton = createMaterialButton("Write to File", BUTTON_ACCENT_BG, BUTTON_ACCENT_FG, BUTTON_ACCENT_HOVER);
        writeButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        writeButton.setEnabled(false);
        writeButton.setPreferredSize(new Dimension(250, 48));
        writeButton.addActionListener(e -> onWriteToFile());// Button panel with shadow effect
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        buttonPanel.add(writeButton);
        centerPanel.add(buttonPanel, BorderLayout.NORTH);// Status area with refined Material Design style
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);JPanel statusHeader = new JPanel(new BorderLayout());
        statusHeader.setOpaque(false);JLabel statusLabel = new JLabel("Status Log");
        statusLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        statusLabel.setForeground(TEXT_SECONDARY);JLabel counterLabel = new JLabel("0 files selected");
        counterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        counterLabel.setForeground(TEXT_HINT);statusHeader.add(statusLabel, BorderLayout.WEST);
        statusHeader.add(counterLabel, BorderLayout.EAST);
        statusHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        statusPanel.add(statusHeader, BorderLayout.NORTH);statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setRows(15);
        statusArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusArea.setBackground(CARD_COLOR);
        statusArea.setForeground(TEXT_PRIMARY);
        statusArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));// Add line numbers
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setBorder(BorderFactory.createEmptyBorder());
        statusScroll.getViewport().setBackground(CARD_COLOR);// Custom vertical scrollbar
        JScrollBar verticalScrollBar = statusScroll.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = TEXT_HINT;
                this.trackColor = BACKGROUND_COLOR;
            }@Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }@Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });statusPanel.add(statusScroll, BorderLayout.CENTER);
        centerPanel.add(statusPanel, BorderLayout.CENTER);
        rightPanel.add(centerPanel, BorderLayout.CENTER);return rightPanel;
    }// Create refined Material Design styled button
    private static JButton createMaterialButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// Button shadow
                if (isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 8, 8);
                }// Button background
                if (!isEnabled()) {
                    g2.setColor(BUTTON_DISABLED_BG);
                } else if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(bgColor);
                }g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);// Button border
                if (isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 10));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }g2.dispose();super.paintComponent(g);
            }
        };
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));// Set disabled colors
        button.setDisabledIcon(null);
        button.addChangeListener(e -> {
            if (!button.isEnabled()) {
                button.setForeground(BUTTON_DISABLED_FG);
            } else {
                button.setForeground(fgColor);
            }
        });return button;
    }// Handler for Select Folder button
    private static void onSelectFolder(ActionEvent e) {
        JFileChooser chooser = createStyledFileChooser("Select Folder to Browse", JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));int res = chooser.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            selectedRootDir = selected;
            pathField.setText(selected.getAbsolutePath());
            loadDirectoryTree(selected);
            writeButton.setEnabled(true);
            appendStatus("✓ Loaded: " + selected.getAbsolutePath());
        }
    }// Create styled file chooser with custom button colors
    private static JFileChooser createStyledFileChooser(String title, int selectionMode) {
        JFileChooser chooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setBackground(BACKGROUND_COLOR);// Style dialog buttons
                Container contentPane = dialog.getContentPane();
                Component[] components = contentPane.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        Component[] panelComps = panel.getComponents();
                        for (Component panelComp : panelComps) {
                            if (panelComp instanceof JButton) {
                                styleDialogButton((JButton) panelComp);
                            }
                        }
                    }
                }
                return dialog;
            }
        };chooser.setFileSelectionMode(selectionMode);
        chooser.setDialogTitle(title);
        chooser.setBackground(BACKGROUND_COLOR);
        chooser.setForeground(TEXT_PRIMARY);return chooser;
    }// Style dialog buttons with custom colors
    private static void styleDialogButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(DIALOG_BUTTON_BG);
        button.setForeground(DIALOG_BUTTON_FG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIALOG_BUTTON_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(DIALOG_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(DIALOG_BUTTON_BG);
            }
        });
    }// Build tree model recursively from selected root directory and set it to tree
    private static void loadDirectoryTree(File rootFile) {
        DefaultMutableTreeNode rootNode = createFileTreeNode(rootFile);
        treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        tree.expandRow(0);
        tree.setAllChecked(true); // default: all checkboxes selected
    }// Recursively create tree nodes; userObject = File for each node
    private static DefaultMutableTreeNode createFileTreeNode(File file) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        File[] children = file.listFiles();
        if (children != null) {
            java.util.Arrays.sort(children, (a, b) -> {
// Directories first, then files
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File c : children) {
                node.add(createFileTreeNode(c));
            }
        }
        return node;
    }// Handler for Write to File button: gather selected files, prompt save path, write combined file
    private static void onWriteToFile() {
        List<File> selectedFiles = tree.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            showStyledMessageDialog("No files selected to write.",
                    "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }JFileChooser saver = createStyledFileChooser("Save output combined file as...", JFileChooser.FILES_ONLY);
        saver.setSelectedFile(new File("Output Combined File.txt"));// set default directory to same folder as selectedRootDir (if available)
        if (selectedRootDir != null && selectedRootDir.exists()) {
            saver.setCurrentDirectory(selectedRootDir);
        } else {
            saver.setCurrentDirectory(fsv.getHomeDirectory());
        }int res = saver.showSaveDialog(frame);
        if (res != JFileChooser.APPROVE_OPTION) return;File out = saver.getSelectedFile();if (out.exists()) {
            int option = showStyledConfirmDialog(
                    "The file '" + out.getName() + "' already exists. Do you want to replace it?",
                    "File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }appendStatus("● Saving combined file to: " + out.getAbsolutePath());
        writeButton.setEnabled(false);// Background writing using SwingWorker
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                int fileCount = 0;
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out))) {
                    for (File f : selectedFiles) {
                        publish("● Writing: " + f.getName());
// Header format: /** FileName /
                        String header = "/ File: " + f.getName() + " **/\n\n";
                        bos.write(header.getBytes(StandardCharsets.UTF_8));if (f.isFile() && f.canRead()) {
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = bis.read(buffer)) != -1) {
                                    bos.write(buffer, 0, len);
                                }
                                bos.write("\n\n".getBytes(StandardCharsets.UTF_8)); // newline after file
                                fileCount++;
                            } catch (IOException ex) {
                                publish("⚠ Failed to read: " + f.getName() + " -> " + ex.getMessage());
                            }
                        } else {
                            publish("⏭ Skipping (not a readable file): " + f.getName());
                        }
                    }
                    bos.flush();
                    publish("✓ Successfully wrote " + fileCount + " files to: " + out.getAbsolutePath());
                } catch (IOException ex) {
                    publish("✗ Error writing output: " + ex.getMessage());
                    SwingUtilities.invokeLater(() ->
                            showStyledMessageDialog("Failed to write file:\n" + ex.getMessage(),
                                    "Write Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }@Override
            protected void process(List<String> chunks) {
                for (String s : chunks) appendStatus(s);
            }@Override
            protected void done() {
                writeButton.setEnabled(true);
                appendStatus("✓ Finished saving.");
            }
        }.execute();
    }// Show styled message dialog
    private static void showStyledMessageDialog(String message, String title, int messageType) {
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);JOptionPane pane = new JOptionPane(message, messageType);
        JDialog dialog = pane.createDialog(frame, title);
        dialog.setBackground(BACKGROUND_COLOR);// Style buttons in the dialog
        for (Component comp : pane.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component panelComp : panel.getComponents()) {
                    if (panelComp instanceof JButton) {
                        styleDialogButton((JButton) panelComp);
                    }
                }
            }
        }dialog.setVisible(true);
    }// Show styled confirm dialog
    private static int showStyledConfirmDialog(String message, String title, int optionType, int messageType) {
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);JOptionPane pane = new JOptionPane(message, messageType, optionType);
        JDialog dialog = pane.createDialog(frame, title);
        dialog.setBackground(BACKGROUND_COLOR);// Style buttons in the dialog
        for (Component comp : pane.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component panelComp : panel.getComponents()) {
                    if (panelComp instanceof JButton) {
                        styleDialogButton((JButton) panelComp);
                    }
                }
            }
        }// Align buttons to the right for file exists dialog
        Component[] comps = pane.getComponents();
        if (comps.length > 1) {
            Component lastComp = comps[comps.length - 1];
            if (lastComp instanceof JPanel) {
                JPanel buttonPanel = (JPanel) lastComp;
                buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
                dialog.pack(); // Adjust dialog size after layout change
            }
        }dialog.setVisible(true);Object value = pane.getValue();
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return JOptionPane.CLOSED_OPTION;
    }// Append status text to right panel log
    private static void appendStatus(String text) {
        if (text.startsWith("✓")) {
            statusArea.setForeground(SUCCESS_COLOR);
        } else if (text.startsWith("⚠") || text.startsWith("✗")) {
            statusArea.setForeground(ERROR_COLOR);
        } else if (text.startsWith("●")) {
            statusArea.setForeground(PRIMARY_COLOR);
        } else {
            statusArea.setForeground(TEXT_PRIMARY);
        }statusArea.append(text + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());// Reset to default color
        statusArea.setForeground(TEXT_PRIMARY);
    }// Center the frame on screen
    private static void centerFrame(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = Math.max(0, (screen.width - w.getWidth()) / 2);
        int y = Math.max(0, (screen.height - w.getHeight()) / 2);
        w.setLocation(x, y);
    }
}