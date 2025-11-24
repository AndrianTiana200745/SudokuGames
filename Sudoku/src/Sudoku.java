import javax.swing.*;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class Sudoku {
    private int[][] table;
    private long tpsD;
    private Timer time;

    public int[][] getTable() {
        return table;
    }

    public Sudoku(){
        this.table = new int[9][9];
    }

    public void afficherTable(){
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(" " + table[i][j] + " ");
                if ((j + 1) % 3 == 0 && j < 8){
                    System.out.print(" | ");
                }
            }
            System.out.println();
            if ((i + 1) % 3 == 0 && i < 8){
                System.out.println("----------+-----------+----------");
            }
        }
    }

    public void generate(){
        remplir(0, 0);
    }

    private boolean remplir(int l, int c){
        if (c >= 9){
            c = 0;
            l++;
        }
        if (l >= 9){
            return true;
        }

        if (table[l][c] != 0){
            return remplir(l, c+1);
        }

        int[] nbre = melangerNbre();
        for(int valeur : nbre){
            if (estValide(l, c, valeur)){
                table[l][c] = valeur;
                if (remplir(l, c+1)){
                    return true;
                }
                table[l][c] = 0;
            }
        }
        return false;
    }

    private int[] melangerNbre(){
        int[] nbre = new int[9];
        for (int i = 0; i < 9; i++) {
            nbre[i] = i + 1;
        }
        Random rand = new Random();
        for (int i = 0; i < 9; i++) {
            int ind = rand.nextInt(9);
            int temp = nbre[i];
            nbre[i] = nbre[ind];
            nbre[ind] = temp;
        }
        return nbre;
    }

    private boolean estValide(int l, int c, int val){
        for (int i = 0; i < 9; i++) {
            if (table[l][i] == val){
                return false;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (table[i][c] == val){
                return false;
            }
        }

        int d = l - l % 3;
        int dc = c - c % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (table[d + i][dc + j] == val){
                    return false;
                }
            }
        }
        return true;
    }

    public void play(){
        tpsD = System.currentTimeMillis();

        Scanner scan = new Scanner(System.in);
        while (true){
            long tpsE = System.currentTimeMillis() - tpsD;
            long minut = tpsE/60000;
            long secon = (tpsE%60000)/1000;
            System.out.println("\rTemps écoulé : " + minut + "min" + secon + "s");
            System.out.flush();
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            afficherTable();
            System.out.print("Entrez la ligne (1-9): ");
            int ligne = scan.nextInt() - 1;
            System.out.print("Entrez la collonne (1-9): ");
            int col = scan.nextInt() - 1;
            System.out.print("Entrez la valeur (1-9): ");
            int val = scan.nextInt();
            if (ligne < 0 || ligne >= 9 || col < 0 || col >= 9 || val < 1 || val > 9){
                System.out.println("Valeur invalide");
                continue;
            }
            if (table[ligne][col] != 0){
                System.out.println("Case déjà occupée!");
                continue;
            }
            if (!estValide(ligne, col, val)){
                System.out.println("Valeur invalide pour cette case!");
                continue;
            }
            table[ligne][col] = val;
            boolean complete = true;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (table[i][j] == 0){
                        complete = false;
                        break;
                    }
                }
                if (!complete){
                    break;
                }
            }
            if (complete){
                System.out.println("Félicitations! vous avez réussis");
                return;
            }
        }
    }

    public static void main(String[] args) {
        Sudoku game = new Sudoku();
        game.generate();
        for (int i = 0; i < 50; i++) {
            int l = (int) (Math.random() * 9);
            int c = (int) (Math.random() * 9);
            game.getTable()[l][c] = 0;
        }
        game.play();
    }
}