public class Ship{
    private int[][] hp_ship;
    private int life;

    public Ship(int[][] coordinates, int l ){
        hp_ship = coordinates;
        life = l;
    }

    public boolean Cells_ship(int y, int x){

        for(int i =0 ; i<hp_ship.length ; i++){
            if(hp_ship[i][0] == y && hp_ship[i][1] == x){
                return true;
            }
        }

        return false;
    }

    public int[][] GetCells(){
        return hp_ship;
    }

    public int GetLife(){
        return life;
    }
    public void HpDamage(int l){life -= l;}

}