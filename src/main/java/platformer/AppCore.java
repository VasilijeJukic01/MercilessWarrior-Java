package platformer;

import platformer.core.Framework;

public class AppCore {

    public static void main(String[] args) {
        Framework.getInstance().init(args[0], args[1]);
        Framework.getInstance().start();
    }

}
