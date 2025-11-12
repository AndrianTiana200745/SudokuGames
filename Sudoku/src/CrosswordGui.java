import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import org.json.JSONObject;

public class CrosswordGui extends JFrame {
    private static final int SIZE = 12;
    private final Cell[][] cells = new Cell[SIZE][SIZE];
    private final JPanel gridPanel;
    private final JPanel cluesPanel;
    private final JLabel statusLabel;
    private final Map<String, Clue> clues = new LinkedHashMap<>();
    private final Map<String, String> solutions = new HashMap<>();

    private final List<String> wordPool = Arrays.asList(
            "JAVA","PYTHON","LOGIC","PUZZLE","CROSS","WORD","CODE","ARRAY",
            "FRAME","THREAD","STRING","INTEGER","BOOLEAN","FUNCTION","OBJECT",
            "CLASS","METHOD","VARIABLE","SWING","COMPILER","INTERFACE","PACKAGE",
            "EXCEPTION","LOOP","CONDITION","STACK","QUEUE","ALGORITHM","DEBUG"
    );

    public CrosswordGui() {
        setTitle("📝 Mots Croisés – IA Moderne");
        setSize(1150, 800);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(10,10));

        // TITRE
        JLabel title = new JLabel("Mots Croisés — IA Moderne", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        add(title, BorderLayout.NORTH);

        // CENTRE
        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBackground(getContentPane().getBackground());

        // GRILLE
        gridPanel = new JPanel(new GridLayout(SIZE,SIZE,2,2));
        gridPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180,180,200),2),
                BorderFactory.createEmptyBorder(12,12,12,12)
        ));
        gridPanel.setBackground(new Color(230,233,240));
        center.add(gridPanel, BorderLayout.CENTER);

        // PANEL INDICES
        cluesPanel = new JPanel();
        cluesPanel.setLayout(new BoxLayout(cluesPanel, BoxLayout.Y_AXIS));
        cluesPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        cluesPanel.setBackground(getContentPane().getBackground());

        JScrollPane scroll = new JScrollPane(cluesPanel);
        scroll.setPreferredSize(new Dimension(480,500));
        center.add(scroll, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);

        // CONTROLES
        JPanel controls = new JPanel(new BorderLayout());
        controls.setBackground(getContentPane().getBackground());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,12,10));
        btns.setBackground(getContentPane().getBackground());

        JButton checkBtn = new JButton("✔ Vérifier");
        JButton revealBtn = new JButton("🔎 Révéler mot");
        JButton revealAllBtn = new JButton("✨ Tout révéler");
        JButton clearBtn = new JButton("🧼 Effacer");
        JButton newBtn = new JButton("🔁 Nouveau");

        checkBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        revealBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        revealAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        checkBtn.addActionListener(e -> checkAll());
        revealBtn.addActionListener(e -> revealSelected());
        revealAllBtn.addActionListener(e -> revealAll());
        clearBtn.addActionListener(e -> clearFillable());
        newBtn.addActionListener(e -> buildGridWithWordsAsync());

        btns.add(checkBtn); btns.add(revealBtn); btns.add(revealAllBtn); btns.add(clearBtn); btns.add(newBtn);
        controls.add(btns, BorderLayout.CENTER);

        statusLabel = new JLabel("Cliquez sur un indice pour sélectionner le mot", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        controls.add(statusLabel, BorderLayout.SOUTH);

        add(controls, BorderLayout.SOUTH);

        buildGridWithWordsAsync(); // initialisation asynchrone
        setVisible(true);
    }

    /** Génération asynchrone de la grille avec placement logique */
    private void buildGridWithWordsAsync() {
        gridPanel.removeAll();
        clues.clear();
        solutions.clear();
        statusLabel.setText("Chargement des mots et définitions…");

        for (int r=0;r<SIZE;r++)
            for (int c=0;c<SIZE;c++) {
                cells[r][c] = new Cell(r,c,false);
                gridPanel.add(cells[r][c].getComponent());
            }
        gridPanel.revalidate();
        gridPanel.repaint();

        SwingWorker<Void, WordEntry> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Random rand = new Random();
                List<String> wordsToPlace = new ArrayList<>(wordPool);
                Collections.shuffle(wordsToPlace);

                int number=1;

                for (String w : wordsToPlace) {
                    String clueText = fetchDefinitionFromWiktionary(w);
                    if (clueText==null) clueText = "Indice pour « "+w+" »";

                    boolean placed=false;

                    // Premier mot au centre horizontal
                    if(number==1){
                        int r = SIZE/2;
                        int c = (SIZE-w.length())/2;
                        if(canPlace(w,r,c,true)){
                            storeSolution(w,r,c,true); // STOCKE solution mais NE PAS remplir la grille
                            String key = "A"+number+"_"+r+"_"+c;
                            clues.put(key,new Clue(number,"Across",r,c,w.length(),number+". "+clueText));
                            number++;
                            publish(new WordEntry(w,clueText));
                            continue;
                        }
                    }

                    // Essayer de placer avec croisement
                    for(int attempt=0;attempt<50 && !placed;attempt++){
                        boolean horizontal = rand.nextBoolean();
                        int r = rand.nextInt(SIZE);
                        int c = rand.nextInt(SIZE-w.length()+1);
                        for(int i=0;i<w.length() && !placed;i++){
                            for(int rr=0;rr<SIZE && !placed;rr++){
                                for(int cc=0;cc<SIZE && !placed;cc++){
                                    String val = cells[rr][cc].getText();
                                    if(val.equalsIgnoreCase(""+w.charAt(i))){
                                        int rStart = horizontal?rr:i;
                                        int cStart = horizontal?cc-i:cc;
                                        if(rStart>=0 && rStart<SIZE && cStart>=0 && cStart+ w.length() <=SIZE && canPlace(w,rStart,cStart,horizontal)){
                                            storeSolution(w,rStart,cStart,horizontal);
                                            String key = (horizontal?"A":"D")+number+"_"+rStart+"_"+cStart;
                                            clues.put(key,new Clue(number,horizontal?"Across":"Down",rStart,cStart,w.length(),number+". "+clueText));
                                            number++;
                                            publish(new WordEntry(w,clueText));
                                            placed=true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Placement aléatoire simple
                    for(int attempt=0;attempt<50 && !placed;attempt++){
                        boolean horizontal = rand.nextBoolean();
                        int r = horizontal ? rand.nextInt(SIZE) : rand.nextInt(SIZE-w.length());
                        int c = horizontal ? rand.nextInt(SIZE-w.length()) : rand.nextInt(SIZE);
                        if(canPlace(w,r,c,horizontal)){
                            storeSolution(w,r,c,horizontal);
                            String key = (horizontal?"A":"D")+number+"_"+r+"_"+c;
                            clues.put(key,new Clue(number,horizontal?"Across":"Down",r,c,w.length(),number+". "+clueText));
                            number++;
                            publish(new WordEntry(w,clueText));
                            placed=true;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void process(List<WordEntry> chunks) { buildClues(); }

            @Override
            protected void done() { buildClues(); statusLabel.setText("Grille prête !"); }
        };
        worker.execute();
    }

    private void storeSolution(String word,int r,int c,boolean horizontal){
        for(int i=0;i<word.length();i++){
            int rr = r + (horizontal?0:i);
            int cc = c + (horizontal?i:0);
            solutions.put(rr+"_"+cc,""+word.charAt(i)); // stocke solution MAIS NE PAS remplir
        }
    }

    private boolean canPlace(String word,int r,int c,boolean horizontal){
        for(int i=0;i<word.length();i++){
            int rr = r + (horizontal?0:i);
            int cc = c + (horizontal?i:0);
            if(rr>=SIZE || cc>=SIZE) return false;
            String val = cells[rr][cc].getText();
            if(!val.equals("") && !val.equals(""+word.charAt(i))) return false;
        }
        return true;
    }

    private void buildClues(){
        cluesPanel.removeAll();
        JLabel acrossLabel = new JLabel("Across");
        acrossLabel.setFont(new Font("Segoe UI", Font.BOLD,16));
        cluesPanel.add(acrossLabel);
        for(Map.Entry<String,Clue> e: clues.entrySet()) if("Across".equals(e.getValue().direction))
            cluesPanel.add(clueButton(e.getKey(), e.getValue()));

        cluesPanel.add(Box.createRigidArea(new Dimension(0,12)));
        JLabel downLabel = new JLabel("Down");
        downLabel.setFont(new Font("Segoe UI", Font.BOLD,16));
        cluesPanel.add(downLabel);
        for(Map.Entry<String,Clue> e: clues.entrySet()) if("Down".equals(e.getValue().direction))
            cluesPanel.add(clueButton(e.getKey(), e.getValue()));

        cluesPanel.revalidate();
        cluesPanel.repaint();
    }

    private JButton clueButton(String key,Clue cl){
        JButton b = new JButton(cl.text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(450,36));
        b.setBackground(new Color(245,245,255));
        b.setFont(new Font("Segoe UI",Font.PLAIN,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(ev -> selectClue(key));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e){ b.setBackground(new Color(220,235,255)); }
            @Override
            public void mouseExited(MouseEvent e){ b.setBackground(new Color(245,245,255)); }
        });
        return b;
    }

    private String selectedKey=null;

    private void selectClue(String key){
        selectedKey=key;
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++) cells[r][c].setHighlight(false);
        Clue cl = clues.get(key);
        if(cl==null) return;
        for(int i=0;i<cl.length;i++){
            int rr = cl.row + ("Down".equals(cl.direction)?i:0);
            int cc = cl.col + ("Across".equals(cl.direction)?i:0);
            cells[rr][cc].setHighlight(true);
        }
        statusLabel.setText("Mot sélectionné: "+cl.text);
    }

    private void checkAll(){
        boolean allCorrect=true;
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++){
            Cell cell = cells[r][c];
            if(!cell.isBlock()){
                String val = cell.getText().trim();
                String sol = solutions.get(r+"_"+c);
                if(sol!=null && val.length()==1){
                    if(val.equalsIgnoreCase(sol)) cell.setCorrect(true);
                    else { cell.setCorrect(false); allCorrect=false; }
                } else { cell.setNeutral(); allCorrect=false; }
            }
        }
        JOptionPane.showMessageDialog(this, allCorrect?"🎉 Bravo !":"Certaines lettres sont incorrectes.");
    }

    private void revealSelected(){
        if(selectedKey==null) return;
        Clue cl = clues.get(selectedKey);
        for(int i=0;i<cl.length;i++){
            int rr = cl.row + ("Down".equals(cl.direction)?i:0);
            int cc = cl.col + ("Across".equals(cl.direction)?i:0);
            String sol = solutions.get(rr+"_"+cc);
            if(sol!=null) cells[rr][cc].animateReveal(sol);
        }
    }

    private void revealAll(){
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++){
            String sol = solutions.get(r+"_"+c);
            if(sol!=null) cells[r][c].animateReveal(sol);
        }
    }

    private void clearFillable(){
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++){
            Cell cell = cells[r][c];
            if(!cell.isBlock()){
                cell.setText("");
                cell.setNeutral();
                cell.setRevealed(false);
            }
        }
    }

    private String fetchDefinitionFromWiktionary(String word){
        try{
            String endpoint = "https://en.wiktionary.org/w/api.php?action=query&format=json&prop=extracts&titles="
                    +word+"&explaintext=1&redirects=1";
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            int status = conn.getResponseCode();
            if(status!=HttpURLConnection.HTTP_OK) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null) sb.append(line);
            reader.close();

            JSONObject obj = new JSONObject(sb.toString());
            JSONObject query = obj.optJSONObject("query");
            if(query==null) return null;
            JSONObject pages = query.optJSONObject("pages");
            if(pages==null) return null;

            for(String key: pages.keySet()){
                JSONObject page = pages.optJSONObject(key);
                if(page==null) continue;
                String extract = page.optString("extract");
                if(extract!=null && !extract.isEmpty()){
                    String[] lines = extract.split("\\r?\\n");
                    return lines[0];
                }
            }
        }catch(Exception e){ System.out.println("Impossible de récupérer la définition pour "+word);}
        return null;
    }

    private static class Clue{
        int number;
        String direction;
        int row,col,length;
        String text;
        Clue(int n,String d,int r,int c,int l,String t){ number=n; direction=d; row=r; col=c; length=l; text=t; }
    }

    private static class Cell{
        int row,col;
        boolean block=false;
        JTextField field;
        JPanel blockPanel;
        boolean revealed=false;
        Color defaultBg;

        Cell(int row,int col,boolean block){
            this.row=row; this.col=col; this.block=block;
            if(block){
                blockPanel = new JPanel();
                blockPanel.setBackground(new Color(34,34,36));
                blockPanel.setBorder(BorderFactory.createLineBorder(new Color(60,60,70)));
            } else {
                field = new JTextField();
                field.setHorizontalAlignment(SwingConstants.CENTER);
                field.setFont(new Font("Segoe UI",Font.BOLD,18));
                defaultBg = new Color(250,250,250);
                field.setBackground(defaultBg);
                field.setBorder(BorderFactory.createLineBorder(new Color(190,190,200)));
                field.addKeyListener(new KeyAdapter(){
                    @Override
                    public void keyTyped(KeyEvent e){
                        char ch=e.getKeyChar();
                        if(ch==KeyEvent.VK_BACK_SPACE || ch==KeyEvent.VK_DELETE) return;
                        if(!Character.isLetter(ch)) { e.consume(); return; }
                        SwingUtilities.invokeLater(()->{
                            String t=field.getText();
                            if(t.length()>1) field.setText(t.substring(0,1).toUpperCase());
                            else field.setText(field.getText().toUpperCase());
                        });
                    }
                });
            }
        }

        JComponent getComponent(){ return block?blockPanel:field; }
        boolean isBlock(){ return block; }
        void setText(String s){ if(!block) field.setText(s==null?"":s.toUpperCase()); }
        String getText(){ return block?"":field.getText(); }
        void setHighlight(boolean on){
            if(!block) field.setBorder(on?BorderFactory.createLineBorder(new Color(65,105,225),2)
                    :BorderFactory.createLineBorder(new Color(190,190,200)));
            if(on) field.requestFocusInWindow();
        }
        void setCorrect(boolean ok){ if(!block) field.setBackground(ok?new Color(220,255,220):new Color(255,210,210)); }
        void setNeutral(){ if(!block) field.setBackground(defaultBg); }
        void setRevealed(boolean b){ revealed=b; if(b) field.setBackground(new Color(200,255,200)); }

        void animateReveal(String s){
            if(block) return;
            field.setText(s.toUpperCase());
            field.setBackground(new Color(200,255,200));
        }
    }

    private static class WordEntry{
        String word,clue;
        WordEntry(String w,String c){ word=w; clue=c; }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(CrosswordGui::new);
    }
}
