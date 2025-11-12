import java.util.Random;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SudokuGui extends JFrame {
    private int[][] solution;
    private int[][] puzzle;
    private JTextField[][] cells = new JTextField[9][9];
    private JLabel timerLabel;
    private Timer timer;
    private int elapsedSeconds = 0;

    public SudokuGui() {

        setTitle("🎮 Sudoku Challenge");
        setSize(700, 800);
        setLocationRelativeTo(null);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Couleur globale
        getContentPane().setBackground(new Color(245, 247, 250));

        solution = new int[9][9];
        puzzle = new int[9][9];

        generateSolution();
        copySolutionToPuzzle();
        removeRandomCells(50);

        JPanel titlePanel = createTitlePanel();
        JPanel gridPanel = createGridPanel();
        JPanel controlPanel = createControlPanel();

        add(titlePanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        startTimer();
        setVisible(true);
    }

    // --- Génération du Sudoku ---
    private void generateSolution() {
        remplir(0, 0);
    }

    private boolean remplir(int l, int c) {
        if (c == 9) { c = 0; l++; }
        if (l == 9) return true;

        int[] nums = melangerNombres();
        for (int val : nums) {
            if (estValide(solution, l, c, val)) {
                solution[l][c] = val;
                if (remplir(l, c + 1)) return true;
                solution[l][c] = 0;
            }
        }
        return false;
    }

    private int[] melangerNombres() {
        int[] nums = new int[9];
        for (int i = 0; i < 9; i++) nums[i] = i + 1;
        Random rand = new Random();
        for (int i = 0; i < 9; i++) {
            int j = rand.nextInt(9);
            int tmp = nums[i];
            nums[i] = nums[j];
            nums[j] = tmp;
        }
        return nums;
    }

    private boolean estValide(int[][] grille, int l, int c, int val) {
        for (int i = 0; i < 9; i++) {
            if (grille[l][i] == val || grille[i][c] == val) return false;
        }
        int startRow = l - l % 3, startCol = c - c % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grille[startRow + i][startCol + j] == val) return false;
            }
        }
        return true;
    }

    private void copySolutionToPuzzle() {
        for (int i = 0; i < 9; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, 9);
        }
    }

    private void removeRandomCells(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; ) {
            int r = rand.nextInt(9);
            int c = rand.nextInt(9);
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0;
                i++;
            }
        }
    }

    // --- Interface ---
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(240, 243, 247));

        JLabel title = new JLabel("🧩 Sudoku Challenge", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(50, 70, 130));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(180, 180, 180));

        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(separator, BorderLayout.SOUTH);
        return titlePanel;
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 2, 2));
        Font font = new Font("Segoe UI", Font.BOLD, 26);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(font);
                cell.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

                int blockRow = i / 3;
                int blockCol = j / 3;
                Color light = new Color(250, 250, 250);
                Color dark = new Color(235, 239, 245);
                cell.setBackground((blockRow + blockCol) % 2 == 0 ? light : dark);

                if (puzzle[i][j] != 0) {
                    cell.setText(String.valueOf(puzzle[i][j]));
                    cell.setEditable(false);
                    cell.setBackground(new Color(220, 225, 230));
                    cell.setForeground(new Color(60, 60, 60));
                } else {
                    cell.setForeground(new Color(30, 50, 120));
                    // Effet focus visuel doux
                    cell.addFocusListener(new FocusAdapter() {
                        public void focusGained(FocusEvent e) {
                            cell.setBackground(new Color(200, 220, 255));
                        }

                        public void focusLost(FocusEvent e) {
                            cell.setBackground(new Color(245, 247, 250));
                        }
                    });
                }

                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return gridPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(245, 247, 250));

        JButton checkButton = createButton("✔ Vérifier", new Color(80, 180, 100));
        JButton resetButton = createButton("↺ Recommencer", new Color(65, 105, 225));
        JButton newGameButton = createButton("🎲 Nouveau", new Color(152, 109, 247));
        JButton aiHelpButton = createButton("💡 Aide IA", new Color(255, 170, 70));

        checkButton.addActionListener(e -> checkSolution());
        resetButton.addActionListener(e -> resetGrid());
        newGameButton.addActionListener(e -> newGame());
        aiHelpButton.addActionListener(e -> aiAssistance());

        timerLabel = new JLabel("⏳ 0 s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(new Color(40, 50, 90));

        controlPanel.add(checkButton);
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        controlPanel.add(aiHelpButton);
        controlPanel.add(timerLabel);

        return controlPanel;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    // --- Vérification ---
    private void checkSolution() {
        boolean correct = true;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText().trim();
                if (text.isEmpty() || !text.matches("[1-9]")) {
                    cells[i][j].setBackground(new Color(255, 190, 190));
                    correct = false;
                } else {
                    int val = Integer.parseInt(text);
                    if (!estValidePourGrille(i, j, val)) {
                        cells[i][j].setBackground(new Color(255, 190, 190));
                        correct = false;
                    } else {
                        cells[i][j].setBackground(new Color(230, 255, 230));
                    }
                }
            }
        }

        if (!correct) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Certaines cases sont incorrectes ou vides.",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            timer.stop();
            JOptionPane.showMessageDialog(this,
                    "🎉 Bravo ! Vous avez complété le Sudoku en " + elapsedSeconds + " secondes.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean estValidePourGrille(int l, int c, int val) {
        for (int i = 0; i < 9; i++) {
            if (i != c && cells[l][i].getText().equals(String.valueOf(val))) return false;
            if (i != l && cells[i][c].getText().equals(String.valueOf(val))) return false;
        }

        int startRow = l - l % 3, startCol = c - c % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int r = startRow + i, col = startCol + j;
                if ((r != l || col != c) && cells[r][col].getText().equals(String.valueOf(val))) return false;
            }
        }
        return true;
    }

    // --- IA d'aide ---
    private void aiAssistance() {
        boolean progress = false;

        for (int i = 0; i < 9 && !progress; i++) {
            for (int j = 0; j < 9 && !progress; j++) {
                if (cells[i][j].getText().isEmpty()) {
                    List<Integer> possibles = getPossibles(i, j);
                    if (possibles.size() == 1) {
                        int val = possibles.get(0);
                        cells[i][j].setText(String.valueOf(val));
                        cells[i][j].setBackground(new Color(144, 238, 144));
                        progress = true;
                        JOptionPane.showMessageDialog(this,
                                "💡 Déduction IA : (" + (i + 1) + "," + (j + 1) + ") = " + val,
                                "Aide IA",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }

        if (!progress) {
            JOptionPane.showMessageDialog(this,
                    "🤔 Aucune déduction simple possible pour le moment.",
                    "Aide IA",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private List<Integer> getPossibles(int row, int col) {
        List<Integer> possibles = new ArrayList<>();
        for (int val = 1; val <= 9; val++) {
            if (estValidePourGrilleTemp(row, col, val)) possibles.add(val);
        }
        return possibles;
    }

    private boolean estValidePourGrilleTemp(int l, int c, int val) {
        for (int i = 0; i < 9; i++) {
            if (cells[l][i].getText().equals(String.valueOf(val))) return false;
            if (cells[i][c].getText().equals(String.valueOf(val))) return false;
        }

        int startRow = l - l % 3, startCol = c - c % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[startRow + i][startCol + j].getText().equals(String.valueOf(val))) return false;
            }
        }
        return true;
    }

    // --- Timer / reset ---
    private void resetGrid() {
        elapsedSeconds = 0;
        timerLabel.setText("⏳ 0 s");
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    cells[i][j].setText("");
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void newGame() {
        dispose();
        new SudokuGui();
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("⏳ " + elapsedSeconds + " s");
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGui::new);
    }
}

