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
 * JCheckBoxTree - JTree with checkbox support; checked state stored in a Map.
 * Clicking toggles node checkbox, cascades to children, updates parents.
 * Provides helper getSelectedFiles() and setAllChecked(true).
 */
public class JCheckBoxTree extends JTree {
    private final Map<DefaultMutableTreeNode, Boolean> checkedMap = new HashMap<>();

    // Constructor accepts a TreeNode root
    public JCheckBoxTree(TreeNode root) {
        super(root);
        setCellRenderer(new CheckBoxTreeCellRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                toggleNode(node);
                repaint();
            }
        });

        // Modern Material Design enhancements
        setBackground(new Color(250, 250, 250));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    // Toggle a node and update children/parents
    private void toggleNode(DefaultMutableTreeNode node) {
        boolean newState = !isChecked(node);
        setChecked(node, newState);
        setChildrenChecked(node, newState);
        updateParentState(node);
    }

    // Recursively set children checked state
    private void setChildrenChecked(DefaultMutableTreeNode parent, boolean state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            setChecked(child, state);
            setChildrenChecked(child, state);
        }
    }

    // Update parent aggregate state (all->true, none->false, partial->false visually)
    private void updateParentState(DefaultMutableTreeNode node) {
        TreeNode p = node.getParent();
        if (!(p instanceof DefaultMutableTreeNode)) return;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) p;

        boolean all = true;
        boolean none = true;

        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (isChecked(child)) none = false;
            else all = false;
        }

        if (all) setChecked(parent, true);
        else if (none) setChecked(parent, false);
        else setChecked(parent, false); // partial -> treat as unchecked (can be extended)
        updateParentState(parent);
    }

    // Set checked state in map
    private void setChecked(DefaultMutableTreeNode node, boolean state) {
        checkedMap.put(node, state);
    }

    // Get checked state (default false)
    public boolean isChecked(DefaultMutableTreeNode node) {
        return checkedMap.getOrDefault(node, false);
    }

    // Set all nodes checked/unchecked
    public void setAllChecked(boolean state) {
        Object rootObj = getModel().getRoot();
        if (rootObj instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) rootObj;
            setChecked(root, state);
            setChildrenChecked(root, state);
        }
        repaint();
    }

    // Collect and return selected files (only regular files)
    public List<File> getSelectedFiles() {
        List<File> out = new ArrayList<>();
        Object rootObj = getModel().getRoot();
        if (rootObj instanceof DefaultMutableTreeNode) {
            collectCheckedFiles((DefaultMutableTreeNode) rootObj, out);
        }
        return out;
    }

    // Recursive helper to collect files
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
    }

    // Custom renderer with modern Material Design styling
    private class CheckBoxTreeCellRenderer implements javax.swing.tree.TreeCellRenderer {
        private final JPanel panel = new JPanel(new BorderLayout(12, 0));
        private final JCheckBox check = new JCheckBox();
        private final JLabel label = new JLabel();
        private final Color SELECTION_BG = new Color(25, 118, 210, 30); // Blue selection
        private final Color HOVER_BG = new Color(25, 118, 210, 15);
        private boolean hover = false;

        CheckBoxTreeCellRenderer() {
            panel.setLayout(new BorderLayout(12, 0));
            panel.add(check, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // Modern Material Design checkbox
            check.setOpaque(false);
            check.setBackground(new Color(250, 250, 250));
            check.setForeground(new Color(33, 33, 33));
            check.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            check.setIcon(createUncheckedIcon());
            check.setSelectedIcon(createCheckedIcon());
            check.setFocusPainted(false);

            // Modern Material Design label
            label.setOpaque(false);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(new Color(33, 33, 33));

            // Mouse listener for hover effect
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    panel.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    panel.repaint();
                }
            });
        }

        private Icon createUncheckedIcon() {
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

        private Icon createCheckedIcon() {
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

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();

            if (userObj instanceof File) {
                File f = (File) userObj;
                String name = f.getName().isEmpty() ? f.getAbsolutePath() : f.getName();
                label.setText(name);

                // Get and resize icon
                Icon origIcon = FileSystemView.getFileSystemView().getSystemIcon(f);
                if (origIcon != null && origIcon instanceof ImageIcon) {
                    Image img = ((ImageIcon) origIcon).getImage();
                    Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImg));
                } else {
                    label.setIcon(origIcon);
                }

                // Add file size and type info for files
                if (f.isFile()) {
                    String details = String.format("  (%.1f KB)", f.length() / 1024.0);
                    label.setText(name + details);
                }
            } else {
                label.setText(String.valueOf(userObj));
                label.setIcon(null);
            }

            check.setSelected(isChecked(node));

            // Modern Material Design selection and hover effects
            if (selected) {
                panel.setBackground(SELECTION_BG);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(25, 118, 210, 100)),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)
                ));
                label.setForeground(new Color(33, 33, 33));
                check.setForeground(new Color(33, 33, 33));
            } else if (hover) {
                panel.setBackground(HOVER_BG);
                panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                label.setForeground(new Color(33, 33, 33));
                check.setForeground(new Color(33, 33, 33));
            } else {
                panel.setBackground(tree.getBackground());
                panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                label.setForeground(new Color(66, 66, 66));
                check.setForeground(new Color(66, 66, 66));
            }

            return panel;
        }
    }
}