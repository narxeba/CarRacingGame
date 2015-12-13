package com.chungtv.states;

import com.chungtv.Constants;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class WaitingState extends BasicGameState {
    private int id;
    private Image background;
    private int state;
    private boolean wait = true;

    public WaitingState(int id) {
        this.id = id;

    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        background = new Image("data/background/waitingState.jpg");
        state = 1;
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
        background.draw(0,0);
        if (state == 1){
            graphics.drawString("Use keyboard to choose your car: ", 150, 150);
            graphics.drawString("1. PLAYER 1", 150, 200);
            graphics.drawString("2. PLAYER 2", 150, 250);
        }
        if (state == 2 || state == 3){
            if (wait){
                graphics.drawString("Please wait...", 550, 200);
            }
        }
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int i) throws SlickException {
        Input input = gameContainer.getInput();
        if (state == 1){
            if (input.isKeyPressed(Input.KEY_1)){
                state = 2;
            }
            if (input.isKeyPressed(Input.KEY_2)){
                state = 3;
            }
        }

        if (state == 2){
            PlayState.sendPort = Constants.HOST_PORT;
            PlayState.receivePort = Constants.CLIENT_PORT;
            if (input.isKeyPressed(Input.KEY_ENTER)){
                stateBasedGame.getState(2).init(gameContainer, stateBasedGame);
                stateBasedGame.enterState(2);
            }
        }

        if (state == 3){
            PlayState.sendPort = Constants.CLIENT_PORT;
            PlayState.receivePort = Constants.HOST_PORT;
            if (input.isKeyPressed(Input.KEY_ENTER)){
                stateBasedGame.getState(2).init(gameContainer, stateBasedGame);
                stateBasedGame.enterState(2);
            }
        }
    }
}
