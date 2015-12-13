package states;

import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class PlayState extends BasicGameState {
    private int id;

    public PlayState(int id) {
        this.id = id;

        (new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    DatagramSocket socket = new DatagramSocket(19191);

                    while (true){
                        byte[] bytes = new byte[256];
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                        socket.receive(packet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {

    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {

    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int i) throws SlickException {

    }
}
