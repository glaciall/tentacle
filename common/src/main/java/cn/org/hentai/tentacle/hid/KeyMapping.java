package cn.org.hentai.tentacle.hid;

import java.awt.event.KeyEvent;

/**
 * Created by matrixy on 2018/4/22.
 */
public class KeyMapping
{
    static final int[] mappings = new int[256];
    {
        mappings[ 13] = KeyEvent.VK_ENTER;
        mappings[  8] = KeyEvent.VK_BACK_SPACE;
        mappings[  9] = KeyEvent.VK_TAB;
        mappings[ 16] = KeyEvent.VK_SHIFT;
        mappings[ 17] = KeyEvent.VK_CONTROL;
        mappings[ 18] = KeyEvent.VK_ALT;
        mappings[ 20] = KeyEvent.VK_CAPS_LOCK;
        mappings[ 27] = KeyEvent.VK_ESCAPE;
        mappings[ 32] = KeyEvent.VK_SPACE;
        mappings[ 33] = KeyEvent.VK_PAGE_UP;
        mappings[ 34] = KeyEvent.VK_PAGE_DOWN;
        mappings[ 35] = KeyEvent.VK_END;
        mappings[ 36] = KeyEvent.VK_HOME;
        mappings[ 37] = KeyEvent.VK_LEFT;
        mappings[ 38] = KeyEvent.VK_UP;
        mappings[ 39] = KeyEvent.VK_RIGHT;
        mappings[ 40] = KeyEvent.VK_DOWN;
        mappings[188] = KeyEvent.VK_COMMA;
        mappings[189] = KeyEvent.VK_MINUS;
        mappings[190] = KeyEvent.VK_PERIOD;
        mappings[191] = KeyEvent.VK_SLASH;
        mappings[186] = KeyEvent.VK_SEMICOLON;
        mappings[187] = KeyEvent.VK_EQUALS;
        mappings[219] = KeyEvent.VK_OPEN_BRACKET;
        mappings[220] = KeyEvent.VK_BACK_SLASH;
        mappings[221] = KeyEvent.VK_CLOSE_BRACKET;
        mappings[ 96] = KeyEvent.VK_NUMPAD0;
        mappings[ 97] = KeyEvent.VK_NUMPAD1;
        mappings[ 98] = KeyEvent.VK_NUMPAD2;
        mappings[ 99] = KeyEvent.VK_NUMPAD3;
        mappings[100] = KeyEvent.VK_NUMPAD4;
        mappings[101] = KeyEvent.VK_NUMPAD5;
        mappings[102] = KeyEvent.VK_NUMPAD6;
        mappings[103] = KeyEvent.VK_NUMPAD7;
        mappings[104] = KeyEvent.VK_NUMPAD8;
        mappings[105] = KeyEvent.VK_NUMPAD9;
        mappings[106] = KeyEvent.VK_MULTIPLY;
        mappings[107] = KeyEvent.VK_ADD;
        mappings[ 46] = KeyEvent.VK_DELETE;
        mappings[144] = KeyEvent.VK_NUM_LOCK;
        mappings[112] = KeyEvent.VK_F1;
        mappings[113] = KeyEvent.VK_F2;
        mappings[114] = KeyEvent.VK_F3;
        mappings[115] = KeyEvent.VK_F4;
        mappings[116] = KeyEvent.VK_F5;
        mappings[117] = KeyEvent.VK_F6;
        mappings[118] = KeyEvent.VK_F7;
        mappings[119] = KeyEvent.VK_F8;
        mappings[120] = KeyEvent.VK_F9;
        mappings[121] = KeyEvent.VK_F10;
        mappings[122] = KeyEvent.VK_F11;
        mappings[123] = KeyEvent.VK_F12;
    }

    public static int convert(int code)
    {
        if (mappings[code] > 0) return mappings[code];
        else return code;
    }
}


























