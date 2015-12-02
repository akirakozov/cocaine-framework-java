package cocaine.http;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author akirakozov
 */
public class HttpMetaDataTest {

    @Test
    public void getPathAndQueryString() throws Exception {
        HttpMetaData metaData = new HttpMetaData("/some/path?val=1&name=jack");
        Assert.assertEquals("/some/path", metaData.getPath());
        Assert.assertEquals("val=1&name=jack", metaData.getQueryString());
    }

    @Test
    public void getEmptyPathAndQueryString() throws Exception {
        HttpMetaData metaData = new HttpMetaData("?val=1&name=jack");
        Assert.assertEquals("", metaData.getPath());
        Assert.assertEquals("val=1&name=jack", metaData.getQueryString());
    }

    @Test
    public void getPathAndEmptyQueryString() throws Exception {
        HttpMetaData metaData = new HttpMetaData("/some/path");
        Assert.assertEquals("/some/path", metaData.getPath());
        Assert.assertEquals("", metaData.getQueryString());
    }

    @Test
    public void getParameters() throws Exception {
        HttpMetaData metaData = new HttpMetaData("/some/path/?val=1&name=jack");
        Map<String, String[]> params = metaData.getParameters();
        Assert.assertEquals("1", params.get("val")[0]);
        Assert.assertEquals("jack", params.get("name")[0]);
    }

    @Test
    public void getEmptyParameter() throws Exception {
        HttpMetaData metaData = new HttpMetaData("/some/path/?val=");
        Map<String, String[]> params = metaData.getParameters();
        Assert.assertEquals("", params.get("val")[0]);
    }

    @Test
    public void getMultipleParameter() throws Exception {
        HttpMetaData metaData = new HttpMetaData("/some/path/?val=123&val=new&val=&val=some");
        Map<String, String[]> params = metaData.getParameters();
        Assert.assertArrayEquals(new String[]{"123", "new", "", "some"}, params.get("val"));
    }

}
