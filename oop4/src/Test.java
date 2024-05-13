import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.util.*;

public class Test extends JFrame implements Runnable {
    public int FPS = 60;
    long CONST = 1000000000;
    final double timePerFrame = CONST/FPS;

    public GameField gameField;
    private JPanel panel;
    private Random rand = new Random();
    Thread gameThread;
    int minX, minY;

    int maxX, maxY;
    int width, height;

    int type;
    int num=10;



    public static void main(String[] args) {

        new Test(1440, 818, "Game", 30, 30, 1432, 789, 10, 120, 160, 4, 55, 14, 225, 180, 180, 215, 20, 2);
//        new Test(1440, 818, "Game", 800, 500, 1432, 789, 10, 120, 160, 4, 55, 14, 225, 180, 180, 215, 20, 2);

        // new Game(sizeNoInsetsFull().width, sizeNoInsetsFull().height,"Game",50,50,sizeNoInsetsFull().width-50,sizeNoInsetsFull().height-50,0,255,255,175,214,255,20);
        //   new Game(sizeNoInsetsFull().width, sizeNoInsetsFull().height,"Game",50,50,800,700,0,255,255,175,214,255,20);
        // new Game(1440, 789,"Game",0,0,1440,789,0,255,255,175,214,255,20);
    }

    public Test(int w, int h, String title, int x1, int y1, int x2, int y2, int r, int g, int b, int window, int r1, int g1, int b1, int r2, int g2, int b2, int frameSize, int type) {
        setUndecorated(true);
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        size(w, h);
        setLayout(null);
        JPanel p = new JPanel();
        p.setBounds(0, 0, w, h);
        p.setBackground(Color.LIGHT_GRAY);
        gameField = new GameField(x1, y1, x2, y2, r1, g1, b1, r2, g2, b2, frameSize);
        width = gameField.x2Inner - gameField.x1Inner;
        height = gameField.y2Inner - gameField.y1Inner;
        minX = gameField.x1Inner - gameField.x1;
        minY = gameField.y1Inner - gameField.y1;
        maxX = minX + (gameField.x2Inner - gameField.x1Inner);
        maxY = minY + (gameField.y2Inner - gameField.y1Inner);
        this.type = type;

        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics gr) {
                Graphics2D graphics = (Graphics2D) gr;
                super.paintComponent(graphics);
                gameField.draw(graphics);

            }
        };

        panel.setBounds(x1, y1, x2 - x1, y2 - y1);

        getContentPane().add(p);
        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(null);

        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        MetalLookAndFeel.setCurrentTheme(new changeTheme(new Color(r, g, b)));

        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
        getRootPane().setBorder(BorderFactory.createMatteBorder(window, window, window, window, new Color(r, g, b)));
        setVisible(true);
        startGameThread();

    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        createList(type);
        long lastFrame = System.nanoTime();
        long now = System.nanoTime();
        while (gameThread != null) {
            now = System.nanoTime();

            if (now - lastFrame >= timePerFrame) {
                update(now);

                panel.repaint();
                lastFrame = now;
            }
        }
    }

    public void update(long t) {

        gameField.moveAllFigures(type, t);
        ArrayList<Circle> copy = new ArrayList<>(gameField.objects);
        boolean wallCrashFlag = false;
        for (Circle obj : copy) {

            if(obj.x1Frame<=minX){
                obj.changeVector(t, 1);
                wallCrashFlag = true;
            }
            else if(obj.x2Frame>=maxX) {
                obj.changeVector(t, 2);
                wallCrashFlag = true;
            }
            else if(obj.y1Frame<=minY){
                obj.changeVector(t, 3);
                wallCrashFlag = true;

            }
            else if(obj.y2Frame>=maxY){
                obj.changeVector(t, 4);
                wallCrashFlag = true;

            }

        }
        if(!wallCrashFlag){
            crashFind();
            allRotate(t);
        }
    }


    public Circle createFigure(int finish) {
        int rDraw = 0;
        int gDraw = 0;
        int bDraw = 0;
        int rFill = 255;
        int gFill = 255;
        int bFill = 255;
        int lineSize = 3;
        int radius = 35;

        boolean flag = false;
        int x1 = 0;
        int y1 = 0;
        while(!flag){
            flag = true;
            x1 = rand.nextInt(4 + minX, maxX - 2 * radius - 3);
            y1 = rand.nextInt(4 + minY, maxY - 2 * radius - 3);
            for(int i=0; i<= finish;i++ ){
                double temp = Math.pow(Math.pow(gameField.positions[i][0]-x1, 2) + Math.pow(gameField.positions[i][1]-y1, 2),0.5);
                if(temp < 2.5*(radius+lineSize)){
                    flag = false;
                    break;
                }
            }
        }

        return new Circle(x1, y1, radius, rDraw, gDraw, bDraw, rFill, gFill, bFill, lineSize);

    }

    public void createList(int type) {
        gameField.positions=new int[num][num];
        for(int i=0; i<num; i++){
            Circle cir = createFigure(i);
            gameField.addToMove(cir, type);
            gameField.positions[i][0] = cir.x;
            gameField.positions[i][1] = cir.y;
        }
        gameField.crashState=new boolean[num][num];
    }

    // нахождение столкновения
    public void crashFind(){
        boolean flag  = false;
        for (int i = 0; i < num-1; i++){
            for ( int j = (i+1); j< num; j++ ){
                if(gameField.objects.get(i).rr==0) {
                    double temp = Math.pow(gameField.positions[i][0] - gameField.positions[j][0], 2);
                    double temp1 = Math.pow(gameField.positions[i][1] - gameField.positions[j][1], 2);
                    double temp3 = Math.pow(temp + temp1, 0.5);
                    int radius = (gameField.objects.get(i).radius+1) * 2;
                    if (temp3 <= radius) {
                        if (!gameField.crashState[j][i]) {
                            gameField.crashState[i][j] = true;
                            flag = true;
                        }

                    }
                }
                else gameField.objects.get(i).rr--;
            }
        }

//        System.out.println("--------"+ gameField.objects.size());
        if(flag){
            System.out.println("CRash find");

        }
    }



    // Поворот всех при столкновении
    public void allRotate(long t){

        HashSet<Circle> vectorChangedSet = new HashSet< >();
        System.out.println("===========================================================");

        for(int i=0;i<num-1;i++){
            for(int j=0;j<num;j++){
                if(gameField.crashState[i][j]) {
                    for(int k=0;k<num-1;k++){
                            System.out.println(gameField.crashState[i][0]+ "----" + gameField.crashState[i][1]);
                    }
                    Circle c1 =gameField.objects.get(i);
                    Circle c2 =gameField.objects.get(j);

                    c1.rotation(c2,t);
                    c2.rotation(c1,t);
                    vectorChangedSet.add(c1);
                    vectorChangedSet.add(c2);
//                    c1.ang=c1.newang;
//                    c1.v=c1.newv;
//                    c1.x = c1.bufx;
//                    c1.y = c1.bufy;
//                    c2.ang=c2.newang;
//                    c2.v=c2.newv;
//                    c2.x = c2.bufx;
//                    c2.y = c2.bufy;
                    //c1.ang=c1.newang;
                    //c2.ang=c2.newang;
                    //c1.v=c1.newv;
                   // c2.v=c2.newv;
                }
            }
        }
        for(int i=0;i<num-1;i++){
            for(int j=0;j<num;j++) {
                gameField.crashState[i][j]=false;
            }
        }

        for (Circle cir:  vectorChangedSet) {
            cir.ang = cir.newAng;
            cir.v = cir.newV;
            //new
            cir.upd(cir.bufX, cir.bufY);


        }
        System.out.println("===========================================================");


    }



    public Dimension size(int w, int h) {
        Dimension dim = new Dimension();
        dim.width = w;
        dim.height = h;
        this.setPreferredSize(dim);
        this.pack();
        int realW = this.getContentPane().getWidth();
        int realH = this.getContentPane().getHeight();
        System.out.println(realW + " " + realH);
        dim.width = 2 * dim.width - realW;
        dim.height = 2 * dim.height - realH;
        this.setPreferredSize(dim);
        this.pack();
        return dim;
    }


}

class changeTheme extends DefaultMetalTheme {
    public Color c;

    public changeTheme(Color c) {
        super();
        this.c = c;
    }

    public ColorUIResource getWindowTitleInactiveBackground() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getWindowTitleBackground() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getPrimaryControlHighlight() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getPrimaryControlDarkShadow() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getPrimaryControl() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getControlHighlight() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getControlDarkShadow() {
        return new ColorUIResource(c);
    }

    public ColorUIResource getControl() {
        return new ColorUIResource(c);
    }

}
