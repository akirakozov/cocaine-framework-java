package cocaine;

import java.io.IOException;

import org.apache.log4j.Level;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class LevelTemplate extends AbstractTemplate<Level> {

    private static final Template<Level> instance = new LevelTemplate();

    private LevelTemplate() { }

    public static Template<Level> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, Level level, boolean required) throws IOException {
        if (Level.DEBUG.equals(level)) {
            packer.write(0);
        } else if (Level.INFO.equals(level)) {
            packer.write(1);
        } else if (Level.WARN.equals(level)) {
            packer.write(2);
        } else if (Level.ERROR.equals(level)) {
            packer.write(3);
        } else {
            throw new IllegalArgumentException("Unsupported level: " + level
                    + ", class: " + level.getClass()
                    + ", code: " + level.toInt()
                    + ", hash code: " + Integer.toHexString(level.hashCode())
                    + ", info code:" + Level.INFO.toInt()
                    + ", info hash code:" + Integer.toHexString(Level.INFO.hashCode()));
        }
    }

    @Override
    public Level read(Unpacker unpacker, Level level, boolean required) throws IOException {
        int value = unpacker.readInt();
        switch (value) {
            case 0:
                return Level.DEBUG;
            case 1:
                return Level.INFO;
            case 2:
                return Level.WARN;
            case 3:
                return Level.ERROR;
            default:
                throw new IllegalArgumentException("Invalid level: " + value);
        }
    }

}
