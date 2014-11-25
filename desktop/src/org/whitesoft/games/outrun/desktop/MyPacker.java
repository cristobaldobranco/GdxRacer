package org.whitesoft.games.outrun.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
public class MyPacker {
    public static void main (String[] args) throws Exception {
        TexturePacker.process(args[0], args[1], args[2]);
    }
}