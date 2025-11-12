import javax.swing.*;
import java.awt.*;

/**
 * Menu principal moderne avec un design ergonomique et élégant.
 */
public class Menu extends JFrame {

    private JButton sudokuButton, crosswordButton;
    private JPanel mainPanel;
    private JLabel titleLabel, subtitleLabel;

    public Menu() {
        // Configuration de base
        setTitle("Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout()); // Centrage global

        // Couleurs et styles
        Color backgroundColor = new Color(245, 247, 250);
        Color accentColor = new Color(33, 150, 243);  // Bleu moderne
        Color textColor = new Color(50, 50, 70);

        getContentPane().setBackground(backgroundColor);

        // --- Carte principale ---
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230), 1),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));

        // --- Titre principal ---
        titleLabel = new JLabel("🎮 Menu Principal", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Sous-titre ---
        subtitleLabel = new JLabel("Choisissez un jeu pour commencer");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(90, 90, 110));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        // --- Boutons ---
        sudokuButton = createModernButton("🧩 Jouer au Sudoku", accentColor, Color.WHITE);
        sudokuButton.addActionListener(e -> SwingUtilities.invokeLater(SudokuGui::new));

        crosswordButton = createModernButton("📝 Jouer au Mot Croisé", accentColor, Color.WHITE);
        crosswordButton.addActionListener(e -> SwingUtilities.invokeLater(CrosswordGui::new));

        // --- Ajout des éléments ---
        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(sudokuButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(crosswordButton);

        // --- Centrage dans la fenêtre ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(mainPanel, gbc);

        setVisible(true);
    }

    /**
     * Crée un bouton moderne avec coins arrondis et effet de survol.
     */
    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(300, 70));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Effet de survol
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Menu::new);
    }
}

