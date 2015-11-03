package cocaine.message;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageTypeTest {

    @Test
    public void checkValue() {
        Assert.assertEquals(MessageType.HANDSHAKE.value(), 0);
        Assert.assertEquals(MessageType.HEARTBEAT.value(), 0);
        Assert.assertEquals(MessageType.TERMINATE.value(), 1);
        Assert.assertEquals(MessageType.INVOKE.value(), 0);
        Assert.assertEquals(MessageType.WRITE.value(), 0);
        Assert.assertEquals(MessageType.ERROR.value(), 1);
        Assert.assertEquals(MessageType.CLOSE.value(), 2);
    }

}
