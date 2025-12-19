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
    private static File selectedRootDir = null;// Launch UI (called by Main)
    public static void launch() {
        SwingUtilities.invokeLater(DirectoryBrowser::createAndShowGUI);
    }// Build and show GUI
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
// Material Design inspired colors and styles for Nimbus
            Color primary = new Color(0x2196F3); // Blue
            Color primaryVariant = new Color(0x1976D2);
            Color surface = new Color(0xFFFFFF);
            Color background = new Color(0xFAFAFA);
            Color onSurface = new Color(0x000000);
            Color onPrimary = new Color(0xFFFFFF);UIManager.put("swing.plaf.metal.controlFont", new Font("SansSerif", Font.PLAIN, 16));
            UIManager.put("swing.plaf.metal.labelFont", new Font("SansSerif", Font.PLAIN, 16));
            UIManager.put("swing.plaf.metal.buttonFont", new Font("SansSerif", Font.PLAIN, 16));
            UIManager.put("swing.plaf.metal.textFieldFont", new Font("SansSerif", Font.PLAIN, 16));
            UIManager.put("swing.plaf.metal.textAreaFont", new Font("Monospaced", Font.PLAIN, 14));// Colors
            UIManager.put("nimbusBase", primary);
            UIManager.put("nimbusBlueGrey", background);
            UIManager.put("nimbusFocus", primaryVariant);
            UIManager.put("nimbusLightBackground", surface);
            UIManager.put("nimbusSelectionBackground", primary);
            UIManager.put("nimbusSelectedText", onPrimary);
            UIManager.put("control", surface);
            UIManager.put("controlBackground", background);
            UIManager.put("Panel.background", background);
            UIManager.put("Tree.background", surface);
            UIManager.put("Tree.foreground", onSurface);
            UIManager.put("TextArea.background", surface);
            UIManager.put("TextArea.foreground", onSurface);
            UIManager.put("TextArea.inactiveBackground", surface);
            UIManager.put("TextArea.inactiveForeground", onSurface);
            UIManager.put("Button.background", primary);
            UIManager.put("Button.foreground", onPrimary);
            UIManager.put("Button.disabledForeground", new Color(0x9E9E9E));
            UIManager.put("Button.border", BorderFactory.createEmptyBorder());
            UIManager.put("Button.focus", new Color(0x00000000));
            UIManager.put("TextField.background", surface);
            UIManager.put("TextField.foreground", onSurface);
            UIManager.put("TextField.inactiveBackground", new Color(0xF5F5F5));
            UIManager.put("TextField.inactiveForeground", onSurface);
            UIManager.put("CheckBox.background", surface);
            UIManager.put("CheckBox.foreground", onSurface);
            UIManager.put("ScrollPane.background", surface);
            UIManager.put("SplitPane.background", background);
            UIManager.put("SplitPane.dividerForeground", new Color(0xE0E0E0));// Rounded corners simulation via border
            UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(primaryVariant, 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE0E0E0), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));} catch (Exception e) {
            e.printStackTrace();
        }frame = new JFrame("Directory Browser - Combine Selected Files");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(0xFAFAFA));
        frame.setLayout(new BorderLayout(12, 12));// Top panel: pathField (left) and selectButton (right)
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        pathField = new JTextField();
        pathField.setEditable(false);
        pathField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        selectButton = new JButton("Select Folder");
        selectButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        selectButton.setBackground(new Color(0x2196F3));
        selectButton.setForeground(Color.WHITE);
        selectButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        selectButton.addActionListener(DirectoryBrowser::onSelectFolder);topPanel.add(pathField, BorderLayout.CENTER);   // left: text field (expands)
        topPanel.add(selectButton, BorderLayout.EAST);  // right: button
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        frame.add(topPanel, BorderLayout.NORTH);// Initial tree placeholder
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("No folder selected");
        treeModel = new DefaultTreeModel(root);
        tree = new JCheckBoxTree(root);
        tree.setRowHeight(48);
        tree.setBackground(Color.WHITE);
        tree.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));// Right panel: Write button at top and status area below
        JPanel rightPanel = new JPanel(new BorderLayout(12, 12));
        rightPanel.setBackground(Color.WHITE);
        writeButton = new JButton("Write to File");
        writeButton.setEnabled(false);
        writeButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
        writeButton.setBackground(new Color(0x2196F3));
        writeButton.setForeground(Color.WHITE);
        writeButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        writeButton.addActionListener(e -> onWriteToFile());statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setRows(12);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        statusArea.setBackground(Color.WHITE);
        statusArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));rightPanel.add(writeButton, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));// Split pane: tree (left) and rightPanel (right)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, rightPanel);
        split.setResizeWeight(0.75);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(new Color(0xFAFAFA));
        frame.add(split, BorderLayout.CENTER);frame.setSize(1000, 700);
        centerFrame(frame);
        frame.setVisible(true);
    }// Handler for Select Folder button
    private static void onSelectFolder(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Folder to Browse");
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));// Temporarily set button colors for Open/Cancel to black text on white
        Color origFg = UIManager.getColor("Button.foreground");
        Color origBg = UIManager.getColor("Button.background");
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(0xE0E0E0)));int res = chooser.showOpenDialog(frame);// Restore
        UIManager.put("Button.foreground", origFg);
        UIManager.put("Button.background", origBg);
        UIManager.put("Button.border", null);if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            selectedRootDir = selected;
            pathField.setText(selected.getAbsolutePath());
            loadDirectoryTree(selected);
            writeButton.setEnabled(true);
            appendStatus("Loaded: " + selected.getAbsolutePath());
        }
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
            java.util.Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (File c : children) {
                node.add(createFileTreeNode(c));
            }
        }
        return node;
    }// Handler for Write to File button: gather selected files, prompt save path, write combined file
    private static void onWriteToFile() {
        List<File> selectedFiles = tree.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No files selected to write.", "Nothing Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }JFileChooser saver = new JFileChooser();
        saver.setDialogTitle("Save output combined file as...");
        saver.setSelectedFile(new File("Output Combined File.txt"));
        if (selectedRootDir != null && selectedRootDir.exists()) {
            saver.setCurrentDirectory(selectedRootDir);
        } else {
            saver.setCurrentDirectory(fsv.getHomeDirectory());
        }// Temporarily set button colors for Save/Cancel to black text on white
        Color origFg = UIManager.getColor("Button.foreground");
        Color origBg = UIManager.getColor("Button.background");
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(0xE0E0E0)));int res = saver.showSaveDialog(frame);// Restore
        UIManager.put("Button.foreground", origFg);
        UIManager.put("Button.background", origBg);
        UIManager.put("Button.border", null);if (res != JFileChooser.APPROVE_OPTION) return;File out = saver.getSelectedFile();if (out.exists()) {
// Custom confirm dialog for file exists with buttons on bottom, No on right
            Object[] options = {"Yes", "No"};
            Color origConfirmFg = UIManager.getColor("Button.foreground");
            Color origConfirmBg = UIManager.getColor("Button.background");
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(0xE0E0E0)));int option = JOptionPane.showOptionDialog(frame,
                    "The file '" + out.getName() + "' already exists. Do you want to replace it?",
                    "File Exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]); // Default to No// Restore
            UIManager.put("Button.foreground", origConfirmFg);
            UIManager.put("Button.background", origConfirmBg);
            UIManager.put("Button.border", null);if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }appendStatus("Saving combined file to: " + out.getAbsolutePath());
        writeButton.setEnabled(false);// Background writing using SwingWorker
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out))) {
                    for (File f : selectedFiles) {
                        publish("Writing: " + f.getAbsolutePath());
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
                            } catch (IOException ex) {
                                publish("Failed to read: " + f.getAbsolutePath() + " -> " + ex.getMessage());
                            }
                        } else {
                            publish("Skipping (not a readable file): " + f.getAbsolutePath());
                        }
                    }
                    bos.flush();
                    publish("Write complete: " + out.getAbsolutePath());
                } catch (IOException ex) {
                    publish("Error writing output: " + ex.getMessage());
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame, "Failed to write file:\n" + ex.getMessage(), "Write Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }@Override
            protected void process(List<String> chunks) {
                for (String s : chunks) appendStatus(s);
            }@Override
            protected void done() {
                writeButton.setEnabled(true);
                appendStatus("Finished saving.");
            }
        }.execute();
    }// Append status text to right panel log
    private static void appendStatus(String text) {
        statusArea.append(text + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }// Center the frame on screen
    private static void centerFrame(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = Math.max(0, (screen.width - w.getWidth()) / 2);
        int y = Math.max(0, (screen.height - w.getHeight()) / 2);
        w.setLocation(x, y);
    }
}