package com.chungtv.states;

import com.chungtv.Constants;
import com.chungtv.objects.*;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


public class PlayState extends BasicGameState {
    private int id;
    private Car myCar, rivalCar;
    private float[] startPosition;
    private float[] shiftPosition;
    private Image mySkin;
    private Track track;
    private Image map;
    private Shape carShape;
    private Shape exteriorBoundary;
    private Shape innerBoundary;
    private Shape finishLine1, finishLine2;
    private Shape bound;
    private ArrayList<Shape> otherCarShapes;
    private int numberOfPlayer = 2;
    private ArrayList<Car> otherCars;
    private ArrayList<Image> skins;
    private Thread update;
    private double[] onFinishTime;
    private boolean[] isOnFinishLine;
    private int isCrossLine; //0: no, 1: forward, 2: reverse
    private int currentLap;

    private boolean isTurnLeft;
    private boolean isTurnRight;
    private boolean isRun;
    private boolean isBrake;
    private boolean isCollide;

    private SpriteSheet explosionpic;
    private Animation explosion;

    private DatagramSocket sendSocket, receiveSocket;
    private int sendPort = Constants.HOST_PORT;
    private int receivePort = Constants.CLIENT_PORT;
    private String pos = "Initial";

    public PlayState(int id) {
        this.id = id;

        try {
            sendSocket = new DatagramSocket();
            receiveSocket = new DatagramSocket(receivePort);
        } catch (SocketException e){
            e.printStackTrace();
        }

        (new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (true){
                        byte[] bytes = new byte[256];
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                        receiveSocket.receive(packet);
                        pos = new String(packet.getData());

                        if (pos.contains(",")) {
                            String[] sub = pos.split(",");
                            float rivalX = Float.parseFloat(sub[0].trim());
                            float rivalY = Float.parseFloat(sub[1].trim());
                            float rivalD = Float.parseFloat(sub[2].trim());

                            if (rivalCar != null) {
                                rivalCar.setPosition(rivalX, rivalY);
                                rivalCar.setDirection(rivalD);
                            }
                        }
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
        skins = new ArrayList<Image>();
        otherCars = new ArrayList<Car>();
        onFinishTime = new double[]{0.0, 0.0};
        isOnFinishLine = new boolean[]{false, false};
        isCrossLine = 0;
        currentLap = 0;
        isCollide = false;
        explosionpic = new SpriteSheet("data/emap.png", 60, 60);
        explosion = new Animation(explosionpic, 100);

        String bluePath = "./data/car/cb1.png";
        String redPath = "./data/car/cr1.png";
        if (sendPort == Constants.HOST_PORT) {
            rivalCar = new Car(bluePath);
            myCar = new Car(redPath);
        } else {
            rivalCar = new Car(redPath);
            myCar = new Car(bluePath);
        }
        otherCars.add(rivalCar);
        skins.add(rivalCar.getSkin2());
        startPosition = new float[]{myCar.getPosition()[0], myCar.getPosition()[1], myCar.getDirection()};

        shiftPosition = new float[]{0,0};
        shiftPosition[0] = myCar.getPosition()[0];
        shiftPosition[1] = -myCar.getPosition()[1];

        mySkin = myCar.getSkin();
        track = new Track("./data/track/track.png");
        map = track.getMap();

        carShape = new Rectangle(50, 50, 20, 40);
        carShape.setLocation(390, 280);

        exteriorBoundary = track.getShape((float)Math.toRadians(-myCar.getDirection()), shiftPosition[0], shiftPosition[1]);
        innerBoundary = track.getInnerBoundary((float) Math.toRadians(-myCar.getDirection()), shiftPosition[0], shiftPosition[1]);
        finishLine1 = track.getFinishLine1((float) Math.toRadians(-myCar.getDirection()), 860 + shiftPosition[0], 820 + shiftPosition[1]);
        finishLine2 = track.getFinishLine2((float) Math.toRadians(-myCar.getDirection()), 845 + shiftPosition[0], 820 + shiftPosition[1]);
        bound = track.getBound((float) Math.toRadians(-myCar.getDirection()), shiftPosition[0], shiftPosition[1]);

        otherCarShapes = new ArrayList<>();
        for (int i=0; i<numberOfPlayer-1; i++){
            otherCarShapes.add(otherCars.get(i).getShape((float)Math.toRadians(myCar.getDirection()), shiftPosition[0], shiftPosition[1]));
        }

        //update Thread
        update = new Thread(){
            @Override
            public void run() {
            }
        };
        update.start();


    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int delta) throws SlickException {
        Input input = gameContainer.getInput();

        //car control
        myCar.setAcceleration(0.03f);
        isTurnLeft = input.isKeyDown(Input.KEY_LEFT);
        isTurnRight = input.isKeyDown(Input.KEY_RIGHT);
        isRun = input.isKeyDown(Input.KEY_UP);
        isBrake = input.isKeyDown(Input.KEY_DOWN);

        if (isRun){
            myCar.acclerate();
        } else if (!isBrake){
            myCar.slowDown(0.03f);
        }

        if(isTurnLeft&&(myCar.getSpeed()!=0))
            myCar.turn((float) -1.9);
        if(isTurnRight&&(myCar.getSpeed()!=0))
            myCar.turn((float) +1.9);
        if(isBrake)
            myCar.brake((float) 0.09);

        float position[] = myCar.getPosition();
        float dx=(float) Math.sin(Math.toRadians(-myCar.getDirection()))*myCar.getSpeed()*3.2f;
        float dy=(float) Math.cos(Math.toRadians(-myCar.getDirection()))*myCar.getSpeed()*3.2f;
        myCar.setPosition(position[0]+dx, position[1]-dy); //!

        shiftPosition[0]+=dx;
        shiftPosition[1]+=dy;

        //skin update
        for(int i=0; i<skins.size(); i++){
            skins.set(i, otherCars.get(i).getSkin2());
        }

        //shape update
        finishLine1 = track.getFinishLine1((float) Math.toRadians(-myCar.getDirection()),800+shiftPosition[0],620+shiftPosition[1]);
        finishLine2 = track.getFinishLine2((float) Math.toRadians(-myCar.getDirection()),785+shiftPosition[0],620+shiftPosition[1]);
        exteriorBoundary = track.getShape((float) Math.toRadians(-myCar.getDirection()),shiftPosition[0],shiftPosition[1]);
        innerBoundary= track.getInnerBoundary((float) Math.toRadians(-myCar.getDirection()),shiftPosition[0],shiftPosition[1]);
        bound = track.getBound((float) Math.toRadians(-myCar.getDirection()),shiftPosition[0],shiftPosition[1]);

        for (int i=0; i<otherCarShapes.size();i++){
            otherCarShapes.set(i, otherCars.get(i).getShape((float) Math.toRadians(-myCar.getDirection()), shiftPosition[0]+375, shiftPosition[1]+275));
        }

        //for judging lap++ or lap--
        if(carShape.intersects(finishLine1)&& !isOnFinishLine[0])
        {
            onFinishTime[0]=System.currentTimeMillis();
            isOnFinishLine[0]=true;
        }
        else if(!carShape.intersects(finishLine1))
            isOnFinishLine[0]=false;

        if(carShape.intersects(finishLine2)&& !isOnFinishLine[1])
        {
            onFinishTime[1]=System.currentTimeMillis();
            isOnFinishLine[1]=true;
        }
        else if(!carShape.intersects(finishLine2))
            isOnFinishLine[1]=false;

        if(carShape.intersects(finishLine1)&&carShape.intersects(finishLine2)&&isCrossLine==0)
        {
            if(onFinishTime[0]>onFinishTime[1])
                isCrossLine=2;
            else
                isCrossLine=1;
        }

        if((!carShape.intersects(finishLine1))&&(!carShape.intersects(finishLine2))&&isCrossLine!=0)
        {
            if(isCrossLine==2)
            {
                currentLap--;
            }
            else if(isCrossLine==1)
            {
                currentLap++;
            }
            isCrossLine=0;
        }


        //detect collision between player's car and the track boundary
        if(carShape.intersects(innerBoundary)||innerBoundary.contains(carShape)||carShape.intersects(exteriorBoundary)||exteriorBoundary.contains(carShape))
            if(myCar.getSpeed()>1.2)
                myCar.setSpeed((float) 1.2);

        //if player drive out of the track too far, than reset it's position
        if(carShape.intersects(bound))
            reset();

        //collision between cars is judged by the server, client will get the result and restart if collision occurs
        if(isCollide)
        {
            explosion.start();
            reset();
        }

        String s = myCar.getPosition()[0] + "," + myCar.getPosition()[1] + "," + myCar.getDirection();
        byte[] buff = s.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buff, buff.length, InetAddress.getByName("localhost"), sendPort);
            sendSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reset()
    {
        myCar.setSpeed(0);
        myCar.setAcceleration(0);
        myCar.setPosition(startPosition[0], startPosition[1]);
        shiftPosition[0]=myCar.getPosition()[0];
        shiftPosition[1]=-myCar.getPosition()[1];
        myCar.setDirection(startPosition[2]);
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
        graphics.setBackground(Color.gray);

        float position[] = myCar.getPosition();
        float direction = -myCar.getDirection();
        map.setCenterOfRotation(-position[0]+400, position[1]+300);
        map.setRotation(direction);
        map.draw(shiftPosition[0],shiftPosition[1],(float)1);
        for(int i=0; i<skins.size(); i++)
        {
            skins.get(i).setCenterOfRotation(25-position[0]+otherCars.get(i).getPosition()[0], 25+position[1]-otherCars.get(i).getPosition()[1]);
            skins.get(i).setRotation(direction);
            skins.get(i).draw(375+shiftPosition[0] - otherCars.get(i).getPosition()[0],275+shiftPosition[1]+otherCars.get(i).getPosition()[1]);
        }
        mySkin.drawCentered(400, 300);

        // display information
        graphics.setColor(Color.white);
        graphics.draw(bound);
        graphics.drawString("Speed: "+ (int) (myCar.getSpeed() * 140 / 4) +"KM/H", 15, 45);
        graphics.drawString("Other players: " + rivalCar.getPosition()[0] + "," + rivalCar.getPosition()[1],15,75);

    }
}
