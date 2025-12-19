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
 * - Clicking toggles node checkbox, cascades to children, updates parents.
 * - Provides helper getSelectedFiles() and setAllChecked(true).
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

    // Custom renderer draws a checkbox + icon + name
    private class CheckBoxTreeCellRenderer implements javax.swing.tree.TreeCellRenderer {
        private final JPanel panel = new JPanel(new BorderLayout(12, 0));
        private final JCheckBox check = new JCheckBox();
        private final JLabel label = new JLabel();

        CheckBoxTreeCellRenderer() {
            panel.add(check, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
            panel.setOpaque(false);
            check.setOpaque(false);
            label.setOpaque(false);
            check.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            check.setPreferredSize(new Dimension(28, 28));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObj = node.getUserObject();

            if (userObj instanceof File) {
                File f = (File) userObj;
                String name = f.getName().isEmpty() ? f.getAbsolutePath() : f.getName();
                label.setText(name);
                Icon origIcon = FileSystemView.getFileSystemView().getSystemIcon(f);
                if (origIcon != null && origIcon instanceof ImageIcon) {
                    Image img = ((ImageIcon) origIcon).getImage();
                    Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImg));
                } else {
                    label.setIcon(origIcon);
                }
            } else {
                label.setText(String.valueOf(userObj));
                label.setIcon(null);
            }

            check.setSelected(isChecked(node));
            if (selected) {
                panel.setBackground(UIManager.getColor("Tree.selectionBackground"));
                label.setForeground(UIManager.getColor("Tree.selectionForeground"));
                check.setForeground(UIManager.getColor("Tree.selectionForeground"));
            } else {
                panel.setBackground(tree.getBackground());
                label.setForeground(tree.getForeground());
                check.setForeground(tree.getForeground());
            }
            return panel;
        }
    }
}