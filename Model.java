package com.javarush.task.task35.task3513;


import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH ][FIELD_WIDTH ];
    public int score;
    public int maxTile;
    private Stack previousStates;
    private Stack previousScores;
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        previousStates = new Stack();
        previousScores = new Stack();
    }

    public void resetGameTiles() {
        score = 0;
        maxTile = 0;
        for ( int i = 0; i < FIELD_WIDTH; i++){
            for ( int j = 0; j < FIELD_WIDTH; j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    public void setSaveNeeded(boolean saveNeeded) {
        isSaveNeeded = saveNeeded;
    }

    //Глубокое копирование двумерного массива для сохранения состояния игры в Stack
    private void saveState(Tile[][] tile){
        Tile[][] newArr = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for ( int i = 0; i < FIELD_WIDTH; i++){
            for ( int j = 0; j < FIELD_WIDTH; j++){
                newArr[i][j] = new Tile();
                newArr[i][j].setValue(gameTiles[i][j].getValue());
            }
        }
        previousStates.push(newArr);
        previousScores.push(score);
        setSaveNeeded(false);
    }

    //Восстановление предыдущего хода из Stack
    public void rollback(){
        if ( !previousScores.isEmpty() && !previousStates.isEmpty()) {
            score = (int) previousScores.pop();
            gameTiles = (Tile[][]) previousStates.pop();
        }
    }

    private void addTile(){
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles != null && emptyTiles.size() > 0) {
            emptyTiles.get((int) (emptyTiles.size() * Math.random())).setValue(Math.random() < 0.9 ? 2 : 4);
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for ( int i = 0; i < FIELD_WIDTH; i++){
            for ( int j = 0; j < FIELD_WIDTH; j++){
             if ( gameTiles[i][j].value == 0){
                 list.add(gameTiles[i][j]);
             }
            }
        }
        return list;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }


    //Проверка возможности следующего хода в игру (наличия пустых ячеек для заполения)
    public boolean canMove(){
            if  (!getEmptyTiles().isEmpty()){
                return true;
            } else {
                for (int i = 0; i < gameTiles.length; i++) {
                    for (int j = 1; j < gameTiles.length; j++) {
                        if (gameTiles[i][j].value == gameTiles[i][j - 1].value)
                            return true;
                    }
                }
                for (int j = 0; j < gameTiles.length; j++) {
                    for (int i = 1; i < gameTiles.length; i++) {
                        if (gameTiles[i][j].value == gameTiles[i - 1][j].value)
                            return true;
                    }
                }
            }
            return false;
    }

    private boolean compressTiles(Tile[] tiles){
        boolean flag = false;
        for ( int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].isEmpty()) {
                for (int j = i + 1; j < tiles.length; j++) {
                    if (tiles[j].getValue() != 0) {
                        tiles[i].setValue(tiles[j].getValue());
                        tiles[j].setValue(0);
                        flag = true;
                        break;
                    }
                }
            }
        }
            return flag;
    }

    private boolean mergeTiles(Tile[] tiles){
        boolean flag = false;
            for (int i = 0; i < tiles.length - 1; i++) {
                if (!tiles[i].isEmpty() & tiles[i].getValue() == tiles[i + 1].getValue()) {
                    tiles[i].setValue(tiles[i].getValue() * 2);
                    if (maxTile < tiles[i].getValue()) {
                        maxTile = tiles[i].getValue();
                    }
                    score += tiles[i].getValue();
                    tiles[i + 1].setValue(0);
                    flag = true;
                }
            }
            compressTiles(tiles);
        return flag;
    }

    // Проверка случая, когда вес плиток в массиве gameTiles отличается от веса плиток в верхнем массиве стека previousStates
    public boolean hasBoardChanged(){
        Tile[][] stackArr = (Tile[][]) previousStates.peek();
        for ( int i = 0; i < FIELD_WIDTH; i++){
            for ( int j = 0; j < FIELD_WIDTH; j++){
                if ( gameTiles[i][j].getValue() != stackArr[i][j].getValue()){
                    return true;
                }
            }
        }
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveToTest;
        move.move();
        if ( hasBoardChanged()) {
            moveToTest = new MoveEfficiency(getEmptyTiles().size(), score, move);
        } else{
            moveToTest = new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return moveToTest;
    }
    
    public void randomMove(){

        int n = (int) ((Math.random()*100) % 4);
            switch (n) {
                case 0:
                    left();
                    break;
                case 1:
                    right();
                    break;

                case 2:
                    up();
                    break;

                case 3:
                    down();
                    break;
            }
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.poll().getMove().move();
    }

    //Метод для сдвига и/или слияния ячеек влево
    public void left(){
        if ( isSaveNeeded){
            saveState(gameTiles);
        }
        boolean isChanged = false;
        for (int k = 0; k < FIELD_WIDTH; k++) {
            if (compressTiles(gameTiles[k]) | mergeTiles(gameTiles[k]) ){
                isChanged = true;
            }
        }
        if ( isChanged){
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right(){
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();


    }

    public void up(){
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();


     }

     public void down(){
         saveState(gameTiles);
         rotate();
         left();
         rotate();
         rotate();
         rotate();
     }

    //поворот матрицы по часовой стрелке
    private  void rotate() {
        Tile [][] newArr = new Tile[gameTiles[0].length][gameTiles.length];
        int vertical = gameTiles.length - 1;
        int horizontal = 0;
        for ( int i = 0; i < newArr.length; i++){
            for ( int j = 0; j < newArr[i].length; j++){
                newArr[i][j] = gameTiles[vertical--][horizontal];
            }
            vertical = gameTiles.length - 1;
            horizontal++;
        }
        gameTiles = newArr;

    }
}
