// File: JCheckBoxTree.java

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.io.File;
import java.util.*;
import java.util.List;
/**
 JCheckBoxTree - JTree with checkbox support; checked state stored in a Map.
 Clicking toggles node checkbox, cascades to children, updates parents.
 Provides helper getSelectedFiles() and setAllChecked(true).
 */
public class JCheckBoxTree extends JTree {
    private final Map<DefaultMutableTreeNode, Boolean> checkedMap = new HashMap<>();// Icon size configuration
    private int iconSize = 48; // Default large size
    private int fontSize = 16; // Default font size// Refurbished Material Design Colors - Consistent with DirectoryBrowser
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);      // Indigo 600
    private static final Color PRIMARY_DARK = new Color(48, 63, 159);       // Indigo 800
    private static final Color ACCENT_COLOR = new Color(255, 87, 34);       // Deep Orange 500
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light Gray Blue
    private static final Color CARD_COLOR = new Color(255, 255, 255);       // White
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);        // Gray 900
    private static final Color TEXT_SECONDARY = new Color(97, 97, 97);      // Gray 700
    private static final Color TEXT_HINT = new Color(158, 158, 158);        // Gray 500
    private static final Color DIVIDER_COLOR = new Color(224, 224, 224);    // Gray 300
    private static final Color HOVER_COLOR = new Color(245, 245, 245);      // Gray 100
    private static final Color SELECTION_BG = new Color(232, 234, 246);     // Indigo 50
    private static final Color SELECTION_FG = new Color(63, 81, 181);       // Indigo 600
    private static final Color CHECKBOX_BG = new Color(245, 245, 245);      // Checkbox background
    private static final Color CHECKBOX_BORDER = new Color(189, 189, 189);  // Checkbox border// Constructor accepts a TreeNode root
    public JCheckBoxTree(TreeNode root) {
        super(root);
        setCellRenderer(new CheckBoxTreeCellRenderer());
        setBackground(CARD_COLOR);
        setForeground(TEXT_PRIMARY);
        setOpaque(true);// Enhanced tree styling
        putClientProperty("JTree.lineStyle", "None");
        setShowsRootHandles(true);
        setRootVisible(true);
        setToggleClickCount(1); // Single click to expand/collapse// Set larger row height for better touch/click target
        setRowHeight(64);addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                toggleNode(node);
                repaint();
            }@Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }@Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }// Set icon size (called from DirectoryBrowser when view mode changes)
    public void setIconSize(int size) {
        this.iconSize = size;
        repaint();
    }// Set font size (called from DirectoryBrowser when view mode changes)
    public void setFontSize(int size) {
        this.fontSize = size;
        repaint();
    }// Toggle a node and update children/parents
    private void toggleNode(DefaultMutableTreeNode node) {
        boolean newState = !isChecked(node);
        setChecked(node, newState);
        setChildrenChecked(node, newState);
        updateParentState(node);
    }// Recursively set children checked state
    private void setChildrenChecked(DefaultMutableTreeNode parent, boolean state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            setChecked(child, state);
            setChildrenChecked(child, state);
        }
    }// Update parent aggregate state (all->true, none->false, partial->false visually)
    private void updateParentState(DefaultMutableTreeNode node) {
        TreeNode p = node.getParent();
        if (!(p instanceof DefaultMutableTreeNode)) return;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) p;boolean all = true;
        boolean none = true;for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (isChecked(child)) none = false;
            else all = false;
        }if (all) setChecked(parent, true);
        else if (none) setChecked(parent, false);
        else setChecked(parent, false); // partial -> treat as unchecked (can be extended)
        updateParentState(parent);
    }// Set checked state in map
    private void setChecked(DefaultMutableTreeNode node, boolean state) {
        checkedMap.put(node, state);
    }// Get checked state (default false)
    public boolean isChecked(DefaultMutableTreeNode node) {
        return checkedMap.getOrDefault(node, false);
    }// Set all nodes checked/unchecked
    public void setAllChecked(boolean state) {
        Object rootObj = getModel().getRoot();
        if (rootObj instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) rootObj;
            setChecked(root, state);
            setChildrenChecked(root, state);
        }
        repaint();
    }// Collect and return selected files (only regular files)
    public List<File> getSelectedFiles() {
        List<File> out = new ArrayList<>();
        Object rootObj = getModel().getRoot();
        if (rootObj instanceof DefaultMutableTreeNode) {
            collectCheckedFiles((DefaultMutableTreeNode) rootObj, out);
        }
        return out;
    }// Recursive helper to collect files
    private void collectCheckedFiles(DefaultMutableTreeNode node, List<File> out) {
        boolean checked = isChecked(node);
        Object userObj = node.getUserObject();
        if (userObj instanceof File) {
            File f = (File) userObj;
            if (f.isFile() && checked) {
                out.add(f);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            collectCheckedFiles(child, out);
        }
    }// Enhanced Material Design checkbox with smooth transitions
    private class MaterialCheckBox extends JCheckBox {
        private static final int SIZE = 16;
        private float animation = 0f;public MaterialCheckBox() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(SIZE, SIZE));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }@Override
        public void setSelected(boolean selected) {
            if (isSelected() != selected) {
                animation = selected ? 0f : 1f;
            }
            super.setSelected(selected);
        }@Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);int x = (getWidth() - SIZE) / 2;
            int y = (getHeight() - SIZE) / 2;// Animate checkbox state
            if (isSelected() && animation < 1f) {
                animation += 0.2f;
                if (animation > 1f) animation = 1f;
                repaint();
            } else if (!isSelected() && animation > 0f) {
                animation -= 0.2f;
                if (animation < 0f) animation = 0f;
                repaint();
            }// Draw checkbox background with subtle shadow
            if (isEnabled()) {
// Shadow effect
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(x + 1, y + 1, SIZE, SIZE, 6, 6);// Background
                if (isSelected()) {
                    g2.setColor(PRIMARY_COLOR);
                } else {
                    g2.setColor(CHECKBOX_BG);
                }
                g2.fillRoundRect(x, y, SIZE, SIZE, 6, 6);// Border
                g2.setColor(isSelected() ? PRIMARY_DARK : CHECKBOX_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, SIZE, SIZE, 6, 6);// Draw checkmark with animation
                if (animation > 0) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));// Animated checkmark
                    float checkProgress = animation;// First segment of checkmark
                    int x1 = x + 5;
                    int y1 = y + 12;
                    int x2 = x + 10;
                    int y2 = y + 17;// Second segment of checkmark
                    int x3 = x + 18;
                    int y3 = y + 7;// Draw animated checkmark
                    if (checkProgress > 0.5f) {
// Draw first segment fully
                        g2.drawLine(x1, y1, x2, y2);// Draw second segment partially
                        float secondProgress = (checkProgress - 0.5f) * 2;
                        int x3Partial = (int) (x2 + (x3 - x2) * secondProgress);
                        int y3Partial = (int) (y2 + (y3 - y2) * secondProgress);
                        g2.drawLine(x2, y2, x3Partial, y3Partial);
                    } else {
// Draw first segment partially
                        float firstProgress = checkProgress * 2;
                        int x2Partial = (int) (x1 + (x2 - x1) * firstProgress);
                        int y2Partial = (int) (y1 + (y2 - y1) * firstProgress);
                        g2.drawLine(x1, y1, x2Partial, y2Partial);
                    }
                }
            } else {
// Disabled state
                g2.setColor(CHECKBOX_BG);
                g2.fillRoundRect(x, y, SIZE, SIZE, 6, 6);
                g2.setColor(CHECKBOX_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, SIZE, SIZE, 6, 6);
            }g2.dispose();
        }
    }// Custom renderer draws a checkbox + icon + name with refined Material Design
    private class CheckBoxTreeCellRenderer implements javax.swing.tree.TreeCellRenderer {
        private final JPanel panel = new JPanel(new BorderLayout(16, 0));
        private final MaterialCheckBox check = new MaterialCheckBox();
        private final JLabel label = new JLabel();
        private final JLabel iconLabel = new JLabel();
        private boolean isHovered = false;CheckBoxTreeCellRenderer() {
            panel.setLayout(new BorderLayout(16, 0));
            panel.setOpaque(true);
            panel.setBackground(CARD_COLOR);JPanel leftPanel = new JPanel(new BorderLayout(12, 0));
            leftPanel.setOpaque(false);
            leftPanel.add(check, BorderLayout.WEST);
            leftPanel.add(iconLabel, BorderLayout.CENTER);panel.add(leftPanel, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);// Set font size (will be updated when view mode changes)
            label.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
            label.setForeground(TEXT_PRIMARY);// Add hover effect
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    isHovered = true;
                    panel.setBackground(HOVER_COLOR);
                    panel.repaint();
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    isHovered = false;
                    panel.setBackground(CARD_COLOR);
                    panel.repaint();
                }
            });// Add padding and subtle border
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    BorderFactory.createEmptyBorder(12, 16, 12, 24)
            ));
        }@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();// Update font size based on current view mode
            label.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));if (userObj instanceof File) {
                File f = (File) userObj;
                String name = f.getName().isEmpty() ? f.getAbsolutePath() : f.getName();
                label.setText(name);// Get and scale system icon to current icon size
                Icon origIcon = FileSystemView.getFileSystemView().getSystemIcon(f);
                if (origIcon != null) {
                    if (origIcon instanceof ImageIcon) {
                        Image img = ((ImageIcon) origIcon).getImage();
// Scale to current icon size for better visibility
                        Image scaledImg = img.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(scaledImg));
                    } else {
                        iconLabel.setIcon(origIcon);
                    }
                } else {
// Fallback icon
                    iconLabel.setIcon(null);
                }// Enhanced text styling based on file type
                if (f.isDirectory()) {
                    label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, fontSize));
                    label.setForeground(TEXT_PRIMARY);
                    label.setIconTextGap(12);
                } else {
                    label.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
                    label.setForeground(TEXT_SECONDARY);
                    label.setIconTextGap(10);
                }
            } else {
                label.setText(String.valueOf(userObj));
                label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, fontSize + 2));
                label.setForeground(PRIMARY_DARK);
                iconLabel.setIcon(null);
            }check.setSelected(isChecked(node));// Enhanced selection style with depth
            if (selected) {
                panel.setBackground(SELECTION_BG);
                label.setForeground(SELECTION_FG);
                label.setFont(label.getFont().deriveFont(Font.BOLD));// Add accent border for selected items
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, PRIMARY_COLOR),
                        BorderFactory.createEmptyBorder(11, 15, 11, 23)
                ));
            } else {
                if (isHovered) {
                    panel.setBackground(HOVER_COLOR);
                } else {
                    panel.setBackground(CARD_COLOR);
                }
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(12, 16, 12, 24)
                ));
            }// Focus effect
            if (hasFocus) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                        BorderFactory.createEmptyBorder(11, 15, 11, 23)
                ));
            }return panel;
        }
    }
}