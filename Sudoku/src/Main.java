


public class Main {
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