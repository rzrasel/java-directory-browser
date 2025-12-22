// File: DirectoryBrowser.java

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * DirectoryBrowser - main GUI class showing a checkbox tree and handling combine/write operation.
 * Save dialog defaults to the selected folder location.
 */
public class DirectoryBrowser {
    private static JFrame frame;                         // main application window
    private static JTextField pathField;                 // shows selected folder path (left)
    private static JButton selectButton;                 // "Select Folder" button (right)
    private static JCheckBoxTree tree;                   // checkbox-enabled tree
    private static DefaultTreeModel treeModel;           // tree model
    private static JButton writeButton;                  // "Write to File" button (right panel)
    private static JTextArea statusArea;                 // status log area (right panel)
    private static JButton refreshButton;                // Refresh button
    private static JCheckBox fileNameOnlyCheckBox;       // Checkbox for file name only/full path
    private static final FileSystemView fsv = FileSystemView.getFileSystemView(); // system icons/names

    // keep track of the currently selected root folder to use as default save location
    private static File selectedRootDir = null;

    // Colors
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);
    private static final Color ACCENT_DARK = new Color(216, 67, 21);
    private static final Color PRIMARY_COLOR = new Color(18, 19, 20);
    private static final Color BUTTON_PRIMARY_BG = PRIMARY_COLOR;
    private static final Color BUTTON_PRIMARY_FG = Color.WHITE;
    private static final Color BUTTON_PRIMARY_HOVER = new Color(36, 35, 37);
    private static final Color BUTTON_DISABLED_FG = new Color(117, 117, 117);
    private static final Color BUTTON_DISABLED_BG = new Color(189, 189, 189);
    private static final Color BUTTON_ACCENT_FG = Color.WHITE;
    private static final Color BUTTON_ACCENT_BG = ACCENT_COLOR;
    private static final Color BUTTON_ACCENT_HOVER = ACCENT_DARK;

    public static void launch() {
        SwingUtilities.invokeLater(DirectoryBrowser::createAndShowGUI);
    }

    // Build and show GUI
    private static void createAndShowGUI() {
        try {
            // Set Material Design inspired theme
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Modern Material Design 3 color scheme
            Color primaryColor = new Color(25, 118, 210);      // Blue 500
            Color primaryDarkColor = new Color(13, 71, 161);   // Blue 700
            Color primaryLightColor = new Color(187, 222, 251); // Blue 200
            Color accentColor = new Color(255, 87, 34);        // Deep Orange 500
            Color backgroundColor = new Color(250, 250, 250);  // Grey 50
            Color surfaceColor = Color.WHITE;
            Color textPrimary = new Color(33, 33, 33);         // Grey 900
            Color textSecondary = new Color(117, 117, 117);    // Grey 600
            Color dividerColor = new Color(224, 224, 224);     // Grey 300

            // Customize UI defaults for Modern Material Design
            UIManager.put("Panel.background", backgroundColor);
            UIManager.put("TextField.background", surfaceColor);
            UIManager.put("TextArea.background", surfaceColor);
            UIManager.put("Button.background", primaryColor);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.select", primaryDarkColor);
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("Button.border", BorderFactory.createEmptyBorder(12, 24, 12, 24));
            UIManager.put("ToggleButton.background", surfaceColor);
            UIManager.put("ToggleButton.foreground", textPrimary);
            UIManager.put("Label.foreground", textPrimary);
            UIManager.put("TextField.foreground", textPrimary);
            UIManager.put("TextArea.foreground", textPrimary);
            UIManager.put("ScrollPane.background", backgroundColor);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            UIManager.put("Viewport.background", backgroundColor);
            UIManager.put("Tree.background", surfaceColor);
            UIManager.put("Tree.foreground", textPrimary);

            // Font settings with Modern Material Design typography
            Font regularFont = new Font("Segoe UI", Font.PLAIN, 14);
            Font mediumFont = new Font("Segoe UI Semibold", Font.PLAIN, 14);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
            Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
            Font monoFont = new Font("Consolas", Font.PLAIN, 13);

            UIManager.put("Button.font", mediumFont);
            UIManager.put("Label.font", regularFont);
            UIManager.put("TextField.font", regularFont);
            UIManager.put("TextArea.font", monoFont);
            UIManager.put("Tree.font", regularFont);
            UIManager.put("OptionPane.font", regularFont);

        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Directory Browser - Combine Selected Files");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(250, 250, 250));
        frame.setLayout(new BorderLayout(0, 0));

        // Top panel with modern Material Design styling
        JPanel topPanel = new JPanel(new BorderLayout(16, 16));
        topPanel.setBackground(new Color(25, 118, 210));
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel titleLabel = new JLabel("Directory Browser");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createFolderIcon());
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Path field and select button container
        JPanel pathPanel = new JPanel(new BorderLayout(8, 0));
        pathPanel.setBackground(new Color(25, 118, 210));
        pathField = new JTextField();
        pathField.setEditable(false);
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pathField.setBackground(Color.WHITE);
        pathField.setForeground(new Color(33, 33, 33));
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        selectButton = createMaterialButton("Select Folder", BUTTON_PRIMARY_BG, BUTTON_PRIMARY_FG, BUTTON_PRIMARY_HOVER);
        selectButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        selectButton.setPreferredSize(new Dimension(150, 42));
        selectButton.addActionListener(DirectoryBrowser::onSelectFolder);
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(selectButton, BorderLayout.EAST);
        topPanel.add(pathPanel, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);

        // Initial tree placeholder
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("No folder selected");
        treeModel = new DefaultTreeModel(root);
        tree = new JCheckBoxTree(root);
        tree.setRowHeight(36);
        tree.setBackground(Color.WHITE);

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createEmptyBorder());
        treeScroll.getViewport().setBackground(Color.WHITE);
        treeScroll.setBackground(Color.WHITE);

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout(16, 16));
        rightPanel.setBackground(new Color(250, 250, 250));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 24));

        // Control panel for refresh button and checkbox
        JPanel controlPanel = new JPanel(new BorderLayout(12, 0));
        controlPanel.setBackground(new Color(250, 250, 250));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Left side: Checkbox for file name/full path
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkboxPanel.setBackground(new Color(250, 250, 250));
        //fileNameOnlyCheckBox = createMaterialCheckBox("File name only");
        fileNameOnlyCheckBox = createMaterialCheckBox("Use file names only (not full paths)");
        fileNameOnlyCheckBox.setSelected(true);
        fileNameOnlyCheckBox.addActionListener(e -> appendStatus("✓ Output format: " +
                (fileNameOnlyCheckBox.isSelected() ? "File name only" : "Full path")));
        checkboxPanel.add(fileNameOnlyCheckBox);

        // Right side: Refresh button
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshPanel.setBackground(new Color(250, 250, 250));
        refreshButton = createMaterialButton("Refresh", new Color(66, 66, 66), Color.WHITE, new Color(97, 97, 97));
        refreshButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        refreshButton.setPreferredSize(new Dimension(120, 36));
        refreshButton.setEnabled(false);
        refreshButton.addActionListener(e -> onRefresh());
        refreshPanel.add(refreshButton);

        controlPanel.add(checkboxPanel, BorderLayout.WEST);
        controlPanel.add(refreshPanel, BorderLayout.EAST);

        // Write button
        writeButton = createMaterialButton("Write to File", BUTTON_ACCENT_BG, BUTTON_ACCENT_FG, BUTTON_ACCENT_HOVER);
        writeButton.setEnabled(false);
        writeButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        writeButton.setPreferredSize(new Dimension(250, 48));
        writeButton.addActionListener(e -> onWriteToFile());

        // Status area with Material Design card
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel statusLabel = new JLabel("Status Log");
        statusLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(25, 118, 210));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setRows(12);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusArea.setBackground(Color.WHITE);
        statusArea.setForeground(new Color(66, 66, 66));
        statusArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Statistics panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statsPanel.setBackground(Color.WHITE);
        JLabel filesLabel = new JLabel("Files selected: 0");
        filesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filesLabel.setForeground(new Color(117, 117, 117));
        statsPanel.add(filesLabel);

        statusPanel.add(statsPanel, BorderLayout.SOUTH);

        // Container for write button to center it
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setBackground(new Color(250, 250, 250));
        buttonContainer.add(writeButton);

        // Add components to right panel
        rightPanel.add(controlPanel, BorderLayout.NORTH);
        rightPanel.add(buttonContainer, BorderLayout.CENTER);
        rightPanel.add(statusPanel, BorderLayout.SOUTH);

        // Split pane: tree (left) and rightPanel (right)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, rightPanel);
        split.setResizeWeight(1.0);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(new Color(250, 250, 250));
        frame.add(split, BorderLayout.CENTER);

        frame.setSize(1000, 650);
        centerFrame(frame);
        frame.setVisible(true);
    }

    // Create folder icon for title
    private static Icon createFolderIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 4, y + 8, 20, 16, 4, 4);
                g2.fillRect(x + 2, y + 10, 24, 14);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 32; }

            @Override
            public int getIconHeight() { return 32; }
        };
    }

    // Handler for Select Folder button
    private static void onSelectFolder(ActionEvent e) {
        JFileChooser chooser = createMaterialFileChooser("Select Folder to Browse");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File currentDir;
        if (!pathField.getText().trim().isEmpty()) {
            currentDir = new File(pathField.getText().trim());
        } else {
            currentDir = new File(System.getProperty("user.home"), "Desktop");
        }
        chooser.setCurrentDirectory(currentDir);

        int res = chooser.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            selectedRootDir = selected;               // remember selection for save default
            pathField.setText(selected.getAbsolutePath());
            loadDirectoryTree(selected);
            writeButton.setEnabled(true);
            refreshButton.setEnabled(true);
            appendStatus("✓ Folder loaded: " + selected.getAbsolutePath());
            appendStatus("✓ Output format: File name only (default)");
        }
    }

    // Handler for Refresh button
    private static void onRefresh() {
        if (selectedRootDir != null && selectedRootDir.exists()) {
            loadDirectoryTree(selectedRootDir);
            appendStatus("↻ Directory refreshed: " + selectedRootDir.getAbsolutePath());
        } else {
            showMaterialDialog("Refresh Error",
                    "No valid directory selected. Please select a folder first.",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Build tree model recursively from selected root directory and set it to tree
    private static void loadDirectoryTree(File rootFile) {
        Set<String> processing = new HashSet<>();
        DefaultMutableTreeNode rootNode = createFileTreeNode(rootFile, processing);
        treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        tree.expandRow(0);
        tree.setAllChecked(true); // default: all checkboxes selected
    }

    // Recursively create tree nodes; userObject = File for each node
    private static DefaultMutableTreeNode createFileTreeNode(File file, Set<String> processing) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        if (!file.isDirectory()) {
            return node;
        }
        String canonical;
        try {
            canonical = file.getCanonicalPath();
        } catch (IOException e) {
            appendStatus("⚠️ Warning: Could not resolve canonical path for " + file.getAbsolutePath());
            canonical = file.getAbsolutePath();
        }
        if (processing.contains(canonical)) {
            node.add(new DefaultMutableTreeNode("... (cyclic reference skipped)"));
            return node;
        }
        processing.add(canonical);
        File[] children = null;
        try {
            children = file.listFiles();
        } catch (SecurityException e) {
            appendStatus("⚠️ Access denied to directory: " + file.getAbsolutePath());
        }
        if (children != null) {
            Arrays.sort(children, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File c : children) {
                node.add(createFileTreeNode(c, processing));
            }
        }
        processing.remove(canonical);
        return node;
    }

    // Handler for Write to File button: gather selected files, prompt save path, write combined file
    private static void onWriteToFile() {
        List<File> selectedFiles = tree.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            showMaterialDialog("No Files Selected",
                    "Please select at least one file to combine.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser saver = createMaterialFileChooser("Save Combined File As...");
        saver.setSelectedFile(new File("Output Combined File.txt"));
        // set default directory to same folder as selectedRootDir (if available)
        if (selectedRootDir != null && selectedRootDir.exists()) {
            saver.setCurrentDirectory(selectedRootDir);
        } else {
            saver.setCurrentDirectory(fsv.getHomeDirectory());
        }

        int res = saver.showSaveDialog(frame);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File out = saver.getSelectedFile();

        if (out.exists()) {
            // Create custom option pane with button on the right
            JPanel panel = new JPanel(new BorderLayout(16, 16));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

            // Create warning icon using graphics instead of loading from file
            JLabel iconLabel = new JLabel(createWarningIcon(48, 48));

            JPanel textPanel = new JPanel(new BorderLayout(8, 8));
            textPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel("File Exists");
            titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            titleLabel.setForeground(new Color(33, 33, 33));

            JLabel messageLabel = new JLabel("<html>The file '" + out.getName() + "' already exists. Do you want to replace it?</html>");
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            messageLabel.setForeground(new Color(117, 117, 117));

            textPanel.add(titleLabel, BorderLayout.NORTH);
            textPanel.add(messageLabel, BorderLayout.CENTER);

            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);

            // Custom buttons on the right
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttonPanel.setBackground(Color.WHITE);

            JButton noButton = new JButton("Cancel");
            noButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            noButton.setForeground(new Color(117, 117, 117));
            noButton.setBackground(Color.WHITE);
            noButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            JButton yesButton = new JButton("Replace");
            yesButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            yesButton.setForeground(Color.WHITE);
            yesButton.setBackground(new Color(255, 87, 34)); // Deep Orange 500
            yesButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 87, 34), 1),
                    BorderFactory.createEmptyBorder(8, 24, 8, 24)
            ));

            final int[] result = {JOptionPane.NO_OPTION};

            noButton.addActionListener(ev -> {
                result[0] = JOptionPane.NO_OPTION;
                ((Window) SwingUtilities.getWindowAncestor(panel)).dispose();
            });

            yesButton.addActionListener(ev -> {
                result[0] = JOptionPane.YES_OPTION;
                ((Window) SwingUtilities.getWindowAncestor(panel)).dispose();
            });

            buttonPanel.add(noButton);
            buttonPanel.add(yesButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

            JDialog dialog = optionPane.createDialog(frame, "File Exists");
            dialog.setVisible(true);
            dialog.dispose();

            if (result[0] != JOptionPane.YES_OPTION) {
                return;
            }
        }

        appendStatus("📁 Saving combined file to: " + out.getAbsolutePath());
        writeButton.setEnabled(false);
        refreshButton.setEnabled(false);
        writeButton.setText("Combining Files...");

        // Background writing using SwingWorker
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out))) {
                    for (File f : selectedFiles) {
                        publish("📄 Writing: " + f.getName());

                        // Header format based on checkbox selection
                        String fileName = fileNameOnlyCheckBox.isSelected() ?
                                f.getName() : f.getAbsolutePath();
                        String header = "/ File: " + fileName + " **/\n\n";
                        bos.write(header.getBytes(StandardCharsets.UTF_8));

                        if (f.isFile() && f.canRead()) {
                            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = bis.read(buffer)) != -1) {
                                    bos.write(buffer, 0, len);
                                }
                                bos.write("\n\n".getBytes(StandardCharsets.UTF_8)); // newline after file
                            } catch (IOException ex) {
                                publish("❌ Failed to read: " + f.getName() + " -> " + ex.getMessage());
                            }
                        } else {
                            publish("⚠️ Skipping (not a readable file): " + f.getName());
                        }
                    }
                    bos.flush();
                    publish("✅ Write complete: " + out.getAbsolutePath());
                } catch (IOException ex) {
                    publish("❌ Error writing output: " + ex.getMessage());
                    SwingUtilities.invokeLater(() ->
                            showMaterialDialog("Write Error",
                                    "Failed to write file:\n" + ex.getMessage(),
                                    JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String s : chunks) appendStatus(s);
            }

            @Override
            protected void done() {
                writeButton.setEnabled(true);
                refreshButton.setEnabled(true);
                writeButton.setText("Write to File");
                appendStatus("✨ Finished processing.");
            }
        }.execute();
    }

    // Create material design button with hover effects
    private static JButton createMaterialButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button shadow
                if (isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(1, 2, getWidth() - 2, getHeight() - 2, 8, 8);
                }

                // Button background
                if (!isEnabled()) {
                    g2.setColor(BUTTON_DISABLED_BG);
                } else if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(bgColor);
                }

                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Button border
                if (isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 10));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set disabled colors
        button.setDisabledIcon(null);
        button.addChangeListener(e -> {
            if (!button.isEnabled()) {
                button.setForeground(BUTTON_DISABLED_FG);
            } else {
                button.setForeground(fgColor);
            }
        });

        return button;
    }

    // Create material design checkbox
    private static JCheckBox createMaterialCheckBox(String text) {
        /*JCheckBox checkBox = new JCheckBox(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());

                int boxSize = 18;
                int y = (getHeight() - boxSize) / 2;

                // Draw checkbox border
                if (isEnabled()) {
                    g2.setColor(isSelected() ? new Color(25, 118, 210) : new Color(158, 158, 158));
                } else {
                    g2.setColor(new Color(189, 189, 189));
                }
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, y, boxSize, boxSize, 4, 4);

                // Draw checkmark if selected
                if (isSelected() && isEnabled()) {
                    g2.setColor(new Color(25, 118, 210));
                    g2.fillRoundRect(2, y, boxSize, boxSize, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(6, y + 9, 9, y + 12);
                    g2.drawLine(9, y + 12, 14, y + 5);
                } else if (isSelected() && !isEnabled()) {
                    g2.setColor(new Color(189, 189, 189));
                    g2.fillRoundRect(2, y, boxSize, boxSize, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(6, y + 9, 9, y + 12);
                    g2.drawLine(9, y + 12, 14, y + 5);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkBox.setForeground(new Color(33, 33, 33));
        checkBox.setBackground(new Color(250, 250, 250));
        checkBox.setFocusPainted(false);
        checkBox.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        checkBox.setContentAreaFilled(false);

        // Add hover effect
        checkBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (checkBox.isEnabled()) {
                    checkBox.setBackground(new Color(245, 245, 245));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                checkBox.setBackground(new Color(250, 250, 250));
            }
        });

        return checkBox;*/
        JCheckBox fileNameOnlyCheck = new JCheckBox(text);
        fileNameOnlyCheck.setSelected(true);
        fileNameOnlyCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileNameOnlyCheck.setForeground(new Color(117, 117, 117));
        fileNameOnlyCheck.setBackground(Color.WHITE);
        fileNameOnlyCheck.setOpaque(false);
        fileNameOnlyCheck.setBorder(BorderFactory.createEmptyBorder());
        fileNameOnlyCheck.setIcon(createUncheckedIcon());
        fileNameOnlyCheck.setSelectedIcon(createCheckedIcon());
        return fileNameOnlyCheck;
    }

    // Icons for checkbox
    private static Icon createUncheckedIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(158, 158, 158)); // Grey 500
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + 1, y + 1, 16, 16, 4, 4);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return 20; }
            @Override
            public int getIconHeight() { return 20; }
        };
    }
    private static Icon createCheckedIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw background
                g2.setColor(new Color(25, 118, 210)); // Blue 500
                g2.fillRoundRect(x + 1, y + 1, 16, 16, 4, 4);
                // Draw checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 9, x + 8, y + 12);
                g2.drawLine(x + 8, y + 12, x + 13, y + 5);
                g2.dispose();
            }
            @Override
            public int getIconWidth() { return 20; }
            @Override
            public int getIconHeight() { return 20; }
        };
    }

    // Create warning icon using graphics
    private static Icon createWarningIcon(int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw warning triangle
                int[] xPoints = {x + width / 2, x + width - 4, x + 4};
                int[] yPoints = {y + 4, y + height - 8, y + height - 8};
                g2.setColor(new Color(255, 193, 7)); // Amber 500
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.setColor(new Color(255, 160, 0)); // Amber 700
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawPolygon(xPoints, yPoints, 3);

                // Draw exclamation mark
                g2.setColor(new Color(33, 33, 33));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                String exclamation = "!";
                int textWidth = fm.stringWidth(exclamation);
                int textHeight = fm.getAscent();
                g2.drawString(exclamation, x + (width - textWidth) / 2, y + (height + textHeight) / 2 - 8);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }

    // Create Modern Material Design styled file chooser
    private static JFileChooser createMaterialFileChooser(String title) {
        JFileChooser chooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setBackground(Color.WHITE);

                // Apply Modern Material Design styling to all components
                SwingUtilities.invokeLater(() -> {
                    applyMaterialStyling(dialog);
                });

                return dialog;
            }
        };

        chooser.setDialogTitle(title);
        chooser.setBackground(Color.WHITE);
        chooser.setForeground(new Color(33, 33, 33));

        return chooser;
    }

    // Apply Modern Material Design styling to dialog components
    private static void applyMaterialStyling(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK); // Black text color for buttons
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // Add hover effect
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        button.setBackground(new Color(245, 245, 245));
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        button.setBackground(Color.WHITE);
                    }
                });
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setForeground(new Color(33, 33, 33));
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                textField.setBackground(Color.WHITE);
                textField.setForeground(new Color(33, 33, 33));
            } else if (comp instanceof JComboBox) {
                JComboBox comboBox = (JComboBox) comp;
                comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                comboBox.setBackground(Color.WHITE);
                comboBox.setForeground(new Color(33, 33, 33));
            } else if (comp instanceof JList) {
                JList list = (JList) comp;
                list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                list.setBackground(Color.WHITE);
                list.setForeground(new Color(33, 33, 33));
            } else if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                table.setBackground(Color.WHITE);
                table.setForeground(new Color(33, 33, 33));
                table.setGridColor(new Color(224, 224, 224));
            } else if (comp instanceof Container) {
                applyMaterialStyling((Container) comp);
            }
        }
    }

    // Modern Material Design styled dialog
    private static void showMaterialDialog(String title, String message, int messageType) {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Create icon based on message type
        Icon icon = createMaterialIcon(48, messageType);
        JLabel iconLabel = new JLabel(icon);

        JPanel textPanel = new JPanel(new BorderLayout(8, 8));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 33, 33));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setForeground(new Color(117, 117, 117));
        messageArea.setBackground(Color.WHITE);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder());

        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(messageArea, BorderLayout.CENTER);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(panel, messageType, JOptionPane.DEFAULT_OPTION);
        JDialog dialog = optionPane.createDialog(frame, title);
        dialog.setVisible(true);
    }

    // Create modern material icon for dialogs
    private static Icon createMaterialIcon(int size, int messageType) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color iconColor;
                String symbol;

                if (messageType == JOptionPane.ERROR_MESSAGE) {
                    iconColor = new Color(244, 67, 54); // Red 500
                    symbol = "!";
                } else if (messageType == JOptionPane.WARNING_MESSAGE) {
                    iconColor = new Color(255, 193, 7); // Amber 500
                    symbol = "⚠";
                } else if (messageType == JOptionPane.INFORMATION_MESSAGE) {
                    iconColor = new Color(25, 118, 210); // Blue 500
                    symbol = "i";
                } else {
                    iconColor = new Color(76, 175, 80); // Green 500
                    symbol = "✓";
                }

                // Draw circle background
                g2.setColor(iconColor);
                g2.fillOval(x, y, size, size);

                // Draw symbol
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(symbol);
                int textHeight = fm.getAscent();
                g2.drawString(symbol, x + (size - textWidth) / 2, y + (size + textHeight) / 2 - 4);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    // Append status text to right panel log
    private static void appendStatus(String text) {
        statusArea.append(text + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    // Center the frame on screen
    private static void centerFrame(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = Math.max(0, (screen.width - w.getWidth()) / 2);
        int y = Math.max(0, (screen.height - w.getHeight()) / 2);
        w.setLocation(x, y);
    }
}