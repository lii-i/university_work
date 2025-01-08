public class battlefield {
    private int[][] usedCells;
    private int sc;

    public battlefield() {
        usedCells = new int[100][2];
        sc = 0;
    }

    public void add_in_used(int[] y_x) { // когда мы получаем ответ от противника о том, попали мы или нет, мы заполняем массив использованных клеток
        usedCells[sc][0] = y_x[0];
        usedCells[sc][1] = y_x[1];
        sc++;
    }

    public boolean check_cells(int[] y_x) {
        boolean found = false;

        for (int i = 0; i < sc; i++) {
            if (y_x[0] == usedCells[i][0] && y_x[1] == usedCells[i][1]) {
                found = true;
                return found;
            }
        }

        return found;
    }
}
